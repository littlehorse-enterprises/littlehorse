package io.littlehorse.common.util;

import io.littlehorse.common.LHConfig;
import io.littlehorse.common.exceptions.LHConnectionError;
import io.littlehorse.common.exceptions.LHSerdeError;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.meta.TaskDef;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.model.server.LHResponse;
import io.littlehorse.common.model.server.RangeResponse;
import io.littlehorse.common.proto.LHResponseCodePb;
import org.apache.kafka.streams.state.HostInfo;

public class LHApiClient {

    private LHRpcClient client;
    private HostInfo apiHost;
    private LHConfig config;

    public LHApiClient(LHConfig config) {
        this.config = config;
        this.apiHost = config.getApiHostInfo();
        this.client = config.getRpcClient();
    }

    public WfSpec getWfSpec(String idOrName) throws LHConnectionError {
        try {
            LHResponse response = LHSerializable.fromBytes(
                client.getResponseAsBytes(apiHost, "/WfSpec/" + idOrName),
                LHResponse.class,
                config
            );

            // Then it's either a `name` (not `id`) OR it doesn't exist.
            if (response.code == LHResponseCodePb.NOT_FOUND_ERROR) {
                LHResponse lookupResponse = LHSerializable.fromBytes(
                    client.getResponseAsBytes(
                        apiHost,
                        "/search/WfSpec/name/" + idOrName
                    ),
                    LHResponse.class,
                    config
                );
                RangeResponse entries = (RangeResponse) lookupResponse.result;

                // if empty then doesn't exist
                if (entries.ids.isEmpty()) return null;
                String theId = entries.ids.get(0);
                response =
                    LHSerializable.fromBytes(
                        client.getResponseAsBytes(apiHost, "/WfSpec/" + theId),
                        LHResponse.class,
                        config
                    );
            } else if (response.code != LHResponseCodePb.OK) {
                throw new LHConnectionError(
                    null,
                    "Failed to load the thing: " +
                    response.code +
                    " " +
                    response.message
                );
            }

            return (WfSpec) response.result;
        } catch (LHSerdeError exn) {
            LHUtil.log("Caught LHSerdeError, transforming to RuntimeException");
            throw new RuntimeException(exn);
        }
    }

    public TaskDef getTaskDef(String idOrName) throws LHConnectionError {
        byte[] response = client.getResponseAsBytes(apiHost, "/TaskDef/" + idOrName);
        if (response == null) return null;

        LHResponse resp;
        try {
            resp = LHSerializable.fromBytes(response, LHResponse.class, config);
        } catch (LHSerdeError exn) {
            throw new LHConnectionError(exn, "Got an unrecognizable response: ");
        }

        switch (resp.code) {
            case OK:
                return (TaskDef) resp.result;
            case CONNECTION_ERROR:
                throw new LHConnectionError(null, resp.message);
            case NOT_FOUND_ERROR:
                return null;
            case BAD_REQUEST_ERROR:
            case VALIDATION_ERROR:
            case UNRECOGNIZED:
            default:
                // This really shouldn't be possible.
                throw new LHConnectionError(
                    null,
                    "Mysterious error: " + resp.toJson()
                );
        }
    }
}
