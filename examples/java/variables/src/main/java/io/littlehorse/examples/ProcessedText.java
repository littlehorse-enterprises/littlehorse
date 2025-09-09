package io.littlehorse.examples;

import java.util.Date;

public class ProcessedText {

    public String text;
    public Double sentimentScore;
    public Boolean addLength;
    public Integer userId;
    public Date createdAt;

    @Override
    public String toString() {
        return (
            "ProcessedText{" +
            "text='" +
            text +
            '\'' +
            ", sentimentScore=" +
            sentimentScore +
            ", addLength=" +
            addLength +
            ", userId=" +
            userId +
            ", createdAt=" +
            createdAt.toString() +
            '}'
        );
    }
}
