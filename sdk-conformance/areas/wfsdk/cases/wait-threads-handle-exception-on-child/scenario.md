# wait-threads-handle-exception-on-child

Build `probe-wait-threads-handle-exception-on-child` (R10) exercising `WaitForThreadsNodeOutput#handleExceptionOnChild`.
The base is the nearest do-nothing neighbor; the feature adds exactly the
gesture above, and the fixture diff is its entire effect.
The reference body is in WfsdkArea.java (see rules.md, "Minting"); the
frozen fixtures are the contract. Rules: [../../rules.md](../../rules.md).
