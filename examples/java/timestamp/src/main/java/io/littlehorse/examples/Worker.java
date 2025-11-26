package io.littlehorse.examples;

import io.littlehorse.sdk.common.LHLibUtil;
import io.littlehorse.sdk.worker.LHTaskMethod;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Worker {

    private static final Logger log = LoggerFactory.getLogger(Worker.class);

    @LHTaskMethod("get-current-date")
    public Date getDate() {
        return new Date();
    }

    @LHTaskMethod("publish-book")
    public Book publishBook(String name, Instant instant) {
        log.info("Publishing book {} at date {}", name, instant);
        Book publishedBook = new Book();
        publishedBook.name = name;
        publishedBook.publishDate = Date.from(instant);
        publishedBook.publishInstant = instant;
        publishedBook.publishTimestamp = LHLibUtil.fromInstant(instant);
        publishedBook.publishSqlTimestamp = new java.sql.Timestamp(instant.toEpochMilli());
        publishedBook.publishLocalDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        return publishedBook;
    }

    @LHTaskMethod("print-book-details")
    public String printBookDetails(Book publishedBook, Instant instant) {
        log.info(
                "{} was published in {}, this information is printed at {}",
                publishedBook.name,
                publishedBook.publishDate.toString(),
                instant.toString());
        return "";
    }
}
