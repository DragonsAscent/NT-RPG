package cz.neumimto.rpg.spigot.damage;

import com.google.common.collect.Lists;
import cz.neumimto.rpg.api.entity.CommonProperties;
import cz.neumimto.rpg.api.entity.IEntity;
import cz.neumimto.rpg.api.entity.players.IActiveCharacter;
import cz.neumimto.rpg.api.entity.players.classes.ClassDefinition;
import cz.neumimto.rpg.api.items.ClassItem;
import cz.neumimto.rpg.common.damage.AbstractDamageService;
import cz.neumimto.rpg.spigot.entities.players.ISpigotCharacter;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import javax.inject.Singleton;
import java.util.*;
import java.util.stream.Collectors;

@Singleton
public class SpigotDamageService extends AbstractDamageService<LivingEntity> {

    private Map<Double, String> doubleColorMap = new TreeMap<>();

    private String[] colorScale = new String[]{
            "§f",
            "§e",
            "§6",
            "§c",
            "§4",
            "§5",
            "§1"
    };

    @Override
    public void damageEntity(IEntity<LivingEntity> character, double value) {
        character.getEntity().damage(value);
    }

    @Override
    public void init() {
        Collection<ClassDefinition> classes = classService.getClassDefinitions();
        Set<Double> list = new TreeSet<>();

        for (ClassDefinition aClass : classes) {
            Set<ClassItem> classItems = aClass.getWeapons();
            list = classItems.stream().map(ClassItem::getDamage).collect(Collectors.toCollection(TreeSet::new));
        }


        int size = list.size();
        if (size >= colorScale.length) {
            int l = list.size() / colorScale.length;
            int w = 0;
            for (List<Double> partition : Lists.partition(new ArrayList<>(list), l + 1)) {
                OptionalDouble max = partition.stream().mapToDouble(d -> d).max();
                doubleColorMap.put(max.getAsDouble(), colorScale[w]);
                w++;
            }
        }
    }

    public String getColorByDamage(Double damage) {
        if (doubleColorMap.size() != colorScale.length) {
            return "§c";
        }
        String val = "§c";
        for (Map.Entry<Double, String> aDouble : doubleColorMap.entrySet()) {
            if (damage <= aDouble.getKey() || aDouble.getValue().equals(colorScale[colorScale.length - 1])) {
                val = aDouble.getValue();
            }
        }
        return val;
    }

    public DamageCause damageTypeById(String damageType) {
        return DamageCause.valueOf(damageType);
    }

    public double getCharacterProjectileDamage(IActiveCharacter character, EntityType type) {
        if (character.isStub() || type == null) {
            return 1;
        }
        double base = character.getBaseProjectileDamage(type.name())
                + entityService.getEntityProperty(character, CommonProperties.projectile_damage_bonus);
        if (type == EntityType.SPECTRAL_ARROW || type == EntityType.ARROW) {
            base *= entityService.getEntityProperty(character, CommonProperties.arrow_damage_mult);
        }
        return base;
    }

    public double getEntityDamageMult(IEntity entity, DamageCause source) {
        if (source == DamageCause.ENTITY_ATTACK) {
            return entityService.getEntityProperty(entity, CommonProperties.physical_damage_bonus_mult);
        }
        if (source == DamageCause.MAGIC) {
            return entityService.getEntityProperty(entity, CommonProperties.magic_damage_bonus_mult);
        }
        if (source == DamageCause.FIRE) {
            return entityService.getEntityProperty(entity, CommonProperties.fire_damage_bonus_mult);
        }
        if (source == DamageCause.LIGHTNING) {
            return entityService.getEntityProperty(entity, CommonProperties.lightning_damage_bonus_mult);
        }
        return 1;
    }

    public boolean damage(LivingEntity attacker, LivingEntity target, DamageCause cause, double damage, boolean knockback) {
        if (target.isDead() || target.getHealth() <= 0.0) {
            return false;
        }
        target.damage(damage, attacker);

        return true;
    }

    public boolean damage(LivingEntity target, DamageCause cause, double damage, boolean knockback) {
        if (target.isDead() || target.getHealth() <= 0.0) {
            return false;
        }
        target.damage(damage);

        return true;
    }

    public boolean canDamage(ISpigotCharacter caster, LivingEntity l) {
        if (l.getHealth() <= 0 || l.isDead() || l instanceof ArmorStand) {
            return false;
        }
        EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(caster.getEntity(), l, DamageCause.CUSTOM, 0);
        return !event.isCancelled();
    }
}
