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

	private static PluginDescriptionFile pdf;

	private final static SettingsManager sm = SettingsManager.getInstance();

	private ArrayList<String> pages = new ArrayList<String>();
	private ArrayList<String> lore = new ArrayList<String>();

	private String book_title;
	private String author;
	private String identifier;
	private String title;

	private ArrayList<String> hasPlayed = new ArrayList<String>();

	private ItemStack book;
	private BookMeta bm;

	private int configNumber;

	@Override
	public void onEnable() {		
		sm.setup(this);
		
		pdf = getDescription();
		
		title=ChatColor.GOLD+"["+ChatColor.DARK_GRAY+"WelcomeBook"+ChatColor.GOLD+"]"+ChatColor.RESET;

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

		configNumber=1;
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
			for (int y = 0; y < temp.length(); y++) {
				if (temp.charAt(y) == identifier.charAt(0)) {
					temp=temp.substring(0, y) + "\n" + temp.substring(y + 1);
					y=0;
				}
			}
			pages.set(x, temp);
		}
		configNumber = 1;
		while (true) {
			if (sm.config.getString("l" + configNumber) != null) {
				lore.add(ChatColor.translateAlternateColorCodes('&',
						getConfig().getString("l" + configNumber)));
			} else {
				break;
			}
			configNumber++;
		}
		book_title = parseColors(sm.config.getString("book_title"));
		author = parseColors(sm.config.getString("book_author"));
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

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(cmd.getName().equals("wbgive")){
			if(args.length==0){
				if (!(sender.hasPermission("wb.self") || sender.hasPermission("wb.*"))) {
					sender.sendMessage(title+ChatColor.DARK_RED+"You do not have permission to preform this command!");
					return false;
				} else {
					if(sender instanceof Player){
						give((Player)sender);
						return true;
					}else{
						sender.sendMessage(title+ChatColor.DARK_RED+"I can't give a book to you, silly!");
						return false;
					}
				}
			}else if(args.length==1){
				if (!(sender.hasPermission("wb.other") || sender.hasPermission("wb.*"))) {
					sender.sendMessage(title+ChatColor.DARK_RED+"You do not have permission to preform this command!");
					return false;
				} else {
					Player player=Bukkit.getPlayer(args[0]);
					try{
						if(player.isOnline()){
							give(sender, player);
							return true;
						}else{
							sender.sendMessage(title+ChatColor.AQUA+player.getName()+"is not online!");
							return false;
						}
					}catch(NullPointerException e){
						sender.sendMessage(title+ChatColor.AQUA+args[0]+" does not exsist!");
						return false;
					}
				}
			}else{
				sender.sendMessage(title+ChatColor.RED+"Usage: /wbgive [other player]");
			}
		}else if(cmd.getName().equals("wbcredits")){
			if (!(sender.hasPermission("wb.credits") || sender.hasPermission("wb.*"))) {
				sender.sendMessage(title+ChatColor.DARK_RED+"You do not have permission to preform this command!");
				return false;
			} else {
				credits(sender);
				return true;
			}
		}else if(cmd.getName().equals("wbadd")){
			if(!(sender.hasPermission("wb.add") ||  sender.hasPermission("wb.*"))) {
				sender.sendMessage(title+ChatColor.DARK_RED+"You do not have permission to preform this command!");
				return false;
			}else{
				if(args.length<2){
					sender.sendMessage(title+"Usage: /wbadd <-p:-l> <message>");
					return false;
				}
				add(sender, args);
				return true;
			}
		}else if(cmd.getName().equals("wbdel")){
			if(!(sender.hasPermission("wb.del") ||  sender.hasPermission("wb.*"))) {
				sender.sendMessage(title+ChatColor.DARK_RED+"You do not have permission to preform this command!");
				return false;
			}else{
				if(args.length<2){
					sender.sendMessage(title+"Usage: /wbdel <-p:-l> <message_number>");
					return false;
				}
				try{
					del(sender, args);
					return true;
				}catch(NumberFormatException e){
					sender.sendMessage(title+"Usage: /wbdel <-p:-l> <message_number>");
					return false;
				}
			}
		}else if(cmd.getName().equals("wblist")){
			if(!(sender.hasPermission("wb.list") ||  sender.hasPermission("wb.*"))) {
				sender.sendMessage(title+ChatColor.DARK_RED+"You do not have permission to preform this command!");
				return false;
			}else{
				if(args.length==0){
					sender.sendMessage(title+ChatColor.DARK_RED+"Usage: /wblist  <-p:-l> [page]");
					return false;
				}else if(args.length==1){
					list(sender, new String[]{args[0], "1"});
					return true;
				}
				try{
					list(sender, args);
					return true;
				}catch(NumberFormatException e){
					sender.sendMessage(title+ChatColor.DARK_RED+"Usage: /wblist  <-p:-l> [page]");
					return false;
				}
			}
		}else if(cmd.getName().equals("wbreload")){
			if (!(sender.hasPermission("wb.reload") || sender.hasPermission("wb.*"))) {
				sender.sendMessage(title+ChatColor.DARK_RED+"You do not have permission to preform this command!");
				return false;
			} else {
				reload(sender);
				return true;
			}
		}
		return false;
	}

	public String parseColors(String temp) {
		return ChatColor.translateAlternateColorCodes('&', temp);
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
	
	private void give(Player player){
		player.getInventory().addItem(getBook());
	}
	
	private void give(CommandSender sender, Player player){
		player.getInventory().addItem(getBook());
		player.sendMessage(title+ChatColor.AQUA+"You were given a WelcomeBook by "+sender.getName()+"!");
		sender.sendMessage(title+ChatColor.AQUA+player.getName()+" successfully recieved the WelcomeBook!");
	}
	
	private void credits(CommandSender sender) {
		sender.sendMessage(ChatColor.GOLD + "\n======================" + ChatColor.BLUE + "\nName: WelcomeBook\nVersion: " + pdf.getVersion() + "\nDeveloper: jwolff52" + ChatColor.GOLD + "\n======================");
	}

	private void add(CommandSender sender, String[] args) {
		String temp = "";
		for (int x = 1; x < args.length - 1; x++) {
			temp += args[x] + " ";
		}
		temp += args[args.length - 1];
		if(args[0].equalsIgnoreCase("-p")){
			pages.add(parseColors(temp));
			sm.getConfig().set("p"+pages.size(), temp);
			temp=ChatColor.AQUA+"Page: \"" + ChatColor.RESET + parseColors(temp);
		}else if(args[0].equalsIgnoreCase("-l")){
			lore.add(parseColors(temp));
			sm.getConfig().set("l"+pages.size(), temp);
			temp=ChatColor.AQUA+"Lore: \"" + ChatColor.RESET + parseColors(temp);
		}
		sm.saveConfig();
		sender.sendMessage(title + temp + ChatColor.AQUA + "\" was added to the book!");
	}

	private void del(CommandSender sender, String[] args) throws NumberFormatException{
		int cNumber=Integer.valueOf(args[1]);
		String temp=null;
		if(args[0].equalsIgnoreCase("-p")){
			temp= getConfig().getString("p"+args[1]);
			for(int x=1;x<cNumber;x++){
				sm.getConfig().set("p" + x, sm.getConfig().getString("p" + x));
			}
			for(int x=cNumber;x<pages.size();x++){
				sm.getConfig().set("p"+ x, sm.getConfig().getString("p" + (x + 1)));
			}
			sm.getConfig().set("p" + pages.size() + "", null);
			temp=ChatColor.AQUA+"Page: \"" + ChatColor.RESET + parseColors(temp);
		}else if(args[0].equalsIgnoreCase("-l")){
			temp= getConfig().getString("l"+args[1]);
			for(int x=1;x<cNumber;x++){
				sm.getConfig().set("l" + x, sm.getConfig().getString("l" + x));
			}
			for(int x=cNumber;x<lore.size();x++){
				sm.getConfig().set("l" + x, sm.getConfig().getString("l" + (x + 1)));
			}
			sm.getConfig().set(lore.size() + "", null);
			temp=ChatColor.AQUA+"Lore: \"" + ChatColor.RESET + parseColors(temp);
		}
		sm.saveConfig();
		sender.sendMessage(title+ temp + ChatColor.AQUA + "\" was removed from the book!");
	}

	private void list(CommandSender sender, String[] args) throws NumberFormatException{
		if(sender instanceof Player){
			int intPage=Integer.valueOf(args[1])-1;
			String temp=args[0]+ChatColor.RED+" is not a valid identifier!\nUsage: /wblist <-p:-l> [page]";
			if(args[0].equalsIgnoreCase("-p")){
				temp = ChatColor.DARK_BLUE + "=== WelcomeBook ==Page " + (intPage + 1) +"/" + ((pages.size()/10) + 1) + "================";
				for (int x = (intPage*10)%pages.size(); x < (intPage*10)+10 && x < pages.size(); x++) {
					temp += "\n" + ChatColor.GREEN + "[" + (x + 1) + "] " + ChatColor.RESET + parseColors(sm.getConfig().getString("p" + (x + 1) + ""));
				}
			}else if(args[0].equalsIgnoreCase("-l")){
				temp = ChatColor.DARK_BLUE + "=== WelcomeBook ==Page " + (intPage + 1) +"/" + ((lore.size()/10) + 1) + "================";
				for (int x = (intPage*10)%lore.size(); x < (intPage*10)+10 && x < lore.size(); x++) {
					temp += "\n" + ChatColor.GREEN + "[" + (x + 1) + "] " + ChatColor.RESET + parseColors(sm.getConfig().getString("l" + (x + 1) + ""));
				}
			}
			sender.sendMessage(temp);
		}else{
			String temp=args[0]+ChatColor.RED+" is not a valid identifier!\nUsage: /wblist <-p:-l> [page]";
			if(args[0].equalsIgnoreCase("-p")){
				temp = ChatColor.DARK_BLUE + "=== WelcomeBook =========================";
				for (int x = 0; x < pages.size(); x++) {
					temp += "\n" + ChatColor.GREEN + "[" + (x + 1) + "] " + ChatColor.RESET + parseColors(sm.getConfig().getString("p" + (x + 1) + ""));
				}
			}else if(args[0].equalsIgnoreCase("-l")){
				temp = ChatColor.DARK_BLUE + "=== WelcomeBook =========================";
				for (int x = 0; x < pages.size(); x++) {
					temp += "\n" + ChatColor.GREEN + "[" + (x + 1) + "] " + ChatColor.RESET + parseColors(sm.getConfig().getString("l" + (x + 1) + ""));
				}
			}
			sender.sendMessage(temp);
		}
	}
	
	private void reload(CommandSender sender) {
		sm.reloadConfig();
		identifier = sm.config.getString("new_line_identifier");
		
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

		configNumber=1;
		pages = new ArrayList<String>();
		while (true) {
			if (sm.config.getString("p" + configNumber) != null) {
				pages.add(ChatColor.translateAlternateColorCodes('&', getConfig().getString("p" + configNumber)));
			} else {
				break;
			}
			configNumber++;
		}
		String temp;
		for (int x = 0; x < pages.size(); x++) {
			temp = pages.get(x);
			for (int y = 0; y < temp.length(); y++) {
				if (temp.charAt(y) == identifier.charAt(0)) {
					temp=temp.substring(0, y) + "\n" + temp.substring(y + 1);
					y=0;
				}
			}
			pages.set(x, temp);
		}
		
		configNumber = 1;
		lore = new ArrayList<String>();
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
		book_title = parseColors(sm.config.getString("book_title"));
		author = parseColors(sm.config.getString("book_author"));
		setBook(new ItemStack(Material.WRITTEN_BOOK));
		bm = (BookMeta) getBook().getItemMeta();
		bm.setDisplayName(book_title);
		bm.setPages(pages);
		bm.setAuthor(author);
		bm.setTitle(book_title);
		bm.setLore(lore);
		getBook().setItemMeta(bm);
		sender.sendMessage(title+ChatColor.AQUA + "WelcomeBook configuration successfully reloaded!!");
	}
}
