#!/bin/bash

# pardon the grossness here, I struggle with bash string concatenation ):
PAYLOAD='{"wfSpecId":"'
PAYLOAD=${PAYLOAD}$1'"}'
curl -s -d $PAYLOAD localhost:5000/WfRun
