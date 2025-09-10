package io.littlehorse.examples;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import com.google.protobuf.Timestamp;

public class ProcessedText {

    public String text;
    public Double sentimentScore;
    public Boolean addLength;
    public Integer userId;
    public Date creationDate;
    public Instant creationInstant;
    public Timestamp creationTimestamp;
    public java.sql.Timestamp creationSqlTimestamp;
    public LocalDateTime creationLocalDateTime;

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
            creationDate.toString() +
            ", createdInstant=" +
            creationInstant.toString() +
            ", createdTimestamp=" +
            creationTimestamp.toString() +
            ", createdSqlTimestamp=" +
            creationSqlTimestamp.toString() +
            ", createdLocalDateTime=" +
            creationLocalDateTime.toString() +
            '}'
        );
    }
}
