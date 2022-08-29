package io.littlehorse.common.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.streams.KafkaStreams.State;
import org.apache.kafka.streams.KafkaStreams.StateListener;
import org.apache.kafka.streams.processor.StateRestoreListener;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class LHKStreamsListener implements StateRestoreListener, StateListener {
    private List<KstreamRestore> activeRestores;
    private State state;
    private long longestRestoreSeconds;
    private long totalRestoreSeconds;
    private int totalCompletedRestores;

    private ReentrantLock lock;

    public LHKStreamsListener() {
        activeRestores = new ArrayList<>();
        this.state = State.CREATED;
        lock = new ReentrantLock();
    }

    public ProbeResponse getResponse() {
        ProbeResponse out = new ProbeResponse();
        out.activeRestores = activeRestores.size();
        out.state = state;
        out.longestRestoreSeconds = longestRestoreSeconds;
        out.totalCompletedRestores = totalCompletedRestores;
        out.averageRestoreSeconds = totalRestoreSeconds / totalCompletedRestores;
        out.topicPartsProcessing = new ArrayList<>();

        for (KstreamRestore restore: activeRestores) {
            out.recordsLeftToProcess = restore.totalRecordsToProcess - restore.recordsProcessed;
            out.topicPartsProcessing.add(restore.topicPart.toString());
        }
        return out;
    }

    public void onChange(State newState, State oldState) {
        try {
            lock.lock();
            this.state = newState;
        } finally {
            lock.unlock();
        }
        LHUtil.log(new Date(), "New state: ", newState);
    }

    public State getState() {
        return this.state;
    }

    public void onBatchRestored(
        TopicPartition topicPartition, String storeName, long batchEndOffset, long numRestored
    ) {
        try {
            lock.lock();
            KstreamRestore restore = getRestore(topicPartition, storeName, false);
            restore.end = new Date();
            restore.addRecords(numRestored);
        } finally {
            lock.unlock();
        }
    }

    public void onRestoreEnd(TopicPartition topicPartition, String storeName, long totalRestored) {
        try {
            lock.lock();
            boolean shouldDelete = true;
            KstreamRestore restore = getRestore(topicPartition, storeName, shouldDelete);

            restore.end = new Date();
            restore.recordsProcessed = totalRestored;
            long secondsTaken = (restore.end.getTime() - restore.start.getTime()) / 1000;

            if (secondsTaken > longestRestoreSeconds) {
                longestRestoreSeconds = secondsTaken;
            }
            totalCompletedRestores++;
            totalRestoreSeconds += secondsTaken;

            LHUtil.log(
                "Completed restore for ",
                restore.topicPart.partition(),
                restore.topicPart.topic(),
                ", took ",
                ((float)(restore.end.getTime() - restore.start.getTime())) / 1000.0,
                ", numRecords: ",
                totalRestored
            );
        } finally {
            lock.unlock();
        }
    }

    public void onRestoreStart(
        TopicPartition topicPartition, String storeName, long startingOffset, long endingOffset
    ) {
        try {
            lock.lock();
            KstreamRestore restore = new KstreamRestore(
                topicPartition,
                storeName,
                endingOffset - startingOffset
            );
            activeRestores.add(restore);
            LHUtil.log(
                "Starting restore for ",
                topicPartition.partition(),
                topicPartition.topic(),
                startingOffset,
                endingOffset
            );
        } finally {
            lock.unlock();
        }
    }

    @JsonIgnore private KstreamRestore getRestore(TopicPartition part, String store, boolean remove) {
        String key = KstreamRestore.getId(part, store);
        KstreamRestore out = null;
        int i = 0;
        try {
            lock.lock();
            for (KstreamRestore candidate: activeRestores) {
                if (!candidate.done && key.equals(candidate.getId())) {
                    if (out != null) throw new RuntimeException("double");
                    out = candidate;
                    break;
                }
                i++;
            }
            if (out == null) throw new RuntimeException("not found!");
            if (remove) activeRestores.remove(i);
        } finally {
            lock.unlock();
        }
        return out;
    }

    public class KstreamRestore {
        public boolean done;
        public Date start;
        public Date end;
        public TopicPartition topicPart;
        public String storeName;
        public long recordsProcessed;
        public long totalRecordsToProcess;
    
        public KstreamRestore() {}
    
        public KstreamRestore(TopicPartition topicPart, String storeName, long totalRecordsToProcess) {
            this.storeName = storeName;
            this.topicPart = topicPart;
            this.done = false;
            this.start = new Date();
            this.totalRecordsToProcess = totalRecordsToProcess;
        }
    
        public Long getTotalTime() {
            if (end == null) return null;
    
            return end.getTime() - start.getTime();
        }
    
        public static String getId(TopicPartition topicPart, String storeName) {
            return topicPart.topic() + "__" + topicPart.partition() + "__" + storeName;
        }
    
        public void addRecords(long howMany) {
            recordsProcessed += howMany;
        }
    
        @JsonIgnore public String getId() {
            return getId(topicPart, storeName);
        }
    }

    public class ProbeResponse {
        public long longestRestoreSeconds;
        public long averageRestoreSeconds;
        public int totalCompletedRestores;
        public int activeRestores;
        public State state;
        public String message;
        public long recordsLeftToProcess;

        public List<String> topicPartsProcessing;
    }
}