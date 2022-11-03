package io.littlehorse.server;

import com.google.protobuf.MessageOrBuilder;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.littlehorse.common.LHConfig;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.exceptions.LHConnectionError;
import io.littlehorse.common.exceptions.LHSerdeError;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.GETable;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.POSTable;
import io.littlehorse.common.model.event.ExternalEvent;
import io.littlehorse.common.model.event.WfRunEvent;
import io.littlehorse.common.model.event.WfRunRequest;
import io.littlehorse.common.model.meta.ExternalEventDef;
import io.littlehorse.common.model.meta.TaskDef;
import io.littlehorse.common.model.meta.ThreadSpec;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.model.server.LHResponse;
import io.littlehorse.common.model.server.RangeResponse;
import io.littlehorse.common.model.server.Tag;
import io.littlehorse.common.model.wfrun.NodeRun;
import io.littlehorse.common.model.wfrun.Variable;
import io.littlehorse.common.model.wfrun.WfRun;
import io.littlehorse.common.proto.GETableClassEnumPb;
import io.littlehorse.common.proto.LHResponseCodePb;
import io.littlehorse.common.proto.WfRunEventPb.EventCase;
import io.littlehorse.common.util.LHGlobalMetaStores;
import io.littlehorse.common.util.LHKStreamsListener;
import io.littlehorse.common.util.LHProducer;
import io.littlehorse.common.util.LHUtil;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.KafkaException;

public class LHApi {

    private Javalin app;
    private LHConfig config;
    private ApiStreamsContext streams;
    private LHGlobalMetaStores globalStores;
    private LHProducer producer;

    private interface HandlerFunc {
        public void handle(Context ctx) throws Exception;
    }

    private void handle(HandlerFunc func, Context ctx) {
        try {
            func.handle(ctx);
        } catch (Exception exn) {
            exn.printStackTrace();
            throw new RuntimeException(exn);
        }
    }

    public static List<String> GETables = Arrays.asList(
        WfSpec.class.getSimpleName(),
        TaskDef.class.getSimpleName()
    );

    public LHApi(
        LHConfig config,
        ApiStreamsContext streams,
        LHKStreamsListener listener
    ) {
        this.producer = config.getProducer();
        this.streams = streams;
        this.config = config;
        this.app = config.createAppWithHealth(listener);
        this.globalStores = new LHGlobalMetaStores(streams);

        this.app.get("/WfSpec/{id}", ctx -> handle(this::getWfSpec, ctx));
        this.app.get("/TaskDef/{id}", ctx -> handle(this::getTaskDef, ctx));
        this.app.get(
                "/ExternalEventDef/{id}",
                ctx -> handle(this::getExternalEventDef, ctx)
            );

        this.app.get("/WfRun/{id}", ctx -> handle(this::getWfRun, ctx));
        this.app.get(
                "/NodeRun/{wfRunId}/{threadRunNumber}/{nodeRunPosition}",
                ctx -> handle(this::getNodeRun, ctx)
            );
        this.app.get(
                "/search/NodeRun/wfRunId/{wfRunId}",
                ctx -> handle(this::getNodeRunsByWfRun, ctx)
            );

        this.app.get(
                "/Variable/{wfRunId}/{threadRunNumber}/{name}",
                ctx -> handle(this::getVariable, ctx)
            );
        this.app.get(
                "search/Variable/wfRunId/{wfRunId}",
                ctx -> handle(this::getVariablesByWfRun, ctx)
            );
        this.app.get(
                "/ExternalEvent/{wfRunId}/{externalEventDefName}/{guid}",
                ctx -> handle(this::getExternalEvent, ctx)
            );

        this.app.get(
                "/search/WfRun/wfSpecId/{id}",
                ctx -> handle(this::getRunBySpecId, ctx)
            );

        this.app.get(
                "/search/WfRun/wfSpecName/{name}",
                ctx -> handle(this::getRunBySpecName, ctx)
            );
        this.app.get(
                "/search/NodeRun/taskDefId/{taskDefId}/status/{status}",
                ctx -> handle(this::getNodeRunByStatus, ctx)
            );

        this.app.post(
                "/WfSpec",
                ctx -> {
                    handle(
                        c -> {
                            this.post(c, WfSpec.class);
                        },
                        ctx
                    );
                }
            );
        this.app.post(
                "/TaskDef",
                ctx -> {
                    handle(
                        c -> {
                            this.post(c, TaskDef.class);
                        },
                        ctx
                    );
                }
            );
        this.app.post(
                "/ExternalEventDef",
                ctx -> {
                    handle(
                        c -> {
                            this.post(c, ExternalEventDef.class);
                        },
                        ctx
                    );
                }
            );
        this.app.post("/ExternalEvent", ctx -> handle(this::postExternalEvent, ctx));
        this.app.post("/WfRun", ctx -> handle(this::postWfRun, ctx));
        this.app.post("/WfRunMany", ctx -> handle(this::postManyWfRuns, ctx));

        this.app.get(
                "/search/WfSpec/name/{name}",
                ctx -> handle(this::getWfSpecByName, ctx)
            );

        this.app.get(
                "/internal/waitForResponse/{requestId}/{className}",
                ctx -> handle(this::internalWaitForResponse, ctx)
            );
        this.app.get(
                "/internal/storeBytes/{storeName}/{partition}/{storeKey}/{activeHost}",
                ctx -> handle(this::internalGetBytes, ctx)
            );
        this.app.get(
                "/internal/localKeyedPrefixIdxScan/{storeKeyPrefix}",
                this::internalLocalKeyedPrefixIdxScanBytes
            );
        this.app.get(
                "/internal/localKeyedPrefixObjScan/{storeName}/{storeKeyPrefix}",
                this::internalLocalKeyedPrefixObjScanBytes
            );
        this.app.get("/metrics/nodeRuns", this::countNodeRuns);
        this.app.get("/metrics/wfRuns", this::countWfRuns);
        this.app.get("/metrics/indexStore", this::countIndexEntries);
        this.app.get("/internal/countLocal/{storeName}", this::internalCountStore);
    }

