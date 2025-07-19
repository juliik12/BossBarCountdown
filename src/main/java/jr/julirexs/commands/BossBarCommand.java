package jr.julirexs.commands;

import jr.julirexs.managers.BossBarManager;
import jr.julirexs.managers.Lang;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class BossBarCommand implements CommandExecutor {

    private final BossBarManager manager;
    private final Lang lang;

    public BossBarCommand(BossBarManager manager, Lang lang) {
        this.manager = manager;
        this.lang = lang;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            if (!sender.hasPermission("bossbar.command.help")) {
                sender.sendMessage(lang.getMessage("no_permission_help"));
                return true;
            }

            sender.sendMessage(lang.getMessage("help_header"));
            sender.sendMessage(lang.getMessage("help_start"));
            sender.sendMessage(lang.getMessage("help_stop"));
            sender.sendMessage(lang.getMessage("help_reload"));
            sender.sendMessage(lang.getMessage("help_test"));
            sender.sendMessage(lang.getMessage("help_verify"));
            sender.sendMessage(lang.getMessage("help_create"));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "start" -> {
                if (!sender.hasPermission("bossbar.command.start")) {
                    sender.sendMessage(lang.getMessage("no_permission"));
                    return true;
                }

                if (args.length < 3) {
                    sender.sendMessage(lang.getMessage("usage_start"));
                    return true;
                }

                String tipo = args[1].toLowerCase();
                int segundos;
                try {
                    segundos = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(lang.getMessage("invalid_time"));
                    return true;
                }

                if (!manager.isValidType(tipo)) {
                    sender.sendMessage(lang.getMessage("invalid_type"));
                    return true;
                }

                manager.startCountdown(tipo, segundos);
                sender.sendMessage(lang.getMessage("bossbar_started").replace("%tipo%", tipo).replace("%segundos%", String.valueOf(segundos)));
                return true;
            }

            case "stop" -> {
                if (!sender.hasPermission("bossbar.command.stop")) {
                    sender.sendMessage(lang.getMessage("no_permission"));
                    return true;
                }

                manager.stopCountdown();
                sender.sendMessage(lang.getMessage("bossbar_stopped"));
                return true;
            }

            case "reload" -> {
                if (!sender.hasPermission("bossbar.command.reload")) {
                    sender.sendMessage(lang.getMessage("no_permission"));
                    return true;
                }

                manager.reload();
                sender.sendMessage(lang.getMessage("config_reloaded"));
                return true;
            }

            case "test" -> {
                if (!sender.hasPermission("bossbar.command.test")) {
                    sender.sendMessage(lang.getMessage("no_permission"));
                    return true;
                }

                if (args.length < 2) {
                    sender.sendMessage(lang.getMessage("usage_test"));
                    return true;
                }

                String tipo = args[1].toLowerCase();
                if (!manager.isValidType(tipo)) {
                    sender.sendMessage(lang.getMessage("invalid_type"));
                    return true;
                }

                manager.executeFinishActionsForType(tipo);
                sender.sendMessage(lang.getMessage("actions_executed").replace("%tipo%", tipo));
                return true;
            }

            case "verify" -> {
                if (!sender.hasPermission("bossbar.command.verify")) {
                    sender.sendMessage(lang.getMessage("no_permission"));
                    return true;
                }

                var errores = manager.verifyConfig();
                if (errores.isEmpty()) {
                    sender.sendMessage(lang.getMessage("config_correct"));
                } else {
                    sender.sendMessage(lang.getMessage("config_errors"));
                    for (String err : errores) {
                        sender.sendMessage("§7- " + err);
                    }
                }
                return true;
            }

            case "create" -> {
                if (!sender.hasPermission("bossbar.command.create")) {
                    sender.sendMessage(lang.getMessage("no_permission"));
                    return true;
                }

                if (args.length < 4) {
                    sender.sendMessage(lang.getMessage("usage_create"));
                    return true;
                } 

                String tipo = args[1].toLowerCase();
                String color = args[2].toUpperCase();
                String style = args[3].toUpperCase();

                if (manager.createBossBarConfig(tipo, color, style)) {
                    sender.sendMessage(lang.getMessage("bossbar_created").replace("%tipo%", tipo).replace("%color%", color).replace("%style%", style));
                } else {
                    sender.sendMessage(lang.getMessage("bossbar_create_error"));
                }
                return true;
            }

            default -> {
                sender.sendMessage(lang.getMessage("unknown_command"));
                return true;
            }
        }
    }
}
