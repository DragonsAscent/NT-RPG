package cz.neumimto.rpg.api.skills.types;

import cz.neumimto.rpg.api.skills.PlayerSkillContext;
import cz.neumimto.rpg.api.skills.mods.SkillContext;
import cz.neumimto.rpg.api.entity.players.IActiveCharacter;

public interface IActiveSkill<T extends IActiveCharacter> {

    void  cast(T character, PlayerSkillContext info, SkillContext modifier);
}