    public void start() {
        app.start(config.getExposedPort());
    }

    public <U extends MessageOrBuilder, T extends POSTable<U>> void post(
        Context ctx,
        Class<T> cls
    ) throws LHSerdeError { // should never throw it though
        LHResponse resp = new LHResponse(config);
        boolean asProto = ctx
            .queryParamAsClass("asProto", Boolean.class)
            .getOrDefault(false);
        try {
            T t;
            if (asProto) {
                t = LHSerializable.fromBytes(ctx.bodyAsBytes(), cls, config);
            } else {
                t = LHSerializable.fromJson(ctx.body(), cls, config);
            }
            byte[] rawResponse = streams.post(t, cls);
            resp = LHSerializable.fromBytes(rawResponse, LHResponse.class, config);
        } catch (LHSerdeError exn) {
            resp.code = LHResponseCodePb.VALIDATION_ERROR;
            resp.message = "Couldn't deserialize resource: " + exn.getMessage();
            ctx.status(400);
        } catch (LHConnectionError exn) {
            resp.code = LHResponseCodePb.CONNECTION_ERROR;
            resp.message = "Error: " + exn.getMessage();
            ctx.status(500);
        }

        ctx.status(resp.getStatus());
        ctx.json(resp);
    }

    public void getNodeRun(Context ctx) {
        String wfRunId = ctx.pathParam("wfRunId");
        int threadRunNumber = ctx
            .pathParamAsClass("threadRunNumber", Integer.class)
            .get();
        int nodeRunPosition = ctx
            .pathParamAsClass("nodeRunPosition", Integer.class)
            .get();

        String storeKey = NodeRun.getStoreKey(
            wfRunId,
            threadRunNumber,
            nodeRunPosition
        );

        returnLookup(wfRunId, storeKey, NodeRun.class, ctx);
    }

    public void getWfSpec(Context ctx) {
        String id = ctx.pathParam("id");
        returnLookup(id, id, WfSpec.class, ctx);
    }

