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

/**
 * A Netty channel handler that filters and processes incoming HTTP GET requests.
 * It only supports GET requests, another method immediately responds METHOD_NOT_ALLOWED status code.
 */
public class FilterHandler extends SimpleChannelInboundHandler<HttpRequest> {

    private final StringBuilder responseData = new StringBuilder();
    private final AtomicReference<HttpRequest> requestReference = new AtomicReference<>();
    private final AtomicReference<String> requestPath = new AtomicReference<>();

    public FilterHandler() {}

    /**
     * Handles incoming HTTP requests by filtering them. Only GET method is supported.
     */
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

    /**
     * Writes a Method Not Allowed (405) response when a non-GET HTTP method is received.
     *
     * @param ctx The channel handler context
     */
    private void writeUnsupportedMethod(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, HttpResponseStatus.METHOD_NOT_ALLOWED, Unpooled.EMPTY_BUFFER);
        ctx.writeAndFlush(response);
    }

    /**
     * Writes a 100 Continue for CONTINUE requests.
     *
     * @param ctx The channel handler context
     */
    private void writeContinueResponse(ChannelHandlerContext ctx) {
        FullHttpResponse response =
                new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE, Unpooled.EMPTY_BUFFER);
        ctx.writeAndFlush(response);
    }

    /**
     * Handles any exceptions that occur during request processing by closing the channel.
     *
     * @param ctx   The channel handler context
     * @param cause The exception that was thrown
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
    }
}
