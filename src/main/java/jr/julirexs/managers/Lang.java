package jr.julirexs.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class Lang {

    private final JavaPlugin plugin;
    private File langFile;
    private FileConfiguration langConfig;

    private static Lang instance;

    public Lang(JavaPlugin plugin, String filename) {
        this.plugin = plugin;
        loadLanguageFile(filename);
        instance = this;  // asigno instancia en el constructor
    }

    private void loadLanguageFile(String filename) {
        langFile = new File(plugin.getDataFolder(), filename);
        if (!langFile.exists()) {
            plugin.saveResource(filename, false);
        }
        langConfig = YamlConfiguration.loadConfiguration(langFile);
    }

    public String getMessage(String key) {
        if (langConfig.contains(key)) {
            return translateColorCodes(langConfig.getString(key));
        } else {
            return "§cMensaje no encontrado: " + key;
        }
    }

    private String translateColorCodes(String msg) {
        if (msg == null) return "";
        return msg.replace("&", "§");
    }

    public void reload() {
        if (langFile == null) return;
        langConfig = YamlConfiguration.loadConfiguration(langFile);
    }

    // Método estático para asignar instancia
    public static void setInstance(Lang lang) {
        instance = lang;
    }

    // Obtener la instancia estática
    public static Lang getInstance() {
        return instance;
    }
}
