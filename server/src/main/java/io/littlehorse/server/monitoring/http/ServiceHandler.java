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

public class ServiceHandler extends SimpleChannelInboundHandler<HttpObject> {
    private final StatusServer.HttpEndpointRegistry registry;

    public ServiceHandler(StatusServer.HttpEndpointRegistry registry) {
        this.registry = registry;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject request) throws Exception {
        if (request instanceof HttpRequest httpRequest) {
            if (!registry.isPresent(httpRequest.uri())) {
                writeNotFoundResponse(ctx);
            } else {
                SimpleResponse response = registry.getResponse(httpRequest.uri());
                writeResponse(ctx, response);
            }
        } else if (request instanceof LastHttpContent trailer) {
            ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }

    private void writeResponse(ChannelHandlerContext ctx, SimpleResponse response) throws Exception {
        ByteBuf bytes = Unpooled.copiedBuffer(response.content(), StandardCharsets.UTF_8);
        FullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, bytes);
        httpResponse.headers().set("Content-type", response.contentType());
        ctx.writeAndFlush(httpResponse).addListener(ChannelFutureListener.CLOSE);
    }

    private void writeNotFoundResponse(ChannelHandlerContext ctx) {
        FullHttpResponse response =
                new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND, Unpooled.EMPTY_BUFFER);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}
