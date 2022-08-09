package io.littlehorse.common;

import org.apache.kafka.streams.state.HostInfo;
import io.littlehorse.common.exceptions.LHConnectionError;
import io.littlehorse.common.exceptions.LHSerdeError;
import io.littlehorse.common.model.LHSerializable;
import io.littlehorse.common.model.meta.TaskDef;
import io.littlehorse.common.model.meta.WfSpec;
import io.littlehorse.common.proto.server.LHResponseCodePb;
import io.littlehorse.common.util.LHApiClient;
import io.littlehorse.server.model.internal.LHResponse;
import io.littlehorse.server.model.internal.RangeResponse;

public class LHDatabaseClient {
    private LHApiClient client;
    private HostInfo apiHost;

    public LHDatabaseClient(LHConfig config) {
        this.client = config.getApiClient();
        this.apiHost = config.getApiHostInfo();
    }

    public WfSpec getWfSpec(String idOrName) throws LHConnectionError{
        try {
            LHResponse response = LHSerializable.fromBytes(
                client.getResponse(apiHost, "/WfSpec/" + idOrName),
                LHResponse.class
            );

            // Then it's either a `name` (not `id`) OR it doesn't exist.
            if (response.code == LHResponseCodePb.NOT_FOUND_ERROR) {
                LHResponse lookupResponse = LHSerializable.fromBytes(
                    client.getResponse(apiHost, "/search/WfSpec/name/" + idOrName),
                    LHResponse.class
                );
                RangeResponse entries = (RangeResponse) lookupResponse.result;

                // if empty then doesn't exist
                if (entries.ids.isEmpty()) return null;
                String theId = entries.ids.get(0);
                response = LHSerializable.fromBytes(
                    client.getResponse(apiHost, "/WfSpec/" + theId),
                    LHResponse.class
                );
            }

            return (WfSpec) response.result;
        } catch(LHSerdeError exn) {
            throw new RuntimeException(exn);
        }
    }

    public TaskDef getTaskDef(String idOrName) throws LHConnectionError {
        byte[] response = client.getResponseAsBytes(
            apiHost, "/TaskDef/" + idOrName
        );
        if (response == null) return null;

        LHResponse resp;
        try {
            resp = LHSerializable.fromBytes(
                response, LHResponse.class
            );
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
            throw new LHConnectionError(null, "Mysterious error: " + resp.toJson());
        }

    }
}
