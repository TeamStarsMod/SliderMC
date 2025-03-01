package xyz.article.api.packetprocessor;

import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.packet.Packet;

public interface PacketProcessor {
    void process(Session session, Packet packet);
}
