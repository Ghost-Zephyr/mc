package dev.bitsnthings.mc.blank;

import org.bukkit.command.*;

public class BlankCommand implements CommandExecutor {
  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    sender.sendMessage("Blank command from BlankPlugin executed!");
    return true;
  }
}
