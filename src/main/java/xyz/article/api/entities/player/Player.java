package xyz.article.api.entities.player;

import org.geysermc.mcprotocollib.auth.GameProfile;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.GameMode;
import xyz.article.api.inventory.PlayerInventory;
import xyz.article.api.world.World;

public class Player {
    private final Session session;
    private final GameProfile profile;
    private PlayerInventory inventory;
    private World world;
    private double locationX;
    private double locationY;
    private double locationZ;
    private float angleYaw;
    private float anglePitch;
    private final int entityId;
    private GameMode gameMode;
    private final Hand mainHand, leftHand;

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
    public Player(int entityId, Session session, GameProfile profile, PlayerInventory playerInventory, World world, GameMode gameMode, double x, double y, double z, float yaw, float pitch) {
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
    public double getX() {
        return locationX;
    }
    public double getY() {
        return locationY;
    }
    public double getZ() {
        return locationZ;
    }

    public float getYaw() {
        return angleYaw;
    }
    public float getPitch() {
        return anglePitch;
    }

    public void setWorld(World world) {
        this.world = world;
    }
    public void setX(double locationX) {
        this.locationX = locationX;
    }
    public void setY(double locationY) {
        this.locationY = locationY;
    }
    public void setZ(double locationZ) {
        this.locationZ = locationZ;
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
}
