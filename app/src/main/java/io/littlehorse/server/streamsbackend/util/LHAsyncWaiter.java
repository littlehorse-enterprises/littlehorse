package io.littlehorse.server.streamsbackend.util;

import com.google.protobuf.MessageOrBuilder;
import io.littlehorse.common.model.command.AbstractResponse;

public class LHAsyncWaiter<T extends MessageOrBuilder> {

    public LHAsyncWaiter(LHAsyncResponseCallback<T> callback, Class<T> responseCls) {
        this.callback = callback;
        this.responseCls = responseCls;
    }

    private LHAsyncResponseCallback<T> callback;
    private Class<T> responseCls;

    public void onResponse(AbstractResponse<?> response) {
        T t = responseCls.cast(response);

        callback.onResponse(t);
    }
}
/*

Places we need the async waiter:

** Wait For Processing **
- Send a command to kafka
- MAKE REQUEST to that host
- upon receipt of the response from request in step above, fire the callback.
** Scheduling a task **
##########################################################################

Things to do:
** Queue for processed commands **
Things get added to this queue when:
 - the internalWaitForProcessing rpc is called.
 - a localWaitForProcessing() java method is called
 note that item 1) also causes item 2).


** Use the Async Client for all internal point-to-point communications **


** Dual queues for scheduled tasks **



 */
