package me.Destro168.FC_AEMCraft;

import java.util.List;

import me.Destro168.FC_AEMCraft.Configs.PlayerConfig;
import me.Destro168.FC_Suite_Shared.ArgParser;
import me.Destro168.FC_Suite_Shared.NameMatcher;
import me.Destro168.FC_Suite_Shared.ConfigManagers.FileConfigurationWrapper;
import me.Destro168.FC_Suite_Shared.Leaderboards.Leaderboard;
import me.Destro168.FC_Suite_Shared.Messaging.MessageLib;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.command.ColouredConsoleSender;
import org.bukkit.entity.Player;

public class AemCE implements CommandExecutor
{
	private CommandSender sender;
	private ColouredConsoleSender console;
	private Player player;
	private ArgParser ap;
	private MessageLib msgLib;
	private AEMCraftPermissions perms;
	NameMatcher nm = new NameMatcher();
	
	@Override
	public boolean onCommand(CommandSender sender_, Command cmd, String commandLabel, String[] args)
	{
		//Variable Declarations/Initializations
		ap = new ArgParser(args);
		sender = sender_;
		
		//Assign key variables based on command input and arguments.
		if (sender instanceof Player)
		{
			player = (Player) sender;
			msgLib = new MessageLib(player);
			perms = new AEMCraftPermissions(player);
		}
		else if (sender instanceof ColouredConsoleSender)
		{
			console = (ColouredConsoleSender) sender;
			msgLib = new MessageLib(console);
			perms = new AEMCraftPermissions(true);
		}
		else
		{
			FC_AEMCraft.plugin.getLogger().info("Unknown command sender, returning track command.");
			return false;
		}
		
		if (ap.getArg(0).equalsIgnoreCase("track") || ap.getArg(0).equalsIgnoreCase("tracker"))
			return commandTrack();
		else
		{
			if (ap.getArg(0).equalsIgnoreCase("mostPlayed"))
				return commandMostPlayed();
			else if (ap.getArg(0).equalsIgnoreCase("mostChatted"))
				return commandMostChatted();
			
			if (perms.isAdmin() == false)
				return commandHelp();
			
			if (ap.getArg(0).equalsIgnoreCase("mList"))
				return commandMList();
			else if (ap.getArg(0).equalsIgnoreCase("mEP"))
				return commandMEP();
			else if (ap.getArg(0).equalsIgnoreCase("mMin"))
				return commandMMin();
			else if (ap.getArg(0).equalsIgnoreCase("mFlux"))
				return commandMFlux();
			else if (ap.getArg(0).equalsIgnoreCase("mDel"))
				return commandMDel();
			else if (ap.getArg(0).equalsIgnoreCase("mAdd"))
				return commandMAdd();
		}
		
		return commandHelp();
	}
	
	private boolean commandMostPlayed()
	{
		//Requires track permission to use.
		if (perms.canTrack() == false)
			return msgLib.errorNoPermission();
		
		//Update leaderboard
		FileConfigurationWrapper fcw = new FileConfigurationWrapper(FC_AEMCraft.plugin.getDataFolder().getAbsolutePath(), "Leaderboards");
		Leaderboard lb = new Leaderboard(fcw, "LongestPlayed", "Most Active", "seconds");
		lb.displayLeaderboard(msgLib);
		return true;
	}
	
	private boolean commandMostChatted()
	{
		//Requires track permission to use.
		if (perms.canTrack() == false)
			return msgLib.errorNoPermission();
		
		//Update leaderboard
		FileConfigurationWrapper fcw = new FileConfigurationWrapper(FC_AEMCraft.plugin.getDataFolder().getAbsolutePath(), "Leaderboards");
		Leaderboard lb = new Leaderboard(fcw, "MostChatLines", "Most Chat Lines", "lines");
		lb.displayLeaderboard(msgLib);
		return true;
	}
	
