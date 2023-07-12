package my.domain.tokenenchant.enchants;

import com.vk2gpz.tokenenchant.api.EnchantHandler;
import com.vk2gpz.tokenenchant.api.InvalidTokenEnchantException;
import com.vk2gpz.tokenenchant.api.TokenEnchantAPI;
import com.vk2gpz.tokenenchant.event.EventPriorityHandler;
import com.vk2gpz.tokenenchant.event.TEBlockExplodeEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class TokenMiner extends EnchantHandler {
	private double multiplier;
	private boolean applyFortune;
	private double fortuneMultiplier;
	private String earningMessage;
	private Sound sound;
	private int interval;
	private Set<String> triggerEvents;
	private Map<UUID, Integer> blockMined;
	
	public TokenMiner(TokenEnchantAPI plugin) throws InvalidTokenEnchantException {
		super(plugin);
		loadConfig();
	}
	
	public void loadConfig() {
		super.loadConfig();
		ConfigurationSection configSection = this.config.getConfigurationSection("Enchants.TokenMiner");
		if (configSection == null)
			return;
		
		this.multiplier = configSection.getDouble("multiplier", 2);
		if (this.multiplier < 0)
			this.multiplier = 2;
		this.applyFortune = configSection.getBoolean("apply_fortune", false);
		this.fortuneMultiplier = configSection.getDouble("fortune_multiplier", 2);
		if (this.fortuneMultiplier < 0)
			this.fortuneMultiplier = 2;
		this.earningMessage = ChatColor.translateAlternateColorCodes('&', configSection.getString("earning_message", "&aYou mined &e%amount% &atokens!"));
		this.sound = Sound.valueOf(configSection.getString("sound", "ORB_PICKUP"));
		this.interval = configSection.getInt("interval", 1);
		if (this.interval < 1)
			this.interval = 1;
		this.blockMined = new HashMap<>();
		this.triggerEvents = new HashSet<>();
		ConfigurationSection triggerEventSection = configSection.getConfigurationSection("trigger_event");
		if (triggerEventSection != null) {
			for (String key : triggerEventSection.getKeys(false)) {
				if (triggerEventSection.getBoolean(key)) {
					this.triggerEvents.add(key);
				}
			}
		}
	}
	
	public String getName() {
		return "TokenMiner";
	}
	
	@Override
	public String getVersion() {
		return "1.0.0";
	}
	
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	@EventPriorityHandler(key = "BlockBreakEvent")
	public void onBlockBreak(BlockBreakEvent e) {
		if (this.triggerEvents.isEmpty() || this.triggerEvents.contains("BlockBreakEvent")) {
			processMining(e.getPlayer(), 1);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	@EventPriorityHandler(key = "TEBlockExplodeEvent")
	public void onBlockExplode(TEBlockExplodeEvent e) {
		if (this.triggerEvents.isEmpty() || this.triggerEvents.contains("TEBlockExplodeEvent")) {
			processMining(e.getPlayer(), e.blockList().size());
		}
	}
	
	private void processMining(Player p, int numOfBlocks) {
		ItemStack itemStack = p.getInventory().getItemInMainHand();
		int lvl = getCELevel(itemStack);
		if (!shouldProceed(p, lvl, p.getLocation()))
			return;
		
		UUID uuid = p.getUniqueId();
		int mined = this.blockMined.getOrDefault(uuid, 0);
		mined += numOfBlocks;
		this.blockMined.put(uuid, mined);
		
		if (mined >= this.interval) {
			int left = mined % this.interval;
			int multi = mined / this.interval;
			this.blockMined.put(uuid, left);
			
			double amount =  multi * lvl * this.multiplier;
			
			if (this.applyFortune && itemStack.containsEnchantment(Enchantment.LOOT_BONUS_BLOCKS)) {
				int fortuneLevel = itemStack.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS);
				amount *= (fortuneLevel * this.fortuneMultiplier);
			}
			OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(p.getUniqueId());
			TokenEnchantAPI.getInstance().addTokens(offlinePlayer, amount);
			
			if (this.earningMessage != null && !this.earningMessage.isEmpty()) {
				String msg = this.earningMessage.replace("%amount%", String.valueOf(amount));
				p.sendMessage(msg);
			}
			if (this.sound != null) {
				p.playSound(p.getLocation(), this.sound, 1.0f, 1.0f);
			}
		}
	}
}
