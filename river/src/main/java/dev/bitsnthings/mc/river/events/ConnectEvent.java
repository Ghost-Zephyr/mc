package dev.bitsnthings.mc.river.events;

import dev.bitsnthings.mc.river.RiverPlugin;

import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventPriority;
import net.md_5.bungee.event.EventHandler;

public class ConnectEvent implements Listener {
  @EventHandler(priority=EventPriority.NORMAL)
  public void onHandshakeEvent(ServerConnectEvent event) {
    if (RiverPlugin.conf.getBoolean("discordIntegration")) {
      ProxiedPlayer player = event.getPlayer();
      ServerInfo target = event.getTarget();
      RiverPlugin.getInstance().sendMinebotMsg(String.format("%s joined %s!", player.getDisplayName(), target.getName()));
    }
  }
}
