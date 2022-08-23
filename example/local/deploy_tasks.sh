#!/bin/bash

curl -XPOST -Hcontent-type:application/json localhost:5000/TaskDef -d '{"name":"task1"}'

