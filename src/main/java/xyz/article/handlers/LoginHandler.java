package xyz.article.handlers;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.cloudburstmc.math.vector.Vector2i;
import org.cloudburstmc.nbt.NbtMap;
import org.geysermc.mcprotocollib.auth.GameProfile;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.protocol.MinecraftConstants;
import org.geysermc.mcprotocollib.protocol.ServerLoginHandler;
import org.geysermc.mcprotocollib.protocol.data.game.PlayerListEntry;
import org.geysermc.mcprotocollib.protocol.data.game.PlayerListEntryAction;
import org.geysermc.mcprotocollib.protocol.data.game.chunk.BitStorage;
import org.geysermc.mcprotocollib.protocol.data.game.entity.EquipmentSlot;
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.Equipment;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.GameMode;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.PlayerSpawnInfo;
import org.geysermc.mcprotocollib.protocol.data.game.entity.type.EntityType;
import org.geysermc.mcprotocollib.protocol.packet.common.clientbound.ClientboundDisconnectPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundLoginPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundPlayerInfoUpdatePacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundSystemChatPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundSetEquipmentPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.player.ClientboundPlayerPositionPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.player.ClientboundSetCarriedItemPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.spawn.ClientboundAddEntityPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.inventory.ClientboundContainerSetContentPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundSetChunkCacheCenterPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundSetChunkCacheRadiusPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundSetTimePacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.article.RunningData;
import xyz.article.Settings;
import xyz.article.api.Slider;
import xyz.article.api.entities.EntityID;
import xyz.article.api.entities.player.Player;
import xyz.article.api.entities.player.PlayerAbilities;
import xyz.article.api.event.events.PlayerJoinEvent;
import xyz.article.api.inventory.PlayerInventory;
import xyz.article.api.world.chunk.ChunkData;
import xyz.article.api.world.chunk.ChunkPos;
import xyz.article.packets.ClientboundServerBrandPacket;

