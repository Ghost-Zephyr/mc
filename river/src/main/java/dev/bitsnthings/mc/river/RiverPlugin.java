package dev.bitsnthings.mc.river;

import dev.bitsnthings.mc.river.commands.*;
import dev.bitsnthings.mc.river.events.*;

import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.JsonConfiguration;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ChatColor;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.InputStream;
import java.io.File;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;
import java.util.Map;

public class RiverPlugin extends Plugin {
  private static Queue<String> discordQueue;
  private static BufferedReader minebotIn;
  private static PrintWriter minebotOut;
  private static Socket minebotSock;
  private static RiverPlugin plugin;
  public static Runnable getDiscordMessages;
  public static boolean minebotConnected;
  public static Configuration conf;
  public static Logger logger;
  @Override
  public void onEnable() {
    plugin = this;
    logger = getLogger();
    discordQueue = new LinkedList();
    loadConf();
    if (conf.getBoolean("discordIntegration")) {
      logger.info("Discord minebot integration is enabled, opening connection!");
      readyMinebotConn();
    }
    getProxy().getPluginManager().registerListener(this, new ChatMessageEvent());
    getProxy().getPluginManager().registerListener(this, new ConnectEvent());
    getProxy().getPluginManager().registerCommand(this, new TravelCommand());
  }
  @Override
  public void onDisable() {
    stopMinebotConn();
  }

  public static RiverPlugin getInstance() {
    return plugin;
  }

// Discord integration stuff
  public void readyMinebotConn() {
    connectMinebot();
    getDiscordMessages = new Runnable() {
      @Override
      public void run() {
        RiverPlugin plugin = RiverPlugin.getInstance();
        if (!minebotConnected)
          plugin.connectMinebot();
        ProxyServer proxy = getProxy();
        try {
          String message = minebotIn.readLine();
          if (message == null || message.length() == 0) {
            plugin.minebotError("Could not get latest message from minebot!");
            return;
          }
          logger.info(String.format("Got message \"%s\" from minebot!", message));
          proxy.broadcast(new ComponentBuilder(String.format("[%sDiscord%s] %s", ChatColor.BLUE, ChatColor.RESET, message)).create());
          proxy.getScheduler().schedule(plugin, plugin.getDiscordMessages, 3L, TimeUnit.SECONDS);
        } catch (IOException e) {
          minebotError(String.format("Encounteded error \"%s\" while getting latest minebot message!", e.toString()));
        }
      }
    };
    getProxy().getScheduler().runAsync(this, getDiscordMessages);
  }
  public void connectMinebot() {
    try {
      InetAddress minebot = InetAddress.getByName(conf.getString("minebotHost"));
      minebotSock = new Socket(minebot.getHostAddress(), 1337);
      minebotOut = new PrintWriter(minebotSock.getOutputStream(), true);
      minebotIn = new BufferedReader(new InputStreamReader(minebotSock.getInputStream(), "UTF8"));
      minebotConnected = true;
      logger.info("Connected to minebot.");
    } catch (Exception e) {
      minebotError(String.format("Encounteded error \"%s\" while connecting to minebot!", e.toString()));
      minebotConnected = false;
    }
  }
  public void sendMinebotMsg(String message) {
    discordQueue.add(message);
    logger.info(String.format("Sending message \"%s\" to minebot!", message));
    getProxy().getScheduler().schedule(this, new Runnable() {
      @Override
      public void run() {
        minebotOut.println(RiverPlugin.getInstance().discordQueue.poll());
      }
    }, 3L, TimeUnit.SECONDS);
  }
  public void stopMinebotConn() {
    try {
      minebotIn.close();
      minebotOut.close();
      minebotSock.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  public void minebotError(String errorMessage) {
    logger.warning(errorMessage);
    minebotConnected = false;
    try {
      getProxy().getScheduler().schedule(RiverPlugin.getInstance(), RiverPlugin.getInstance().getDiscordMessages, 3L, TimeUnit.MINUTES);
    } catch (NullPointerException e) {
      logger.warning("Unable to retry minebot connection after connection error on startup!");
    }
    logger.info("Retrying in 3 minutes.");
  }

// Config shits
  public void setDefaults() {
    conf.set("allowTravelTo", new String[]{"lobby", "survival"});
    conf.set("discordIntegration", false);
    conf.set("minebotHost", "minebot");
    //      conf.set("players", new Map.of(
    //        "b879ccdf-6408-4420-93aa-6441d5f315e8", Arrays.asList("god")
    //      ));
  }
  public void loadConf() {
    try {
      conf = ConfigurationProvider.getProvider(JsonConfiguration.class).load(new File(getDataFolder(), "config.json"));
    } catch (java.io.FileNotFoundException e) {
      getConfFile();
      try {
        conf = ConfigurationProvider.getProvider(JsonConfiguration.class).load(new File(getDataFolder(), "config.json"));
      } catch (Exception ex) {
        ex.printStackTrace();
      }
      setDefaults();
      saveConf();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  public void saveConf() {
    File file = getConfFile();
    try {
      ConfigurationProvider.getProvider(JsonConfiguration.class).save(conf, file);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  public File getConfFile() {
    if (!getDataFolder().exists()) getDataFolder().mkdir();
    File file = new File(getDataFolder(), "config.json");
    if (!file.exists()) {
      try {
        file.createNewFile();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return file;
  }
}

