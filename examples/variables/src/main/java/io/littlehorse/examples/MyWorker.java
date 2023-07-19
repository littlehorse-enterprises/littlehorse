package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHTaskMethod;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyWorker {

    private static final Logger log = LoggerFactory.getLogger(MyWorker.class);

    @LHTaskMethod("sentiment-analysis")
    public Double greeting(String text) {
        log.debug("Executing task sentiment-analysis vars (%s)".formatted(text));
        return RandomUtils.nextDouble(0.0, 100.0);
    }

    @LHTaskMethod("process-text")
    public ProcessedText result(
        String text,
        Double sentimentScore,
        Boolean addLength,
        Integer userId
    ) {
        log.debug(
            "Executing task sentiment-analysis vars (%s, %s, %s, %s)".formatted(
                    text,
                    sentimentScore,
                    addLength,
                    userId
                )
        );
        ProcessedText processedText = new ProcessedText();
        processedText.text = text;
        processedText.addLength = addLength;
        processedText.userId = userId;
        processedText.sentimentScore = sentimentScore;
        return processedText;
    }

    @LHTaskMethod("send")
    public String result(ProcessedText processedText) {
        log.debug(
            "Executing task sentiment-analysis vars (%s)".formatted(processedText)
        );
        return "";
    }
}