    public void getExternalEvent(Context ctx) {
        String wfRunId = ctx.pathParam("wfRunId");
        String eedId = ctx.pathParam("externalEventDefName");
        String guid = ctx.pathParam("guid");

        String partitionKey = wfRunId;
        String objectId = ExternalEvent.getObjectId(wfRunId, eedId, guid);
        returnLookup(partitionKey, objectId, ExternalEvent.class, ctx);
    }

    public void getWfRun(Context ctx) {
        String id = ctx.pathParam("id");
        returnLookup(id, id, WfRun.class, ctx);
    }

    public void getVariable(Context ctx) {
        String wfRunId = ctx.pathParam("wfRunId");
        int threadRunNumber = ctx
            .pathParamAsClass("threadRunNumber", Integer.class)
            .get();
        String name = ctx.pathParam("name");

        String objId = Variable.getObjectId(wfRunId, threadRunNumber, name);
        returnLookup(wfRunId, objId, Variable.class, ctx);
    }

    public void getVariablesByWfRun(Context ctx) {
        String wfRunId = ctx.pathParam("wfRunId");
        keyedPrefixObjScan(wfRunId, Variable.class, wfRunId, ctx);
    }

    public void getExternalEventDef(Context ctx) {
        String id = ctx.pathParam("id");
        returnLookup(id, id, ExternalEventDef.class, ctx);
    }

    public void getTaskDef(Context ctx) {
        String id = ctx.pathParam("id");
        returnLookup(id, id, TaskDef.class, ctx);
    }

    public void postWfRun(Context ctx) {
        boolean async = ctx
            .queryParamAsClass("async", Boolean.class)
            .getOrDefault(false);
        boolean asProto = ctx
            .queryParamAsClass("asProto", Boolean.class)
            .getOrDefault(false);

        LHResponse resp = new LHResponse(config);
        resp.code = LHResponseCodePb.OK;
        try {
            WfRunRequest req;
            if (asProto) {
                req =
                    LHSerializable.fromBytes(
                        ctx.bodyAsBytes(),
                        WfRunRequest.class,
                        config
                    );
            } else {
                req = LHSerializable.fromJson(ctx.body(), WfRunRequest.class, config);
            }

            if (req.wfRunId == null) {
                req.wfRunId = LHUtil.generateGuid();
            }

            WfSpec spec = globalStores.getWfSpec(req.wfSpecId);
            if (spec == null) {
                resp.code = LHResponseCodePb.NOT_FOUND_ERROR;
                resp.message = "Could not find specified WfSpec.";
            } else {
                // Validate that all of the variables are present.
                // NOTE: the validation here needs to happen every time we start a thread, not just
                // when we start a workflow. This is just a special case of starting the entrypoint
                // thread. Therefore, this logic will also be executed in the scheduler. That means
                // this is just a "pre-emptive check" to make sure that it doesn't blow up later on.

                ThreadSpec entrypoint = spec.threadSpecs.get(
                    spec.entrypointThreadName
                );
                try {
                    entrypoint.validateStartVariables(req.variables);
                    WfRunEvent event = new WfRunEvent();
                    event.type = EventCase.RUN_REQUEST;
                    event.runRequest = req;
                    event.time = new Date();
                    event.wfRunId = req.wfRunId;
                    event.wfSpecId = spec.getObjectId();
                    req.wfSpecId = spec.getObjectId();
                    resp.id = req.wfRunId;
                    Future<RecordMetadata> future = producer.send(
                        req.wfRunId,
                        event,
                        LHConstants.WF_RUN_EVENT_TOPIC
                    );
                    if (!async) future.get();
                } catch (LHValidationError exn) {
                    resp.code = LHResponseCodePb.VALIDATION_ERROR;
                    resp.message = exn.getMessage();
                    resp.id = null;
                }
            }
        } catch (LHSerdeError exn) {
            resp.code = LHResponseCodePb.BAD_REQUEST_ERROR;
            resp.message = "Failed to unmarshal input: " + exn.getMessage();
        } catch (InterruptedException | ExecutionException | KafkaException exn) {
            resp.code = LHResponseCodePb.CONNECTION_ERROR;
            resp.message = "Problem sending Kafka Record: " + exn.getMessage();
        }
        ctx.status(resp.getStatus());
        if (asProto) {
            ctx.result(resp.toBytes(config));
        } else {
            ctx.json(resp);
        }
    }

