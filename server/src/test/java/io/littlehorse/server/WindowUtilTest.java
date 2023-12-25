package io.littlehorse.server;

import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.MetricsWindowLength;
import org.apache.commons.lang3.time.DateUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

@ExtendWith(MockitoExtension.class)
public class WindowUtilTest {

    @Test
    public void shouldGenerateWindowStartInTheSameBucket() {
        Date firstStatusChange = new Date(0);
        Date secondStatusChange = DateUtils.addMinutes(firstStatusChange, 24);
        Date firstWindowStart = LHUtil.getWindowStart(firstStatusChange, MetricsWindowLength.DAYS_1);
        Date secondWindowStart = LHUtil.getWindowStart(secondStatusChange, MetricsWindowLength.DAYS_1);
        Assertions.assertThat(firstWindowStart).isEqualTo(secondWindowStart);
    }

}
