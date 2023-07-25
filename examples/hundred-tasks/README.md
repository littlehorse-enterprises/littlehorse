## Running Hundred-Tasks

This workflow doesn't do anything particularly useful other than run a hundred `TaskRun`s such that we can stress test + tune RocksDB.

To run 100,000 Tasks, you can do the following. Note that it is a LOT of load.

```
for i in $(seq 1 10)
do
    for j in $(seq 1 100)
    do
        lhctl run hundred-tasks --wfRunId $i-$j-some-long-string-to-stress-rocksdb &
    done
    sleep 1
done
```