    public void postManyWfRuns(Context ctx) throws Exception {
        int howMany = ctx
            .queryParamAsClass("howMany", Integer.class)
            .getOrDefault(1000);
        LHResponse resp = new LHResponse(config);
        resp.code = LHResponseCodePb.OK;
        boolean asProto = ctx
            .queryParamAsClass("asProto", Boolean.class)
            .getOrDefault(false);

        WfRunRequest req;
        if (asProto) {
            req =
                LHSerializable.fromBytes(
                    ctx.bodyAsBytes(),
                    WfRunRequest.class,
                    config
                );
        } else {
            req = LHSerializable.fromJson(ctx.body(), WfRunRequest.class, config);
        }

        String guid = req.wfRunId == null ? LHUtil.generateGuid() : req.wfRunId;

        WfSpec spec = globalStores.getWfSpec(req.wfSpecId);

        if (spec == null) {
            resp.code = LHResponseCodePb.NOT_FOUND_ERROR;
            resp.message = "Could not find specified WfSpec.";
        } else {
            for (int i = 0; i < howMany; i++) {
                req.wfRunId = guid + "-" + i;
                WfRunEvent event = new WfRunEvent();
                event.type = EventCase.RUN_REQUEST;
                event.runRequest = req;
                event.time = new Date();
                event.wfSpecId = spec.getObjectId();
                req.wfSpecId = spec.getObjectId();
                event.wfRunId = req.wfRunId;
                resp.id = event.wfRunId;
                producer.send(event.wfRunId, event, LHConstants.WF_RUN_EVENT_TOPIC);
            }
        }
        ctx.status(resp.getStatus());
        if (asProto) {
            ctx.result(resp.toBytes(config));
        } else {
            ctx.json(resp);
        }
    }

    public void getWfSpecByName(Context ctx) {
        String name = ctx.pathParam("name");
        keyedPrefixIdxScan(Arrays.asList(Pair.of("name", name)), WfSpec.class, ctx);
    }

    public void getRunBySpecId(Context ctx) {
        String id = ctx.pathParam("id");
        keyedPrefixIdxScan(Arrays.asList(Pair.of("wfSpecId", id)), WfRun.class, ctx);
    }

    public void getNodeRunsByWfRun(Context ctx) {
        String wfRunId = ctx.pathParam("wfRunId");
        keyedPrefixObjScan(wfRunId, NodeRun.class, wfRunId, ctx);
    }

    public void getRunBySpecName(Context ctx) {
        String name = ctx.pathParam("name");
        keyedPrefixIdxScan(
            Arrays.asList(Pair.of("wfSpecName", name)),
            WfRun.class,
            ctx
        );
    }

    public void getNodeRunByStatus(Context ctx) {
        String taskDefId = ctx.pathParam("taskDefId");
        String status = ctx.pathParam("status");
        keyedPrefixIdxScan(
            Arrays.asList(Pair.of("taskDefId", taskDefId), Pair.of("status", status)),
            NodeRun.class,
            ctx
        );
    }

    // This method returns the protobuf data in binary format, not json.
    @SuppressWarnings("unchecked")
    public void internalWaitForResponse(Context ctx) {
        String requestId = ctx.pathParam("requestId");
        String clsName = ctx.pathParam("className");
        Class<? extends POSTable<?>> cls;
        try {
            cls = (Class<? extends POSTable<?>>) Class.forName(clsName);
            ctx.result(streams.localWait(requestId, cls));
        } catch (ClassNotFoundException exn) {
            ctx.status(400);
        }
    }

    public void internalLocalKeyedPrefixIdxScanBytes(Context ctx) {
        String token = ctx
            .queryParamAsClass("token", String.class)
            .getOrDefault(null);
        String prefix = ctx.pathParam("storeKeyPrefix");
        int limit = ctx.queryParamAsClass("limit", Integer.class).getOrDefault(1000);

        RangeResponse resp = streams.internalLocalKeyedIdxPrefixScan(
            prefix,
            token,
            limit
        );
        ctx.result(resp.toBytes(config));
    }

