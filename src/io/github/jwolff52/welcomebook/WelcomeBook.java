package io.github.jwolff52.welcomebook;

import io.github.jwolff52.welcomebook.utility.SettingsManager;
import io.github.jwolff52.welcomebook.utility.WelcomeBookListener;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;


public class WelcomeBook extends JavaPlugin {

	public final Logger logger = Logger.getLogger("Minecraft");

	public static WelcomeBook plugin;

	public WelcomeBookListener wbl;

	public static File players;

	private String identifier;

	private static PluginDescriptionFile pdf;

	private static SettingsManager sm;

	private ArrayList<String> pages = new ArrayList<String>();
	private ArrayList<String> lore = new ArrayList<String>();

	private String book_title;
	private String author;

	private ArrayList<String> hasPlayed = new ArrayList<String>();

	private ItemStack book;
	private BookMeta bm;

	private int configNumber = 1;

	@Override
	public void onEnable() {
		sm.setup(this);
		
		pdf = this.getDescription();

		wbl = new WelcomeBookListener(this);

		players = new File(getDataFolder(), "players.txt");

		identifier = sm.config.getString("new_line_identifier");

		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(wbl, this);
		
		if (!players.exists()) {
			try {
				players.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			this.logger
					.info("WelcomeBook - INFO - \"players.txt\" file created!");
		}
		try {
			setHasPlayed((ArrayList<String>) Files.readAllLines(
					players.toPath(), StandardCharsets.UTF_8));
		} catch (Exception e) {
			e.printStackTrace();
		}

		while (true) {
			if (sm.config.getString("p" + configNumber) != null
					&& !sm.config.getString("p" + configNumber)
							.equalsIgnoreCase("empty")) {
				pages.add(ChatColor.translateAlternateColorCodes('&',
						getConfig().getString("p" + configNumber)));
			} else {
				break;
			}
			configNumber++;
		}
		String temp;
		for (int x = 0; x < pages.size(); x++) {
			temp = pages.get(x);
			pages.remove(x);
			for (int y = 0; y < temp.length(); y++) {
				if (temp.charAt(y) == identifier.charAt(0)) {
					pages.add(x,
							temp.substring(0, y) + "\n" + temp.substring(y + 1));
					temp = temp.substring(y + 1);
					x++;
				}
			}
		}
		configNumber = 1;
		while (true) {
			if (sm.config.getString("l" + configNumber) != null
					&& !sm.config.getString("l" + configNumber)
							.equalsIgnoreCase("empty")) {
				lore.add(ChatColor.translateAlternateColorCodes('&',
						getConfig().getString("l" + configNumber)));
			} else {
				break;
			}
			configNumber++;
		}
		book_title = parseColors("book_title");
		author = parseColors("book_author");
		setBook(new ItemStack(Material.WRITTEN_BOOK));
		bm = (BookMeta) getBook().getItemMeta();
		bm.setDisplayName(book_title);
		bm.setPages(pages);
		bm.setAuthor(author);
		bm.setTitle(book_title);
		bm.setLore(lore);
		getBook().setItemMeta(bm);
		this.logger.info("WelcomeBook Version: " + pdf.getVersion()
				+ " has been enabled!");
	}

	public void onDisable() {
		try {
			Files.write(players.toPath(), getHasPlayed(),
					StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.logger
				.info("WelcomeBook - INFO - \"players.txt\" has been saved!");
		this.logger.info("WelcomeBook has been disabled!");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (label.equalsIgnoreCase("welcomebook")||label.equalsIgnoreCase("wb")) {
				if(args.length==0&&(player.hasPermission("wb.*")||player.hasPermission("wb.wb"))){
					player.getInventory().addItem(getBook());
				}else if(args.length==1){
					if(args[0].equalsIgnoreCase("config")){
						player.sendMessage(ChatColor.GOLD+"["+ChatColor.AQUA+"WelcomeBook"+ChatColor.GOLD+"]"+ChatColor.RED+"Usage: /wb config <add:del:list> <-p:-l> [message:number]");
					}else{
						try{
							Bukkit.getPlayer(args[0]).getInventory().addItem(getBook());
						}catch(Exception e){
							player.sendMessage(ChatColor.GOLD+"["+ChatColor.AQUA+"WelcomeBook"+ChatColor.GOLD+"]"+ChatColor.RED+"That player is not online or does not exist!");
						}
					}
				}else if(args.length==2){
					if(args[0].equalsIgnoreCase("config")){
						if(!args[1].equalsIgnoreCase("add")||!args[1].equalsIgnoreCase("del")||!args[1].equalsIgnoreCase("add")){
							player.sendMessage(ChatColor.GOLD+"["+ChatColor.AQUA+"WelcomeBook"+ChatColor.GOLD+"]"+ChatColor.RED+"Usage: /wb config <add:del:list> <-p:-l> [message:number]");
						}
					}
				}else if(args.length==4){
					if(args[0].equalsIgnoreCase("config")){
						if(!args[1].equalsIgnoreCase("add")||!args[1].equalsIgnoreCase("del")||!args[1].equalsIgnoreCase("add")){
							if(!(args[2].charAt(0)=='-')){
								player.sendMessage(ChatColor.GOLD+"["+ChatColor.AQUA+"WelcomeBook"+ChatColor.GOLD+"]"+ChatColor.RED+"Usage: /wb config <add:del:list> <-p:-l> [message:number]");
							}else{
								if((args[2].charAt(1)=='p')){
									pages.add(parseColors(args[3]));
								}
							}
						}
					}
				}
			}
		} else {
			sender.sendMessage(ChatColor.DARK_RED
					+ "You must be a player to use this command!");
		}
		return false;
	}

	public String parseColors(String temp) {
		return ChatColor.translateAlternateColorCodes('&', getConfig()
				.getString(temp));
	}

	public boolean containedInList(Player p, ArrayList<String> list) {
		for (int x = 0; x < getHasPlayed().size(); x++) {
			if (p.getName().equalsIgnoreCase(getHasPlayed().get(x)))
				return true;
		}
		return false;
	}

	public ArrayList<String> getHasPlayed() {
		return hasPlayed;
	}

	public void setHasPlayed(ArrayList<String> hasPlayed) {
		this.hasPlayed = hasPlayed;
	}

	public ItemStack getBook() {
		return book;
	}

	public void setBook(ItemStack book) {
		this.book = book;
	}
}
