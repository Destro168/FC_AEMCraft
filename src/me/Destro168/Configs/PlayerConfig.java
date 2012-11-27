package me.Destro168.Configs;

import java.util.Date;

import me.Destro168.ConfigManagers.ConfigGod;
import me.Destro168.FC_AEMCraft.FC_AEMCraft;
import me.Destro168.TimeUtils.DateManager;

public class PlayerConfig extends ConfigGod
{
	public Date logonDate = new Date();
	
	public int getPlayTime() { return ccm.getInt(prefix + "playTime"); }
	public int getChatLines() { return ccm.getInt(prefix + "chatLines"); }
	
	public void addChatLine()
	{
		int lines;
		try { lines = getChatLines() + 1; } catch (NullPointerException e) { lines = 1; }
		ccm.set(prefix + "chatLines", lines);
	}
	
	public PlayerConfig(String playerName) 
	{
		super(FC_AEMCraft.plugin.getDataFolder().getAbsolutePath() + "/userinfo", playerName);
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
		ccm.set(prefix + "playTime", timePlayedInSeconds); //Store it
		
		//Update the logon date to now.
		logonDate = new Date(); //Update for future time player updates.
	}
	

	public String getPlayTimeNormal() 
	{ 
		DateManager dm = new DateManager();
		return dm.getTimeStringFromTimeInteger(ccm.getInt(prefix + "playTime"));
	}
}
