package e2e;

// import static org.mockito.ArgumentMatchers.booleanThat;
import static org.assertj.core.api.Assertions.assertThat;

import io.littlehorse.sdk.common.proto.DeleteTaskDefRequest;
import io.littlehorse.sdk.common.proto.LittleHorseGrpc.LittleHorseBlockingStub;
import io.littlehorse.sdk.common.proto.PutTaskDefRequest;
import io.littlehorse.sdk.common.proto.SearchTaskDefRequest;
import io.littlehorse.sdk.common.proto.TaskDefId;
import io.littlehorse.sdk.common.proto.TaskDefIdList;
import io.littlehorse.test.LHTest;
import io.littlehorse.test.exception.LHTestExceptionUtil;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

@LHTest
public class MetadataSearchTest {

    private LittleHorseBlockingStub client;

    @Test
    void shouldPaginateTaskDefSearch() {
        // First create a bunch of taskDef's
        List<String> taskDefNames = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            String taskDefName = UUID.randomUUID().toString();
            taskDefNames.add(taskDefName);
            client.putTaskDef(
                    PutTaskDefRequest.newBuilder().setName(taskDefName).build());
        }

        // Wait until they are all findable. Note that metadata propagation is necessarily eventually-consistent
        Awaitility.await()
                .atMost(Duration.ofSeconds(2))
                .ignoreExceptionsMatching(exn -> LHTestExceptionUtil.isNotFoundException(exn))
                .until(() -> {
                    for (String tdn : taskDefNames) {
                        // Throws NOT_FOUND
                        client.getTaskDef(TaskDefId.newBuilder().setName(tdn).build());
                    }
                    return true;
                });

        // Now we need to search for all of them. First, let's
        List<String> returnedTaskDefNames = doPaginatedTaskDefSearch();
        assertThat(returnedTaskDefNames).containsAll(taskDefNames);

        // Cleanup
        for (String taskDefName : taskDefNames) {
            client.deleteTaskDef(DeleteTaskDefRequest.newBuilder()
                    .setId(TaskDefId.newBuilder().setName(taskDefName))
                    .build());
        }
    }

    List<String> doPaginatedTaskDefSearch() {
        // We know that there are *AT LEAST* 10 TaskDef's already out there, since we deployed 10 in this
        // test and there are probably others from other tests. So we will get all of them.

        List<String> out = new ArrayList<>();
        int limit = 5;
        SearchTaskDefRequest.Builder reqBuilder =
                SearchTaskDefRequest.newBuilder().setLimit(limit);

        do {
            TaskDefIdList response = client.searchTaskDef(reqBuilder.build());
            assertThat(response.getResultsCount() <= limit);

            for (TaskDefId id : response.getResultsList()) {
                String taskDefName = id.getName();
                assertThat(out).doesNotContain(taskDefName);
                out.add(taskDefName);
            }

            if (response.hasBookmark()) {
                reqBuilder.setBookmark(response.getBookmark());
            } else {
                reqBuilder.clearBookmark();
            }
        } while (reqBuilder.hasBookmark());

        return out;
    }
}
