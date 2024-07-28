# Concepts

The LittleHorse Server is, at its core, a _Workflow Engine_. What makes LittleHorse unique is that it is built with developers in mind. The way users define workflow specifications in LittleHorse (a `WfSpec`) was designed from the ground up to be developer-friendly and have concepts that are analogous to the primitives used in day-to-day programming languages.

This section covers the following concepts:

* The structure of a `WfSpec` and a `WfRun`
* How Task Workers execute Tasks
* How User Tasks allow humans to interact with a workflow
* How External Events allow the outside world to interact with LittleHorse
* Control Flow in LittleHorse
* Authentication, Authorization and Multi-Tenancy using `Principal`s and `Tenant`s
