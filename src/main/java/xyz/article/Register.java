package xyz.article;

import xyz.article.api.packetprocessor.PacketProcessor;
import xyz.article.packetprocessor.ChatPacketProcessor;
import xyz.article.packetprocessor.MovePlayerPosPacketProcessor;
import xyz.article.packetprocessor.MovePlayerPosRotPacketProcessor;
import xyz.article.packetprocessor.MovePlayerRotPacketProcessor;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Register {
    private static final List<PacketProcessor> packetProcessors = new CopyOnWriteArrayList<>();

    /**
     * 注册一个数据包处理器
     * @param processor 数据包处理器实例
     */
    public static void registerPacketProcessor(PacketProcessor processor) {
        packetProcessors.add(processor);
    }

    public static void register() {
        registerPacketProcessor(new MovePlayerPosPacketProcessor());
        registerPacketProcessor(new MovePlayerPosRotPacketProcessor());
        registerPacketProcessor(new MovePlayerRotPacketProcessor());
        registerPacketProcessor(new ChatPacketProcessor());
    }

    public static List<PacketProcessor> getPacketProcessors() {
        return packetProcessors;
    }
}
