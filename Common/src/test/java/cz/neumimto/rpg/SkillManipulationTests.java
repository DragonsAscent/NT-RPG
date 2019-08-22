package cz.neumimto.rpg;

import cz.neumimto.rpg.api.configuration.PluginConfig;
import cz.neumimto.rpg.api.entity.players.ICharacterService;
import cz.neumimto.rpg.api.entity.players.classes.ClassDefinition;
import cz.neumimto.rpg.api.entity.players.classes.PlayerClassData;
import cz.neumimto.rpg.api.events.EventFactoryService;
import cz.neumimto.rpg.api.persistance.model.CharacterBase;
import cz.neumimto.rpg.api.persistance.model.CharacterClass;
import cz.neumimto.rpg.api.skills.ISkill;
import cz.neumimto.rpg.api.skills.PlayerSkillContext;
import cz.neumimto.rpg.api.skills.SkillData;
import cz.neumimto.rpg.api.skills.SkillDependency;
import cz.neumimto.rpg.api.skills.tree.SkillTree;
import cz.neumimto.rpg.api.utils.ActionResult;
import cz.neumimto.rpg.common.entity.TestCharacter;
import cz.neumimto.rpg.common.entity.players.ActiveCharacter;
import cz.neumimto.rpg.junit.NtRpgExtension;
import cz.neumimto.rpg.junit.TestGuiceModule;
import cz.neumimto.rpg.persistance.model.JPACharacterBase;
import name.falgout.jeffrey.testing.junit.guice.GuiceExtension;
import name.falgout.jeffrey.testing.junit.guice.IncludeModule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import javax.inject.Inject;
import java.util.UUID;

@ExtendWith({NtRpgExtension.class, GuiceExtension.class})
@IncludeModule(TestGuiceModule.class)
public class SkillManipulationTests {

    @Inject
    private ICharacterService characterService;

    @Inject
    private EventFactoryService eventFactoryService;


    ISkill main;
    ISkill conflicting;
    ISkill hardDepending2;
    ISkill hardDepending1;
    ISkill softDepending0;
    ISkill softDepending1;

    ClassDefinition classDefinition;

    CharacterClass characterClass;

    CharacterBase characterBase;

    SkillData skillData;
    SkillData sconflicting;
    SkillData shardDepending2;
    SkillData shardDepending1;
    SkillData ssoftDepending0;
    SkillData ssoftDepending1;

    ActiveCharacter character;
    PlayerClassData playerClassData;


    @BeforeEach
    public void before() throws Exception {

        //lets not invoke constructor
        PluginConfig o = (PluginConfig) TestHelper.getUnsafe().allocateInstance(PluginConfig.class);
        o.PRIMARY_CLASS_TYPE = "Primary";

        main = TestHelper.createMockSkill("main");

        conflicting = TestHelper.createMockSkill("conflicting");
        hardDepending2 = TestHelper.createMockSkill("hardDepending2");
        hardDepending1 = TestHelper.createMockSkill("hardDepending1");
        softDepending0 = TestHelper.createMockSkill("softDepending0");
        softDepending1 = TestHelper.createMockSkill("softDepending1");

        classDefinition = TestHelper.createClassDefinition();

        characterClass = TestHelper.createCharacterClass();
        characterClass.setSkillPoints(1);
        characterClass.setUsedSkillPoints(1);
        characterClass.setLevel(0);

        skillData = new SkillData(main.getId());
        skillData.setSkill(main);


        sconflicting = new SkillData(conflicting.getId());
        sconflicting.setSkill(conflicting);


        shardDepending2 = new SkillData(hardDepending2.getId());
        shardDepending2.setSkill(hardDepending2);


        shardDepending1 = new SkillData(hardDepending1.getId());
        shardDepending1.setSkill(hardDepending1);


        ssoftDepending0 = new SkillData(softDepending0.getId());
        ssoftDepending0.setSkill(softDepending0);


        ssoftDepending1 = new SkillData(softDepending1.getId());
        ssoftDepending1.setSkill(softDepending1);

        skillData.getConflicts().add(sconflicting);
        skillData.getHardDepends().add(new SkillDependency(shardDepending1, 1));
        skillData.getHardDepends().add(new SkillDependency(shardDepending2, 2));
        skillData.getSoftDepends().add(new SkillDependency(ssoftDepending0, 1));
        skillData.getSoftDepends().add(new SkillDependency(ssoftDepending1, 2));


        TestHelper.setField(classDefinition, "skillTree", new SkillTree() {{
            getSkills().put(main.getId(), skillData);
            getSkills().put(conflicting.getId(), sconflicting);
            getSkills().put(hardDepending2.getId(), shardDepending2);
            getSkills().put(hardDepending1.getId(), shardDepending1);
            getSkills().put(softDepending0.getId(), ssoftDepending0);
            getSkills().put(softDepending1.getId(), ssoftDepending1);
        }});

        characterBase = new JPACharacterBase();
        characterBase.getCharacterClasses().add(characterClass);

        character = new TestCharacter(UUID.randomUUID(), characterBase, 0);

        playerClassData = new PlayerClassData(classDefinition, characterClass);
        character.addClass(playerClassData);
    }

    @Test
    public void learnSkill() throws Exception {
        characterService.learnSkill(character, playerClassData, main);

        Assertions.assertEquals(0, characterClass.getSkillPoints());
        Assertions.assertEquals(2, characterClass.getUsedSkillPoints());
        Assertions.assertTrue(character.hasSkill(main.getId()));
        Assertions.assertSame(character.getPrimaryClass(), playerClassData);
        Mockito.verify(main, Mockito.times(1)).skillLearn(Mockito.any());

    }

