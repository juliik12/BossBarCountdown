package jr.julirexs.utils;

import jr.julirexs.managers.BossBarManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BossBarTabCompleter implements TabCompleter {

    private final BossBarManager manager;

    public BossBarTabCompleter(BossBarManager manager) {
        this.manager = manager;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1) {
            // Todos los subcomandos disponibles
            return Arrays.asList("start", "stop", "create", "reload", "test", "verify", "help");
        }

        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "start":
                case "test":
                    // Mostrar tipos de bossbar para start y test
                    return new ArrayList<>(manager.getBossBarTypes());

                case "create":
                    // Sugerir nombre de ejemplo para create
                    return Collections.singletonList("name");

                default:
                    return Collections.emptyList();
            }
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("create")) {
            // Colores válidos para create
            return Arrays.asList("RED", "GREEN", "BLUE", "YELLOW", "PINK", "PURPLE");
        }

        if (args.length == 4 && args[0].equalsIgnoreCase("create")) {
            // Estilos válidos para create
            return Arrays.asList("SOLID", "SEGMENTED_6", "SEGMENTED_10", "SEGMENTED_12", "SEGMENTED_20");
        }

        return Collections.emptyList();
    }
}
