package xyz.article.api.entities.player;

import org.geysermc.mcprotocollib.auth.GameProfile;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.packet.Packet;
import xyz.article.api.inventory.PlayerInventory;

public class Player {
    private final Session session;
    private final GameProfile profile;
    private PlayerInventory inventory;

    public Player(Session session, GameProfile profile, PlayerInventory playerInventory) {
        this.session = session;
        this.profile = profile;
        this.inventory = playerInventory;
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
}
