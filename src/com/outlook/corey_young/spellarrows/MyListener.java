package com.outlook.corey_young.spellarrows;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class MyListener implements Listener {

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

						ItemStack result = getArrowFromData(potion.toDamageValue());

						inventory.setResult(result);

						return;
					}
				}
			}
		}
	}

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

	@EventHandler
	public void onShoot(EntityShootBowEvent event) {
		if (event.getEntityType() == EntityType.PLAYER) {
			Player player = (Player) (event.getEntity());
			PlayerInventory inventory = player.getInventory();
			int itemPos = inventory.first(Material.ARROW);

			if (itemPos != -1) {
				ItemStack arrowConsumed = inventory.getItem(itemPos);

				List<String> lore = arrowConsumed.getItemMeta().getLore();

				if (lore != null) {
					if (lore.get(0).startsWith("Potion Data: ")) {
						String loreData = lore.get(0).substring(13);
						Short arrowData = Short.parseShort(loreData);
						Arrow arrow = (Arrow) event.getProjectile();

						SpellArrows.arrowMap.put(arrow.getEntityId(), arrowData);
					}
				}
			}
		}
	}

	@EventHandler
	public void arrowHitMob(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Arrow) {
			Arrow arrow = (Arrow) event.getDamager();
			LivingEntity entity = (LivingEntity) event.getEntity();

			if (SpellArrows.arrowMap.containsKey(arrow.getEntityId())) {

				if (SpellArrows.arrowDamage == false) {
					event.setDamage(0);
				}

				Short ID = SpellArrows.arrowMap.get(arrow.getEntityId());
				Potion potion = Potion.fromDamage(ID);
				potion.apply(entity);

				SpellArrows.arrowMap.remove(arrow.getEntityId());
			}
		}
	}

	@EventHandler
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		int ID = event.getItem().getEntityId();
		if (SpellArrows.arrowMap.containsKey(ID)) {
			short data = SpellArrows.arrowMap.get(ID);
			event.setCancelled(true);
			event.getItem().remove();
			ItemStack itemStack = getArrowFromData(data);
			event.getPlayer().getInventory().addItem(itemStack);
			SpellArrows.arrowMap.remove(event.getItem().getEntityId());
		}
	}

	public ItemStack getArrowFromData(short data) {
		Potion potion = Potion.fromDamage(data);

		ItemStack result = new ItemStack(Material.ARROW);
		ItemMeta resultMeta = result.getItemMeta();

		String potionName = potion.getType().name();
		potionName = potionName.charAt(0) + potionName.toLowerCase().substring(1);
		potionName = potionName.replace('_', ' ');

		if (potion.hasExtendedDuration()) {
			potionName += " (Extended)";
		}

		if (potion.getLevel() != 1) {
			potionName += " " + potion.getLevel();
		}

		ArrayList<String> lore = new ArrayList<>();
		lore.add("Potion Data: " + Short.toString(potion.toDamageValue()));
		resultMeta.setLore(lore);

		resultMeta.setDisplayName("Arrow of " + potionName);
		result.setItemMeta(resultMeta);

		return result;
	}

	public void addEffectToEntity(Entity entity, PotionEffectType effectType, int length, int strength) {
		LivingEntity livingEntity = (LivingEntity) entity;
		livingEntity.addPotionEffect(new PotionEffect(effectType, length, strength));
	}

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