import java.io.File;
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
        Player player = null;
        boolean pass = false;
        File playerSaveDir = new File("./" + Settings.SAVE_FOLDER + "/playerdata");
        if (playerSaveDir.exists() && playerSaveDir.isDirectory()) {
            File[] files = playerSaveDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().endsWith(".slider")) {
                        String fileName = file.getName().replace(".slider", "");
                        if (fileName.equals(profile.getId().toString())) {
                            pass = true;
                            player = Player.getPlayerFromSave(file, session);
                        }
                    }
                }
            }
        }
        if (!pass) {
            player = new Player(entityId, session, profile, new PlayerInventory(), RunningData.worldMap.get(Key.key("minecraft:overworld")), GameMode.CREATIVE, new PlayerAbilities(true, true, false, true, 0.05f, 0.1f),0, 64, 0, 0, 0);
            int spawnChunkX = ((int) player.getPosition().getX()) >> 4;
            int spawnChunkZ = ((int) player.getPosition().getZ()) >> 4;

            // 优先生成玩家所在出生点的区块
            ChunkData spawnChunk;
            if (player.getWorld().getChunkDataMap().get(Vector2i.from(spawnChunkX, spawnChunkZ)) != null) {
                spawnChunk = player.getWorld().getChunkDataMap().get(Vector2i.from(spawnChunkX, spawnChunkZ));
            } else {
                File chunksDir = new File(Settings.SAVE_FOLDER, "worlds/" + player.getWorld().getKey().namespace() + "_" + player.getWorld().getKey().value() + "/chunks");
                if (chunksDir.mkdirs()) log.debug("已新建区块文件夹");
                File chunkFile = new File(chunksDir, "chunk_" + spawnChunkX + "_" + spawnChunkZ + ".slider");
                if (chunkFile.exists()) {
                    spawnChunk = ChunkData.deserializeFromFile(chunkFile);
                } else {
                    spawnChunk = player.getWorld().getGenerator().generateChunk(new ChunkPos(player.getWorld(), Vector2i.from(spawnChunkX, spawnChunkZ)));
                }
            }

            // 确定玩家出生点的y坐标
            if (spawnChunk != null) {
                NbtMap heightMap = spawnChunk.getHeightMap();
                long[] surfaceData = heightMap.getLongArray("WORLD_SURFACE");
                BitStorage decoded = new BitStorage(9, 256, surfaceData);
                int heightMapY = decoded.get((player.getPosition().getFloorX() & 15) * 16 + (player.getPosition().getFloorZ() & 15)) - 64; // 获取原始Y坐标
                // 更新玩家的出生位置到最高方块之上
                player.updatePosition(player.getPosition().getX(), heightMapY + 1, player.getPosition().getZ(), player.getYaw(), player.getPitch(), player.isOnGround());
                log.debug("已设置玩家 {} 的出生点y坐标为 {}", player.getProfile().getName(), heightMapY + 1);
            } else {
                log.error("未能获取到玩家 {} 的出生区块！", player.getProfile().getName());
            }
        }
        Component component = Component.text(profile.getName() + " 加入了游戏").color(NamedTextColor.YELLOW);
        PlayerJoinEvent joinEvent = new PlayerJoinEvent(player, component);
        Slider.getEventManager().callEvent(joinEvent);

        if (player == null) {
            log.error("错误！玩家为Null！");
            return;
        }

        if (player.getPosition().getY() < -64) {
            player.updatePosition(player.getPosition().getX(), 128, player.getPosition().getZ(), player.getYaw(), player.getPitch(), player.isOnGround());
        }

        // 发送登录数据包
        session.send(new ClientboundLoginPacket(player.getEntityId(), false, new Key[]{ Key.key("minecraft:overworld") }, Settings.MAX_PLAYERS, Settings.VIEW_DISTANCE, 16, false, false, false, new PlayerSpawnInfo(0, player.getWorld().getKey(), 100, player.getGameMode(), player.getGameMode(), false, false, null, 100), true));
        // 发送服务器品牌数据包
        session.send(new ClientboundServerBrandPacket("SliderMC - Rebuild").getPacket());
        // 发送视野距离数据包
        session.send(new ClientboundSetChunkCacheRadiusPacket(Settings.VIEW_DISTANCE));
        session.send(new ClientboundSetChunkCacheCenterPacket(((int) player.getPosition().getX()) >> 4, ((int) player.getPosition().getZ() >> 4)));
        // 保持玩家客户端位置与player实体同步
        session.send(new ClientboundPlayerPositionPacket(player.getPosition().getX(), player.getPosition().getY(), player.getPosition().getZ(), player.getYaw(), player.getPitch(), new Random().nextInt()));
        // 保持玩家客户端物品栏同步
        session.send(new ClientboundContainerSetContentPacket(0, 0, player.getInventory().getItems(), player.getInventory().getDraggingItem()));
        session.send(new ClientboundSetCarriedItemPacket(player.getMainHand().getCurrentSlot()));
        // 保持玩家能力同步
        session.send(player.getPlayerAbilities().getPacket());
        // 保持世界时间同步
        session.send(new ClientboundSetTimePacket(player.getWorld().getWorldAge(), player.getWorld().getWorldTime()));

        // 将玩家添加到RunningData的各个数据列表中
        RunningData.globalEntities.add(player.getEntityId());
        RunningData.globalSessions.add(session);
        RunningData.globalSessionPlayerMap.put(session, player);
        RunningData.globalPlayers.add(player);

        // 同步玩家属性
        for (Player player1 : RunningData.globalPlayers) {
            EnumSet<PlayerListEntryAction> actions = EnumSet.of(PlayerListEntryAction.ADD_PLAYER, PlayerListEntryAction.UPDATE_GAME_MODE, PlayerListEntryAction.UPDATE_LATENCY, PlayerListEntryAction.UPDATE_LISTED);
            player1.sendPacket(new ClientboundPlayerInfoUpdatePacket(actions, new PlayerListEntry[]{new PlayerListEntry(
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

        List<ClientboundSetEquipmentPacket> clientboundSetEquipmentPacketList = new ArrayList<>();
        for (Player player1 : player.getWorld().getPlayers()) {
            if (!player1.equals(player)) {
                session.send(new ClientboundAddEntityPacket(player1.getEntityId(), player1.getProfile().getId(), EntityType.PLAYER, player1.getPosition().getX(), player1.getPosition().getY(), player1.getPosition().getZ(), player1.getYaw(), player1.getPitch(), 0));
                clientboundSetEquipmentPacketList.add(new ClientboundSetEquipmentPacket(player1.getEntityId(), new Equipment[]{new Equipment(EquipmentSlot.MAIN_HAND, player1.getMainHand().getCurrentItem())}));
                clientboundSetEquipmentPacketList.add(new ClientboundSetEquipmentPacket(player1.getEntityId(), new Equipment[]{new Equipment(EquipmentSlot.OFF_HAND, player1.getLeftHand().getCurrentItem())}));
                player1.sendPacket(new ClientboundSetEquipmentPacket(player.getEntityId(), new Equipment[]{new Equipment(EquipmentSlot.MAIN_HAND, player.getMainHand().getCurrentItem())}));
                player1.sendPacket(new ClientboundSetEquipmentPacket(player.getEntityId(), new Equipment[]{new Equipment(EquipmentSlot.OFF_HAND, player.getLeftHand().getCurrentItem())}));
                player1.sendPacket(new ClientboundSystemChatPacket(joinEvent.getJoinMessage(), false));
                player1.sendPacket(new ClientboundAddEntityPacket(player.getEntityId(), profile.getId(), EntityType.PLAYER, player.getPosition().getX(), player.getPosition().getY(), player.getPosition().getZ(), player.getYaw(), player.getPitch(), 0));
            }
        }
        for (ClientboundSetEquipmentPacket packet : clientboundSetEquipmentPacketList) {
            session.send(packet);
        }

        // 完成Login
        log.info("{} 加入了游戏", profile.getName());
    }
}
