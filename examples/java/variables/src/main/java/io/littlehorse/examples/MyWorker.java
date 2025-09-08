package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.LHType;
import java.time.Instant;
import java.util.Date;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.protobuf.Timestamp;


public class MyWorker {

    private static final Logger log = LoggerFactory.getLogger(MyWorker.class);

    @LHTaskMethod("sentiment-analysis")
    public Double greeting(@LHType(masked = true) String text) {
        log.debug("Executing task sentiment-analysis vars (%s)".formatted(text));
        return RandomUtils.nextDouble(0.0, 100.0);
    }

    @LHTaskMethod("process-text")
    @LHType(masked = true)
    public ProcessedText result(@LHType(masked = true) String text, Double sentimentScore,
            Boolean addLength, Integer userId) {
        log.debug("Executing task sentiment-analysis vars (%s, %s, %s, %s)".formatted(text,
                sentimentScore, addLength, userId));
        ProcessedText processedText = new ProcessedText();
        processedText.text = text;
        processedText.addLength = addLength;
        processedText.userId = userId;
        processedText.sentimentScore = sentimentScore;
        return processedText;
    }

    @LHTaskMethod("send")
    public String result(@LHType(masked = true) ProcessedText processedText) {
        log.debug("Executing task sentiment-analysis vars (%s)".formatted(processedText));
        return "";
    }

    @LHTaskMethod("expr-add-one")
    public int addOne(int input) {
        return input + 1;
    }

    @LHTaskMethod("get-current-date")
    public Date getDate() {
        return new Date();
    }
    @LHTaskMethod("add-15-minutes")
    public Date addFifteenMinutes(Date input) {
        return new Date(input.getTime() + 15 * 60 * 1000);
    }

    @LHTaskMethod("print-proto-timestamp")
    public void printTimestamp(Timestamp input) {
        log.info("Timestamp: {}", input);
    }
    @LHTaskMethod("print-date")
    public void printDate(Date input) {
        log.info("Date: {}", input);
    }
    @LHTaskMethod("print-instant")
    public void printInstant(Instant input) {
        log.info("instant: {}", input);
    }
}
