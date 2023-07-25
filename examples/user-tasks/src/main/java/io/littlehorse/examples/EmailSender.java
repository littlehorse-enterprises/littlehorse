package io.littlehorse.examples;

import io.littlehorse.sdk.worker.LHTaskMethod;

public class EmailSender {

    @LHTaskMethod("send-email")
    public void sendEmail(String address, String content) {
        System.out.println("\n\nSending email to " + address);
        System.out.println("Content: " + content);
    }
}
