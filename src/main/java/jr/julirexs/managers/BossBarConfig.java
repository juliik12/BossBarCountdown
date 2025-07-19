package jr.julirexs.managers;

import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;

public class BossBarConfig {
    private final String title;
    private final BarColor color;
    private final BarStyle style;
    private final String type; // "global" o "permission"

    public BossBarConfig(String title, BarColor color, BarStyle style, String type) {
        this.title = title;
        this.color = color;
        this.style = style;
        this.type = type.toLowerCase();
    }

    public String getTitle() {
        return title;
    }

    public BarColor getColor() {
        return color;
    }

    public BarStyle getStyle() {
        return style;
    }

    public String getType() {
        return type;
    }

    public boolean isPermissionType() {
        return "permission".equalsIgnoreCase(type);
    }
}
