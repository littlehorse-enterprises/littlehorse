package io.littlehorse.examples;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.LHType;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
                                Boolean addLength, Integer userId, Instant instant) {
        log.debug("Executing task sentiment-analysis vars (%s, %s, %s, %s, %s)".formatted(text,
                sentimentScore, addLength, userId, instant));
        ProcessedText processedText = new ProcessedText();
        processedText.text = text;
        processedText.addLength = addLength;
        processedText.userId = userId;
        processedText.sentimentScore = sentimentScore;
        processedText.creationDate = Date.from(instant);
        processedText.creationInstant = instant;
        processedText.creationTimestamp = LHLibUtil.fromInstant(instant);
        processedText.creationSqlTimestamp = new java.sql.Timestamp(instant.toEpochMilli());
        processedText.creationLocalDateTime =
                LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        return processedText;
    }

    @LHTaskMethod("print-processed-text")
    public ProcessedText printProcessedText(ProcessedText processedText) {
        log.info("ProcessedText: {}", processedText);
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

    @LHTaskMethod("print-timestamps")
    public void printTimestamps(
            Instant instant,
            Date date,
            LocalDateTime localDateTime,
            java.sql.Timestamp sqlTimestamp,
            Timestamp timestamp
            ) {
        log.info("Instant: {}", instant);
        log.info("Date: {}", date);
        log.info("LocalDateTime: {}", localDateTime);
        log.info("Java SQL Timestamp: {}", sqlTimestamp);
        log.info("Protobuf Timestamp: {}", timestamp);
    }
}