	private boolean commandTrack()
	{
		//Variable Declaration
		String target = "";
		
		//If no permission, then return false.
		if (perms.canTrack() == false)
			return msgLib.errorNoPermission();
		
		//Set sender name
		if (ap.getArg(1).equals(""))
			target = sender.getName();
		else
			target = nm.getNameByMatch(ap.getArg(1));
		
		PlayerConfig pc = null;
		
		if (Bukkit.getServer().getPlayer(target) != null)
		{
			Player playerTarget = Bukkit.getServer().getPlayer(target);
			
			if (playerTarget.isOnline())
			{
				pc = FC_AEMCraft.playerConfigMap.get(playerTarget);
				pc.storePlayedTime();
			}
			else
				pc = new PlayerConfig(target);
		}
		else
			pc = new PlayerConfig(target);
		
		String[] msg = new String[4];
		
		msg[0] = "&p" + target + "&p" + " - PT: ";
		msg[1] = pc.getPlayTimeNormal();
		msg[2] = " LC: ";
		msg[3] = String.valueOf(pc.getChatLines());
		
		msgLib.standardMessage(msg);
		
		return true;
	}
	
	private boolean commandMList()
	{
		String ep = "";
		
		msgLib.standardHeader("Harvesting Information");
		
		if (FC_AEMCraft.mc.getExploitPrevention() == true)
			ep = "Enabled";
		else
			ep = "Disabled";
		
		msgLib.standardMessage("Exploit Checking Status: ",ep);
		msgLib.standardMessage("Exploit Prevention And Mineral Logging Minimum","&q" + FC_AEMCraft.mc.getBlockValueMinimum() + "&q");
		msgLib.standardMessage("Harvest Reward Fluctuation",FC_AEMCraft.mc.getHarvestableFlux() + "");
		msgLib.standardMessage("Material IDs", FC_AEMCraft.mc.getHarvestableIDs());
		msgLib.standardMessage("Material Values", FC_AEMCraft.mc.getHarvestableValues());
		msgLib.standardMessage("Material Visibility", FC_AEMCraft.mc.getHarvestableVisible());
		
		return true;
	}
	
	private boolean commandMEP()
	{
		boolean isTrue = false;
		
		if (ap.getArg(0).equals("t") || ap.getArg(0).equals("true"))
			isTrue = true;
		else if (!ap.getArg(0).equals("f") && !ap.getArg(0).equals("false"))
			isTrue = false;
		else if (ap.getArg(0).equals("") || ap.getArg(0).equals("toggle"))
		{
			if (FC_AEMCraft.mc.getExploitPrevention() == false)
				isTrue = true;
		}
		
		FC_AEMCraft.mc.setExploitPrevention(isTrue);
		
		return msgLib.standardMessage("Successfully set exploit prevention to: " + isTrue);
	}
	
	private boolean commandMMin()
	{
		if (ap.getArg(0).equals(""))
			return msgLib.errorInvalidCommand();
		
		double newMin = 0;
		
		try { newMin = Double.valueOf(ap.getArg(1)); } catch (NumberFormatException e) { return msgLib.errorInvalidCommand(); }
		
		FC_AEMCraft.mc.setBlockValueMinimum(newMin);
		
		return msgLib.successCommand();
	}
	
	private boolean commandMFlux()
	{
		if (ap.getArg(0).equals(""))
			return msgLib.errorInvalidCommand();
		
		int newFlux = 0;
		
		try { newFlux = Integer.valueOf(ap.getArg(1)); } catch (NumberFormatException e) { return msgLib.errorInvalidCommand(); }
		
		FC_AEMCraft.mc.setHarvestableFlux(newFlux);
		
		return msgLib.successCommand();
	}
	
