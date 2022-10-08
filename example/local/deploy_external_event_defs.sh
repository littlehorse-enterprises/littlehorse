#!/bin/bash

curl -XPOST -Hcontent-type:application/json localhost:5000/ExternalEventDef -d '{"name":"some-event"}'

