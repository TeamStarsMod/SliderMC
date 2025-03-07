package xyz.article.api.entities.player;

import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.player.ClientboundPlayerAbilitiesPacket;

public class PlayerAbilities {
    private boolean isFlying;
    private boolean invincible;
    private boolean canFly;
    private boolean creative;
    private float walkSpeed;
    private float flySpeed;

    public PlayerAbilities(boolean invincible, boolean canFly, boolean isFlying, boolean creative, float flySpeed, float walkSpeed) {
        this.isFlying = isFlying;
        this.canFly = canFly;
        this.invincible = invincible;
        this.creative = creative;
        this.flySpeed = flySpeed;
        this.walkSpeed = walkSpeed;
    }

    public void setFlying(boolean flying) {
        isFlying = flying;
    }

    public boolean isFlying() {
        return isFlying;
    }

    public void setCanFly(boolean canFly) {
        this.canFly = canFly;
    }

    public boolean isCanFly() {
        return canFly;
    }

    public void setInvincible(boolean invincible) {
        this.invincible = invincible;
    }

    public boolean isInvincible() {
        return invincible;
    }

    public void setCreative(boolean creative) {
        this.creative = creative;
    }

    public boolean isCreative() {
        return creative;
    }

    public float getFlySpeed() {
        return flySpeed;
    }

    public void setFlySpeed(float flySpeed) {
        this.flySpeed = flySpeed;
    }

    public float getWalkSpeed() {
        return walkSpeed;
    }

    public void setWalkSpeed(float walkSpeed) {
        this.walkSpeed = walkSpeed;
    }

    /**
     * 获取玩家能力数据包
     * @return 玩家能力数据包
     */
    public ClientboundPlayerAbilitiesPacket getPacket() {
        return new ClientboundPlayerAbilitiesPacket(invincible, canFly, isFlying, creative, flySpeed, walkSpeed);
    }
}
