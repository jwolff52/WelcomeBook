package com.github.jwolff52.welcomebook;

import org.bukkit.permissions.Permission;

public class Permissions{
	public Permission canPreformAll=new Permission("wb.*");
	
	public Permission canPreformWb=new Permission("wb.wb");
	public Permission canPreformAdd=new Permission("wb.config.add");
	public Permission canPreformDel=new Permission("wb.config.del");
}
