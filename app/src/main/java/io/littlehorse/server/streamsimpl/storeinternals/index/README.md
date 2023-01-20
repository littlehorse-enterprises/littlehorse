This directory contains utilities for the various types of secondary index. As of this writing, we have a hairball... but in the future it will be simplified to the following:

- Local Uncounted Index
  - This is used for tag items that have a high frequency of occurrence (eg. `status=COMPLETED` for a NodeRun). Because it's such a hot key, you can't use hashing, as it would mean all secondary index entries end up on the same node, which is a big bottleneck.
  - In this case, we just store the tags locally.
- Local Counted (to be deprecated)
  - Same as Local Uncounted, but we periodically broadcast the current local count from each partition to a global store so that we can keep track of how many total `RUNNING` wfRun's we have.
  - Will likely be deprecated because it probably should just be replaced by an external stream processing system and the observability events, and should not take up more processing room on the core processor.
- Remote Hash
  - Tag items with sparse values. A great candidate for this is a workflow variable such as `customer_email`. The value `customer_email=foo@bar.com` is hashed and used as the key to store the secondary index entry.
