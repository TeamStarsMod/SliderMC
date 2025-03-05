package xyz.article.api.event.events;

import net.kyori.adventure.text.Component;
import xyz.article.api.entities.player.Player;
import xyz.article.api.event.Event;

public class PlayerJoinEvent extends Event {
    private final Player player;
    private Component joinMessage;

    public PlayerJoinEvent(Player player, Component joinMessage) {
        this.player = player;
        this.joinMessage = joinMessage;
    }

    public Player getPlayer() {
        return player;
    }

    public Component getJoinMessage() {
        return joinMessage;
    }

    public void setJoinMessage(Component joinMessage) {
        this.joinMessage = joinMessage;
    }
}
