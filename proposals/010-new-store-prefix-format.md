
# New Store Prefix Format

This proposal changes the prefix structure 

## Motivation

### History
- The initial prototype of LittleHorse had a separate Kafka Streams State Store for every single object
  type, including WfRun, TaskRun, Tag, NodeRun, etc. Total: 22 stores
- We introduced `Storeable` and `ReadOnlyBaseStore`/`BaseStore` as wrappers around the Kafka Streams
  `KeyValueStore` to reduce the number of RocksDB instances and Changelog Topics.

The initial layout (which is now LEGACY) was as follows:

```
{tenant_id}/{storeable_type}/{storeable_key}
```

The `store_key` is somewhat nested in the case of a Getable: it is first the getable type then the getable id.
To put everything together, here's a few rocksdb keys related to a single WfRun. Note that they don't share a
good prefix:

```
default/STORED_GETABLE/WF_RUN/asdf
default/STORED_GETABLE/NODE_RUN/asdf/0/1
default/STORED_GETABLE/TASK_RUN/asdf/0-1
default/WF_RUN_STORED_INVENTORY/asdf
```

(note that enums like STORED_GETABLLE and WF_RUN are substituted for their ordinal numbers for efficiency)
In order to optimize rocksdb, we want all objects that are accessed together frequently to live on the
same block, which optimizes many things (especially cold reads such as those that happen during rpc
DeleteWfRun). This is accomplished by making them share the same prefix. In order to do that, we want
all objects related to a WfRun to have the WfRunId as the prefix. This is possible to do with:

- All `Getable`'s associated with a WfRun
- The `WfRunStoredInventory`

We cannot put tags on the same prefix because we rely on the prefix for searching. Fortunately, when doing
a `rpc DeleteWfRun` we do not need to get() the Tag's; we only need to delete them. Therefore we can still
significantly reduce the number of cold disk reads (my tests show 70-80% fewer IOPs for workflow retention).

## Proposal

The new Rocksdb structure will look like this:

```
default/WF_RUN_GROUPED_OBJECT/asdf/WF_RUN
default/WF_RUN_GROUPED_OBJECT/asdf/NODE_RUN/0_1
default/WF_RUN_GROUPED_OBJECT/asdf/TASK_RUN/0-1
default/WF_RUN_GROUPED_OBJECT/asdf/WF_RUN_STORED_INVENTORY
```

In LittleHorse, the `_` and `/` characters are not allowed as part of an object ID. They are used in 

We need objects created with the legacy format to remain backward-compatible.

## Migration

We will take care to make this change fully backwards-compatible, which is difficult. Our `1.x` releases will include code that transparently understands both storage formats and incrementally migrates each rocksdb object to the correct format. Our `2.0` release will remove this migration code.

### Point Gets

We need to support objects that were put into RocksDB before the new upgrade was released. The flow for a point get() will be:

1. Check the new RocksDB key. If the object exists, then return it.
2. If the object doesn't exist, then check the old key.

The above will be done in the read path. However, in the Core Processor, we can also do an online, non-intrusive migration. In the `GetableManager` when we read an object and discover that it exists with the old key format, we will:

1. Call `delete(oldKey)` on RocksDB.
2. Call `put(newKey, theObject)` on RocksDB.

This will be a small performance penalty at the start, but writing a tombstone is cheap and we've chosen the Level Compaction Priority mechanism that optimizes for a lot of tombstones and overwrites. It will also allow a fully backward-compatible migration process for our users.

If the user has a lot of long-running `WfRun`s that werer started before the upgrade, this will initially result in a lot of `get()`'s to keys that don't exist. However, there are two mitigating factors:
* We have bloom filters on all levels, which prevent 99% of SST file reads in missing cases.
* Over time, the majority of objects will be written with the correct format. Since we will check the new (expected) key first, the majority of reads will succeed on the first check.

### Searches and `Tag`s

This proposal does not affect `Tag`s. Any `rpc Search{...}` will be unaffected.

### Object ID Scans

It is with great reluctance that I say this, but we can do two range scans on each `rpc List{foo}`. The flow is to:

* Check the `created_at` of the original `WfRun` and compare it to the timestamp of the upgrade according to the Initialization Log.
* If the `created_at` is older than the new initialization log, we do range scans over both the old keys and the new keys.
* Otherwise we only do scans over the new key.

Our Object ID Scans happen only in the Request Context, where we do not have write access. Therefore there is no migration happening here.

### Initialization Log

We can extend the `ServerInitializationLog` protobuf to understand when the server was created so that it ignores the double-get pattern for new clusters. This will help avoid all of the performance losses except for clusters created after `0.15.0`.

### Internal Class Structure

First, we create a new interface called `WfRunGroupedObject`:

```
public interface WfRunGroupedObject {
    /**
     * Returns the Id of the `WfRun` that this object is grouped to.
     */
    WfRunIdModel getGroupingWfRun();
}
```

## Benchmarks

I ran the Canary Workflow at a rate of 200 tasks per second (three servers) and default retention (24 hours). The server's RAM was small enough that only L0 blocks (and maybe some of L1) could fit in memory; therefore, most of the database was on cold disk. I analyzed the number of IOPs (particularly, disk reads) when the 24-hour retention period started expiring and we processed `DeleteWfRUn` timers.

Note that each server was processing about 66 `WfRun`'s per second.

### Results
**Before**: I observed between 200-225 disk IOP's.
**After**: I observed about 50-60 disk IOP's.

Note: RocksDB Compaction results in a lot of disk I/O. However, it's somewhat bursty, and there are "quiet periods." These numbers are from the quiet periods, measured by kubernetes pod metrics.

### Interpretation

In the "before," we expect that `NodeRun`'s, `TaskRun`'s, and `WfRun`'s all end up on different blocks. In the Canary we do not utilize `ExternalEvent`s; therefore, we do not expect to have a `WfRunStoredInventoryModel`. Because of bloom filters, we expect to not actually read the disk for most of the `WfRunStoredInventory` get()s. Therefore, we expect to need to fetch three different blocks per `WfRun`: one for the three `NodeRun`s (which share a common prefix), one for the `TaskRun`, and one for the `WfRun`.

Three blocks per `WfRun` times 66 per second is about 200 disk reads per second. We observed slightly more, which is likely due to extra block cache churn.

In the "after," we expect all of the objects (in most `WfRun`s) to fit on a single block. We therefore expected 66 disk reads per second; however, we actually observed _less_ than 66 disk reads in most seconds (generally 50-60). This is probably because of a few factors:
* More efficient compaction means less competition with the block cache.
* Block size of 64kb means multiple `WfRun`s can fit on a single block.
* Due to reduced block cache churn, 

TLDR: this proposal does what it's supposed to do, with no breaking changes to existing workloads.
