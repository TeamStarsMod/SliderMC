package xyz.article.handlers;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.cloudburstmc.math.vector.Vector2i;
import org.geysermc.mcprotocollib.auth.GameProfile;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.protocol.MinecraftConstants;
import org.geysermc.mcprotocollib.protocol.ServerLoginHandler;
import org.geysermc.mcprotocollib.protocol.data.game.PlayerListEntry;
import org.geysermc.mcprotocollib.protocol.data.game.PlayerListEntryAction;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.BitStorage;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.ChunkSection;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.DataPalette;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.palette.GlobalPalette;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.palette.PaletteType;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.GameMode;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.PlayerSpawnInfo;
import org.geysermc.mcprotocollib.protocol.data.game.entity.type.EntityType;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundLoginPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundPlayerInfoUpdatePacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundSystemChatPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.spawn.ClientboundAddEntityPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.article.RunningData;
import xyz.article.api.entities.EntityID;
import xyz.article.api.entities.player.Player;
import xyz.article.api.inventory.PlayerInventory;
import xyz.article.api.world.World;
import xyz.article.api.world.chunk.ChunkData;
import xyz.article.api.world.chunk.ChunkPos;
import xyz.article.packets.ClientboundServerBrandPacket;

import java.util.*;

/**
 * 玩家登入逻辑类
 */
public class LoginHandler implements ServerLoginHandler {
    private final Logger log = LoggerFactory.getLogger(LoginHandler.class);
    @Override
    public void loggedIn (Session session) {
        // Player Login Logic
        GameProfile profile = session.getFlag(MinecraftConstants.PROFILE_KEY);
        int entityId = EntityID.getRandomEntityId();
        Player player = new Player(entityId, session, profile, new PlayerInventory(), RunningData.worldMap.get(Key.key("minecraft:overworld")), 8.5, 64, 8.5, 0, 0);
        RunningData.globalEntities.add(player.getEntityId());
        session.send(new ClientboundLoginPacket(player.getEntityId(), false, new Key[]{ Key.key("minecraft:overworld") }, 100, 10, 16, false, false, false, new PlayerSpawnInfo(0, Key.key("minecraft:overworld"), 100, GameMode.CREATIVE, GameMode.CREATIVE, false, false, null, 100), true));
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
                session.send(new ChunkData(new ChunkPos(RunningData.worldMap.get(Key.key("minecraft:overworld")), Vector2i.from(i, l)), chunkSections).getPacket());
            }
        }
        RunningData.globalSessions.add(session);
        RunningData.globalSessionPlayerMap.put(session, player);
        RunningData.globalPlayers.add(player);
        for (Session session1 : RunningData.globalSessions) {
            session1.send(new ClientboundSystemChatPacket(Component.text(profile.getName() + " 加入了游戏").color(NamedTextColor.YELLOW), false));
            EnumSet<PlayerListEntryAction> actions = EnumSet.of(PlayerListEntryAction.ADD_PLAYER, PlayerListEntryAction.UPDATE_GAME_MODE, PlayerListEntryAction.UPDATE_LATENCY, PlayerListEntryAction.UPDATE_LISTED);
            session1.send(new ClientboundPlayerInfoUpdatePacket(actions, new PlayerListEntry[]{new PlayerListEntry(
                    player.getProfile().getId(),
                    player.getProfile(),
                    true,
                    0,
                    GameMode.CREATIVE,
                    null,
                    UUID.randomUUID(),
                    -1,
                    null,
                    null
            )}));
            if (!session1.equals(session)) {
                session1.send(new ClientboundAddEntityPacket(player.getEntityId(), profile.getId(), EntityType.PLAYER, player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch(), 0));
            }
        }
        List<PlayerListEntry> list = new ArrayList<>();
        for (Player player1 : RunningData.globalPlayers) {
            list.add(new PlayerListEntry(
                    player1.getProfile().getId(),
                    player1.getProfile(),
                    true,
                    0,
                    GameMode.CREATIVE,
                    null,
                    UUID.randomUUID(),
                    -1,
                    null,
                    null
            ));
        }
        EnumSet<PlayerListEntryAction> actions1 = EnumSet.of(PlayerListEntryAction.ADD_PLAYER, PlayerListEntryAction.UPDATE_GAME_MODE, PlayerListEntryAction.UPDATE_LATENCY, PlayerListEntryAction.UPDATE_LISTED);
        session.send(new ClientboundPlayerInfoUpdatePacket(actions1, list.toArray(new PlayerListEntry[0])));
        for (Player player1 : RunningData.globalPlayers) {
            if (!player1.equals(player)) {
                session.send(new ClientboundAddEntityPacket(player1.getEntityId(), player1.getProfile().getId(), EntityType.PLAYER, player1.getX(), player1.getY(), player1.getZ(), player1.getYaw(), player1.getPitch(), 0));
            }
        }
        log.info("{} 加入了游戏", profile.getName());
    }
}
