package me.Destro168.FC_AEMCraft;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import me.Destro168.FC_AEMCraft.Configs.MiningConfig;
import me.Destro168.FC_AEMCraft.Configs.PlayerConfig;
import me.Destro168.FC_Suite_Shared.AutoUpdate;
import me.Destro168.FC_Suite_Shared.ConfigManagers.FileConfigurationWrapper;
import me.Destro168.FC_Suite_Shared.Leaderboards.Leaderboard;
import me.Destro168.FC_Suite_Shared.Messaging.MessageLib;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class FC_AEMCraft extends JavaPlugin
{
	public static final DecimalFormat df = new DecimalFormat("#.#");

	public static FC_AEMCraft plugin;
	public static Economy economy;
	public static MiningConfig mc;
	public static Map<Player, Long> lastPayNotificationMap = new HashMap<Player, Long>();
	public static Map<Player, PlayerConfig> playerConfigMap = new HashMap<Player, PlayerConfig>();
	
	private AemCE aemCE;
	
	@Override
	public void onDisable()
	{
		for (Player player : Bukkit.getServer().getOnlinePlayers())
			playerConfigMap.get(player).storePlayedTime();
		
		FC_AEMCraft.plugin.getLogger().info("Successfully Disabled.");
	}
	
	@Override
	public void onEnable()
	{
		//Set the plugin to this.
		plugin = this;
		
		//Setup the economy.
		setupEconomy();
		
		//Register listeners
		Bukkit.getServer().getPluginManager().registerEvents(new PlayerListener(), plugin);
		getServer().getPluginManager().registerEvents(new BlockListener(), plugin);
		
		//Register commands.
		aemCE = new AemCE();
		getCommand("aem").setExecutor(aemCE);
		
		for (Player player : Bukkit.getServer().getOnlinePlayers())
			playerConfigMap.put(player, new PlayerConfig(player.getName()));
		
		mc = new MiningConfig();
		
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable()
		{
			public void run()
			{
				mc.clearOldLogs();	//Perform log clear checking every day.
			}
		}, 20, 1728000);
		
		try {
			new AutoUpdate(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		FC_AEMCraft.plugin.getLogger().info("Successfully Enabled.");
	}
	
	public class PlayerListener implements Listener
	{
		@EventHandler
		public void onPlayerJoin(PlayerJoinEvent event)
		{
			playerConfigMap.put(event.getPlayer(), new PlayerConfig(event.getPlayer().getName()));
		}
		
		@EventHandler
		public void onPlayerQuit(PlayerQuitEvent event)
		{
			playerConfigMap.get(event.getPlayer()).storePlayedTime();
			playerConfigMap.remove(event.getPlayer());
		}
		
		@EventHandler
		public void onPlayerChat(AsyncPlayerChatEvent event)
		{
			Player p = event.getPlayer();
			
			playerConfigMap.get(p).addChatLine();
			
			//Update leaderboard for most played.
			FileConfigurationWrapper fcw = new FileConfigurationWrapper(FC_AEMCraft.plugin.getDataFolder().getAbsolutePath(), "Leaderboards");
			Leaderboard lb = new Leaderboard(fcw, "MostChatLines", "Most Chat Lines", "lines");
			lb.attemptUpdate(p.getName(), playerConfigMap.get(p).getChatLines());
		}
	}
	
	public class BlockListener implements Listener
	{
		@EventHandler
		public void onBlockPlace(BlockPlaceEvent event) 
		{
			//If already cancelled return.
			if (event.isCancelled()) 
				return;
			
			mc.logBlock(event.getBlock().getTypeId(), event.getBlock().getLocation());
		}
		
		@EventHandler
		public void onBlockBreak(BlockBreakEvent event) 
		{
			Player player = event.getPlayer();
			AEMCraftPermissions perms = new AEMCraftPermissions(player);
			
			if (event.isCancelled())
				return;
			
			if (!perms.canBeRewarded())
				return;
			
			if (player.getGameMode() == GameMode.CREATIVE)
				return;
			
			if (mc.exploitCheck(event.getBlock().getTypeId(), event.getBlock().getLocation()) == true)
			{
				double reward = mc.getOreReward(event.getBlock().getTypeId());
				
				if (reward == -1)
					return;
				
				FC_AEMCraft.economy.depositPlayer(player.getName(), reward);
				
				Date now = new Date();
				
				if (mc.harvestableVisible.get(event.getBlock().getTypeId()) == 1)
				{
					if (lastPayNotificationMap.containsKey(player))
					{
						if (now.getTime() - lastPayNotificationMap.get(player) > 2000)
						{
							notifyPlayer(player, reward, event.getBlock().getType().toString().toLowerCase());
							lastPayNotificationMap.put(player, now.getTime());
						}
					}
					else
					{
						notifyPlayer(player, reward, event.getBlock().getType().toString().toLowerCase());
						lastPayNotificationMap.put(player, now.getTime());
					}
				}
				
				mc.logBlock(event.getBlock().getTypeId(), event.getBlock().getLocation());
			}
			
			return;
		}
		
		private void notifyPlayer(Player player, double reward, String blockName)
		{
			MessageLib msgLib = new MessageLib(player);
			msgLib.standardMessage("You broke " + blockName + " and recieved &q" + df.format(reward) + "&q!");
		}
	}
	
	private Boolean setupEconomy()
	{
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);

		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}
		
		return (economy != null);
	}
}










