package cz.neumimto.rpg.api.configuration;

import com.electronwill.nightconfig.core.conversion.Conversion;
import com.electronwill.nightconfig.core.conversion.Path;
import cz.neumimto.rpg.api.configuration.adapters.PropertiesMapAdapter;

import java.util.Map;

public class AttributeConfig {

    @Path("Id")
    private String id;

    @Path("Name")
    private String name;

    @Path("MaxValue")
    private float maxValue;

    @Path("Properties")
    @Conversion(PropertiesMapAdapter.class)
    private Map<Integer, Float> propBonus;

    @Path("ItemType")
    private String itemType;

    @Path("Description")
    private String description;

    public AttributeConfig(String id, String name, float maxValue, Map<Integer, Float> propBonus, String itemType, String description) {
        this.id = id;
        this.name = name;
        this.maxValue = maxValue;
        this.propBonus = propBonus;
        this.itemType = itemType;
        this.description = description;
    }

    public AttributeConfig() {
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Map<Integer, Float> getPropBonus() {
        return propBonus;
    }

    public float getMaxValue() {
        return maxValue;
    }

    public String getItemType() {
        return itemType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}