package me.Destro168.Util;

import org.bukkit.entity.Player;

import me.Destro168.FC_Suite_Shared.PermissionManager;

public class AEMCraftPermissions extends PermissionManager
{
	public AEMCraftPermissions(Player player) {
		super(player);
	}
	public AEMCraftPermissions(boolean isConsole_) {
		super(isConsole_);
	}
	
	public boolean isAdmin()
	{
		if (isGlobalAdmin() == true)
			return true;
		
		if (permission.playerHas(player, "FC_AEMCraft.admin"))
			return true;
		
		return isConsole;
	}
	
	public boolean canTrack()
	{
		if (permission.playerHas(player, "FC_AEMCraft.track"))
			return true;
		
		return isConsole;
	}
	
	public boolean canBeRewarded()
	{
		if (isAdmin() == true)
			return true;
		
		else if (permission.has(player, "FC_AEMCraft.reward"))
			return true;
		
		return isConsole;
	}
}
