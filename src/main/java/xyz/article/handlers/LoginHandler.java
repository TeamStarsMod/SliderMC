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
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.GameMode;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.PlayerSpawnInfo;
import org.geysermc.mcprotocollib.protocol.data.game.entity.type.EntityType;
import org.geysermc.mcprotocollib.protocol.packet.common.clientbound.ClientboundDisconnectPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundLoginPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundPlayerInfoUpdatePacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundSystemChatPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.spawn.ClientboundAddEntityPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundSetChunkCacheRadiusPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.border.ClientboundInitializeBorderPacket;
import org.geysermc.mcprotocollib.protocol.packet.login.clientbound.ClientboundLoginCompressionPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.article.RunningData;
import xyz.article.Settings;
import xyz.article.api.Slider;
import xyz.article.api.entities.EntityID;
import xyz.article.api.entities.player.Player;
import xyz.article.api.event.events.PlayerChatEvent;
import xyz.article.api.event.events.PlayerJoinEvent;
import xyz.article.api.inventory.PlayerInventory;
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
        // 检查玩家是否可以加入游戏
        if ((RunningData.globalPlayers.size() + 1) > Settings.MAX_PLAYERS) {
            session.send(new ClientboundDisconnectPacket("这个服务器没有地方容纳你了！"));
            return;
        }

        GameProfile profile = session.getFlag(MinecraftConstants.PROFILE_KEY);

        boolean inServer = false;
        for (Player player : RunningData.globalPlayers) {
            if (player.getProfile().getId().equals(profile.getId())) {
                inServer = true;
                break;
            }
        }
        if (inServer) {
            session.send(new ClientboundDisconnectPacket("你已经在这个服务器里了！"));
            return;
        }

        int entityId = EntityID.getRandomEntityId();
        Player player = new Player(entityId, session, profile, new PlayerInventory(), RunningData.worldMap.get(Key.key("minecraft:overworld")), GameMode.CREATIVE,8.5, 64, 8.5, 0, 0);
        Component component = Component.text(profile.getName() + " 加入了游戏").color(NamedTextColor.YELLOW);
        PlayerJoinEvent joinEvent = new PlayerJoinEvent(player, component);
        Slider.getEventManager().callEvent(joinEvent);
        RunningData.globalEntities.add(player.getEntityId());
        session.send(new ClientboundLoginPacket(player.getEntityId(), false, new Key[]{ Key.key("minecraft:overworld") }, Settings.MAX_PLAYERS, Settings.VIEW_DISTANCE, 16, false, false, false, new PlayerSpawnInfo(0, player.getWorld().getKey(), 100, player.getGameMode(), player.getGameMode(), false, false, null, 100), true));
        session.send(new ClientboundServerBrandPacket("SliderMC - Rebuild").getPacket());
        session.send(new ClientboundSetChunkCacheRadiusPacket(Settings.VIEW_DISTANCE));
        /*for (int i = -6; i < 6; i++) {  // 注释原因：为什么要在玩家刚加入游戏时就发送地形? WorldTick中不是已经会加载了?
            for (int l = -6; l < 6; l++) {
                ChunkData chunkData = player.getWorld().getChunkDataMap().get(Vector2i.from(i, l));
                session.send(chunkData.getPacket());
            }
        }*/
        RunningData.globalSessions.add(session);
        RunningData.globalSessionPlayerMap.put(session, player);
        RunningData.globalPlayers.add(player);
        for (Session session1 : RunningData.globalSessions) {
            session1.send(new ClientboundSystemChatPacket(joinEvent.getJoinMessage(), false));
            EnumSet<PlayerListEntryAction> actions = EnumSet.of(PlayerListEntryAction.ADD_PLAYER, PlayerListEntryAction.UPDATE_GAME_MODE, PlayerListEntryAction.UPDATE_LATENCY, PlayerListEntryAction.UPDATE_LISTED);
            session1.send(new ClientboundPlayerInfoUpdatePacket(actions, new PlayerListEntry[]{new PlayerListEntry(
                    player.getProfile().getId(),
                    player.getProfile(),
                    true,
                    0,
                    player.getGameMode(),
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
                    player1.getGameMode(),
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
