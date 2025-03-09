package xyz.article.api.entities.player;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.cloudburstmc.math.vector.Vector2i;
import org.cloudburstmc.math.vector.Vector3d;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtMapBuilder;
import org.cloudburstmc.nbt.NbtType;
import org.cloudburstmc.nbt.NbtUtils;
import org.geysermc.mcprotocollib.auth.GameProfile;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.GameMode;
import org.geysermc.mcprotocollib.protocol.data.game.item.ItemStack;
import org.geysermc.mcprotocollib.protocol.packet.common.clientbound.ClientboundDisconnectPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundSystemChatPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundMoveEntityPosRotPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.player.ClientboundPlayerPositionPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.article.RunningData;
import xyz.article.api.command.CommandSender;
import xyz.article.api.entities.EntityID;
import xyz.article.api.entities.interfaces.Entity;
import xyz.article.api.inventory.PlayerInventory;
import xyz.article.api.world.World;
import xyz.article.api.world.block.BlockFace;
import xyz.article.api.world.chunk.ChunkData;
import xyz.article.api.world.chunk.ChunkPos;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Player implements Entity, CommandSender {
    private static final Logger log = LoggerFactory.getLogger(Player.class);
    private final Session session;
    private final GameProfile profile;
    private PlayerInventory inventory;
    private World world;
    private double locationX;
    private double locationY;
    private double locationZ;
    private float angleYaw;
    private float anglePitch;
    private boolean onGround;
    private final int entityId;
    private GameMode gameMode;
    private final Hand mainHand, leftHand;
    private PlayerAbilities playerAbilities;

    private final Map<Vector2i, ChunkData> loadedChunks = new ConcurrentHashMap<>();
    private ChunkPos lastChunkPos = null;
    private Vector3d lastValidPosition;

    /**
     * 创建一个新玩家实例
     * @param entityId 此玩家的entityId
     * @param session 此玩家的session实例
     * @param profile 此玩家的GameProfile实例
     * @param playerInventory 此玩家的物品栏
     * @param world 此玩家初始化时所在的世界(此玩家将会自动被添加到此世界)
     * @param gameMode 玩家的游戏模式
     * @param x 此玩家初始化时的x坐标
     * @param y 此玩家初始化时的y坐标
     * @param z 此玩家初始化时的z坐标
     * @param yaw 此玩家初始化时的yaw角度
     * @param pitch 此玩家初始化时的pitch角度
     */
    public Player(int entityId, Session session, GameProfile profile, PlayerInventory playerInventory, World world, GameMode gameMode, PlayerAbilities playerAbilities, double x, double y, double z, float yaw, float pitch) {
        this.session = session;
        this.profile = profile;
        this.inventory = playerInventory;
        this.world = world;
        this.locationX = x;
        this.locationY = y;
        this.locationZ = z;
        this.angleYaw = yaw;
        this.anglePitch = pitch;
        this.entityId = entityId;
        this.gameMode = gameMode;
        this.mainHand = new Hand();
        this.leftHand = new Hand();
        this.playerAbilities = playerAbilities;
        this.lastValidPosition = Vector3d.from(x, y, z);
        this.onGround = false;

        world.getPlayers().add(this);
    }

    public void sendPacket (Packet packet) {
        session.send(packet);
    }

    public Session getSession() {
        return session;
    }

    public GameProfile getProfile() {
        return profile;
    }

    public PlayerInventory getInventory() {
        return inventory;
    }

    public void setInventory(PlayerInventory inventory) {
        this.inventory = inventory;
    }

    public World getWorld() {
        return world;
    }

    public Vector3d getPosition() {
        return Vector3d.from(locationX, locationY, locationZ);
    }

    public float getYaw() {
        return angleYaw;
    }
    public float getPitch() {
        return anglePitch;
    }
    public boolean isOnGround() {
        return onGround;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    /**
     * 此方法用于更新玩家对象内部保存的位置(不会同步给其他玩家和绑定的客户端) (可使用第二个构造函数来自动同步位置)
     * @param x 要设置的x坐标
     * @param y 要设置的y坐标
     * @param z 要设置的z坐标
     * @param yaw 要设置的yaw
     * @param pitch 要设置的pitch
     */
    public void updatePosition(double x, double y, double z, float yaw, float pitch, boolean onGround) {
        this.locationX = x;
        this.locationY = y;
        this.locationZ = z;
        this.angleYaw = yaw;
        this.anglePitch = pitch;
        this.onGround = onGround;
    }

    /**
     * 此方法用于更新玩家对象内部保存的位置(不会同步给其他玩家和绑定的客户端)
     * @param x 要设置的x坐标
     * @param y 要设置的y坐标
     * @param z 要设置的z坐标
     * @param yaw 要设置的yaw
     * @param pitch 要设置的pitch
     * @param syncOtherPlayer 是否将玩家实体位置同步给其他玩家
     */
    public void updatePosition(double x, double y, double z, float yaw, float pitch, boolean onGround, boolean syncOtherPlayer) {
        if (syncOtherPlayer) {
            for (Player player1 : world.getPlayers()) {
                if (!player1.equals(this)) {
                    player1.sendPacket(new ClientboundMoveEntityPosRotPacket(
                            entityId,
                            (x - locationX), (y - locationY), (z - locationZ),
                            yaw, pitch,
                            onGround
                    ));
                }
            }
        }

        this.locationX = x;
        this.locationY = y;
        this.locationZ = z;
        this.angleYaw = yaw;
        this.anglePitch = pitch;
    }

    /**
     * 同步玩家客户端(绑定的Session)与此玩家实体的位置
     */
    public void syncClient() {
        session.send(new ClientboundPlayerPositionPacket(
                locationX, locationY, locationZ,
                angleYaw, anglePitch,
                new Random().nextInt()
        ));
    }

    /**
     * 从服务器踢出此玩家
     * @param reason 踢出理由
     */
    public void kick(String reason) {
        session.send(new ClientboundDisconnectPacket(reason));
        session.disconnect(reason);
    }

    public void setPitch(float anglePitch) {
        this.anglePitch = anglePitch;
    }
    public void setYaw(float angleYaw) {
        this.angleYaw = angleYaw;
    }

    public int getEntityId() {
        return entityId;
    }

    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public Hand getMainHand() {
        return mainHand;
    }

    public Hand getLeftHand() {
        return leftHand;
    }

    public Map<Vector2i, ChunkData> getLoadedChunks() {
        return loadedChunks;
    }

    public void setLastChunkPos(ChunkPos lastChunkPos) {
        this.lastChunkPos = lastChunkPos;
    }

    public ChunkPos getLastChunkPos() {
        return lastChunkPos;
    }

    public PlayerAbilities getPlayerAbilities() {
        return playerAbilities;
    }

    public void setPlayerAbilities(PlayerAbilities playerAbilities) {
        this.playerAbilities = playerAbilities;
    }

    public Vector3d getLastValidPosition() {
        return lastValidPosition;
    }

    public void setLastValidPosition(Vector3d lastValidPosition) {
        this.lastValidPosition = lastValidPosition;
    }

    public BlockFace getHorizontalFacing() {
        float normalizedYaw = (angleYaw % 360 + 360) % 360;
        if ((normalizedYaw >= 315) || (normalizedYaw < 45)) {
            return BlockFace.SOUTH;
        } else if (normalizedYaw >= 45 && normalizedYaw < 135) {
            return BlockFace.WEST;
        } else if (normalizedYaw >= 135 && normalizedYaw < 225) {
            return BlockFace.NORTH;
        } else {
            return BlockFace.EAST;
        }
    }

    public BlockFace getVerticalFacing() {
        if (anglePitch < -45) {
            return BlockFace.UP;
        } else if (anglePitch > 45) {
            return BlockFace.DOWN;
        }
        return null;
    }


    @Override
    public void sendMessage(String msg) {
        session.send(new ClientboundSystemChatPacket(Component.text(msg), false));
    }

    /**
     * 将此玩家的信息保存到存档文件中
     * @param file 存档文件
     */
    public void saveToFile(File file) {
        NbtMapBuilder root = NbtMap.builder();

        // 序列化基础信息
        root.putDouble("x", locationX)
                .putDouble("y", locationY)
                .putDouble("z", locationZ)
                .putFloat("yaw", angleYaw)
                .putFloat("pitch", anglePitch)
                .putString("gameMode", gameMode.name());

        // 序列化Profile
        NbtMapBuilder profileBuilder = NbtMap.builder()
                .putString("name", profile.getName())
                .putString("uuid", profile.getId().toString());
        root.putCompound("profile", profileBuilder.build());

        // 序列化世界信息
        root.putString("world", world.getKey().toString());

        // 序列化物品栏
        NbtMapBuilder inventoryBuilder = NbtMap.builder();
        List<NbtMap> items = new ArrayList<>();
        for (int i = 0; i < inventory.getItems().length; i++) {
            ItemStack item = inventory.getItems()[i];
            if (item != null) {
                NbtMapBuilder itemBuilder = NbtMap.builder()
                        .putInt("slot", i)
                        .putInt("id", item.getId())
                        .putInt("count", item.getAmount());

                /*if (item.getDataComponents() != null) {
                    itemBuilder.putCompound("nbt", item.getDataComponents()); // TODO: 由于技术原因，暂时无法实现存储DataComponents，需要完成
                }*/
                items.add(itemBuilder.build());
            }
        }
        inventoryBuilder.putList("items", NbtType.COMPOUND, items);
        root.putCompound("inventory", inventoryBuilder.build());

        // 序列化手持物品
        root.putCompound("mainHand", serializeHand(mainHand));
        root.putCompound("offHand", serializeHand(leftHand));

        // 序列化玩家能力
        NbtMapBuilder abilitiesNbt = NbtMap.builder();
        abilitiesNbt.putBoolean("isFlying", playerAbilities.isFlying());
        abilitiesNbt.putBoolean("invincible", playerAbilities.isInvincible());
        abilitiesNbt.putBoolean("canFly", playerAbilities.isCanFly());
        abilitiesNbt.putBoolean("creative", playerAbilities.isCreative());
        abilitiesNbt.putFloat("flySpeed", playerAbilities.getFlySpeed());
        abilitiesNbt.putFloat("walkSpeed", playerAbilities.getWalkSpeed());

        root.putCompound("abilities", abilitiesNbt.build());

        // 写入文件
        try (FileOutputStream fos = new FileOutputStream(file)) {
            NbtMap nbt = root.build();
            NbtUtils.createWriter(fos).writeValue(nbt);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private NbtMap serializeHand(Hand hand) {
        ItemStack item = hand.getCurrentItem();
        if (item == null) {
            return NbtMap.EMPTY;
        }

        NbtMapBuilder builder = NbtMap.builder()
                .putInt("id", item.getId())
                .putInt("count", item.getAmount())
                .putInt("slot", hand.getCurrentSlot());

        /*if (item.getDataComponents() != null) {
            builder.putCompound("nbt", item.getDataComponents());
        }*/

        return builder.build();
    }

    /**
     * 从指定存档文件中获取玩家
     * @param file 存档文件
     * @param session 玩家的session实例
     * @return 获取到的玩家
     */
    public static Player getPlayerFromSave(File file, Session session) {
        try (FileInputStream fis = new FileInputStream(file)) {
            // 读取NBT数据
            NbtMap nbt = NbtUtils.createReader(fis).readValue(NbtType.COMPOUND);

            // 解析基础信息
            double x = nbt.getDouble("x");
            double y = nbt.getDouble("y");
            double z = nbt.getDouble("z");
            float yaw = nbt.getFloat("yaw");
            float pitch = nbt.getFloat("pitch");
            GameMode gameMode = GameMode.valueOf(nbt.getString("gameMode"));

            // 解析Profile
            NbtMap profileNbt = nbt.getCompound("profile");
            GameProfile profile = new GameProfile(
                    UUID.fromString(profileNbt.getString("uuid")),
                    profileNbt.getString("name")
            );

            // 获取世界实例
            String worldKey = nbt.getString("world");
            World world = RunningData.worldMap.get(Key.key((worldKey)));
            if (world == null) {
                throw new IllegalArgumentException("未获取到世界" + worldKey + " ！");
            }

            // 创建空玩家物品栏
            PlayerInventory inventory = new PlayerInventory();

            // 反序列化物品栏
            NbtMap inventoryNbt = nbt.getCompound("inventory");
            List<NbtMap> itemsNbt = inventoryNbt.getList("items", NbtType.COMPOUND);
            for (NbtMap itemNbt : itemsNbt) {
                int slot = itemNbt.getInt("slot");
                int id = itemNbt.getInt("id");
                int count = itemNbt.getInt("count");

                ItemStack item = new ItemStack(id, count);
                // TODO: 当支持DataComponents时反序列化nbt
                inventory.setItem(slot, item);
            }

            // 反序列化玩家能力
            NbtMap playerAbilitiesNbt = nbt.getCompound("abilities");
            boolean isFlying = playerAbilitiesNbt.getBoolean("isFlying");
            boolean invincible = playerAbilitiesNbt.getBoolean("invincible");
            boolean canFly = playerAbilitiesNbt.getBoolean("canFly");
            boolean creative = playerAbilitiesNbt.getBoolean("creative");
            float flySpeed = playerAbilitiesNbt.getFloat("flySpeed");
            float walkSpeed = playerAbilitiesNbt.getFloat("walkSpeed");

            // 创建玩家实例
            Player player = new Player(
                    EntityID.getRandomEntityId(),
                    session,
                    profile,
                    inventory,
                    world,
                    gameMode,
                    new PlayerAbilities(invincible, canFly, isFlying, creative, flySpeed, walkSpeed),
                    x, y, z,
                    yaw, pitch
            );

            // 反序列化手持物品
            NbtMap mainHandNbt = nbt.getCompound("mainHand");
            if (!mainHandNbt.isEmpty()) {
                ItemStack mainHandItem = deserializeItem(mainHandNbt);
                int slot = mainHandNbt.getInt("slot");
                player.getMainHand().setCurrentItem(mainHandItem);
                player.getMainHand().setCurrentSlot(slot);
            }

            NbtMap offHandNbt = nbt.getCompound("offHand");
            if (!offHandNbt.isEmpty()) {
                ItemStack offHandItem = deserializeItem(offHandNbt);
                player.getLeftHand().setCurrentItem(offHandItem);
            }

            return player;
        } catch (FileNotFoundException e) {
            log.error("存档文件不存在: {}", file.getAbsolutePath());
        } catch (IOException e) {
            log.error("读取存档文件失败: {}", e.getMessage());
        } catch (Exception e) {
            log.error("反序列化玩家数据时发生错误: {}", e.getMessage());
        }
        return null;
    }

    private static ItemStack deserializeItem(NbtMap itemNbt) {
        int id = itemNbt.getInt("id");
        int count = itemNbt.getInt("count");
        ItemStack item = new ItemStack(id, count);

        // TODO: 当支持DataComponents时处理nbt字段

        return item;
    }
}
