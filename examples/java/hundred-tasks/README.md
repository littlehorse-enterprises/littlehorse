## Running Hundred-Tasks

This workflow doesn't do anything particularly useful other than run a hundred `TaskRun`s such that we can stress test + tune RocksDB.

To run 100,000 Tasks, you can do the following.

```
for i in $(seq 1 1000)
do
    lhctl run hundred-tasks
done
```

You can use this for a *highly informal* performance benchmark. To see the number of TaskRun's scheduled per Stream Thread, basically:

* Note the time you ran the command.
* Get the last `WfRun` and note the time it completes. Use this time to calculate the total time from the experiment.
* Your tasks per second per Stream Thread is given by:

```
100,000 / (<number of seconds> * <number of stream threads>)
```

`number of seconds` is determined by the process above. `number of stream threads` depends on your server config, but if you use the [Docker Quickstart](../../docs/QUICKSTART_DOCKER.md), [Docker Compose Quickstart](../docker-compose/) or `./local-dev/do-server.sh` without any fancy config, it's just one stream thread.
