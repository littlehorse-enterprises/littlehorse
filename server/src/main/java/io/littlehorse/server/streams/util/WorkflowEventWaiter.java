package io.littlehorse.server.streams.util;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.core.events.WorkflowEventModel;
import io.littlehorse.common.model.getable.objectId.WorkflowEventDefIdModel;
import io.littlehorse.common.model.getable.objectId.WorkflowEventIdModel;
import io.littlehorse.sdk.common.proto.AwaitWorkflowEventRequest;
import io.littlehorse.sdk.common.proto.WorkflowEvent;
import io.littlehorse.sdk.common.proto.WorkflowEventDefId;
import io.littlehorse.sdk.common.proto.WorkflowEventId;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;

import lombok.Getter;

@Getter
public class WorkflowEventWaiter {

    private List<WorkflowEventDefIdModel> eventsToWaitFor;
    private List<WorkflowEventIdModel> ignoredEvents;
    private Date arrivalTime;
    private StreamObserver<WorkflowEvent> observer;

    private boolean alreadyCompleted;

    public WorkflowEventWaiter(
            AwaitWorkflowEventRequest request, StreamObserver<WorkflowEvent> observer, ExecutionContext context) {
        this.alreadyCompleted = false;
        this.arrivalTime = new Date();
        this.ignoredEvents = new ArrayList<>();
        this.eventsToWaitFor = new ArrayList<>();
        this.observer = observer;

        for (WorkflowEventId id : request.getWorkflowEventsToIgnoreList()) {
            ignoredEvents.add(LHSerializable.fromProto(id, WorkflowEventIdModel.class, context));
        }
        for (WorkflowEventDefId id : request.getEventDefIdsList()) {
            eventsToWaitFor.add(LHSerializable.fromProto(id, WorkflowEventDefIdModel.class, context));
        }
    }

    public boolean maybeComplete(WorkflowEventModel event) {
        if (!eventsToWaitFor.isEmpty()
                && !eventsToWaitFor.contains(event.getId().getWorkflowEventDefId())) {
            return false;
        }

        if (ignoredEvents.contains(event.getId())) return false;

        if (alreadyCompleted) return false;

        alreadyCompleted = true;
        observer.onNext(event.toProto().build());
        observer.onCompleted();

        return true;
    }

    public boolean maybeExpire() {
        Duration timePassed = Duration.between(arrivalTime.toInstant(), Instant.now());
        if (timePassed.compareTo(LHConstants.MAX_INCOMING_REQUEST_IDLE_TIME) > 0) {
            observer.onError(
                    new LHApiException(Status.DEADLINE_EXCEEDED, "No matching WorkflowEvent thrown within deadline"));
            return true;
        }
        return false;
    }
}
