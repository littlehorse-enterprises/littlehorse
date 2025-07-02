package io.littlehorse.canary.metronome.internal;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import io.littlehorse.canary.CanaryException;
import io.littlehorse.canary.infra.ShutdownHook;
import io.littlehorse.canary.proto.Attempt;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;

public class LocalRepository {

    private final RocksDB db;

    public LocalRepository(final String dataPath) {
        RocksDB.loadLibrary();
        final Options options = new Options().setCreateIfMissing(true);

        try {
            db = RocksDB.open(options, dataPath);
        } catch (RocksDBException e) {
            options.close();
            throw new CanaryException(e);
        }

        ShutdownHook.add("Local Repository", () -> {
            db.close();
            options.close();
        });
    }

    public void save(final String id, final long attempt) {
        final Timestamp currentTime = Timestamps.now();
        save(
                id,
                Attempt.newBuilder()
                        .setAttempt(attempt)
                        .setStart(currentTime)
                        .setLastAttempt(currentTime)
                        .build());
    }

    public void save(final String id, final Attempt attempt) {
        try {
            db.put(id.getBytes(), attempt.toByteArray());
        } catch (RocksDBException e) {
            throw new CanaryException(e);
        }
    }

    public Attempt get(final String id) {
        try {
            final byte[] data = db.get(id.getBytes());
            return data == null ? null : Attempt.parseFrom(data);
        } catch (RocksDBException | InvalidProtocolBufferException e) {
            throw new CanaryException(e);
        }
    }

    public void delete(final String id) {
        try {
            db.delete(id.getBytes());
        } catch (RocksDBException e) {
            throw new CanaryException(e);
        }
    }

    public Map<String, Attempt> getAttemptsBefore(final Instant instant) {
        final Map<String, Attempt> attempts = new HashMap<>();
        final RocksIterator iterator = db.newIterator();

        try (iterator) {
            iterator.seekToFirst();
            while (iterator.isValid()) {
                final String id = new String(iterator.key());
                final Attempt attempt = Attempt.parseFrom(iterator.value());
                final Instant attemptInstant = Instant.ofEpochMilli(Timestamps.toMillis(attempt.getLastAttempt()));

                if (attemptInstant.isBefore(instant)) {
                    attempts.put(id, attempt);
                }

                iterator.next();
            }
        } catch (InvalidProtocolBufferException e) {
            throw new CanaryException(e);
        }

        return attempts;
    }
}
