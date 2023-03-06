# LittleHorse Server
- [LittleHorse Server](#littlehorse-server)
  - [LH Repository Inventory](#lh-repository-inventory)
- [Development](#development)
  - [Prerequisities](#prerequisities)
  - [Running LH](#running-lh)
  - [Configuring Clients](#configuring-clients)
  - [Building the Image](#building-the-image)
  - [Advanced](#advanced)
    - [Running Multiple LH Servers](#running-multiple-lh-servers)
    - [Debug Cycle](#debug-cycle)
  - [Cleanup](#cleanup)

This repository contains the code for the core LittleHorse Server. It has a dependency on the `io-littlehorse-jlib` repository's Java library.

For a description of the architecture, see the [architecture docs](docs/ARCH.md).

## LH Repository Inventory
The LittleHorse project currently has multiple repositories, described below:
* [`io-littlehorse`](https://github.com/littlehorse-eng/io-littlehorse)
    *  Code for the LittleHorse Server.
* [`io-littlehorse-jlib`](https://github.com/littlehorse-eng/io-littlehorse-jlib)
    * Protocol Buffer definitions used by clients and the `io-littlehorse` repo.
    * Library for creating `WfSpec`'s in Java.
    * Library for executing `TaskRun`'s in Java.
    * Shared constants, including configuration constants used by the LH Server.
* [`io-littlehorse-golib`](https://github.com/littlehorse-eng/io-littlehorse-golib)
    * The `lhctl` command line interface.
    * Library for creating `WfSpec`'s in GoLang (under development).
    * Library for executing `TaskRun`'s in GoLang (under development).
* [`io-littlehorse-jtests`](https://github.com/littlehorse-eng/io-littlehorse-jtests)
    * A series of system integration tests used to verify the cohesive behavior of the LH Server, Java Workflow SDK, and Java Task Library together.
* [`io-littlehorse-operator`](https://github.com/littlehorse-eng/io-littlehorse-operator)
    * Code for a K8s Init Container that sets up the LittleHorse config file based on the `topology.kubernetes.io/zone` and pod name.
    * Scripts to deploy LittleHorse on KIND or EKS.
    * (FUTURE) code for a LittleHorse Controller/Operator.

# Development

This section describes how to run the LittleHorse server in a development environment.

## Prerequisities

Your system need the following:
* `openjdk`, preferably version 17 or later.
* `gradle`, preferably version 7.4 or later.
* `docker` and `docker-compose`

Once you've set up your system, you *also* need to publish the `io-littlehorse-jlib` library to your local Maven repository. See the `README` on that repo for instructions.

Additionally, you'll eventually want to install `lhctl` as per the `io-littlehorse-golib` repository.

## Running LH

The LH Server depends on Kafka as a backend database. To start Kafka using docker-compose, you can run:

```
./local-dev/setup.sh
```

Next, you can start the LH server itself. The server can be started in a single command:

```
./local-dev/do-server
```

You can confirm that the Server is running via:

```
-> lhctl search wfSpec
{
  "code":  "OK",
  "objectIds":  []
}
```

## Configuring Clients

When you run the LH Server according to the command above, the API Host is `localhost` and the API Port is `5000`.

Also note that mTLS will NOT be enabled; therefore, you should not have any client keys or certs configured in your `~/.config/littlehorse.config` if you wish to interact with the LH Server running in your terminal as per this README.

At this point, you're ready to consult the `README` in the `io-littlehorse-jlib` repository for a tutorial on how to run your first workflow.

## Building the Image

To build the `littlehorse` docker image, run:

```
./build/build.sh
```
This step is needed to develop on KIND.

## Advanced
This section covers more advanced topics that you'll need to know when modifying the actual LH Server code.

### Running Multiple LH Servers

LittleHorse is a distributed system in which the different LH Server Instances (Brokers) need to communicate with each other. For example (among many others), all GET requests on the API use Interactive Queries, which involves requests between the different Brokers. Therefore, you'll need to be able to test with multiple brokers running at once.

Running two brokers is slightly tricky as you must configure the ports, advertised hostnames, and Kafka group instance ID's correctly.

However, you can start two Brokers in your terminal as follows:

```
# The first server has an external API port of 5000
./local-dev/do-server

# <In another terminal>
# The second server has an external API port of 5002
./local-dev/do-server2
```

### Debug Cycle

To "reset" the LittleHorse cluster, you need to delete the data in Kafka and also delete the KafkaStreams RocksDB state. That can be done as follows:

1. Stop all LH Server processes.
2. Run `./local-dev/refresh.sh`.
3. Start the LH Servers again.

## Cleanup

You can clean up (i.e. stop Kafka and delete the data from the state directory) as follows:

```
./local-dev/cleanup.sh
```