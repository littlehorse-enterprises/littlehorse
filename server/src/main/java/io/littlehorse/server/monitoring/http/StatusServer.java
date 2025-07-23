package io.littlehorse.server.monitoring.http;

import io.grpc.netty.shaded.io.netty.bootstrap.ServerBootstrap;
import io.grpc.netty.shaded.io.netty.channel.ChannelInitializer;
import io.grpc.netty.shaded.io.netty.channel.ChannelOption;
import io.grpc.netty.shaded.io.netty.channel.ChannelPipeline;
import io.grpc.netty.shaded.io.netty.channel.EventLoopGroup;
import io.grpc.netty.shaded.io.netty.channel.nio.NioEventLoopGroup;
import io.grpc.netty.shaded.io.netty.channel.socket.nio.NioServerSocketChannel;
import io.grpc.netty.shaded.io.netty.channel.socket.nio.NioSocketChannel;
import io.grpc.netty.shaded.io.netty.handler.codec.http.HttpRequestDecoder;
import io.grpc.netty.shaded.io.netty.handler.codec.http.HttpResponseEncoder;
import java.io.Closeable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class StatusServer implements Closeable {

    private final HttpEndpointRegistry registry = new HttpEndpointRegistry();
    private final ServerBootstrap server = new ServerBootstrap();
    private final EventLoopGroup workerGroup = new NioEventLoopGroup(2);
    private final EventLoopGroup bossGroup = new NioEventLoopGroup(2);

    public void start(int port) {
        server.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new HttpRequestDecoder());
                        pipeline.addLast(new HttpResponseEncoder());
                        pipeline.addLast(new FilterHandler());
                        pipeline.addLast(new ServiceHandler(registry));
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, false);
        server.bind(port);
    }

    public void handle(String path, ContentType contentType, Supplier<String> functionResponse) {
        registry.handleGet(path, contentType.getType(), functionResponse);
    }

    @Override
    public void close() {}

    class HttpEndpointRegistry {

        private final ConcurrentHashMap<String, Endpoint> supportedEndpoints = new ConcurrentHashMap<>();

        private void handleGet(String path, String contentType, Supplier<String> functionResponse) {
            supportedEndpoints.put(path, new Endpoint(path, contentType, functionResponse));
        }

        boolean isPresent(String path) {
            return supportedEndpoints.containsKey(path);
        }

        /**
         * gets the response for a path
         * @throws RuntimeException thrown by the handler function
         * @return Content
         */
        public SimpleResponse getResponse(String path) {
            Endpoint endpoint = supportedEndpoints.get(path);
            String content = endpoint.buildContent();
            return new SimpleResponse(content, endpoint.contentType);
        }

        private record Endpoint(String path, String contentType, Supplier<String> functionResponse) {

            public String buildContent() {
                return functionResponse.get();
            }
        }
    }
}
