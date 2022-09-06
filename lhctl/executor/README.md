# Prototype Python Executor

*NOTE: THIS IS A PROTOTYPE*

This folder is a task executor which loads info from a TaskDef and executes a python function. It integrates with the prototype `lhctl compile` output (specifically, the `Dockerfile` section) and the java `BashExecutor` task executor. The integration is brittle and doesn't port over to other forms of executors, so it should be moved to an `examples` folder rather than being presented as The Way. However, it's just a prototype for now and we will move it out later.