package cz.neumimto.rpg.sponge.commands.elements;

import cz.neumimto.rpg.sponge.NtRpgPlugin;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.PatternMatchingCommandElement;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;

/**
 * Created by NeumimTo on 16.11.2017.
 */
public class LearnedSkillCommandElement extends PatternMatchingCommandElement {

    public LearnedSkillCommandElement(@Nullable Text key) {
        super(key);
    }

    @Override
    protected Iterable<String> getChoices(CommandSource source) {
        return NtRpgPlugin.GlobalScope.characterService.getCharacter((Player) source).getSkills().keySet();
    }

    @Override
    protected Object getValue(String choice) {
        return NtRpgPlugin.GlobalScope.skillService.getById(choice);
    }

}
