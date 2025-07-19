package jr.julirexs.managers;

import jr.julirexs.utils.TextUtils;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class BossBarManager {

    private final Plugin plugin;
    private final Map<String, BossBarConfig> bossBarTypes = new HashMap<>();
    private final Map<UUID, BossBar> activeBars = new HashMap<>();
    private BukkitRunnable countdownTask;

    public BossBarManager(Plugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        bossBarTypes.clear();
        FileConfiguration config = plugin.getConfig();

        if (!config.isConfigurationSection("bossbars")) return;

        for (String key : config.getConfigurationSection("bossbars").getKeys(false)) {
            String path = "bossbars." + key + ".";
            String title = config.getString(path + "title", "Contador");
            BarColor color = getEnum(config.getString(path + "color"), BarColor.GREEN);
            BarStyle style = getEnum(config.getString(path + "style"), BarStyle.SOLID);
            String type = config.getString(path + "type", "global").toLowerCase();

            // on_finish
            BossBarConfig.OnFinishConfig onFinish = null;
            ConfigurationSection onFinishSec = config.getConfigurationSection(path + "on_finish");
            if (onFinishSec != null) {
                int repeat = onFinishSec.getInt("repeat", 1);
                int delay = onFinishSec.getInt("delay_between", 0);
                Map<String, BossBarConfig.Action> actions = new LinkedHashMap<>();
                ConfigurationSection actionsSec = onFinishSec.getConfigurationSection("actions");

                if (actionsSec != null) {
                    for (String aKey : actionsSec.getKeys(false)) {
                        ConfigurationSection sec = actionsSec.getConfigurationSection(aKey);
                        if (sec == null) continue;

                        String typeAct = sec.getString("type", "message");
                        List<BossBarConfig.ActionValue> values = new ArrayList<>();

                        if (typeAct.equalsIgnoreCase("title")) {
                            String t = sec.getString("title", "");
                            String st = sec.getString("subtitle", "");
                            int fi = sec.getInt("fadeIn", 10);
                            int stt = sec.getInt("stay", 70);
                            int fo = sec.getInt("fadeOut", 20);
                            values.add(new BossBarConfig.ActionValue.TitleValue(t, st, fi, stt, fo));
                        } else {
                            List<Object> raw = (List<Object>) sec.getList("values", Collections.emptyList());
                            for (Object o : raw) {
                                if (o instanceof String)
                                    values.add(new BossBarConfig.ActionValue.StringValue((String) o));
                                else if (o instanceof Map<?, ?> map && map.containsKey("delay")) {
                                    Object d = map.get("delay");
                                    int seconds = (d instanceof Number) ? ((Number) d).intValue()
                                            : Integer.parseInt(d.toString());
                                    values.add(new BossBarConfig.ActionValue.DelayValue(seconds));
                                }
                            }
                        }

                        actions.put(aKey, new BossBarConfig.Action(typeAct.toLowerCase(), values));
                    }
                }

                onFinish = new BossBarConfig.OnFinishConfig(repeat, delay, actions);
            }

            bossBarTypes.put(key.toLowerCase(), new BossBarConfig(title, color, style, type, onFinish));
        }
    }
    public List<String> verifyConfig() {
    List<String> errores = new ArrayList<>();

    for (Map.Entry<String, BossBarConfig> entry : bossBarTypes.entrySet()) {
        String tipo = entry.getKey();
        BossBarConfig cfg = entry.getValue();

        // Verificar tipo
        if (!cfg.type.equals("global") && !cfg.type.equals("permission")) {
            errores.add("Tipo inválido en bossbar '" + tipo + "': " + cfg.type);
        }

        // Verificar color
        if (cfg.color == null) {
            errores.add("Color inválido en bossbar '" + tipo + "'");
        }

        // Verificar estilo
        if (cfg.style == null) {
            errores.add("Estilo inválido en bossbar '" + tipo + "'");
        }

        // Verificar acciones
        if (cfg.onFinish != null) {
            for (Map.Entry<String, BossBarConfig.Action> act : cfg.onFinish.actions.entrySet()) {
                String actionKey = act.getKey();
                String type = act.getValue().type;

                if (!Arrays.asList("message", "command", "broadcast", "sound", "title").contains(type)) {
                    errores.add("Acción inválida '" + type + "' en bossbar '" + tipo + "', acción '" + actionKey + "'");
                }

                if (act.getValue().values == null || act.getValue().values.isEmpty()) {
                    errores.add("La acción '" + actionKey + "' en bossbar '" + tipo + "' no tiene valores definidos.");
                }
            }
        }
    }

    return errores;
}
    public boolean createBossBarConfig(String tipo, String color, String style) {
    try {
        @SuppressWarnings("unused")
        BarColor barColor = BarColor.valueOf(color);
        @SuppressWarnings("unused")
        BarStyle barStyle = BarStyle.valueOf(style);

        plugin.getConfig().set("bossbars." + tipo + ".title", "&aNew BossBar: %time%");
        plugin.getConfig().set("bossbars." + tipo + ".color", color);
        plugin.getConfig().set("bossbars." + tipo + ".style", style);
        plugin.getConfig().set("bossbars." + tipo + ".type", "global");

        plugin.saveConfig();
        loadConfig();
        return true;
    } catch (IllegalArgumentException e) {
        return false;
    }
}

    public void startCountdown(String tipo, int segundos) {
        stopCountdown();
        BossBarConfig config = bossBarTypes.get(tipo);
        if (config == null) return;

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (config.type.equals("permission") && !p.hasPermission("bossbar.view." + tipo)) continue;
            String txt = config.title.replace("%time%", TextUtils.formatTime(segundos));
            if (isPapiEnabled()) txt = PlaceholderAPI.setPlaceholders(p, txt);
            BossBar bar = Bukkit.createBossBar(ChatColor.translateAlternateColorCodes('&', txt), config.color, config.style);
            bar.addPlayer(p);
            bar.setProgress(1.0);
            activeBars.put(p.getUniqueId(), bar);
        }

        countdownTask = new BukkitRunnable() {
            int timeLeft = segundos;

            @Override
            public void run() {
                if (timeLeft <= 0) {
                    executeFinishActionsForType(tipo);
                    removeAllBossBars();
                    cancel();
                    return;
                }

                double progress = (double) timeLeft / segundos;
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (config.type.equals("permission") && !p.hasPermission("bossbar.view." + tipo)) continue;
                    BossBar bar = activeBars.get(p.getUniqueId());
                    if (bar != null) {
                        String txt2 = config.title.replace("%time%", TextUtils.formatTime(timeLeft));
                        if (isPapiEnabled()) txt2 = PlaceholderAPI.setPlaceholders(p, txt2);
                        bar.setTitle(ChatColor.translateAlternateColorCodes('&', txt2));
                        bar.setProgress(progress);
                    }
                }

                timeLeft--;
            }
        };
        countdownTask.runTaskTimer(plugin, 0L, 20L);
    }

    public void stopCountdown() {
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }
        removeAllBossBars();
    }

    public void reload() {
        plugin.reloadConfig();
        loadConfig();
    }

    public void cleanup() {
        stopCountdown();
    }

    public boolean isValidType(String tipo) {
        return bossBarTypes.containsKey(tipo.toLowerCase());
    }

    public Set<String> getBossBarTypes() {
        return bossBarTypes.keySet();
    }

    public void executeFinishActionsForType(String tipo) {
        BossBarConfig cfg = bossBarTypes.get(tipo.toLowerCase());
        if (cfg != null && cfg.onFinish != null) runFinishActions(cfg);
    }

    private void runFinishActions(BossBarConfig cfg) {
        int repeat = Math.max(cfg.onFinish.repeat, 1);
        int delay = cfg.onFinish.delayBetween;
        new BukkitRunnable() {
            int count = 0;

            @Override
            public void run() {
                if (count >= repeat) { cancel(); return; }
                for (BossBarConfig.Action a : cfg.onFinish.actions.values()) {
                    runActionSequence(a);
                }
                count++;
            }
        }.runTaskTimer(plugin, 0L, delay * 20L);
    }

    private void runActionSequence(BossBarConfig.Action action) {
        runActionSequenceRecursive(action, action.values, 0);
    }

    private void runActionSequenceRecursive(BossBarConfig.Action action, List<BossBarConfig.ActionValue> values, int index) {
        if (index >= values.size()) return;
        BossBarConfig.ActionValue val = values.get(index);

        if (val instanceof BossBarConfig.ActionValue.DelayValue d) {
            new BukkitRunnable() {
                @Override public void run() {
                    runActionSequenceRecursive(action, values, index + 1);
                }
            }.runTaskLater(plugin, d.seconds * 20L);
        } else if (val instanceof BossBarConfig.ActionValue.StringValue s) {
            executeAction(action.type, s.content);
            runActionSequenceRecursive(action, values, index + 1);
        } else if (val instanceof BossBarConfig.ActionValue.TitleValue t) {
            executeTitle(t);
            runActionSequenceRecursive(action, values, index + 1);
        }
    }

    private void executeAction(String type, String content) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                String finalMsg = content.replace("%player%", p.getName());
                if (isPapiEnabled()) finalMsg = PlaceholderAPI.setPlaceholders(p, finalMsg);
                switch (type) {
                    case "message" -> p.sendMessage(ChatColor.translateAlternateColorCodes('&', finalMsg));
                    case "command" -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalMsg);
                    case "broadcast" -> Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', finalMsg));
                    case "sound" -> {
                        try {
                            Sound s = Sound.valueOf(finalMsg.toUpperCase());
                            p.playSound(p.getLocation(), s, 1f, 1f);
                        } catch (IllegalArgumentException e) {
                            plugin.getLogger().warning("Sound inválido: " + finalMsg);
                        }
                    }
                    default -> plugin.getLogger().warning("Tipo de acción desconocido: " + type);
                }
            }
        });
    }

    private void executeTitle(BossBarConfig.ActionValue.TitleValue titleVal) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                String t = titleVal.title.replace("%player%", p.getName());
                String st = titleVal.subtitle.replace("%player%", p.getName());
                if (isPapiEnabled()) {
                    t = PlaceholderAPI.setPlaceholders(p, t);
                    st = PlaceholderAPI.setPlaceholders(p, st);
                }
                p.sendTitle(
                    ChatColor.translateAlternateColorCodes('&', t),
                    ChatColor.translateAlternateColorCodes('&', st),
                    titleVal.fadeIn, titleVal.stay, titleVal.fadeOut
                );
            }
        });
    }

    private void removeAllBossBars() {
        activeBars.values().forEach(BossBar::removeAll);
        activeBars.clear();
    }

    public boolean isPapiEnabled() {
        return Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
    }

    private <T extends Enum<T>> T getEnum(String value, T defaultValue) {
        try {
            return Enum.valueOf(defaultValue.getDeclaringClass(), value.toUpperCase());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    // Config
    public static class BossBarConfig {
        public final String title;
        public final BarColor color;
        public final BarStyle style;
        public final String type;
        public final OnFinishConfig onFinish;

        public BossBarConfig(String title, BarColor color, BarStyle style, String type, OnFinishConfig onFinish) {
            this.title = title;
            this.color = color;
            this.style = style;
            this.type = type;
            this.onFinish = onFinish;
        }

        public static class OnFinishConfig {
            public final int repeat, delayBetween;
            public final Map<String, Action> actions;

            public OnFinishConfig(int repeat, int delayBetween, Map<String, Action> actions) {
                this.repeat = repeat;
                this.delayBetween = delayBetween;
                this.actions = actions;
            }
        }

        public static class Action {
            public final String type;
            public final List<ActionValue> values;

            public Action(String type, List<ActionValue> values) {
                this.type = type;
                this.values = values;
            }
        }

        public abstract static class ActionValue {
            public static class StringValue extends ActionValue {
                public final String content;
                public StringValue(String content) { this.content = content; }
            }

            public static class DelayValue extends ActionValue {
                public final int seconds;
                public DelayValue(int seconds) { this.seconds = seconds; }
            }

            public static class TitleValue extends ActionValue {
                public final String title, subtitle;
                public final int fadeIn, stay, fadeOut;
                public TitleValue(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
                    this.title = title;
                    this.subtitle = subtitle;
                    this.fadeIn = fadeIn;
                    this.stay = stay;
                    this.fadeOut = fadeOut;
                }
            }
        }
    }
}
