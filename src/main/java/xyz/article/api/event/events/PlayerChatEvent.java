package xyz.article.api.event.events;

import xyz.article.api.entities.player.Player;
import xyz.article.api.event.Event;

public class PlayerChatEvent extends Event {
    private final Player player;
    private String message;

    public PlayerChatEvent(Player player, String message) {
        this.player = player;
        this.message = message;
    }

    public Player getPlayer() {
        return player;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
