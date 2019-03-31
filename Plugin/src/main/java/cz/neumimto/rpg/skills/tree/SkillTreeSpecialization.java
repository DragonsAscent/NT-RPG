package cz.neumimto.rpg.skills.tree;

import static cz.neumimto.rpg.NtRpgPlugin.pluginConfig;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import cz.neumimto.core.localization.Arg;
import cz.neumimto.rpg.configuration.Localizations;
import cz.neumimto.rpg.players.IActiveCharacter;
import cz.neumimto.rpg.skills.*;
import cz.neumimto.rpg.skills.parents.PassiveSkill;
import cz.neumimto.rpg.skills.utils.SkillLoadingErrors;
import cz.neumimto.rpg.utils.Utils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.text.Text;

import java.util.*;

/**
 * Created by NeumimTo on 16.8.17.
 */

public class SkillTreeSpecialization extends PassiveSkill {

	public SkillTreeSpecialization() {
		super();
		SkillSettings settings = new SkillSettings();
		addSkillType(SkillType.PATH);
		setIcon(ItemTypes.BOOK);
		super.setSettings(settings);
	}

	@Override
	public void skillLearn(IActiveCharacter IActiveCharacter) {
		if (pluginConfig.PLAYER_CHOOSED_SKILLTREE_SPECIALIZATION_GLOBAL_MESSAGE) {
			Text t = Localizations.PLAYER_CHOOSED_SKILLTREE_PATH_GLOBAL_MESSAGE_CONTENT.toText(
					Arg.arg("player", IActiveCharacter.getPlayer().getName())
							.with("character", IActiveCharacter.getName())
							.with("path", getName()));
			game.getServer().getOnlinePlayers().forEach(p -> p.sendMessage(t));
		}
		onCharacterInit(IActiveCharacter, 1);
	}

	@Override
	public void applyEffect(PlayerSkillContext info, IActiveCharacter character) {

	}

	@Override
	public void onCharacterInit(IActiveCharacter c, int level) {
		super.onCharacterInit(c, level);
		PlayerSkillContext skillInfo = c.getSkillInfo(this);
		SkillData skillData = skillInfo.getSkillData();
		SkillPathData pdata = (SkillPathData) skillData;

		if (pdata.getEnterCommands() != null) {
			Map<String, String> args = new HashMap<>();
			Player pl = c.getPlayer();
			args.put("player", pl.getName());
			args.put("uuid", pl.getUniqueId().toString());
			Utils.executeCommandBatch(args, pdata.getEnterCommands());
		}

		for (Map.Entry<String, Integer> entry : pdata.getSkillBonus().entrySet()) {
			PlayerSkillContext skill = c.getSkill(entry.getKey());
			skill.setBonusLevel(skill.getBonusLevel() + entry.getValue());
		}

	}

	@Override
	public void skillRefund(IActiveCharacter c) {
		PlayerSkillContext skillInfo = c.getSkillInfo(this);
		SkillData skillData = skillInfo.getSkillData();
		SkillPathData pdata = (SkillPathData) skillData;

		if (pdata.getEnterCommands() != null) {
			Map<String, String> args = new HashMap<>();
			Player player = c.getPlayer();
			args.put("player", player.getName());
			args.put("uuid", player.getUniqueId().toString());
			Utils.executeCommandBatch(args, pdata.getExitCommands());
		}
	}


	@Override
	public SkillPathData constructSkillData() {
		return new SkillPathData(getId());
	}

	@Override
	public <T extends SkillData> void loadSkillData(T skillData, SkillTree skillTree, SkillLoadingErrors logger, Config c) {
		SkillPathData pdata = (SkillPathData) skillData;
		try {
			List<String> ec = c.getStringList("EnterCommands");
			pdata.getEnterCommands().addAll(ec);
		} catch (ConfigException e) {

		}
		try {
			List<String> ec = c.getStringList("ExitCommands");
			pdata.getExitCommands().addAll(ec);
		} catch (ConfigException e) {

		}

		try {
			int tier = c.getInt("Tier");
			pdata.setTier(tier);
		} catch (ConfigException e) {
			logger.log("Found SkillPath in the tree \"" + skillTree.getId() + "\" but no tier defined, setting to 0");
		}

		try {
			pdata.setSkillPointsRequired(c.getInt("SkillPointsRequired"));
		} catch (ConfigException e) {
			logger.log("Found SkillPath in the tree \"" + skillTree.getId() + "\" but no permissions defined, setting to 1");
			pdata.setSkillPointsRequired(1);
		}
		pdata.setMaxSkillLevel(1);
		try {
			List<? extends Config> skillBonus = c.getConfigList("SkillBonus");
			for (Config s : skillBonus) {
				try {
					String skill = s.getString("Skill");
					int levels = s.getInt("Levels");
					pdata.addSkillBonus(skill, levels);
				} catch (ConfigException e) {
					logger.log(
							"Found SkillPath.SkillBonus in the tree \"" + skillTree.getId() + "\" missing \"skill\" or \"level\" configuration "
									+ "node");
				}

			}
		} catch (ConfigException e) {
		}
		try {
			String a = c.getString("ItemIcon");
			Optional<ItemType> type = Sponge.getRegistry().getType(ItemType.class, a);
			type.ifPresent(this::setIcon);
		} catch (ConfigException e) {

		}

		pdata.setCombination(null);
		pdata.setMaxSkillLevel(1);
	}
}
