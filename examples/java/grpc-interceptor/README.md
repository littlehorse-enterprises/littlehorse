## Running GrpcInterceptorExample

This example shows how to configure a custom gRPC `ClientInterceptor` for the
LittleHorse Java client SDK.

`LHConfig` accepts a varargs list of `io.grpc.ClientInterceptor`. Every gRPC
channel created by the SDK (for both the blocking stub and the task worker's
stubs) is wrapped with the interceptors you provide:

```java
PrometheusMeterRegistry prometheusRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
MetricCollectingClientInterceptor interceptor = new MetricCollectingClientInterceptor(prometheusRegistry);

LHConfig config = new LHConfig(props, interceptor);
```

In this example we use Micrometer's `MetricCollectingClientInterceptor` to record
gRPC client metrics, and a Javalin web server exposes them over an HTTP endpoint
that Prometheus can scrape.

> **Note:** This example starts a Javalin web server on port `8080` and serves the
> metrics at `/metrics`. Make sure that port `8080` is free before running it.

Let's run the example in `GrpcInterceptorExample.java`:

```
./gradlew example-grpc-interceptor:run
```

In another terminal, use `lhctl` to run the workflow:

```
# Here, we specify that the "input-name" variable = "Obi-Wan"
lhctl run example-grpc-interceptor input-name Obi-Wan
```

After running the workflow, scrape the metrics endpoint to see the collected
gRPC client metrics:

```
curl http://localhost:8080/metrics
```

The output will include `grpc.client.*` metrics tagged by service, method,
method type, and status code.

