package com.outlook.corey_young.spellarrows;

import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

public class MyListener implements Listener {

	/**
	 * Renames magic arrows according to the ingredient potion's type.
	 * @param event A PrepareItemCraftEvent.
	 */
	@EventHandler
	public void onCraftItem(PrepareItemCraftEvent event) {
		ItemMeta itemMeta = event.getRecipe().getResult().getItemMeta();

		if (itemMeta.hasDisplayName()) {
			if (itemMeta.getDisplayName().equals("Magic Arrow")) {
				CraftingInventory inventory = event.getInventory();
				ItemStack[] contents = inventory.getContents();

				for (ItemStack itemStack : contents) {
					if (itemStack.getType().equals(Material.POTION)) {
						Potion potion = Potion.fromItemStack(itemStack);
						ItemStack result = createPotionArrow(potion);
						inventory.setResult(result);

						return;
					}
				}
			}
		}
	}

	/**
	 * Shuffles arrow ItemStacks in a player's inventory on left click, if they have arrowsorting enabled.
	 * @param event A PlayerInteractEvent.
	 */
	@EventHandler
	public void onLeftClick(PlayerInteractEvent event) {
		Player player = event.getPlayer();

		boolean sortArrows = true;

		if (SpellArrows.sortArrowMap.containsKey(player.getName())) {
			sortArrows = SpellArrows.sortArrowMap.get(player.getName());
		}

		Inventory inventory = player.getInventory();

		if (sortArrows && inventory.first(Material.ARROW) != -1) {
			if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
				if (player.getItemInHand().getType() == Material.BOW) {
					//Sort arrowStacks
					ItemStack[] arrowStacks = getArrowStacks(inventory);
					int[] arrowStackIndices = getArrowStackIndices(inventory);
					int numberOfStacks = getNumberOfArrowStacks(inventory);
					ItemStack tempStack = arrowStacks[0];

					for (int i = 1; i < arrowStacks.length; i++) {
						inventory.setItem(arrowStackIndices[i - 1], arrowStacks[i]);
					}

					inventory.setItem(arrowStackIndices[numberOfStacks - 1], tempStack);
					ItemStack arrowStackSelected = inventory.getItem(inventory.first(Material.ARROW));
					String arrowSelectedName = arrowStackSelected.getItemMeta().getDisplayName();

					if (arrowSelectedName == null) {
						player.sendMessage("Normal arrow selected.");
					} else {
						player.sendMessage(arrowSelectedName + " selected.");
					}
				}
			}
		}
	}

	/**
	 * Gets a magic arrow's potion and saves it to SpellArrows.arrowMap.
	 * @param event An EntityShootBowEvent.
	 */
	@EventHandler
	public void onShoot(EntityShootBowEvent event) {
		if (event.getEntityType() == EntityType.PLAYER) {
			Player player = (Player) (event.getEntity());
			PlayerInventory inventory = player.getInventory();
			int itemPos = inventory.first(Material.ARROW);

			if (itemPos != -1) {
				ItemStack arrowConsumed = inventory.getItem(itemPos);
				Potion potion = getPotionFromArrowStack(arrowConsumed);

				if (potion != null) {
					Arrow arrow = (Arrow) event.getProjectile();
					SpellArrows.arrowMap.put(arrow.getEntityId(), potion);
				}
			}
		}
	}

	/**
	 * Applies a potion effect to a mob if hit by an arrow in SpellArrows.arrowMap.
	 * @param event An EntityDamageByEntityEvent.
	 */
	@EventHandler
	public void arrowHitMob(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Arrow) {
			Arrow arrow = (Arrow) event.getDamager();
			LivingEntity entity = (LivingEntity) event.getEntity();

			if (SpellArrows.arrowMap.containsKey(arrow.getEntityId())) {

				if (SpellArrows.arrowDamage == false) {
					event.setDamage(0d);
				}

				Potion potion = SpellArrows.arrowMap.get(arrow.getEntityId());
				potion.apply(entity);

				SpellArrows.arrowMap.remove(arrow.getEntityId());
			}
		}
	}

	/**
	 * Makes an arrow back into a magic arrow if found in SpellArrows.arrowMap.
	 * @param event A PlayerPickupItemEvent.
	 */
	@EventHandler
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		int ID = event.getItem().getEntityId();
		if (SpellArrows.arrowMap.containsKey(ID)) {
			Potion potion = SpellArrows.arrowMap.get(ID);
			ItemStack itemStack = createPotionArrow(potion);

			event.setCancelled(true);
			event.getItem().remove();

			event.getPlayer().getInventory().addItem(itemStack);
			SpellArrows.arrowMap.remove(event.getItem().getEntityId());
		}
	}

	/**
	 * Creates an arrow with the given potion's data in the name.
	 * @param potion The potion used to name the arrow.
	 * @return An arrow with potion data in the name.
	 */
	public ItemStack createPotionArrow(Potion potion) {
		ItemStack result = new ItemStack(Material.ARROW);
		ItemMeta resultMeta = result.getItemMeta();
		System.out.println(potion.getEffects());

		String effectName = potion.getType().name();

		effectName = effectName.charAt(0) + effectName.toLowerCase().substring(1);
		effectName = effectName.replace('_', ' ');

		if (potion.hasExtendedDuration()) {
			effectName += " (Extended)";
		}

		if (potion.getLevel() != 1) {
			effectName += " " + potion.getLevel();
		}

		resultMeta.setDisplayName("Arrow of " + effectName);
		result.setItemMeta(resultMeta);

		return result;
	}

	/**
	 * Removes a substring from a string.
	 * @param string The main string.
	 * @param substring The substring to remove from the main string.
	 * @return The main string with the substring removed.
	 */
	private String removeSubstring(String string, String substring) {
		int start = string.indexOf(substring);
		int end = start + substring.length();

		string = string.substring(0, start) + string.substring(end, string.length());

		return string;
	}

	/**
	 * Creates a Potion from data stored in a magic arrow's name.
	 * @param itemStack An ItemStack that contains magic arrows.
	 * @return The potion just created. Null if invalid data in name.
	 */
	public Potion getPotionFromArrowStack(ItemStack itemStack) {
		String name = itemStack.getItemMeta().getDisplayName();
		boolean extended = false;
		int level = 1;

		if (name.contains("Arrow of ")) {
			name = removeSubstring(name, "Arrow of ");

			if (name.contains(" (Extended)")) {
				name = removeSubstring(name, " (Extended)");
				extended = true;
			}

			if (name.contains(" 2")) {
				name = removeSubstring(name, " 2");
				level = 2;
			}

			name = name.toUpperCase();
			PotionType potionType = PotionType.valueOf(name);
			Potion potion = new Potion(potionType);

			potion.setLevel(level);
			potion.setHasExtendedDuration(extended);

			return potion;
		}

		return null;
	}

	/**
	 * Gets the number of arrow ItemStacks in an inventory.
	 * @param inventory The inventory to search for arrow ItemStacks.
	 * @return The number of arrow ItemStacks found.
	 */
	public int getNumberOfArrowStacks(Inventory inventory) {
		int numberOfStacks = 0;

		for (int i = 0; i < inventory.getSize(); i++) {
			if (inventory.getItem(i) != null) {
				if (inventory.getItem(i).getType() == Material.ARROW) {
					numberOfStacks++;
				}
			}
		}

		return numberOfStacks;
	}

	/**
	 * Gets an array of all arrow ItemStacks in an inventory.
	 * @param inventory The inventory to search for arrow ItemStacks.
	 * @return An array of all arrow ItemStacks found.
	 */
	public ItemStack[] getArrowStacks(Inventory inventory) {
		int numberOfStacks = getNumberOfArrowStacks(inventory);
		ItemStack[] arrowStacks = new ItemStack[numberOfStacks];
		int count = 0;

		while (count < numberOfStacks) {
			for (int i = 0; i < inventory.getSize(); i++) {
				ItemStack itemStack = inventory.getItem(i);

				if (itemStack != null) {
					if (itemStack.getType() == Material.ARROW) {
						arrowStacks[count] = itemStack;
						count++;
					}
				}
			}
		}

		return arrowStacks;
	}

	/**
	 * Gets an array with the indice of every arrow ItemStack for a given inventory.
	 * @param inventory The inventory to search for arrow ItemStacks.
	 * @return An array of all arrow ItemStacks.
	 */
	public int[] getArrowStackIndices(Inventory inventory) {
		int numberOfStacks = getNumberOfArrowStacks(inventory);
		int[] arrowStackIndices = new int[numberOfStacks];
		int count = 0;

		while (count < numberOfStacks) {
			for (int i = 0; i < inventory.getSize(); i++) {
				ItemStack itemStack = inventory.getItem(i);

				if (itemStack != null) {
					if (itemStack.getType() == Material.ARROW) {
						arrowStackIndices[count] = i;
						count++;
					}
				}
			}
		}

		return arrowStackIndices;
	}
}