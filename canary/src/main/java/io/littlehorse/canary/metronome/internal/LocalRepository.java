package io.littlehorse.canary.metronome.internal;

import com.google.protobuf.InvalidProtocolBufferException;
import io.littlehorse.canary.CanaryException;
import io.littlehorse.canary.proto.Attempt;
import io.littlehorse.canary.util.ShutdownHook;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

public class LocalRepository {

    private final RocksDB db;

    public LocalRepository(final String dataPath) {
        RocksDB.loadLibrary();
        final Options options = new Options().setCreateIfMissing(true);

        try {
            db = RocksDB.open(options, dataPath);
        } catch (RocksDBException e) {
            throw new CanaryException(e);
        }

        ShutdownHook.add("Local Repository", () -> {
            db.close();
            options.close();
        });
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
}
