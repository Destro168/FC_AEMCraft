package me.Destro168.Configs;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Location;

import me.Destro168.ConfigManagers.ConfigGod;
import me.Destro168.ConfigManagers.CustomConfigurationManager;
import me.Destro168.FC_AEMCraft.FC_AEMCraft;

public class MiningConfig extends ConfigGod
{
	public Map<Integer, Double> harvestableValues = new HashMap<Integer, Double>();
	public Map<Integer, Integer> harvestableVisible = new HashMap<Integer, Integer>();
	public double minimum;
	private Calendar cal = new GregorianCalendar();
	private String loggingPrefix = "Logging.";
	CustomConfigurationManager lccm;
	
	public void setExploitPrevention(boolean x) { ccm.set(prefix + "exploitPrevention", x); }
	public void setLogCheckTime(int x) { ccm.set(prefix + "logCheckTime", x); }
	public void setBlockValueMinimum(double x) { ccm.set(prefix + "blockValueMinimum", x); }
	public void setHarvestableFlux(int x) { ccm.set(prefix + "harvestableFlux", x); }
	public void setHarvestableIDs(List<?> x) { ccm.setCustomList(prefix + "harvestable.IDs", x); }
	public void setHarvestableValues(List<?> x) { ccm.setCustomList(prefix + "harvestable.Values", x); }
	public void setHarvestableVisible(List<?> x) { ccm.setCustomList(prefix + "harvestable.Visible", x); }
	
	public boolean getExploitPrevention() { return ccm.getBoolean(prefix + "exploitPrevention"); }
	public int getLogCheckTime() { return ccm.getInt(prefix + "logCheckTime"); }
	public double getBlockValueMinimum() { return ccm.getDouble(prefix + "blockValueMinimum"); }
	public int getHarvestableFlux() { return ccm.getInt(prefix + "harvestableFlux"); }
	
	public String getHarvestableIDs() { return ccm.getString(prefix + "harvestable.IDs"); }
	public String getHarvestableValues() { return ccm.getString(prefix + "harvestable.Values"); }
	public String getHarvestableVisible() { return ccm.getString(prefix + "harvestable.Visible"); }
	
	public List<Integer> getHarvestableIDsIL() { return converter.getIntegerListFromString(getHarvestableIDs()); }
	public List<Double> getHarvestableValuesIL() { return converter.getDoubleListFromString(getHarvestableValues()); }
	public List<Integer> getHarvestableVisibleIL() { return converter.getIntegerListFromString(getHarvestableVisible()); }
	
	public void logBlock(int matID, Location blockLocation)
	{
		if (blockLocation == null)
			return;
		
		if (getExploitPrevention() == false)
			return;
		
		if (!harvestableValues.containsKey(matID))
			return;
		
		if (harvestableValues.get(matID) < minimum)
			return;
		
		int day = cal.get(Calendar.DAY_OF_YEAR);
		int count = 0;
		
		try
		{
			while (lccm.getLocation(loggingPrefix + String.valueOf(day) + "." + matID + "." + count) != null)
				count++;
		} catch (NullPointerException e) { }
		
		lccm.setLocation(loggingPrefix + String.valueOf(day) + "." + matID + "." + count, blockLocation.getWorld().getName(), blockLocation.getX(), blockLocation.getY(), blockLocation.getZ(),0,0);
	}
	
	public boolean exploitCheck(int blockMatID, Location blockLoc)
	{
		Calendar cal = new GregorianCalendar();
		int day = cal.get(Calendar.DAY_OF_YEAR);
		
		List<Location> pastBreaks = new ArrayList<Location>();
		
		for (int i = day; i > day - getLogCheckTime(); i--)
		{
			pastBreaks = getBlockLocationsByDay(blockMatID, i);
			
			if (pastBreaks.contains(blockLoc))
				return false;
		}
		
		return true;
	}
	
	private List<Location> getBlockLocationsByDay(int id, int day)
	{
		List<Location> a = new ArrayList<Location>(); //today
		Location loc;
		
		for (int j = 0; j < 999999; j++)
		{
			try
			{
				loc = lccm.getLocation(loggingPrefix + String.valueOf(day) + "." + id + "." + j);
			} catch (NullPointerException e) {
				break;
			}
			
			if (loc != null)
				a.add(loc);
			else
				break;
		}
		
		return a;
	}
	
	public void clearOldLogs()
	{
		int day = cal.get(Calendar.DAY_OF_YEAR);
		
		for (int i = 1; i < 366; i++)
		{
			if (i > day || i <= day - getLogCheckTime())
			{
				lccm.set(loggingPrefix + i, null);
			}
		}
	}
	
	public MiningConfig()
	{
		super(FC_AEMCraft.plugin.getDataFolder().getAbsolutePath(), "Settings");
		
		lccm = new CustomConfigurationManager(FC_AEMCraft.plugin.getDataFolder().getAbsolutePath(), "Logging");
		
		handleDefaults();
	}
	
	public void handleDefaults()
	{
		if (getVersion() < 1.0)
		{
			setVersion(1.0);
			
			List<String> ids = new ArrayList<String>();
			ids.add("1");
			ids.add("56");
			setHarvestableIDs(ids);
			
			List<String> values = new ArrayList<String>();
			values.add("3");
			values.add("8");
			setHarvestableValues(values);
			
			List<String> visible = new ArrayList<String>();
			visible.add("0");
			visible.add("1");
			setHarvestableVisible(visible);
			
			setHarvestableFlux(500);
			setBlockValueMinimum(5);
			setExploitPrevention(true);
			setLogCheckTime(3);
		}
		
		if (getVersion() < 1.4)
		{
			setVersion(1.4);
		}
		
		loadSettings();
	}
	
	public double getOreReward(int type) 
	{
		if (!harvestableValues.containsKey(type))
			return -1;
		
		Random rand = new Random();
		double value = harvestableValues.get(type);
		double flux = getHarvestableFlux();
		
		if (flux > 0)
		{
			boolean isNegative = rand.nextBoolean();
			
			flux = rand.nextInt(getHarvestableFlux()); 
			flux = flux / 100;
			
			//Have a chance at negativity.
			if (isNegative == true)
				flux = flux * -1;
			
			value = value + (value * (flux * .01));
		}
		
		return value;
	}
	
	private void loadSettings() 
	{
		List<Integer> ids = getHarvestableIDsIL();
		List<Double> values = getHarvestableValuesIL();
		List<Integer> visible = getHarvestableVisibleIL();
		
		for (int i = 0; i < ids.size(); i++)
		{
			harvestableValues.put(ids.get(i), values.get(i));
			harvestableVisible.put(ids.get(i), visible.get(i));
		}
		
		minimum = getBlockValueMinimum();
	}
}




