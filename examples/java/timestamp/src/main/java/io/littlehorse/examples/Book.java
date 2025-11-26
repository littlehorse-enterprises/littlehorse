package io.littlehorse.examples;

import com.google.protobuf.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;

public class Book {

    public String name;
    // These are different representations of timestamp that Littlehorse supports
    public Date publishDate;
    public Instant publishInstant;
    public Timestamp publishTimestamp;
    public java.sql.Timestamp publishSqlTimestamp;
    public LocalDateTime publishLocalDateTime;

    @Override
    public String toString() {
        return ("Book{" + "name='"
                + name
                + '\''
                + ", publishAt="
                + publishDate.toString()
                + ", publishInstant="
                + publishInstant.toString()
                + ", publishTimestamp="
                + publishTimestamp.toString()
                + ", publishSqlTimestamp="
                + publishSqlTimestamp.toString()
                + ", publishLocalDateTime="
                + publishLocalDateTime.toString()
                + '}');
    }
}
