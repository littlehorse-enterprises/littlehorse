import json
import requests
import subprocess
import sys
import time

IMG = 'lh-example-worker:latest'

if len(sys.argv) > 1:
  IMG = sys.argv[1]

def gen_task_yaml(name: str, image: str = IMG):
    return f'''
apiVersion: apps/v1
kind: Deployment
metadata:
  name: {name}
  labels:
    app: {name}
    io.littlehorse/deployedBy: "true"
spec:
  replicas: 3
  selector:
    matchLabels:
      app: {name}
  template:
    metadata:
      labels:
        app: {name}
    spec:
      topologySpreadConstraints:
      - maxSkew: 1
        topologyKey: topology.kubernetes.io/zone
        whenUnsatisfiable: DoNotSchedule
        labelSelector:
          matchLabels:
            app: {name}
      containers:
      - name: {name}
        image: {image}
        imagePullPolicy: IfNotPresent
        env:
        - name: "LHORSE_API_HOST"
          value: "lh-server.default"
        - name: "LHORSE_API_PORT"
          value: "5000"
        - name: "LHORSE_TASK_DEF_ID"
          value: {name}
        - name: "LHORSE_NUM_WORKER_THREADS"
          value: "2"
'''

task_name = "task1"

subprocess.run(
  "lhctl deploy taskDef task1.json".split()
)

yaml = gen_task_yaml(task_name)
subprocess.run(
    "kubectl apply -f -".split(), input=yaml.encode(),
)

subprocess.run(
  "lhctl deploy wfSpec simple_wf.json".split()
)
