package io.littlehorse.examples;

public class ProcessedText {

    public String text;
    public Double sentimentScore;
    public Boolean addLength;
    public Integer userId;

    @Override
    public String toString() {
        return ("ProcessedText{" + "text='"
                + text
                + '\''
                + ", sentimentScore="
                + sentimentScore
                + ", addLength="
                + addLength
                + ", userId="
                + userId
                + '}');
    }
}
