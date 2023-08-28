# Kubernetes Sandbox

This example is intended to be used in development environments.

## Prerequisites

- A running `k8s` cluster (ex: [kind](https://kind.sigs.k8s.io/))
- A running `kafka` cluster (ex: [strimzi](https://strimzi.io/quickstarts/))
- `docker`

## 1. Build the Image

```
docker build --file ../../docker/Dockerfile --tag littlehorse/lh-server:latest ../../
```

> Take into account that the image has to be loaded into your kubernetes cluster

## 2. Deploying

```
kubectl apply -f deployment.yaml
```

Check your `~/.config/littlehorse.config` file:

```
LHC_API_HOST=localhost
LHC_API_PORT=32023 # NodePort
```

You can confirm that the Server is running via:

```
lhctl search wfSpecModel
```

Result:

```
{
  "code":  "OK",
  "objectIds":  []
}
```
