package xyz.article.packets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.kyori.adventure.key.Key;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftCodecHelper;
import org.geysermc.mcprotocollib.protocol.packet.common.clientbound.ClientboundCustomPayloadPacket;

import java.nio.charset.StandardCharsets;

public class ClientboundServerBrandPacket {
    private final String brand;

    public ClientboundServerBrandPacket(String brand) {
        this.brand = brand;
    }

    public ClientboundCustomPayloadPacket getPacket() {
        ByteBuf buf = Unpooled.buffer(brand.length() + 1);
        MinecraftCodecHelper helper = new MinecraftCodecHelper();
        helper.writeVarInt(buf, brand.length());
        buf.writeBytes(brand.getBytes(StandardCharsets.UTF_8));
        return new ClientboundCustomPayloadPacket(Key.key("minecraft:brand"), buf.array());
    }
}
