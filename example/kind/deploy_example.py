import json
import requests
import subprocess
import sys
import time

IMG = 'littlehorse:latest'

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
        command: ['/worker']
        env:
        - name: "LHORSE_KAFKA_BOOTSTRAP"
          value: "lh-kafka-kafka-bootstrap.kafka:9092"
        - name: "LHORSE_KAFKA_GROUP_ID"
          value: {name}-worker
        - name: "LHORSE_KAFKA_GROUP_IID"
          valueFrom:
            fieldRef:
              fieldPath: metadata.name
        - name: "LHORSE_TASK_DEF_ID"
          value: {name}
        - name: "LHORSE_NUM_WOERKER_THREADS"
          value: "64"
        args:
        - "worker"
'''

# First, deploy tasks
for i in range(1, 4):
    name = f'task{i}'
    tpost = {"name": name}
    print(json.dumps(requests.post("http://localhost:5000/TaskDef", json=tpost).json()))
    yaml = gen_task_yaml(name)
    subprocess.run(
        "kubectl apply -f -".split(), input=yaml.encode(),
    )

with open("simple_wf.json", 'r') as f:
    wf = json.loads(f.read())

response = requests.post("http://localhost:5000/WfSpec", json=wf)
print(response.json())
