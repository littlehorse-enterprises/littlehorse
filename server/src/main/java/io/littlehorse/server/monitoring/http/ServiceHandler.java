package io.littlehorse.server.monitoring.http;

import io.grpc.netty.shaded.io.netty.buffer.ByteBuf;
import io.grpc.netty.shaded.io.netty.buffer.Unpooled;
import io.grpc.netty.shaded.io.netty.channel.ChannelFutureListener;
import io.grpc.netty.shaded.io.netty.channel.ChannelHandlerContext;
import io.grpc.netty.shaded.io.netty.channel.SimpleChannelInboundHandler;
import io.grpc.netty.shaded.io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.grpc.netty.shaded.io.netty.handler.codec.http.FullHttpResponse;
import io.grpc.netty.shaded.io.netty.handler.codec.http.HttpObject;
import io.grpc.netty.shaded.io.netty.handler.codec.http.HttpRequest;
import io.grpc.netty.shaded.io.netty.handler.codec.http.HttpResponseStatus;
import io.grpc.netty.shaded.io.netty.handler.codec.http.HttpVersion;
import io.grpc.netty.shaded.io.netty.handler.codec.http.LastHttpContent;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;

/**
 * A Netty channel handler that processes HTTP requests by delegating to registered endpoints.
 * This handler supports request routing, error handling, and response generation based on
 * the registered endpoints in the StatusServer.
 */
@Slf4j
class ServiceHandler extends SimpleChannelInboundHandler<HttpObject> {
    private final StatusServer.HttpEndpointRegistry registry;

    ServiceHandler(StatusServer.HttpEndpointRegistry registry) {
        this.registry = registry;
    }

    /**
     * Handles incoming HTTP requests by routing them to appropriate endpoints or generating error responses.
     * @param ctx     The channel handler context
     * @param request The incoming HTTP request object
     * @throws Exception If an error occurs during request processing
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject request) throws Exception {
        if (request instanceof HttpRequest httpRequest) {
            if (!registry.isPresent(httpRequest.uri())) {
                writeNotFoundResponse(ctx);
            } else {
                try {
                    SimpleResponse response = registry.getResponse(httpRequest.uri());
                    writeResponse(ctx, response);
                } catch (Exception e) {
                    log.error("Error processing http request", e);
                    writeInternalErrorResponse(ctx);
                }
            }
        } else if (request instanceof LastHttpContent trailer) {
            ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }

    // Creates the http response from the handler content.
    private void writeResponse(ChannelHandlerContext ctx, SimpleResponse response) throws Exception {
        ByteBuf bytes = Unpooled.copiedBuffer(response.content(), StandardCharsets.UTF_8);
        FullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, bytes);
        httpResponse.headers().set("Content-type", response.contentType());
        ctx.writeAndFlush(httpResponse).addListener(ChannelFutureListener.CLOSE);
    }

    // Writes a 404 when the registry does not contain the request path
    private void writeNotFoundResponse(ChannelHandlerContext ctx) {
        FullHttpResponse response =
                new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND, Unpooled.EMPTY_BUFFER);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    // Any exception from the handler
    private void writeInternalErrorResponse(ChannelHandlerContext ctx) {
        ByteBuf responseBody = Unpooled.copiedBuffer("Internal server error", StandardCharsets.UTF_8);
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR, responseBody);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}
