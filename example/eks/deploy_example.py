import json
import requests
import subprocess
import sys
import time

IMG = '834373697194.dkr.ecr.us-east-2.amazonaws.com/lh-example-worker:latest'

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
        whenUnsatisfiable: ScheduleAnyway
        labelSelector:
          matchLabels:
            app: {name}
      containers:
      - name: {name}
        image: {image}
        imagePullPolicy: Always
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

# First, deploy tasks
for i in range(1, 6):
    name = f"task{i}"
    # subprocess.run(
    #     f"lhctl deploy taskDef {name}.json".split()
    # )

    yaml = gen_task_yaml(name)
    subprocess.run(
        "kubectl apply -f -".split(), input=yaml.encode(),
    )

# subprocess.run(
#   "lhctl deploy wfSpec simple_wf.json".split()
# )
