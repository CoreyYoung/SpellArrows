package com.outlook.corey_young.spellarrows;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.Potion;

public final class SpellArrows extends JavaPlugin {

	//HashMaps for storing shot arrow's potion effects and player settings.
	public static HashMap<Integer, Potion> arrowMap = new HashMap<>();
	public static HashMap<String, Boolean> sortArrowMap = new HashMap<>();
	public static Boolean arrowDamage = false;

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(new MyListener(), this);
		MyCommandExecutor commandExecutor = new MyCommandExecutor(this);
		getCommand("sortarrows").setExecutor(commandExecutor);
		getCommand("spellarrows").setExecutor(commandExecutor);
		loadConfiguration();
		createMagicArrowRecipes();
	}

	@Override
	public void onDisable() {
		//Save config.yml
		saveConfiguration();
	}

	/**
	 * Loads settings from the config file.
	 */
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

	/**
	 * Saves the HashMap sortArrowMap to the config.
	 * The other HashMaps don't need to be saved, as they haven't changed since being loaded.
	 */
	public void saveConfiguration() {
		for (String playerName : sortArrowMap.keySet()) {
			getConfig().set("sortArrows." + playerName, sortArrowMap.get(playerName));
		}

		saveConfig();
	}

	/**
	 * Creates a recipe for magic arrows.
	 * Ingredients include any potion and and an arrow.
	 * The recipe result is named "Magic Arrow", and is renamed properly in MyListener.onCraftItem.
	 */
	public void createMagicArrowRecipes() {
		ItemStack arrowStack = new ItemStack(Material.ARROW);
		ItemMeta itemMeta = arrowStack.getItemMeta();
		itemMeta.setDisplayName("Magic Arrow");

		ItemStack potionStack = new ItemStack(Material.POTION);
		potionStack.setDurability((short) -1);

		arrowStack.setItemMeta(itemMeta);

		ShapedRecipe recipe = new ShapedRecipe(arrowStack);
		recipe.shape("P", "A");
		recipe.setIngredient('P', potionStack.getData());
		recipe.setIngredient('A', Material.ARROW);

		getServer().addRecipe(recipe);
	}
}