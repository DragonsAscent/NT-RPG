package cz.neumimto.rpg.api.configuration.adapters;

import com.google.common.reflect.TypeToken;
import cz.neumimto.rpg.api.Rpg;
import cz.neumimto.rpg.api.entity.IPropertyService;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by NeumimTo on 5.1.2019.
 */
public class PropertiesArrayAdapter implements TypeSerializer<float[]> {

    @Override
    public float[] deserialize(TypeToken<?> typeToken, ConfigurationNode configurationNode) throws ObjectMappingException {
        int lastId = Rpg.get().getPropertyService().getLastId();
        float[] arr = new float[lastId];

        Map<Object, ? extends ConfigurationNode> childrenMap = configurationNode.getChildrenMap();
        IPropertyService propertyService = Rpg.get().getPropertyService();
        for (Map.Entry<Object, ? extends ConfigurationNode> objectEntry : childrenMap.entrySet()) {
            String propertyName = ((String) objectEntry.getKey()).toLowerCase();
            float f = ((Number) objectEntry.getValue().getValue()).floatValue();

            if (propertyService.exists(propertyName)) {
                int idByName = propertyService.getIdByName(propertyName);
                arr[idByName] = f;
            } else {
                throw new ObjectMappingException("Unknown property " + propertyName);
            }
        }

        return arr;
    }

    @Override
    public void serialize(TypeToken<?> typeToken, float[] floats, ConfigurationNode configurationNode) throws ObjectMappingException {
        Map<String, Float> map = new HashMap<>();
        IPropertyService propertyService = Rpg.get().getPropertyService();
        for (int i = 0; i < floats.length; i++) {
            String nameById = propertyService.getNameById(i);
            if (floats[i] != 0) {
                map.put(nameById, floats[i]);
            }
        }
        configurationNode.setValue(map);
    }
}
