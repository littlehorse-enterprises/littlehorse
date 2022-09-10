#!/bin/bash

curl -d @$1 -Hcontent-type:application/json localhost:5000/WfSpec
