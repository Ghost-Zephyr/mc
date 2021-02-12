package dev.bitsnthings.mc.river.commands;

import dev.bitsnthings.mc.river.RiverPlugin;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ChatColor;

import java.util.List;

public class TravelCommand extends Command {
  public TravelCommand() {
    super("travel", "river.command.travel");
  }
  @Override
  public void execute(CommandSender sender, String[] args) {
    if (args.length != 1) {
      error(sender, "Usage: /travel <destination>");
      return;
    }
    if (!(sender instanceof ProxiedPlayer)) {
      error(sender, "Can't send the console to a server!");
      return;
    }
    ProxiedPlayer player = (ProxiedPlayer) sender;
    if (player.getServer().getInfo().getName().equalsIgnoreCase(args[0])) {
      error(sender, "You are already in the %s server!", args[0]);
      return;
    }
    if (RiverPlugin.conf.getList("allowTravelTo").contains(args[0]))
      player.connect(ProxyServer.getInstance().getServerInfo(args[0]));
    else error(sender, "Insufficent permissions or non existent server!");
  }
  private void error(CommandSender sender, String formatString, String... args) {
    sender.sendMessage(new ComponentBuilder(
      String.format(formatString, args))
        .color(ChatColor.RED).create());
  }
}
