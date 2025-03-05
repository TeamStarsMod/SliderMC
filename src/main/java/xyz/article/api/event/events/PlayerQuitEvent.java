package xyz.article.api.event.events;

import net.kyori.adventure.text.Component;
import xyz.article.api.entities.player.Player;
import xyz.article.api.event.Event;

public class PlayerQuitEvent extends Event {
    private final Player player;
    private Component quitMessage;

    public PlayerQuitEvent(Player player, Component joinMessage) {
        this.player = player;
        this.quitMessage = joinMessage;
    }

    public Player getPlayer() {
        return player;
    }

    public Component getQuitMessage() {
        return quitMessage;
    }

    public void setQuitMessage(Component joinMessage) {
        this.quitMessage = joinMessage;
    }
}
