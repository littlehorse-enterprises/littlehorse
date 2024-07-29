# Concepts

A `WfSpec` in LittleHorse is a Protobuf that defines your technical or business process (it can also be representated in JSON). While the LittleHorse Server only understands the protobuf format of the `WfSpec`, the well-defined nature of the data format allowed LittleHorse to create SDK's in Java and GoLang that let you create `WfSpec`s from natural code. Since most of the LittleHorse concepts map nicely to programming concepts, this is a very natural and easy way to define your `WfSpec`s.

The Developer Guide discusses in detail how to develop `WfSpec`s with your SDK of choice.

**This section discusses how the `WfSpecs` and `WfRun`s created by those SDKs behave.**
