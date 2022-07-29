package io.littlehorse.common.util;

import java.io.IOException;
import org.apache.kafka.streams.state.HostInfo;
import io.littlehorse.common.exceptions.LHConnectionError;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LHApiClient {
    private OkHttpClient client;

    public LHApiClient() {
        client = new OkHttpClient();
    }

    public byte[] getResponse(HostInfo host, String path)
    throws LHConnectionError {
        String url = "http://" + host.host() + ":" + host.port() + path;
        Request req = new Request.Builder().url(url).build();

        try {
            Response resp = client.newCall(req).execute();
            byte[] body = resp.body().bytes();
            if (resp.code() < 300 && resp.code() >= 200) {
                return body;
            } else {
                throw new LHConnectionError(
                    null,
                    String.format(
                        "Got a %s response from API: %s", resp.code(),
                        new String(body)
                    )
                );
            }
        } catch(IOException exn) {
            // java.net.ConnectException is included in IOException.
            // java.net.SocketTimeoutException also included in IOException.
            throw new LHConnectionError(
                exn,
                String.format(
                    "Had %s error connectiong to %s: %s",
                    exn.getClass().getName(), url, exn.getMessage()
                )
            );
        }
    }

}
