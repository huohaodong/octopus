package com.huohaodong.octopus.broker.server;

import com.huohaodong.octopus.broker.config.BrokerProperties;
import com.huohaodong.octopus.broker.protocol.mqtt.MqttPacketDispatcher;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;

import static com.huohaodong.octopus.broker.server.Constants.*;

@Slf4j(topic = "BROKER_INITIALIZER")
@RequiredArgsConstructor
@Component
public class BrokerInitializer {

    private final BrokerProperties brokerProperties;

    private final MqttPacketDispatcher mqttPacketDispatcher;

    @PostConstruct
    public void start() {
        new Thread(this::initBroker, "OCTOPUS_BROKER").start();
    }

    public void initBroker() {
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();
        ServerBootstrap b = new ServerBootstrap().group(boss, worker).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel ch) throws Exception {
                log.info("Channel init {}", ch);
                ch.pipeline().addLast(HANDLER_HEARTBEAT, new IdleStateHandler(0, 0, 3600));
                ch.pipeline().addLast(HANDLER_MQTT_DECODER, new MqttDecoder());
                ch.pipeline().addLast(HANDLER_MQTT_DISPATCHER, mqttPacketDispatcher);
                ch.pipeline().addLast(HANDLER_MQTT_ENCODER, MqttEncoder.INSTANCE);
            }
        }).option(ChannelOption.SO_BACKLOG, 1000);
        try {
            ChannelFuture cf = b.bind(new InetSocketAddress(brokerProperties.getHost(), brokerProperties.getPort())).sync();
            InetSocketAddress socketAddress = (InetSocketAddress) cf.channel().localAddress();
            log.info("Start octopus server, host: {}, port: {}", socketAddress.getAddress(), socketAddress.getPort());
            cf.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}
