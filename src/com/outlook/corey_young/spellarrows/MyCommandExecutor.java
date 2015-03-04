package com.outlook.corey_young.spellarrows;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MyCommandExecutor implements CommandExecutor {

	SpellArrows plugin;

	public MyCommandExecutor(SpellArrows plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		//Toggle arrowSorting with command "sortarrows"
		if (cmd.getName().equalsIgnoreCase("sortarrows")) {
			if (sender instanceof Player) {
				Player player = (Player) sender;

				if (SpellArrows.sortArrowMap.containsKey(player.getName())) {
					boolean sortArrows = SpellArrows.sortArrowMap.get(player.getName());

					if (sortArrows == false) {
						SpellArrows.sortArrowMap.put(player.getName(), true);
						player.sendMessage("Arrow sorting turned on.");
					} else {
						SpellArrows.sortArrowMap.put(player.getName(), false);
						player.sendMessage("Arrow sorting turned off.");
					}
				} else {
					SpellArrows.sortArrowMap.put(player.getName(), false);
					player.sendMessage("Arrow sorting turned off");
				}
			} else {
				sender.sendMessage("This command can only be executed by players.");
			}

			return true;
		}

		if (cmd.getName().equalsIgnoreCase("spellarrows")) {
			if (args.length == 1) {
				if (args[0].equalsIgnoreCase("reload")) {
					plugin.loadConfiguration();
					sender.sendMessage("SpellArrows config reloaded!");

					return true;
				}
			}
		}

		return false;
	}
}
