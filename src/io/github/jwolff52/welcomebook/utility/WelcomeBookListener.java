package io.github.jwolff52.welcomebook.utility;

import io.github.jwolff52.welcomebook.WelcomeBook;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.logging.Logger;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;


public class WelcomeBookListener implements Listener{
public static WelcomeBook plugin;
	
	public WelcomeBookListener(WelcomeBook instance){
		plugin=instance;
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e){
		if(!plugin.containedInList(e.getPlayer(), plugin.getHasPlayed())){
			e.getPlayer().getInventory().addItem(plugin.getBook());
			plugin.getHasPlayed().add(e.getPlayer().getName());
			try {
				Files.write(WelcomeBook.players.toPath(), plugin.getHasPlayed(), StandardCharsets.UTF_8);
			} catch (IOException e1) {
				Logger.getLogger("Minecraft").severe(e1.toString());
			}
			Logger.getLogger("Minecraft").info("WelcomeBook - INFO - "+e.getPlayer().getName()+" given book and added to \"players.txt\"!");
		}
	}
}