	private boolean commandMAdd()
	{
		if (ap.getArg(1).equals("") || ap.getArg(2).equals("") || ap.getArg(3).equals(""))
			return commandHelp();
		
		//Variable Declarations
		List<Integer> ids = FC_AEMCraft.mc.getHarvestableIDsIL();
		List<Double> values = FC_AEMCraft.mc.getHarvestableValuesIL();
		List<Integer> visible = FC_AEMCraft.mc.getHarvestableVisibleIL();
		
		int newID = 0;
		double newValue = 0;
		int newVisible = 0;
		boolean isContained = false;
		
		try { newID = Integer.valueOf(ap.getArg(1)); } catch (NumberFormatException e) { return msgLib.errorInvalidCommand(); }
		try { newValue = Double.valueOf(ap.getArg(2)); } catch (NumberFormatException e) { return msgLib.errorInvalidCommand(); }
		try { newVisible = Integer.valueOf(ap.getArg(3)); } catch (NumberFormatException e) { return msgLib.errorInvalidCommand(); }
		
		for (int i = 0; i < ids.size(); i++)
		{
			if (ids.get(i) == newID)
			{
				isContained = true;
				
				ids.set(i, newID);
				values.set(i, newValue);
				visible.set(i, newVisible);
				
				break;
			}
		}
		
		if (isContained == false)
		{
			ids.add(newID);
			values.add(newValue);
			visible.add(newVisible);
		}
		
		FC_AEMCraft.mc.setHarvestableIDs(ids);
		FC_AEMCraft.mc.setHarvestableValues(values);
		FC_AEMCraft.mc.setHarvestableVisible(visible);
		
		if (!FC_AEMCraft.mc.harvestableValues.containsKey(newID))
			FC_AEMCraft.mc.harvestableValues.put(newID, newValue);
		
		if (!FC_AEMCraft.mc.harvestableVisible.containsKey(newID))
			FC_AEMCraft.mc.harvestableVisible.put(newID, newVisible);
		
		return msgLib.successCommand();
	}
	
	private boolean commandMDel()
	{
		if (ap.getArg(1).equals(""))
			return commandHelp();
		
		List<Integer> ids = FC_AEMCraft.mc.getHarvestableIDsIL();
		List<Double> values = FC_AEMCraft.mc.getHarvestableValuesIL();
		List<Integer> visible = FC_AEMCraft.mc.getHarvestableVisibleIL();
		
		int newID = 0;
		
		try { newID = Integer.valueOf(ap.getArg(1)); } catch (NumberFormatException e) { return msgLib.errorInvalidCommand(); }
		
		for (int i = 0; i < ids.size(); i++)
		{
			if (ids.get(i) == newID)
			{
				ids.remove(i);
				values.remove(i);
				visible.remove(i);
				
				break;
			}
		}
		
		FC_AEMCraft.mc.setHarvestableIDs(ids);
		FC_AEMCraft.mc.setHarvestableValues(values);
		FC_AEMCraft.mc.setHarvestableVisible(visible);

		FC_AEMCraft.mc.harvestableValues.remove(newID);
		FC_AEMCraft.mc.harvestableVisible.remove(newID);
		
		return msgLib.successCommand();
	}
	
	private boolean commandHelp()
	{
		boolean showOnce = false;
		String header = "AEMCraft Custom Plugin Help";
		
		if (perms.canTrack() == true)
		{
			showOnce = true;
			msgLib.standardHeader(header);
			msgLib.standardMessage("/aem [track/tracker]", "See player lines of chat and time played.");
			msgLib.standardMessage("/aem mostPlayed", "See top most active players.");
			msgLib.standardMessage("/aem mostChatted", "See top most chatty players.");
		}
		
		if (perms.isAdmin() == false)
		{
			if (perms.canTrack() == false)
				msgLib.errorNoPermission();
			
			return true;
		}
		
		if (showOnce == false)
			msgLib.standardHeader(header);
		
		msgLib.standardMessage("/aem mList", "See information regarding minable minerals.");
		msgLib.standardMessage("/aem mEP [true/false]", "Toggle exploit prevention setting.");
		msgLib.standardMessage("/aem mMin [new val]", "Change exploit prevention and Material logging minimum value.");
		msgLib.secondaryMessage("The new value here is simply a number like XXX.XX");
		msgLib.standardMessage("/aem mFlux [new val]", "Change random fluctuation while mining.");
		msgLib.secondaryMessage("Remember flux is XXX.XX%, but without the decimal or % sign.");
		msgLib.standardMessage("/aem mAdd [id] [value] [visibility]", "Add a new material to be harvested. Can also be used to overwrite and edit existing information. " +
				"Visibility as 0 is false, as 1 = true. Visibility determines if a material is logged and displayed to harvester on break.");
		msgLib.standardMessage("/aem mDel [id]", "Remove a material by id.");
		
		return true;
	}
}











