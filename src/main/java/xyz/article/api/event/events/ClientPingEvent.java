package xyz.article.api.event.events;

import net.kyori.adventure.text.Component;
import org.geysermc.mcprotocollib.protocol.data.status.PlayerInfo;
import org.geysermc.mcprotocollib.protocol.data.status.VersionInfo;
import xyz.article.api.event.Event;

/**
 * 在玩家ping服务器时触发
 */
public class ClientPingEvent extends Event {
    public Component motd;
    public PlayerInfo playerInfo;
    public VersionInfo versionInfo;
    public byte[] icon;
    public boolean enforcesSecureChat;
    public ClientPingEvent (Component motd, PlayerInfo playerInfo, VersionInfo versionInfo, byte[] icon, boolean enforcesSecureChat) {
        this.motd = motd;
        this.playerInfo = playerInfo;
        this.versionInfo = versionInfo;
        this.icon = icon;
        this.enforcesSecureChat = enforcesSecureChat;
    }

    public void setMotd (Component motd) {
        this.motd = motd;
    }

    public void setPlayerInfo (PlayerInfo playerInfo) {
        this.playerInfo = playerInfo;
    }

    public void setVersionInfo (VersionInfo versionInfo) {
        this.versionInfo = versionInfo;
    }

    public void setIcon (byte[] icon) {
        this.icon = icon;
    }

    public void setEnforcesSecureChat (boolean enforcesSecureChat) {
        this.enforcesSecureChat = enforcesSecureChat;
    }

    public Component getMotd () {
        return motd;
    }

    public PlayerInfo getPlayerInfo () {
        return playerInfo;
    }

    public VersionInfo getVersionInfo () {
        return versionInfo;
    }

    public byte[] getIcon () {
        return icon;
    }

    public boolean getEnforcesSecureChat () {
        return enforcesSecureChat;
    }
}