    @Test
    public void mayLearnSkill_NotEnoughSkillpoints() throws Exception {
        characterClass.setSkillPoints(0);
        ActionResult actionResult = characterService.canLearnSkill(character, classDefinition, main);

        Assertions.assertTrue(!actionResult.isOk());
    }

    @Test
    public void mayLearnSkill_WrongClassSkillOrigin() throws Exception {
        ActionResult actionResult = characterService.canLearnSkill(character, new ClassDefinition("test", "Primary"), main);

        Assertions.assertTrue(!actionResult.isOk());
    }

    @Test
    public void mayLearnSkill_WrongClassSkillBothNull() throws Exception {
        classDefinition.setSkillTree(null);
        ActionResult actionResult = characterService.canLearnSkill(character, new ClassDefinition("test", "Primary"), main);

        Assertions.assertTrue(!actionResult.isOk());
    }

    @Test
    public void checkSkillSoftDependencies_all_ok() throws Exception {
        character.addSkill(ssoftDepending0.getSkillId(), new PlayerSkillContext(classDefinition, ssoftDepending0.getSkill(), character) {{
            setLevel(1);
        }});
        character.addSkill(ssoftDepending1.getSkillId(), new PlayerSkillContext(classDefinition, ssoftDepending1.getSkill(), character) {{
            setLevel(2);
        }});

        boolean result = characterService.hasSoftSkillDependencies(character, skillData);
        Assertions.assertTrue(result);
    }

    @Test
    public void checkSkillSoftDependencies_one_ok() throws Exception {
        character.addSkill(ssoftDepending0.getSkillId(), new PlayerSkillContext(classDefinition, ssoftDepending0.getSkill(), character) {{
            setLevel(1);
        }});

        boolean result = characterService.hasSoftSkillDependencies(character, skillData);
        Assertions.assertTrue(result);
    }

    @Test
    public void checkSkillSoftDependencies_none_ok() throws Exception {
        character.addSkill(shardDepending1.getSkillId(), new PlayerSkillContext(classDefinition, shardDepending1.getSkill(), character) {{
            setLevel(1);
        }});

        boolean result = characterService.hasSoftSkillDependencies(character, skillData);
        Assertions.assertFalse(result);
    }

    @Test
    public void checkSkillSoftDependencies_empty() throws Exception {
        boolean result = characterService.hasSoftSkillDependencies(character, new SkillData(""));
        Assertions.assertTrue(result);
    }

    @Test
    public void checkSkillHardDependencies_empty() throws Exception {
        boolean result = characterService.hasHardSkillDependencies(character, new SkillData(""));
        Assertions.assertTrue(result);
    }

    @Test
    public void checkSkillHardDependencies_missing() throws Exception {
        boolean result = characterService.hasHardSkillDependencies(character, skillData);
        Assertions.assertFalse(result);
    }

    @Test
    public void checkSkillHardDependencies_all_ok() throws Exception {
        character.addSkill(shardDepending1.getSkillId(), new PlayerSkillContext(classDefinition, shardDepending1.getSkill(), character) {{
            setLevel(1);
        }});
        character.addSkill(shardDepending2.getSkillId(), new PlayerSkillContext(classDefinition, shardDepending2.getSkill(), character) {{
            setLevel(2);
        }});
        boolean result = characterService.hasHardSkillDependencies(character, skillData);
        Assertions.assertTrue(result);
    }

    @Test
    public void checkSkillHardDependencies_only_one() throws Exception {
        character.addSkill(shardDepending1.getSkillId(), new PlayerSkillContext(classDefinition, shardDepending1.getSkill(), character) {{
            setLevel(1);
        }});
        character.addSkill(shardDepending2.getSkillId(), new PlayerSkillContext(classDefinition, shardDepending2.getSkill(), character) {{
            setLevel(1);
        }});
        boolean result = characterService.hasHardSkillDependencies(character, skillData);
        Assertions.assertFalse(result);
    }


    @Test
    public void checkSkillConflictingDependencies_empty() throws Exception {
        boolean result = !characterService.hasConflictingSkillDepedencies(character, new SkillData(""));
        Assertions.assertTrue(result);
    }

    @Test
    public void checkSkillConflictingDependencies_has_conflicting_node() throws Exception {
        character.addSkill(sconflicting.getSkillId(), new PlayerSkillContext(classDefinition, sconflicting.getSkill(), character) {{
            setLevel(1);
        }});
        boolean result = characterService.hasConflictingSkillDepedencies(character, new SkillData(""));
        Assertions.assertFalse(result);
    }

    @Test
    public void mayLearnSkill_ok() {
        character.addSkill(shardDepending1.getSkillId(), new PlayerSkillContext(classDefinition, shardDepending1.getSkill(), character) {{
            setLevel(1);
        }});
        character.addSkill(shardDepending2.getSkillId(), new PlayerSkillContext(classDefinition, shardDepending2.getSkill(), character) {{
            setLevel(2);
        }});
        character.addSkill(ssoftDepending1.getSkillId(), new PlayerSkillContext(classDefinition, ssoftDepending1.getSkill(), character) {{
            setLevel(2);
        }});
        ActionResult actionResult = characterService.canLearnSkill(character, classDefinition, main);

        Assertions.assertTrue(actionResult.isOk());
    }


}