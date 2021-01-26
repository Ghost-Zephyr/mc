package dev.bitsnthings.mc.blank;

import org.bukkit.plugin.java.JavaPlugin;

public class BlankPlugin extends JavaPlugin {
  @Override
  public void onEnable() {
    this.getCommand("blank").setExecutor(new BlankCommand());
  }
  @Override
  public void onDisable() {

  }
}

