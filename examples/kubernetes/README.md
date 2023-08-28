# Kubernetes Sandbox

This example is intended to be used in development environments.

## Prerequisites

- A running `k8s` cluster (ex: [kind](https://kind.sigs.k8s.io/)).
- A running `kafka` cluster (ex: [strimzi](https://strimzi.io/quickstarts/)).
- `docker`.

## Deploying

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
lhctl search wfSpec
```

Result:

```
{
  "results": []
}
```
