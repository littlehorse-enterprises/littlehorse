# LittleHorse Java Examples

Here you will find a few examples to demonstrate simple `WfSpec` concepts in LittleHorse.

## Prerequisites

First, you need a running LH Server (either locally in your own terminal, or remote).

> The [LittleHorse Website](https://littlehorse.io/docs/server) has information about how to run it.
> In addition, you can go to [docker-compose](docker-compose) and check the documentation.

Next, you'll need to have the `lhctl` command line tool installed.

Finally, you'll need `gradle` and Java 17 or later.

## Understanding the Java Wf SDK

See the Concepts documentation on the [LittleHorse Website](https://littlehorse.io/docs/server).

## Configuration

Create a file `~/.config/littlehorse.config` and configure it as follows:

```
LHC_API_HOST=foo.bar.com  # Host for the LH Server
LHC_API_PORT=2023

# If necessary, configure the following client certs:
LHC_CA_CERT=<path to ca cert file>
LHC_CLIENT_CERT=<path to client cert file>
LHC_CLIENT_KEY=<path to client key file>
```

> More information at [sdk-java](../sdk-java)

## Extra Useful Commands

Check the result of a workflow with:

```
# This call shows the result
lhctl get wfRun <wf_run_id>

# This will show you all nodes in the run
lhctl list nodeRun <wf_run_id>

# This shows the task run information
lhctl get taskRun <wf_run_id> <task_run_global_id>
```

You can list all the deployed workflows with:

```
# This call shows the list of registered Wf
lhctl search wfSpec
```
