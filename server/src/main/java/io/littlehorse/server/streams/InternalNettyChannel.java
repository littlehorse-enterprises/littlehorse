package io.littlehorse.server.streams;

import io.grpc.ChannelCredentials;
import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.channel.EventLoopGroup;
import io.grpc.netty.shaded.io.netty.channel.nio.NioEventLoopGroup;
import io.grpc.netty.shaded.io.netty.channel.socket.nio.NioSocketChannel;
import io.grpc.netty.shaded.io.netty.util.concurrent.DefaultThreadFactory;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class InternalNettyChannel implements AutoCloseable {

    private static final int MAX_NUMBER_IO_THREADS = 3;
    private static final String THREAD_NAME_PREFIX = "lh-grpc-internal";

    private final ThreadFactory namedFactory = new DefaultThreadFactory(THREAD_NAME_PREFIX);
    private final EventLoopGroup nioEventLoopGroup = new NioEventLoopGroup(MAX_NUMBER_IO_THREADS, namedFactory);
    private final ManagedChannel grpcChannel;

    public InternalNettyChannel(String host, int port) {
        Objects.requireNonNull(host);
        this.grpcChannel = NettyChannelBuilder.forAddress(host, port)
                .eventLoopGroup(nioEventLoopGroup)
                .channelType(NioSocketChannel.class)
                .usePlaintext()
                .executor(Executors.newVirtualThreadPerTaskExecutor())
                .build();
    }

    public InternalNettyChannel(String host, int port, ChannelCredentials credentials) {
        Objects.requireNonNull(host);
        Objects.requireNonNull(credentials);
        this.grpcChannel = NettyChannelBuilder.forAddress(host, port, credentials)
                .eventLoopGroup(nioEventLoopGroup)
                .channelType(NioSocketChannel.class)
                .usePlaintext()
                .executor(Executors.newVirtualThreadPerTaskExecutor())
                .build();
    }

    public ManagedChannel getChannel() {
        return grpcChannel;
    }

    @Override
    public void close() {
        nioEventLoopGroup.shutdownGracefully();
        grpcChannel.shutdown();
    }
}
