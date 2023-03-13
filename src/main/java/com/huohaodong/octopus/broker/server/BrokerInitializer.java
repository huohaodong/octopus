package com.huohaodong.octopus.broker.server;

import com.huohaodong.octopus.broker.config.BrokerConfig;
import com.huohaodong.octopus.broker.protocol.mqtt.MqttPacketDispatcher;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.InetSocketAddress;

@Slf4j
@AllArgsConstructor
@Component
public class BrokerInitializer {

    BrokerConfig brokerConfig;

    MqttPacketDispatcher mqttPacketDispatcher;

    @PostConstruct
    public void start() {
        new Thread(this::initBroker, "octopus-broker").start();
    }

    public void initBroker() {
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();
        ServerBootstrap b = new ServerBootstrap().group(boss, worker).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel ch) throws Exception {
                log.info("channel init {}", ch);
                ch.pipeline().addLast("heartbeat", new IdleStateHandler(0, 0, 3600));
                ch.pipeline().addLast("dispatcher", mqttPacketDispatcher);
                ch.pipeline().addLast("decoder", new MqttDecoder());
                ch.pipeline().addLast("encoder", MqttEncoder.INSTANCE);
            }
        });
        try {
            ChannelFuture cf = b.bind(new InetSocketAddress(brokerConfig.getHost(), brokerConfig.getPort())).sync();
            InetSocketAddress socketAddress = (InetSocketAddress) cf.channel().localAddress();
            log.info("start octopus server, host: {}, port: {}", socketAddress.getAddress(), socketAddress.getPort());
            cf.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}