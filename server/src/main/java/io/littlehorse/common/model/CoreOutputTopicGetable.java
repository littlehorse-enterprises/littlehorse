package io.littlehorse.common.model;

import com.google.protobuf.Message;

/**
 * Classes that extend this class can be sent to the output topic.
 */
public abstract class CoreOutputTopicGetable<T extends Message> extends CoreGetable<T> {}
