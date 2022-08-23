#!/bin/bash
kubectl port-forward svc/little-horse-api 5000:5000 -ndefault

