package xyz.article.handlers;

import net.kyori.adventure.key.Key;
import org.cloudburstmc.math.vector.Vector2i;
import org.geysermc.mcprotocollib.auth.GameProfile;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.protocol.MinecraftConstants;
import org.geysermc.mcprotocollib.protocol.ServerLoginHandler;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.BitStorage;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.ChunkSection;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.DataPalette;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.palette.GlobalPalette;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.palette.PaletteType;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.GameMode;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.PlayerSpawnInfo;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundLoginPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.article.api.world.World;
import xyz.article.api.world.chunk.ChunkData;
import xyz.article.api.world.chunk.ChunkPos;
import xyz.article.packets.ClientboundServerBrandPacket;

public class LoginHandler implements ServerLoginHandler {
    private final Logger log = LoggerFactory.getLogger(LoginHandler.class);
    @Override
    public void loggedIn (Session session) {
        // Player Login Logic
        session.send(new ClientboundLoginPacket(0, false, new Key[]{ Key.key("minecraft:overworld") }, 100, 10, 16, false, false, false, new PlayerSpawnInfo(0, Key.key("minecraft:overworld"), 100, GameMode.CREATIVE, GameMode.CREATIVE, false, false, null, 100), true));
        session.send(new ClientboundServerBrandPacket("SliderMC - Rebuild").getPacket());
        ChunkSection[] chunkSections = new ChunkSection[24];
        for (int i = 0; i < 24; i++) {
            chunkSections[i] = new ChunkSection(0, DataPalette.createForChunk(), new DataPalette(GlobalPalette.INSTANCE, new BitStorage(16, 4 * 4 * 4), PaletteType.BIOME));
        }
        for (int i = 0; i < 16; i++) {
            chunkSections[0].setBlock(i, 15, i, 9);
        }
        for (int i = -6; i < 6; i++) {
            for (int l = -6; l < 6; l++) {
                session.send(new ChunkData(new ChunkPos(new World(), Vector2i.from(i, l)), chunkSections).getPacket());
            }
        }

        GameProfile profile = session.getFlag(MinecraftConstants.PROFILE_KEY);
        log.info("玩家 {} 加入了游戏", profile.getName());
    }
}
