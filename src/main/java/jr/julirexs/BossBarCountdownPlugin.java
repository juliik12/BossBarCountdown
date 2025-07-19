package jr.julirexs;

import jr.julirexs.commands.BossBarCommand;
import jr.julirexs.managers.BossBarManager;
import jr.julirexs.utils.BossBarTabCompleter;
import jr.julirexs.managers.Lang;
import org.bukkit.plugin.java.JavaPlugin;

public class BossBarCountdownPlugin extends JavaPlugin {

    private BossBarManager bossBarManager;
    private Lang lang;

@Override
public void onEnable() {
    saveDefaultConfig();

    String language = getConfig().getString("language", "en").toLowerCase();
    String filename = "lang/lang_" + language + ".yml";

    // Carga o crea archivo de idioma
    lang = new Lang(this, filename);

    bossBarManager = new BossBarManager(this);

    // PASAR instancia lang al comando
    getCommand("bossbar").setExecutor(new BossBarCommand(bossBarManager, lang));
    getCommand("bossbar").setTabCompleter(new BossBarTabCompleter(bossBarManager));

    getLogger().info("§aBossBarCountdown enabled.");
}


    @Override
    public void onDisable() {
        if (bossBarManager != null) bossBarManager.cleanup();
    }

    public BossBarManager getBossBarManager() {
        return bossBarManager;
    }

    public static BossBarCountdownPlugin getInstance() {
        return JavaPlugin.getPlugin(BossBarCountdownPlugin.class);
    }
}
