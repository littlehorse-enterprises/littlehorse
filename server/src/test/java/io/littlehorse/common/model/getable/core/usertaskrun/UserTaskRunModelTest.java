package io.littlehorse.common.model.getable.core.usertaskrun;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.littlehorse.common.model.AbstractGetable;
import io.littlehorse.common.model.corecommand.CommandModel;
import io.littlehorse.common.model.getable.core.usertaskrun.usertaskevent.UserTaskEventModel;
import io.littlehorse.sdk.common.proto.UserTaskEvent.EventCase;
import io.littlehorse.server.streams.storeinternals.GetableIndex;
import io.littlehorse.server.streams.topology.core.ProcessorExecutionContext;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class UserTaskRunModelTest {

    @Test
    void shouldHave15Indexes() {
        UserTaskRunModel utr = new UserTaskRunModel();
        Assertions.assertThat(utr.getIndexConfigurations()).size().isEqualTo(15);
    }

    @Test
    void indexesShouldBeOrdered() {
        UserTaskRunModel utr = new UserTaskRunModel();
        List<String> tagOrders = List.of("status", "userTaskDefName", "userId", "userGroup");

        Predicate<GetableIndex<? extends AbstractGetable<?>>> isProperlySorted = index -> {
            List<String> attributes = index.getAttributes().stream()
                    .map(attrib -> attrib.getLeft())
                    .toList();

            for (String attribute : attributes) {
                Assertions.assertThat(tagOrders).contains(attribute);
            }

            for (int i = 0; i < attributes.size() - 1; i++) {
                String first = attributes.get(i);
                String second = attributes.get(i + 1);

                if (tagOrders.indexOf(first) > tagOrders.indexOf(second)) {
                    return false;
                }
            }

            return true;
        };

        List<GetableIndex<? extends AbstractGetable<?>>> indices = utr.getIndexConfigurations();
        for (GetableIndex<? extends AbstractGetable<?>> index : indices) {
            Assertions.assertThat(isProperlySorted.test(index)).isTrue();
        }
    }

    @Test
    void indexesShouldBeUnique() {
        UserTaskRunModel utr = new UserTaskRunModel();
        Set<String> deduplicatedIndexes = utr.getIndexConfigurations().stream()
                .map(index -> {
                    return index.getAttributes().stream()
                            .map(attrib -> attrib.getLeft())
                            .toList();
                })
                .map(attribList -> {
                    return String.join("_", attribList);
                })
                .collect(Collectors.toSet());

        Assertions.assertThat(deduplicatedIndexes.size()).isEqualTo(15);
    }

    @Test
    void commentMethodShouldProperlyAddCommentEvent() {
        // Mock the context and command
        ProcessorExecutionContext mockContext = mock(ProcessorExecutionContext.class);
        CommandModel mockCommand = mock(CommandModel.class);
        when(mockCommand.getTime()).thenReturn(new java.util.Date());
        when(mockContext.currentCommand()).thenReturn(mockCommand);

        UserTaskRunModel utr = new UserTaskRunModel();
        utr.setProcessorContext(mockContext);

        UserTaskEventModel addEvent = utr.comment("user1", "hello world");
        Integer commentId = addEvent.getCommented().getUserCommentId();

        Assertions.assertThat(utr.getLastEventForComment().get(commentId)).isEqualTo(addEvent);
        Assertions.assertThat(addEvent.getType()).isEqualByComparingTo(EventCase.COMMENT_ADDED);
        Assertions.assertThat(addEvent.getCommented().getComment()).isEqualTo("hello world");
        Assertions.assertThat(addEvent.getCommented().getUserCommentId()).isEqualTo(1);
        Assertions.assertThat(utr.getEvents().get(0)).isEqualTo(addEvent);
    }

    @Test
    void editCommentMethodShouldProperlyAddCommentEventAndReplaceInLastEventForComment() {
        // Mock the context and command
        ProcessorExecutionContext mockContext = mock(ProcessorExecutionContext.class);
        CommandModel mockCommand = mock(CommandModel.class);
        when(mockCommand.getTime()).thenReturn(new java.util.Date());
        when(mockContext.currentCommand()).thenReturn(mockCommand);

        UserTaskRunModel utr = new UserTaskRunModel();
        utr.setProcessorContext(mockContext);

        UserTaskEventModel addEvent = utr.comment("user1", "hello world");
        Integer commentId = addEvent.getCommented().getUserCommentId();

        UserTaskEventModel editEvent = utr.editComment("user1", "new comment", commentId);

        Assertions.assertThat(editEvent.getType()).isEqualByComparingTo(EventCase.COMMENT_EDITED);
        Assertions.assertThat(utr.getEvents().get(1)).isEqualTo(editEvent);
        Assertions.assertThat(utr.getLastEventForComment().get(commentId)).isEqualTo(editEvent);
        Assertions.assertThat(utr.getLastEventForComment().size()).isEqualTo(1);
    }

    @Test
    void deleteCommentMethodShouldProperlyAddToEventsAndLastEventForComment() {
        // Mock the context and command
        ProcessorExecutionContext mockContext = mock(ProcessorExecutionContext.class);
        CommandModel mockCommand = mock(CommandModel.class);
        when(mockCommand.getTime()).thenReturn(new java.util.Date());
        when(mockContext.currentCommand()).thenReturn(mockCommand);

        UserTaskRunModel utr = new UserTaskRunModel();
        utr.setProcessorContext(mockContext);

        UserTaskEventModel addEvent = utr.comment("user1", "hello world");
        Integer commentId = addEvent.getCommented().getUserCommentId();

        utr.deleteComment(commentId);

        Assertions.assertThat(utr.getEvents().get(1).getType()).isEqualTo(EventCase.COMMENT_DELETED);
        Assertions.assertThat(utr.getEvents().get(1).getCommentDeleted().getUserCommentId())
                .isEqualTo(commentId);
        Assertions.assertThat(utr.getLastEventForComment().get(commentId).getType())
                .isEqualByComparingTo(EventCase.COMMENT_DELETED);
        Assertions.assertThat(utr.getLastEventForComment().size()).isEqualTo(1);
    }

    @Test
    void getLastEventForCommentShouldProperlyMaintainKeyAndValueStore() {
        // Mock the context and command
        ProcessorExecutionContext mockContext = mock(ProcessorExecutionContext.class);
        CommandModel mockCommand = mock(CommandModel.class);
        when(mockCommand.getTime()).thenReturn(new java.util.Date());
        when(mockContext.currentCommand()).thenReturn(mockCommand);

        UserTaskRunModel utr = new UserTaskRunModel();
        utr.setProcessorContext(mockContext);

        UserTaskEventModel addEvent = utr.comment("user1", "hello world");
        Integer commentId = addEvent.getCommented().getUserCommentId();

        utr.deleteComment(commentId);

        UserTaskEventModel addEventTwo = utr.comment("user2", "hello world");
        int commentIdTwo = addEventTwo.getCommented().getUserCommentId();

        utr.deleteComment(commentIdTwo);

        UserTaskEventModel addEventThree = utr.comment("user3", "hello world");
        int commentIdThree = addEventThree.getCommented().getUserCommentId();
        UserTaskEventModel addEventFour = utr.editComment("user2", "comment", commentIdThree);
        UserTaskEventModel addEventFive = utr.comment("user", "comment");

        Assertions.assertThat(utr.getLastEventForComment().size()).isEqualTo(4);
        Assertions.assertThat(utr.getLastEventForComment().get(commentIdTwo).getType())
                .isEqualTo(EventCase.COMMENT_DELETED);
        Assertions.assertThat(utr.getLastEventForComment().get(commentId).getType())
                .isEqualTo(EventCase.COMMENT_DELETED);
        Assertions.assertThat(utr.getLastEventForComment().get(commentIdThree).getType())
                .isEqualTo(EventCase.COMMENT_EDITED);
    }
}
