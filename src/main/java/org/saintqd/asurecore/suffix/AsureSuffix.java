package org.saintqd.asurecore.suffix;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.configuration.ConfigurationSection;
import org.saintqd.asurecore.AsureCore;
import org.saintqd.asurelib.AsureLib;

import java.util.List;

public class AsureSuffix {

    private final String name;
    private final String displayName;
    private final String itemModel;
    private final String permission;
    private final List<String> desc;
    private final String symbol;
    private final String placeholder;

    public AsureSuffix(String name, ConfigurationSection suffixConfig) {
        this.name = name;
        this.displayName = suffixConfig.getString("Display",name);
        this.itemModel = suffixConfig.getString("Model",null);
        this.desc = suffixConfig.getStringList("Desc");
        this.permission = suffixConfig.getString("Permission","asurecore.suffix."+name.toLowerCase());
        this.symbol = suffixConfig.getString("Symbol","+");
        this.placeholder = AsureCore.inst().getSuffixManager().getPlaceholderTemplate().replace("{0}",name);
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getItemModel() {
        return itemModel;
    }

    public List<String> getDesc() {
        return desc;
    }

    public String getPermission() {
        return permission;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public String getParsedPlaceholder() {
        return AsureLib.inst().isPlaceholderAPIEnabled() ? PlaceholderAPI.setPlaceholders(null,placeholder) : placeholder;
    }
}
