package cz.neumimto.rpg.api.skills.scripting;

import cz.neumimto.rpg.api.skills.PlayerSkillContext;
import cz.neumimto.rpg.api.entity.IEffectConsumer;

/**
 * Created by NeumimTo on 7.10.2018.
 */
@FunctionalInterface
@JsBinding(JsBinding.Type.CLASS)
public interface PassiveScriptSkillHandler {

    void init(IEffectConsumer consumer, PlayerSkillContext info, SkillScriptContext context);
}
