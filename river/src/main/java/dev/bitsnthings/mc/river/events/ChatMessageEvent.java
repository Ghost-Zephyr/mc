package dev.bitsnthings.mc.river.events;

import dev.bitsnthings.mc.river.RiverPlugin;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.event.EventPriority;
import net.md_5.bungee.event.EventHandler;

public class ChatMessageEvent implements Listener {
  @EventHandler(priority=EventPriority.NORMAL)
  public void onPlayerChat(ChatEvent event) {
    if (RiverPlugin.conf.getBoolean("discordIntegration") && RiverPlugin.minebotConnected) {
      ProxiedPlayer player = (ProxiedPlayer) event.getSender();
      RiverPlugin.getInstance().sendMinebotMsg(String.format("<%s> %s", player.getDisplayName(), event.getMessage()));
    }
  }
}
