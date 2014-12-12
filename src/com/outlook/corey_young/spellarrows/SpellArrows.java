package com.outlook.corey_young.spellarrows;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public final class SpellArrows extends JavaPlugin {

	//HashMaps for storing shot arrow's potion effects and player settings.
	public static HashMap<Integer, Short> arrowMap = new HashMap<>();
	public static HashMap<String, Boolean> sortArrowMap = new HashMap<>();
	public static Boolean arrowDamage = false;

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(new MyListener(), this);
		MyCommandExecutor commandExecutor = new MyCommandExecutor(this);
		getCommand("sortarrows").setExecutor(commandExecutor);
		getCommand("spellarrows").setExecutor(commandExecutor);
		loadConfiguration();
		createMagicArrows();
	}

	@Override
	public void onDisable() {
		//Save config.yml
		saveConfiguration();
	}

	public void loadConfiguration() {
		reloadConfig();

		if (getConfig().contains("arrowDamage")) {
			arrowDamage = getConfig().getBoolean("arrowDamage");
			getConfig().set("arrowDamage", arrowDamage);
			saveConfiguration();
		} else {
			getConfig().set("arrowDamage", arrowDamage);
			saveConfiguration();
		}

		//load config player settings as Map, then copy into HashMap
		if (getConfig().contains("sortArrows")) {
			Map<String, Object> configMap = getConfig().getConfigurationSection("sortArrows").getValues(false);

			for (String playerName : configMap.keySet()) {
				boolean sortArrows = (Boolean) configMap.get(playerName);
				sortArrowMap.put(playerName, sortArrows);
			}
		}
	}

	public void saveConfiguration() {
		//Save sortArrowMap to config.yml
		//The other HashMaps don't need to be saved, as they havn't changed since being loaded.
		for (String playerName : sortArrowMap.keySet()) {
			getConfig().set("sortArrows." + playerName, sortArrowMap.get(playerName));
		}

		saveConfig();
	}

	public void createMagicArrows() {
		ItemStack arrowStack = new ItemStack(Material.ARROW);
		ItemMeta itemMeta = arrowStack.getItemMeta();
		itemMeta.setDisplayName("Magic Arrow");

		ItemStack potionStack = new ItemStack(Material.POTION);
		potionStack.setDurability((short) -1);

		arrowStack.setItemMeta(itemMeta);

		ShapelessRecipe recipe = new ShapelessRecipe(arrowStack);
		recipe.addIngredient(Material.ARROW);
		recipe.addIngredient(potionStack.getData());

		getServer().addRecipe(recipe);
	}
}