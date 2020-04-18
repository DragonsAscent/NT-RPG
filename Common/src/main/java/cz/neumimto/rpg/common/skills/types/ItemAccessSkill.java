package cz.neumimto.rpg.common.skills.types;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import cz.neumimto.rpg.api.Rpg;
import cz.neumimto.rpg.api.configuration.ItemString;
import cz.neumimto.rpg.api.effects.EffectSourceType;
import cz.neumimto.rpg.api.effects.IEffectSource;
import cz.neumimto.rpg.api.entity.players.IActiveCharacter;
import cz.neumimto.rpg.api.inventory.InventoryService;
import cz.neumimto.rpg.api.items.ClassItem;
import cz.neumimto.rpg.api.items.ItemService;
import cz.neumimto.rpg.api.items.RpgItemType;
import cz.neumimto.rpg.api.skills.PlayerSkillContext;
import cz.neumimto.rpg.api.skills.SkillData;
import cz.neumimto.rpg.api.skills.SkillExecutionType;
import cz.neumimto.rpg.api.skills.SkillResult;
import cz.neumimto.rpg.api.skills.mods.SkillContext;
import cz.neumimto.rpg.api.skills.tree.SkillTree;
import cz.neumimto.rpg.api.skills.types.AbstractSkill;
import cz.neumimto.rpg.api.skills.utils.SkillLoadingErrors;

import javax.inject.Inject;
import java.util.*;


public class ItemAccessSkill extends AbstractSkill {

    @Inject
    private ItemService itemService;

    @Inject
    private InventoryService inventoryService;

    public ItemAccessSkill() {
        super();
    }

    @Override
    public void onPreUse(IActiveCharacter character, SkillContext skillContext) {
        skillContext.result(SkillResult.CANCELLED);
    }

    @Override
    public void skillLearn(IActiveCharacter IActiveCharacter, PlayerSkillContext context) {
        super.skillLearn(IActiveCharacter, context);
        resolveItemAccess(IActiveCharacter);

    }

    @Override
    public void skillUpgrade(IActiveCharacter IActiveCharacter, int level, PlayerSkillContext context) {
        super.skillUpgrade(IActiveCharacter, level, context);
        resolveItemAccess(IActiveCharacter);
    }

    @Override
    public void onCharacterInit(IActiveCharacter c, int level, PlayerSkillContext context) {
        super.onCharacterInit(c, level, context);
        resolveItemAccess(c);
    }

    @Override
    public void skillRefund(IActiveCharacter IActiveCharacter, PlayerSkillContext context) {
        super.skillRefund(IActiveCharacter, context);
        resolveItemAccess(IActiveCharacter);
    }

    private void resolveItemAccess(IActiveCharacter c) {
        c.updateItemRestrictions();
        inventoryService.invalidateGUICaches(c);
    }


    @Override
    public IEffectSource getType() {
        return EffectSourceType.ITEM_ACCESS_SKILL;
    }


    @Override
    public ItemAccessSkillData constructSkillData() {
        return new ItemAccessSkillData(getId());
    }

    @Override
    public <T extends SkillData> void loadSkillData(T skillData, SkillTree context, SkillLoadingErrors errors, Config c) {
        ItemAccessSkillData data = (ItemAccessSkillData) skillData;
        try {

            List<? extends Config> items = c.getConfigList("Items");
            for (Config item : items) {
                int level = item.getInt("level");
                List<String> citems = item.getStringList("items");
                for (String allowedWeapon : citems) {
                    ItemString parsed = ItemString.parse(allowedWeapon);
                    Optional<RpgItemType> type = itemService.getRpgItemType(parsed.itemId, parsed.variant);
                    if (type.isPresent()) {
                        ClassItem citem = itemService.createClassItemSpecification(type.get(), parsed.damage);

                        data.addItemType(level, citem);
                    }
                }
            }
        } catch (ConfigException e) {

        }
    }

    @Override
    public SkillExecutionType getSkillExecutionType() {
        return SkillExecutionType.PASSIVE;
    }

    public class ItemAccessSkillData extends SkillData {

        private Map<Integer, Set<ClassItem>> items = new HashMap<>();

        public ItemAccessSkillData(String skill) {
            super(skill);
        }

        public Map<Integer, Set<ClassItem>> getItems() {
            return items;
        }

        public void setItems(Map<Integer, Set<ClassItem>> items) {
            this.items = items;
        }

        public void addItemType(Integer level, ClassItem item) {
            Set<ClassItem> set = items.get(level);
            if (set == null) {
                set = new HashSet<>();
                set.add(item);
                items.put(level, set);
            } else {
                set.add(item);
            }
        }
    }
}
