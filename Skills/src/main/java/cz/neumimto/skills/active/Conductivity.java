package cz.neumimto.skills.active;

import cz.neumimto.rpg.api.ResourceLoader;
import cz.neumimto.rpg.api.entity.players.IActiveCharacter;
import cz.neumimto.rpg.api.skills.PlayerSkillContext;
import cz.neumimto.rpg.api.skills.SkillNodes;
import cz.neumimto.rpg.api.skills.mods.SkillContext;
import cz.neumimto.rpg.api.skills.tree.SkillType;
import cz.neumimto.rpg.api.skills.types.ActiveSkill;

import javax.inject.Singleton;

/**
 * Created by NeumimTo on 1.8.2017.
 */
@Singleton
@ResourceLoader.Skill("ntrpg:conductivity")
public class Conductivity extends ActiveSkill {

    @Override
    public void init() {
        super.init();
        settings.addNode(SkillNodes.DURATION, 10000, 500);
        settings.addNode(SkillNodes.RADIUS, 10, 1);
        settings.addNode(SkillNodes.RANGE, 15, 1);
        addSkillType(SkillType.CURSE);
        addSkillType(SkillType.DECREASED_RESISTANCE);
    }

    @Override
    public void cast(IActiveCharacter character, PlayerSkillContext info, SkillContext modifier) {

    }
}
