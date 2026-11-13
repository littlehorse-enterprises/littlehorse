package e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.grpc.Status.Code;
import io.grpc.StatusRuntimeException;
import io.littlehorse.sdk.common.proto.BulkJobId;
import io.littlehorse.sdk.common.proto.GetBulkJobRequest;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.SearchTaskDefRequest;
import io.littlehorse.test.LHTest;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;
import java.util.OptionalDouble;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

@LHTest
@Isolated
public class GrpcServerMetricsTest {

    private static final String TIMER = "grpc_server_processing_duration_seconds";
    private static final String SERVICE_LABEL = "service=\"littlehorse.LittleHorse\"";
    private static final String METHOD_TYPE_LABEL = "methodType=\"UNARY\"";

    private LittleHorseBlockingStub client;

    @Test
    void shouldExposeGrpcServerLatencyHistograms() throws IOException, InterruptedException {
        String before = scrapeMetrics();

        client.searchTaskDef(SearchTaskDefRequest.getDefaultInstance());
        StatusRuntimeException notFound = assertThrows(
                StatusRuntimeException.class,
                () -> client.getBulkJob(GetBulkJobRequest.newBuilder()
                        .setId(BulkJobId.newBuilder().setId(UUID.randomUUID().toString()))
                        .build()));
        assertThat(notFound.getStatus().getCode()).isEqualTo(Code.NOT_FOUND);

        String after = scrapeMetrics();
        String[] searchLabels = {SERVICE_LABEL, METHOD_TYPE_LABEL, "method=\"SearchTaskDef\"", "statusCode=\"OK\""};
        String[] getLabels = {SERVICE_LABEL, METHOD_TYPE_LABEL, "method=\"GetBulkJob\"", "statusCode=\"NOT_FOUND\""};

        for (String boundary : List.of(
                "0.005", "0.01", "0.025", "0.05", "0.1", "0.25", "0.5", "1.0", "2.0", "5.0", "10.0", "30.0", "60.0")) {
            assertThat(sample(after, TIMER + "_bucket", with(searchLabels, "le=\"" + boundary + "\"")))
                    .as("bucket %s", boundary)
                    .isPresent();
        }

        assertThat(delta(before, after, TIMER + "_count", searchLabels)).isEqualTo(1.0);
        assertThat(delta(before, after, TIMER + "_bucket", with(searchLabels, "le=\"+Inf\"")))
                .isEqualTo(1.0);
        assertThat(delta(before, after, TIMER + "_count", getLabels)).isEqualTo(1.0);
        assertThat(sample(after, TIMER + "_sum", searchLabels)).isPresent();
        assertThat(sample(after, TIMER + "_sum", getLabels)).isPresent();
        assertThat(delta(
                        before,
                        after,
                        "grpc_server_requests_received_messages_total",
                        SERVICE_LABEL,
                        METHOD_TYPE_LABEL,
                        "method=\"SearchTaskDef\""))
                .isEqualTo(1.0);
        assertThat(delta(
                        before,
                        after,
                        "grpc_server_responses_sent_messages_total",
                        SERVICE_LABEL,
                        METHOD_TYPE_LABEL,
                        "method=\"SearchTaskDef\""))
                .isEqualTo(1.0);
    }

    private String scrapeMetrics() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:1822/metrics"))
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).isEqualTo(200);
        return response.body();
    }

    private static OptionalDouble sample(String scrape, String metric, String... labels) {
        return scrape.lines()
                .filter(line -> line.startsWith(metric + "{"))
                .filter(line -> Arrays.stream(labels).allMatch(line::contains))
                .mapToDouble(line -> Double.parseDouble(line.substring(line.lastIndexOf(' ') + 1)))
                .findFirst();
    }

    private static double delta(String before, String after, String metric, String... labels) {
        return sample(after, metric, labels).orElse(0.0)
                - sample(before, metric, labels).orElse(0.0);
    }

    private static String[] with(String[] labels, String label) {
        String[] result = Arrays.copyOf(labels, labels.length + 1);
        result[labels.length] = label;
        return result;
    }
}
