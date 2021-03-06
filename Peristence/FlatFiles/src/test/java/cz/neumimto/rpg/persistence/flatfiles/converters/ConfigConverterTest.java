package cz.neumimto.rpg.persistence.flatfiles.converters;

import cz.neumimto.persistence.TestHelper;
import cz.neumimto.rpg.api.Rpg;
import cz.neumimto.rpg.api.RpgTests;
import cz.neumimto.rpg.api.entity.players.IActiveCharacter;
import cz.neumimto.rpg.api.inventory.InventoryService;
import cz.neumimto.rpg.api.persistance.model.CharacterBase;
import cz.neumimto.rpg.api.persistance.model.CharacterClass;
import cz.neumimto.rpg.api.persistance.model.DateKeyPair;
import cz.neumimto.rpg.api.persistance.model.EquipedSlot;
import cz.neumimto.rpg.api.skills.PlayerSkillContext;
import cz.neumimto.rpg.api.skills.mods.ActiveSkillPreProcessorWrapper;
import cz.neumimto.rpg.common.inventory.AbstractInventoryService;
import cz.neumimto.rpg.persistence.flatfiles.dao.FlatFilePlayerDao;
import cz.neumimto.rpg.persistence.model.CharacterBaseImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

class ConfigConverterTest {


    @BeforeAll
    public static void before() {
        new RpgTests() {
            @Override
            public InventoryService getInventoryService() {
                return new AbstractInventoryService() {
                    @Override
                    public Set<ActiveSkillPreProcessorWrapper> processItemCost(IActiveCharacter character, PlayerSkillContext info) {
                        return null;
                    }

                    @Override
                    public void initializeCharacterInventory(IActiveCharacter character) {

                    }

                    @Override
                    public EquipedSlot createEquipedSlot(String className, int slotId) {
                        return () -> slotId;
                    }
                };
            }
        };
        new File(Rpg.get().getWorkingDirectory()).mkdirs();

    }

    @Test
    public void testCharacterLoadAndSave() {
        CharacterBaseImpl characterBase = TestHelper.createCharacterBase();

        FlatFilePlayerDao flatFilePlayerDao = new FlatFilePlayerDao();
        flatFilePlayerDao.create(characterBase);

        CharacterBase loadded = flatFilePlayerDao.getCharacter(characterBase.getUuid(), characterBase.getName());

        Assertions.assertEquals(characterBase.getAttributePoints(), loadded.getAttributePoints());
        Assertions.assertEquals(characterBase.getAttributePointsSpent(), loadded.getAttributePointsSpent());
        Assertions.assertEquals(characterBase.getAttributes(), loadded.getAttributes());
        Assertions.assertEquals(characterBase.getBaseCharacterAttribute(), loadded.getBaseCharacterAttribute());
        Assertions.assertEquals(characterBase.getCharacterClasses(), loadded.getCharacterClasses());
        Assertions.assertEquals(characterBase.getCharacterSkills(), loadded.getCharacterSkills());
        Assertions.assertEquals(characterBase.getHealthScale(), loadded.getHealthScale());
        Assertions.assertEquals(characterBase.getId(), loadded.getId());

        Assertions.assertEquals(characterBase.getLastKnownPlayerName(), loadded.getLastKnownPlayerName());
        Assertions.assertEquals(characterBase.getMarkedForRemoval(), loadded.getMarkedForRemoval());
        Assertions.assertEquals(characterBase.getName(), loadded.getName());
        Assertions.assertEquals(characterBase.getUuid(), loadded.getUuid());
        Assertions.assertEquals(characterBase.getWorld(), loadded.getWorld());
        Assertions.assertEquals(characterBase.getX(), loadded.getX());
        Assertions.assertEquals(characterBase.getY(), loadded.getY());
        Assertions.assertEquals(characterBase.getZ(), loadded.getZ());

        if (characterBase.getInventoryEquipSlotOrder() != null) {
            Assertions.assertEquals(characterBase.getInventoryEquipSlotOrder().size(), loadded.getInventoryEquipSlotOrder().size());
            for (int i = 0; i < characterBase.getInventoryEquipSlotOrder().size(); i++) {
                Assertions.assertEquals(characterBase.getInventoryEquipSlotOrder().get(i).getSlotIndex(), loadded.getInventoryEquipSlotOrder().get(i).getSlotIndex());
            }
        }

        Assertions.assertNotNull(loadded.getUniqueSkillpoints());
        Assertions.assertEquals(characterBase.getUniqueSkillpoints().size(), loadded.getUniqueSkillpoints().size());

        for (Map.Entry<String, Set<DateKeyPair>> entry : characterBase.getUniqueSkillpoints().entrySet()) {
            Set<DateKeyPair> dateKeyPairs = loadded.getUniqueSkillpoints().get(entry.getKey());
            Assertions.assertNotNull(dateKeyPairs);
            Set<DateKeyPair> value = entry.getValue();

            Assertions.assertEquals(value.size(), dateKeyPairs.size());
            for (DateKeyPair dateKeyPair : value) {
                Optional<String> any = dateKeyPairs.stream().map(DateKeyPair::getSourceKey).filter(a -> dateKeyPair.getSourceKey().equalsIgnoreCase(a)).findAny();
                Assertions.assertTrue(any.isPresent());
            }
        }


        TestHelper.addClasses(characterBase);
        flatFilePlayerDao.update(characterBase);
        loadded = flatFilePlayerDao.getCharacter(characterBase.getUuid(), characterBase.getName());
        for (CharacterClass characterClass : characterBase.getCharacterClasses()) {
            Optional<CharacterClass> first = loadded.getCharacterClasses().stream().filter(a -> a.getName().equals(characterClass.getName())).findFirst();
            if (!first.isPresent()) {
                throw new IllegalStateException("");
            }
            CharacterClass loadedClass = first.get();

            Assertions.assertEquals(characterClass.getCharacterBase().getId(), loadedClass.getCharacterBase().getId());
            Assertions.assertEquals(characterClass.getExperiences(), loadedClass.getExperiences());
            Assertions.assertEquals(characterClass.getLevel(), loadedClass.getLevel());
            Assertions.assertEquals(characterClass.getSkillPoints(), loadedClass.getSkillPoints());
            Assertions.assertEquals(characterClass.getUsedSkillPoints(), loadedClass.getUsedSkillPoints());
        }


    }

}