    public void internalLocalKeyedPrefixObjScanBytes(Context ctx) {
        String token = ctx
            .queryParamAsClass("token", String.class)
            .getOrDefault(null);
        String storeName = ctx.pathParam("storeName");
        String prefix = ctx.pathParam("storeKeyPrefix");
        int limit = ctx.queryParamAsClass("limit", Integer.class).getOrDefault(1000);

        RangeResponse resp = streams.internalLocalKeyedObjPrefixScan(
            storeName,
            prefix,
            token,
            limit
        );
        ctx.result(resp.toBytes(config));
    }

    // This method returns the protobuf data in binary format, not json.
    public void internalGetBytes(Context ctx) {
        String storeName = ctx.pathParam("storeName");
        String storeKey = ctx.pathParam("storeKey");
        int partition = ctx.pathParamAsClass("partition", Integer.class).get();
        boolean activeHost = ctx.pathParamAsClass("activeHost", Boolean.class).get();
        ctx.result(
            streams
                .handleRemoteStoreQuery(storeName, partition, storeKey, activeHost)
                .toBytes(config)
        );
    }

    private <
        U extends MessageOrBuilder, T extends GETable<U>
    > void keyedPrefixIdxScan(
        List<Pair<String, String>> attributes,
        Class<T> cls,
        Context ctx
    ) {
        GETableClassEnumPb asEnum = GETable.getTypeEnum(cls);
        String prefixKey = Tag.getPartitionKey(attributes, asEnum);
        String token = ctx
            .queryParamAsClass("token", String.class)
            .getOrDefault(null);

        boolean asProto = ctx
            .queryParamAsClass("asProto", Boolean.class)
            .getOrDefault(false);

        int limit = ctx.queryParamAsClass("limit", Integer.class).getOrDefault(1000);

        LHResponse resp = new LHResponse(config);
        try {
            RangeResponse out = streams.keyedPrefixIdxScan(prefixKey, token, limit);
            resp.code = LHResponseCodePb.OK;
            resp.result = out;
        } catch (LHConnectionError exn) {
            resp.code = LHResponseCodePb.CONNECTION_ERROR;
            resp.message =
                "Failed looking up the " +
                cls.getSimpleName() +
                ": " +
                exn.getMessage();
            ctx.status(500);
        }

        if (asProto) {
            ctx.result(resp.toBytes(config));
        } else {
            ctx.json(resp);
        }
    }

    private <
        U extends MessageOrBuilder, T extends GETable<U>
    > void keyedPrefixObjScan(
        String partitionKey,
        Class<T> cls,
        String storeKeyPrefix,
        Context ctx
    ) {
        String token = ctx
            .queryParamAsClass("token", String.class)
            .getOrDefault(null);
        boolean asProto = ctx
            .queryParamAsClass("asProto", Boolean.class)
            .getOrDefault(false);
        int limit = ctx.queryParamAsClass("limit", Integer.class).getOrDefault(1000);

        LHResponse resp = new LHResponse(config);
        try {
            RangeResponse out = streams.keyedPrefixObjScan(
                GETable.getBaseStoreName(cls),
                partitionKey,
                storeKeyPrefix,
                token,
                limit
            );
            resp.code = LHResponseCodePb.OK;
            resp.result = out;
        } catch (LHConnectionError exn) {
            resp.code = LHResponseCodePb.CONNECTION_ERROR;
            resp.message =
                "Failed looking up the " +
                cls.getSimpleName() +
                ": " +
                exn.getMessage();
            ctx.status(500);
        }

        if (asProto) {
            ctx.result(resp.toBytes(config));
        } else {
            ctx.json(resp);
        }
    }

