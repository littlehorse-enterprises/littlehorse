---
title: Helm and Kubernetes Operators
description: To Helm or to Operator?
slug: helm-and-k8s-operators
authors:
  - name: Colt McNealy
    title: LittleHorse Council Member
    url: https://www.linkedin.com/in/colt-mcnealy-900b7a148/
    image_url: https://avatars.githubusercontent.com/u/100447728
tags: [friday-tech-tips, kubernetes]
image: https://avatars.githubusercontent.com/u/140006313?s=400&u=7bf4c91d92dfe590ac71bb6b4821e1a81aa5b712&v=4
hide_table_of_contents: false
---

About [Helm](https://helm.sh) vs Kubernetes Operators.

<!-- truncate -->

## Helm

Helm is like `brew` or `npm` for Kubernetes. There are repositories containing charts, and each chart allows you to install an application into your K8s cluster.

### How it Works

Under the hood, Helm works by filling out some templated Kubernetes yaml files with user-provided values, drastically reducing boilerplate and allowing you to deploy a reasonably complex application without the user of the helm chart having to understand too much about how to manage such an application.

In addition to that template-engine functionality, Helm also manages versions of your application. You can use Helm to release a new version of your app (for example, updating the docker image tag) and then quickly roll back to a previous version if you discover a bug. This is great for teams deploying stateless applications such as microservices or [LittleHorse Task Workers](https://littlehorse.dev/docs/concepts/task-workers).

### The Good

First, it is quite simple to write a Helm chart. This means that most DevOps teams can quickly write a helm chart that can be used by microservice teams across their organization.

Secondly, Helm is a client library (well, it has been since the removal of Tiller...but that's another blog post). Therefore, you don't need to run any privileged pods inside the K8s cluster; all you need is a CI server with permission to create the necessary K8s resources.

### Limitations

Unfortunately, Helm doesn't do much beyond initial installation and upgrades. Monitoring, self-healing, autoscaling, certificate rotation, and management of non-Kubernetes resources (eg. Kafka Topics, LittleHorse Task Definitions, AWS LoadBalancers, etc) are some exercises left to the reader, to name just a few.

## Operators

[Kubernetes Operators](https://operatorframework.io/) are a pattern introduced by Red Hat that intends to capture the knowledge of an expert Site Reliability Engineer (or, more punnily, a software operator) into a program that manages (or operates) a complex application.

To accomplish this, a Kubernetes Operator _extends_ the Kubernetes API to introduce a new resource type that is custom-made for your application. The Operator works in tandem with Kubernetes itself to manage applications of a specific type.

### How they Work

A Kubernetes Operator has two components:

1. A `CustomResourceDefinition`, which defines the extension to the Kubernetes API (including relevant configurations for your application type).
2. A Controller, which watches any resources from your Custom Resource Definition and "reconciles" them.

The `CustomResourceDefinition` can be over-simplified as an Open API (not Open AI) specification for how your custom resource will look. For example, in LittleHorse Platform, the simplest version of a `LHCluster` resource (which creates a, you guessed it, LittleHorse Cluster) is:

```yaml
apiVersion: littlehorse.io/v1
kind: LHCluster
metadata:
  name: hello-littlehorse
  namespace: lh
spec:
  server:
    version: "0.2.0"
    listeners:
    - name: internal-k8s
      type: internal
      port: 2023
    replicas: 3
    storage:
      volumeSize: "10G"
  kafka:
    strimziClusterRef:
      clusterName: my-strimzi-kafka-cluster
      listenerPort: 9093
```

The `CustomResourceDefinition` allows you to `kubectl apply -f <that file up there>`, and then you can `kubectl get lhclusters`:

Now how does the LittleHorse cluster get created, configured, managed, and monitored? That's where the Controller comes into play. In the Operator pattern, a Controller is a process (normally, it runs as a `Pod` in a cluster) that watches for all events related to a `CustomResourceDefinition` and manipulates the external world to match what the Custom Resources specify.

Generally, that means creating a bunch of Kubernetes `Service`s, `Deployment`s, etc. to spin up an instance of an application. For example, the [Strimzi](https://strimzi.io) Kafka Operator watches `Kafka` resources and deploys an actual Kafka cluster.

However, a Controller can also manage non-kuberentes resources. For example, many `Ingress` controllers provision or configure physical load balancers outside of the Kubernetes cluster. As another great example, the [Strimzi](https://strimzi.io) Kafka Topic Operator watches for `KafkaTopic` resources and creates (you guessed it) Kafka Topics using the Kafa Admin API.

We at LittleHorse plan to add similar CRD's that are specific to LittleHorse...stay tuned to learn about the `LHTaskDef` and `LHPrincipal` CRD's :wink:.

### The Good

Kubernetes Operators are beautiful. Since they were developed by Red Hat, they (along with [Strimzi](https://strimzi.io)) are the biggest reason why Red Hat is in my top-three favorite software companies of all time.

A well-written operator can make it a breeze to manage even the most daunting applications. Since the Controller is code written in a general-purpose language (normally Go or Java), an Operator can do just about anything that can be automated by an SRE. This includes:
* Autoscaling and alerting based on metrics
* Self-healing and mitigation in the face of hardware faults or degradations
* Certificate rotation
* Metadata management in your application (for example, creating Kafka Users)
* Intelligent rolling restarts that preserve high availability
* Provisioning infrastructure _outside of_ Kubernetes, for example [CrossPlane](https://crossplane.io).

### The Ugly

The biggest downside to Operators in Kubernetes is that writing a Controller is _hard_. Additionally, it requires running a `Pod` with special privileges that allow the `Pod` to create other K8s resources. Because of this, writing an Operator for something like standardizing your team's blueprints for deploying a microservice just doesn't make sense.

Future blogs will dive into some of the challenges that we had to overcome with LittleHorse Platform, and how we minimized the permissions that our Operator needs to provide a self-driving LittleHorse experience to our customers.

## Helm or Operators?

Well, I'm a software engineer, so I'm going to say "it depends." However, Kafka legend Gwen Shapira said in a fantastic [podcast](https://open.spotify.com/episode/0BYwF3e8y5OzrPt0xYMyqb?si=0c7d44154b434d0e) that some "it depends" are more helpful than others. So in an effort to fall in the "more helpful" side:

* If you want a framework for deploying simple stateless applications while minimizing boilerplate (i.e. allowing different teams to deploy microservices), then you probably want Helm.
* If your application doesn't require much hand-holding after initial configuration on Kubernetes, Helm might do.
* If you want to provide a Kubernetes-native way to manage non-kubernetes infrastructure, you need an Operator.
* If you want to provide a self-driving experience for consumers of a highly complex application such as Kafka, ElasticSearch, or LittleHorse, you need an Operator.

### LittleHorse Platform

LittleHorse Platform is an enterprise-ready distribution of LittleHorse that runs in your own Kubernetes environment. We believe that Helm is fantastic for deploying many stateless applications, and even some stateful applications. However, Helm wouldn't let us go far enough towards providing our customers with a fully self-driving LittleHorse experience. As such, we chose to put in the extra work and build a full Kubernetes Operator. Stay tuned for an extensive list of current and upcoming LH Platform features, all powered by the [Java Operator SDK](https://javaoperatorsdk.io).

To inquire about LittleHorse Platform, contact `sales@littlehorse.io`. To get started with LittleHorse Community (free for production use under the SSPL), check out our [Installation Docs](https://littlehorse.dev/docs/developer-guide/install).
