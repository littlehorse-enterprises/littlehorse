#!/bin/bash

curl -XPOST -Hcontent-type:application/json localhost:5000/TaskDef -d '{"name":"task1", "requiredVars": {"myTaskVar": {"type": "STR", "defaultVal": {"type": "STR", "str": "Hello, there!"}}}}'
