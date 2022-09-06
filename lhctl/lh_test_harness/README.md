# Test Harness Executor

This module contains code for an `lhctl`-native Integration Test Harness. The test harness hijacks the task executions so that every task execution is logged to a SQL database so that task executions can be verified after Workflow Runs.