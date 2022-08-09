#!/bin/bash

HOW_MANY=$1
curl -d '{"wfSpecId":"simple_wf"}' localhost:5000/WfRun
