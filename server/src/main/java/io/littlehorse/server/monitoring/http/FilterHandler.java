package io.littlehorse.server.monitoring.http;

import io.grpc.netty.shaded.io.netty.buffer.Unpooled;
import io.grpc.netty.shaded.io.netty.channel.ChannelHandlerContext;
import io.grpc.netty.shaded.io.netty.channel.SimpleChannelInboundHandler;
import io.grpc.netty.shaded.io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.grpc.netty.shaded.io.netty.handler.codec.http.FullHttpResponse;
import io.grpc.netty.shaded.io.netty.handler.codec.http.HttpMethod;
import io.grpc.netty.shaded.io.netty.handler.codec.http.HttpRequest;
import io.grpc.netty.shaded.io.netty.handler.codec.http.HttpResponseStatus;
import io.grpc.netty.shaded.io.netty.handler.codec.http.HttpUtil;
import io.grpc.netty.shaded.io.netty.handler.codec.http.HttpVersion;
import java.util.concurrent.atomic.AtomicReference;

public class FilterHandler extends SimpleChannelInboundHandler<HttpRequest> {

    private final StringBuilder responseData = new StringBuilder();
    private final AtomicReference<HttpRequest> requestReference = new AtomicReference<>();
    private final AtomicReference<String> requestPath = new AtomicReference<>();

    public FilterHandler() {}

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpRequest request) throws Exception {
        if (HttpUtil.is100ContinueExpected(request)) {
            writeContinueResponse(ctx);
        } else if (!request.method().equals(HttpMethod.GET)) {
            writeUnsupportedMethod(ctx);
        } else {
            ctx.fireChannelRead(request);
        }
    }

    private void writeUnsupportedMethod(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, HttpResponseStatus.METHOD_NOT_ALLOWED, Unpooled.EMPTY_BUFFER);
        ctx.writeAndFlush(response);
    }

    private void writeContinueResponse(ChannelHandlerContext ctx) {
        FullHttpResponse response =
                new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE, Unpooled.EMPTY_BUFFER);
        ctx.writeAndFlush(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
    }
}
