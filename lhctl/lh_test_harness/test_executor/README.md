# Test Executor

This module is a shadow of the `lhctl.executor` module: it provides a bash-y executable that runs a Task; however, it also logs the execution of that task to the LH Test DB (sql) so that it can be verified after-the-fact.