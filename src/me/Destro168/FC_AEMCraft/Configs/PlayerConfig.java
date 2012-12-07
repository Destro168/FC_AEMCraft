package me.Destro168.FC_AEMCraft.Configs;

import java.util.Date;

import me.Destro168.FC_Suite_Shared.ConfigManagers.ConfigGod;
import me.Destro168.FC_Suite_Shared.ConfigManagers.FileConfigurationWrapper;
import me.Destro168.FC_AEMCraft.FC_AEMCraft;
import me.Destro168.FC_Suite_Shared.Leaderboards.Leaderboard;
import me.Destro168.FC_Suite_Shared.TimeUtils.DateManager;

public class PlayerConfig extends ConfigGod
{
	private String playerName;
	
	public Date logonDate = new Date();
	
	public int getPlayTime() { return fcw.getInt(prefix + "playTime"); }
	public int getChatLines() { return fcw.getInt(prefix + "chatLines"); }
	
	public PlayerConfig(String playerName_) 
	{
		super(FC_AEMCraft.plugin.getDataFolder().getAbsolutePath() + "/userinfo", playerName_);
		playerName = playerName_;
	}
	
	public void addChatLine()
	{
		int lines;
		try { lines = getChatLines() + 1; } catch (NullPointerException e) { lines = 1; }
		fcw.set(prefix + "chatLines", lines);
	}
	
	public void storePlayedTime()
	{
		//Variable Declarations
		Date now = new Date();
		Long timeDifference;
		int intDifference;
		int timePlayedInSeconds = getPlayTime();
		
		//We want to first set how long we have played in the configuration file.
		timeDifference = now.getTime() - logonDate.getTime(); //Calulate how long we have been online.
		intDifference = (int) (timeDifference / 1000); //Convert that time to seconds.
		timePlayedInSeconds = timePlayedInSeconds + intDifference;	//Update time played.
		fcw.set(prefix + "playTime", timePlayedInSeconds); //Store it
		
		//Update the logon date to now.
		logonDate = new Date(); //Update for future time player updates.
		
		//Update leaderboard for most played.
		FileConfigurationWrapper fcw = new FileConfigurationWrapper(FC_AEMCraft.plugin.getDataFolder().getAbsolutePath(), "Leaderboards");
		Leaderboard lb = new Leaderboard(fcw, "LongestPlayed", "Most Active", "seconds");
		lb.attemptUpdate(playerName, getPlayTime());
	}
	
	public String getPlayTimeNormal() 
	{ 
		DateManager dm = new DateManager();
		return dm.getTimeStringFromTimeInteger(fcw.getInt(prefix + "playTime"));
	}
}
