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
import java.util.concurrent.TimeUnit;
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

    /**
     * Registers a GET endpoint handler for the specified path with the given content type.
     *
     * @param path             The URL path to handle
     * @param contentType      The content type of the response
     * @param functionResponse Supplier function that provides the response content
     */
    public void handle(String path, ContentType contentType, Supplier<String> functionResponse) {
        registry.handleGet(path, contentType.getType(), functionResponse);
    }

    @Override
    public void close() {
        workerGroup.shutdownGracefully().awaitUninterruptibly(10, TimeUnit.SECONDS);
        bossGroup.shutdownGracefully().awaitUninterruptibly(10, TimeUnit.SECONDS);
        workerGroup.close();
        bossGroup.close();
    }

    /**
     * Registry for HTTP endpoints that manages supported paths and their corresponding handlers.
     * HttpEndpointRegistry maintains a thread-safe mapping of URL paths to their respective endpoint configurations.
     */
    class HttpEndpointRegistry {

        private final ConcurrentHashMap<String, Endpoint> supportedEndpoints = new ConcurrentHashMap<>();

        /**
         * Registers a GET endpoint handler for the specified path.
         *
         * @param path             The URL path to handle
         * @param contentType      The content type of the response
         * @param functionResponse Supplier function that provides the response content
         */
        private void handleGet(String path, String contentType, Supplier<String> functionResponse) {
            supportedEndpoints.put(path, new Endpoint(path, contentType, functionResponse));
        }

        /**
         * Checks if a handler exists for the specified path.
         *
         * @param path The URL path to check
         * @return true if a handler exists for the path, false otherwise
         */
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

        /**
         * Record representing an HTTP endpoint configuration.
         *
         * @param path             The URL path for this endpoint
         * @param contentType      The content type of responses from this endpoint
         * @param functionResponse The supplier function that generates response content
         */
        private record Endpoint(String path, String contentType, Supplier<String> functionResponse) {

            public String buildContent() {
                return functionResponse.get();
            }
        }
    }
}
