package cz.neumimto.rpg.skills.scripting;

import cz.neumimto.core.ioc.Inject;
import cz.neumimto.rpg.NtRpgPlugin;
import cz.neumimto.rpg.players.IActiveCharacter;
import cz.neumimto.rpg.scripting.JSLoader;
import cz.neumimto.rpg.skills.ActiveSkill;
import cz.neumimto.rpg.skills.ExtendedSkillInfo;
import cz.neumimto.rpg.skills.SkillResult;
import cz.neumimto.rpg.skills.configs.ScriptSkillModel;
import cz.neumimto.rpg.skills.configs.SkillBehaviorType;
import cz.neumimto.rpg.skills.utils.SkillModifier;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.asset.Asset;

import java.io.IOException;
import java.util.Optional;

public class ExecutableScriptSkill extends ActiveSkill {

    ScriptExecutorSkill executor;

    @Inject
    private JSLoader jsLoader;

    @Inject
    private NtRpgPlugin plugin;

    public ExecutableScriptSkill() {
        this.executor = executor;


    }

    @Override
    public void init() {
        super.init();

    }

    @Override
    public SkillResult cast(IActiveCharacter character, ExtendedSkillInfo info, SkillModifier modifier) {
        return null;
    }

    public String bindScriptToTemplate(ScriptSkillModel model) {
        Asset asset = Sponge.getAssetManager().getAsset(plugin, "templates.active").get();
        try {
            String s = asset.readString();
            s = s.replaceAll("\\{\\{skill\\.id}}", model.getId());
            s = s.replaceAll("\\{\\{userContent}}", model.getScript());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}