# Quickstart Reference

This page assumes that you have already [installed](./01-install-operator.md) the operator into your kubernetes cluster. It is intended to provide a series of minimal examples that can get you started. Additional support is available from the LittleHorse team, and additional documentation is coming soon.

## Requirements

The `0.5.1` version of LH Platform currently requires:

* The `name` of a Strimzi `Kafka` cluster, and a port number which it can be accessed on.
* The `name` of a `StorageClass` that can be used to provision Persistent Volumes.

:::note
Although te current version requires a Strimzi Kafka cluster, future versions will support other Kafka providers (such as MSK or Confluent) We will also support a "littlehorse-managed kafka" option in which the LH Platform Operator itself manages Kafka.
:::

## Basic Cluster

In this section, we will discuss the most basic `LHCluster` possible.

```yaml
apiVersion: littlehorse.io/v1
kind: LHCluster
metadata:
  name: basic-cluster
  namespace: my-namespace
spec:
  server:
    listeners:
    # Configure exposed ports on the LH Server.
    - name: internal
      port: 2023
    image: public.ecr.aws/littlehorse/lh-server:0.7.2
    imagePullPolicy: IfNotPresent
    hotStandbyReplicas: 1 # Configure hot standby replicas for high availabiltiy
    replicas: 1
    storage:
      storageClassName: "standard" # TODO: Replace with your storage class
      volumeSize: "10G" # required

  kafka:
    # Specify replication factor of 3 for all LH topics, and use 12 partitions
    # for each topic. Note: There are 6 topics, so this config uses 72 partitions total.
    replicationFactor: 3
    clusterPartitions: 12

    # Tell LittleHorse how to access Kafka
    strimziClusterRef:
      clusterName: 'lh-kafka' # Replace with name of your Strimzi cluster
      createTopics: true

      # assumes that the Strimzi Kafka port is plaintext with no authentication
      listener:
        tls: false
        authentication: NONE
        port: 9093
```

In this example, we have the following characteristics:

* There is a Strimzi `Kafka` resource named `lh-kafka` in the same namespace as the `LHCluster`, with a plaintext port on `9093`.
* The LH Server can be contacted via the `basic-cluster-server:2023` endpoint within the `my-namespace` namespace.
* The LH Server cannot be reached from outside the cluster.
* The LH Server does not have authentication configured.

## OpenShift Sample

The following example might be appropriate for users of OpenShift who wish to access LittleHorse from outside the cluster through an OpenShift `Route`.

Accessing LittleHorse from outside the cluster requires some further configuration because LittleHorse Clients need to be able to address individual LH Server Instances directly. This is similar to Kafka, and I recommend this [fantastic blog series](https://strimzi.io/blog/2019/04/17/accessing-kafka-part-1/) by Strimzi.

```yaml
apiVersion: littlehorse.io/v1
kind: LHCluster
metadata:
  name: openshift-example
  namespace: littlehorse
spec:
  server:
    listeners:
      - name: internal
        port: 2023
      - name: external
        port: 2024
        advertisedListeners:
          servers:
            - host: "lh-1.littlehorse.cloud" # TODO: Configure server 1 external URL based on the OpenShift route URL
              port: 443 # TODO: Configure server 1 port based on the OpenShift route port
    image: public.ecr.aws/littlehorse/lh-server:0.7.2
    imagePullPolicy: IfNotPresent
    hotStandbyReplicas: 1
    replicas: 1
    storage:
      storageClassName: "gp2" #TODO: change this to the appropriate storage class
      volumeSize: "10Gi"
  kafka:
    replicationFactor: 3
    clusterPartitions: 6
    strimziClusterRef:
      clusterName: 'kafka' #TODO: change this to the appropriate strimzi cluster name
      createTopics: true
      listener:
        tls: false
        authentication: NONE
        port: 9094 #TODO: change this to the appropriate strimzi cluster port
```

After creating the LH Cluster, you can create OpenShift `Route`s or Kubernetes `Ingress`es as follows:

- Bootstrap: `openshift-example-server` on port `2024`
- LittleHorse Server 1: `openshift-example-server-0` on port `2024`

The advertised hosts should match the configurations from `spec.server.listeners[1].advertisedListeners`.

Note that you can also access the `LHCluster` from within you OpenShift environment on the `openshift-example-server:2023` endpoint.

:::info
Future documentation will show how to deploy and configure multiple servers.
:::