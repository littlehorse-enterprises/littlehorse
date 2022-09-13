package io.littlehorse.common.util;

import io.littlehorse.common.exceptions.LHConnectionError;
import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.kafka.streams.state.HostInfo;

public class LHRpcClient {

  private OkHttpClient client;

  public LHRpcClient() {
    client = new OkHttpClient();
  }

  public byte[] getResponseAsBytes(HostInfo host, String path)
    throws LHConnectionError {
    return getResponse(host, path + "?asProto=true");
  }

  public byte[] getResponse(HostInfo host, String path) throws LHConnectionError {
    String url = "http://" + host.host() + ":" + host.port() + path;
    Request req = new Request.Builder().url(url).build();

    try {
      Response resp = client.newCall(req).execute();
      return resp.body().bytes();
    } catch (IOException exn) {
      // java.net.ConnectException is included in IOException.
      // java.net.SocketTimeoutException also included in IOException.
      throw new LHConnectionError(
        exn,
        String.format(
          "Had %s error connectiong to %s: %s",
          exn.getClass().getName(),
          url,
          exn.getMessage()
        )
      );
    }
  }
}
