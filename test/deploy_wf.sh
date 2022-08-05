#!/bin/bash

curl -d @./simple_wf.json -Hcontent-type:application/json localhost:5000/WFSpec
