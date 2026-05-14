package io.littlehorse.server.streams.lhinternalscan.count;

import com.google.protobuf.Message;
import io.littlehorse.common.LHSerializable;

public abstract class AbstractCountRequest<T extends Message> extends LHSerializable<T> {}
