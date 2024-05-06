---
sidebar_label: Install Operator
---

# Installing the LittleHorse Operator

> `lh-operator` needs to be installed in the same namespace as strimzi, for the strimze features to work

## Create the ImagePullSecret

> As described in the [Overview](/docs/platform/), this section assumes that you have already obtained an LH Platform license.

Create the secret needed to pull the lh-operator image from Quay. This secret will be used in the `lh-operator` helm chart. Make sure to create the secret on the same namespace where the `lh-operator` will be installed.


```yaml
apiVersion: v1
kind: Secret
metadata:
  name: <name-of-the-imagepullsecret>
  namespace: <namespace>
data:
  .dockerconfigjson: <base64encoded-secret-provided-by-littlehorse>
type: kubernetes.io/dockerconfigjson
```

## Install using Helm

Add littlehorse helm charts repository:

```shell
helm repo add littlehorse https://littlehorse-enterprises.github.io/lh-helm-charts/
```

Example values.yaml using strimzi:

```yaml
imagePullSecrets:
    - name: <name-of-the-imagepullsecret>
image:
    repository: quay.io/littlehorse/lh-operator
    tag: 0.5.1
strimzi:
    enabled: true
replicas: 1
```

Install the `lh-opertor` helm chart:

```shell
helm upgrade --install \
  --namespace <namespace> \
  -f <path-to-values-file> \
  lh-operator littlehorse/lh-operator
```

## Install using Helm and ArgoCD

Example of an installation using ArgoCD:

```yaml
---
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: littlehorse-operator
  namespace: <namespace>
spec:
  project: <project>
  source:
    repoURL: 'https://littlehorse-enterprises.github.io/lh-helm-charts'
    targetRevision: 0.5.1
    helm:
      releaseName: lh-operator
      values: |-
        imagePullSecrets:
            - name: <name-of-the-imagepullsecret>
        image:
            repository: quay.io/littlehorse/lh-operator
            tag: 0.5.1
        strimzi:
            enabled: true
        replicas: 1
    chart: lh-operator
  destination:
    namespace: <destination-namespace>
    name: in-cluster
  syncPolicy:
    automated:
      prune: true
      selfHeal: true
    syncOptions:
      - CreateNamespace=true
    retry:
      limit: 5
      backoff:
        duration: 5s
        factor: 2
        maxDuration: 3m0s
```