    private <U extends MessageOrBuilder, T extends GETable<U>> void returnLookup(
        String partitionKey,
        String storeKey,
        Class<T> cls,
        Context ctx
    ) {
        boolean asProto = ctx
            .queryParamAsClass("asProto", Boolean.class)
            .getOrDefault(false);

        LHResponse resp = new LHResponse(config);
        try {
            T out = streams.get(storeKey, partitionKey, cls);
            resp.result = out;

            if (out == null) {
                resp.code = LHResponseCodePb.NOT_FOUND_ERROR;
                resp.message = "Couldn't find described " + cls.getSimpleName();
                ctx.status(404);
            } else {
                resp.result = out;
                resp.code = LHResponseCodePb.OK;
                resp.id = out.getObjectId();
            }
        } catch (LHConnectionError exn) {
            resp.code = LHResponseCodePb.CONNECTION_ERROR;
            resp.message =
                "Failed looking up the " +
                cls.getSimpleName() +
                ": " +
                exn.getMessage();
            ctx.status(500);
        }
        if (asProto) {
            ctx.result(resp.toBytes(config));
        } else {
            ctx.json(resp);
        }
    }

    public void postExternalEvent(Context ctx) {
        boolean async = ctx
            .queryParamAsClass("async", Boolean.class)
            .getOrDefault(false);
        boolean asProto = ctx
            .queryParamAsClass("asProto", Boolean.class)
            .getOrDefault(false);

        LHResponse resp = new LHResponse(config);
        resp.code = LHResponseCodePb.OK;
        try {
            ExternalEvent req;

            if (asProto) {
                req =
                    LHSerializable.fromBytes(
                        ctx.bodyAsBytes(),
                        ExternalEvent.class,
                        config
                    );
            } else {
                req =
                    LHSerializable.fromJson(ctx.body(), ExternalEvent.class, config);
            }
            if (req.guid == null || req.guid.equals("")) {
                req.guid = LHUtil.generateGuid();
            }

            if (req.wfRunId == null) {
                resp.message = "Must provide wfRunId for ExternalEvent!";
                resp.code = LHResponseCodePb.VALIDATION_ERROR;
                ctx.status(resp.getStatus());
                ctx.json(resp);
                return;
            }

            ExternalEventDef eed = globalStores.getExternalEventDef(
                req.externalEventDefName
            );
            if (eed == null) {
                resp.code = LHResponseCodePb.NOT_FOUND_ERROR;
                resp.message = "Could not find specified WfSpec.";
                ctx.status(resp.getStatus());
                ctx.json(resp);
                return;
            } else {
                resp.id = ExternalEvent.getObjectId(req.wfRunId, eed.name, req.guid);

                // TODO: When we add schemas to ExternalEventDef, we will need to
                // do validation here.

                WfRunEvent evt = new WfRunEvent();
                evt.wfRunId = req.wfRunId;
                evt.time = new Date();
                evt.externalEvent = req;
                evt.type = EventCase.EXTERNAL_EVENT;

                Future<RecordMetadata> future = producer.send(
                    evt.wfRunId,
                    evt,
                    LHConstants.WF_RUN_EVENT_TOPIC
                );
                if (!async) {
                    future.get();
                }
            }
        } catch (LHSerdeError exn) {
            resp.code = LHResponseCodePb.BAD_REQUEST_ERROR;
            resp.message = "Failed to unmarshal input: " + exn.getMessage();
        } catch (InterruptedException | ExecutionException | KafkaException exn) {
            resp.code = LHResponseCodePb.CONNECTION_ERROR;
            resp.message = "Problem sending Kafka Record: " + exn.getMessage();
        }
        ctx.status(resp.getStatus());
        ctx.json(resp);
    }

    public void countNodeRuns(Context ctx) throws LHConnectionError {
        ctx.result(streams.count(GETable.getBaseStoreName(NodeRun.class)).toString());
    }

    public void countWfRuns(Context ctx) throws LHConnectionError {
        ctx.result(streams.count(GETable.getBaseStoreName(WfRun.class)).toString());
    }

    public void countIndexEntries(Context ctx) throws LHConnectionError {
        ctx.result(streams.count(LHConstants.INDEX_STORE_NAME).toString());
    }

    public void internalCountStore(Context ctx) {
        ctx.result(streams.countLocal(ctx.pathParam("storeName")).toString());
    }
}
