package io.littlehorse.common.util.kstreamlisteners;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.streams.processor.StateRestoreListener;
import com.fasterxml.jackson.annotation.JsonIgnore;


class KstreamRestore {
    public boolean done;
    public Date start;
    public Date end;
    public TopicPartition topicPart;
    public String storeName;
    public long totalRecordsProcessed;

    public KstreamRestore() {}

    public KstreamRestore(TopicPartition topicPart, String storeName) {
        this.storeName = storeName;
        this.topicPart = topicPart;
        this.done = false;
        this.start = new Date();
    }

    public Long getTotalTime() {
        if (end == null) return null;

        return end.getTime() - start.getTime();
    }

    public static String getId(TopicPartition topicPart, String storeName) {
        return topicPart.topic() + "__" + topicPart.partition() + "__" + storeName;
    }

    public void addRecords(long howMany) {
        totalRecordsProcessed += howMany;
    }

    @JsonIgnore public String getId() {
        return getId(topicPart, storeName);
    }
}


public class KStreamsRestoreListener implements StateRestoreListener {
    private List<KstreamRestore> restores;

    public KStreamsRestoreListener() {
        restores = new ArrayList<>();
    }

    public void onBatchRestored(
        TopicPartition topicPartition, String storeName, long batchEndOffset, long numRestored
    ) {
        KstreamRestore restore = getRestore(topicPartition, storeName);
        restore.end = new Date();
        restore.addRecords(numRestored);
    }

    public void onRestoreEnd(TopicPartition topicPartition, String storeName, long totalRestored) {
        KstreamRestore restore = getRestore(topicPartition, storeName);
        restore.end = new Date();
        restore.totalRecordsProcessed = totalRestored;
    }

    public void onRestoreStart(
        TopicPartition topicPartition, String storeName, long startingOffset, long endingOffset
    ) {
        KstreamRestore restore = new KstreamRestore(topicPartition, storeName);
        restores.add(restore);
    }

    public boolean isRebalancing() {
        for (KstreamRestore restore: restores) {
            if (!restore.done) return true;
        }
        return false;
    }

    @JsonIgnore private KstreamRestore getRestore(TopicPartition part, String store) {
        String key = KstreamRestore.getId(part, store);
        KstreamRestore out = null;
        for (KstreamRestore candidate: restores) {
            if (!candidate.done && key.equals(candidate.getId())) {
                if (out != null) throw new RuntimeException("double");
                out = candidate;
            }
        }
        if (out == null) throw new RuntimeException("not found!");
        return out;
    }
}