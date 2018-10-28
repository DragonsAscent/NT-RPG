package cz.neumimto.rpg.entities;

import static cz.neumimto.rpg.Log.warn;

import cz.neumimto.core.ioc.Inject;
import cz.neumimto.core.ioc.Singleton;
import cz.neumimto.rpg.IEntity;
import cz.neumimto.rpg.IRpgElement;
import static cz.neumimto.rpg.NtRpgPlugin.pluginConfig;
import cz.neumimto.rpg.effects.EffectService;
import cz.neumimto.rpg.effects.IEffectConsumer;
import cz.neumimto.rpg.events.skills.SkillHealEvent;
import cz.neumimto.rpg.players.CharacterService;
import cz.neumimto.rpg.players.properties.PropertyService;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.Living;

import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by NeumimTo on 19.12.2015.
 */
@Singleton
public class EntityService {


	private HashMap<UUID, IMob> entityHashMap = new HashMap<>();

	@Inject
	private CharacterService service;

	@Inject
	private MobSettingsDao dao;

	@Inject
	private PropertyService propertyService;

	@Inject
	private EffectService effectService;

	public IEntity get(Entity id) {
		if (id.getType() == EntityTypes.PLAYER) {
			return service.getCharacter(id.getUniqueId());
		}
		IMob iEntity = entityHashMap.get(id.getUniqueId());
		if (iEntity == null) {
			iEntity = new NEntity((Living) id);
			iEntity.setExperiences(-1);
			entityHashMap.put(id.getUniqueId(), iEntity);
			MobsConfig dimmension = dao.getCache().getDimmension(id.getLocation().getExtent().getName());
			if (!pluginConfig.OVERRIDE_MOBS && dimmension != null) {
				Double aDouble = dimmension.getHealth().get(id.getType());
				if (aDouble == null) {
					warn("No max health configured for " + id.getType().getId() + " in world " + id.getLocation().getExtent().getName());
				} else {
					id.offer(Keys.MAX_HEALTH, aDouble);
					id.offer(Keys.HEALTH, aDouble);
				}
			}
		}

		return iEntity;

	}

	public void remove(UUID e) {
		if (entityHashMap.containsKey(e)) {
			IMob iMob = entityHashMap.get(e);
			effectService.removeAllEffects(iMob);
			entityHashMap.remove(e);
			iMob.detach();
		}
	}

	public void remove(Collection<Entity> l) {
		for (Entity a : l) {
			UUID uniqueId = a.getUniqueId();
			remove(uniqueId);
		}
	}

	public double getMobDamage(Entity type) {
		MobsConfig dimmension = dao.getCache().getDimmension(type.getLocation().getExtent().getName());
		if (dimmension != null) {
			Double aDouble = dimmension.getDamage().get(type.getType());
			if (aDouble == null) {
				warn("No max experience drop configured for " + type.getType().getId()
						+ " in world " + type.getLocation().getExtent().getName());
				aDouble = 0D;
			}
			return aDouble;
		}
		return 0;
	}

	public double getExperiences(Entity type) {
		MobsConfig dimmension = dao.getCache().getDimmension(type.getLocation().getExtent().getName());
		if (dimmension != null) {
			Double aDouble = dimmension.getExperiences().get(type.getType());
			if (aDouble == null) {
				warn("No max experience drop configured for " + type.getType().getId()
						+ " in world " + type.getLocation().getExtent().getName());
				aDouble = 0D;
			}
			return aDouble;
		}
		return 0;
	}

	public float getEntityProperty(IEffectConsumer entity, int id) {
		return Math.min(entity.getProperty(id), propertyService.getMaxPropertyValue(id));
	}

	public void setEntityProperty(IEntity nEntity, int id, Float value) {
		nEntity.setProperty(id, value);
	}

	public void addToEntityProperty(IEntity nEntity, int id, Float value) {
		Float f = getEntityProperty(nEntity, id);
		setEntityProperty(nEntity, id, f == null ? value : f + value);
	}


	/**
	 * Heals the entity and`fire an event
	 *
	 * @param entity
	 * @param healedamount
	 * @return healed hp
	 */
	public double healEntity(IEntity entity, float healedamount, IRpgElement element) {
		if (entity.getHealth().getValue() == entity.getHealth().getMaxValue()) {
			return 0;
		}
		SkillHealEvent event = null;
		if (entity.getHealth().getValue() + healedamount > entity.getHealth().getMaxValue()) {
			healedamount = (float) ((entity.getHealth().getValue() + healedamount) - entity.getHealth().getMaxValue());
		}
		event = new SkillHealEvent(entity, healedamount, element);
		Sponge.getGame().getEventManager().post(event);
		if (event.isCancelled() || event.getAmount() <= 0) {
			return 0;
		}
		return setEntityHealth(event.getEntity(), event.getAmount());
	}

	/**
	 * sets character's hp to choosen amount.
	 *
	 * @param entity
	 * @param amount
	 * @return difference
	 */
	public double setEntityHealth(IEntity entity, double amount) {
		if (entity.getHealth().getValue() + amount > entity.getHealth().getMaxValue()) {
			double k = entity.getHealth().getMaxValue() - (entity.getHealth().getValue() + amount);
			setEntityToFullHealth(entity);
			return k;
		}
		entity.getHealth().setValue(entity.getHealth().getValue() + amount);
		return amount;
	}

	/**
	 * sets character to its full health
	 *
	 * @param entityToFullHealth
	 */
	public void setEntityToFullHealth(IEntity entityToFullHealth) {
		entityToFullHealth.getHealth().setValue(entityToFullHealth.getHealth().getMaxValue());
	}
}
