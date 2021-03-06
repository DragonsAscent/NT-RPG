package cz.neumimto.rpg.spigot.entities.players;

import cz.neumimto.rpg.api.persistance.model.CharacterBase;
import cz.neumimto.rpg.api.skills.ISkill;
import cz.neumimto.rpg.common.entity.players.ActiveCharacter;
import cz.neumimto.rpg.spigot.entities.ISpigotEntity;
import cz.neumimto.rpg.spigot.entities.players.party.SpigotParty;
import cz.neumimto.rpg.spigot.gui.SpigotSkillTreeViewModel;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpigotCharacter extends ActiveCharacter<Player, SpigotParty> implements ISpigotCharacter {

    private ISkill soedc;
    private Map<String, SpigotSkillTreeViewModel> skillTreeviewLocation = new HashMap<>();

    public SpigotCharacter(UUID uuid, CharacterBase base, int propertyCount) {
        super(uuid, base, propertyCount);
    }

    @Override
    public void sendMessage(String message) {
        message = ChatColor.translateAlternateColorCodes('&', message);
        getPlayer().spigot().sendMessage(ChatMessageType.CHAT, TextComponent.fromLegacyText(message));
    }

    @Override
    public void sendNotification(String message) {
        getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
    }

    @Override
    public Map<String, SpigotSkillTreeViewModel> getSkillTreeViewLocation() {
        return skillTreeviewLocation;
    }

    @Override
    public Player getEntity() {
        return getPlayer();
    }

    @Override
    public ISkill skillOrEffectDamageCause() {
        return soedc;
    }

    @Override
    public ISpigotEntity setSkillOrEffectDamageCause(ISkill rpgElement) {
        soedc = rpgElement;
        return this;
    }

    @Override
    public boolean isDetached() {
        return getPlayer() == null;
    }

    @Override
    public Player getPlayer() {
        return Bukkit.getPlayer(getUUID());
    }

    @Override
    public SpigotSkillTreeViewModel getLastTimeInvokedSkillTreeView() {
        for (SpigotSkillTreeViewModel skillTreeViewModel : skillTreeviewLocation.values()) {
            if (skillTreeViewModel.isCurrent()) {
                return skillTreeViewModel;
            }
        }
        return null;
    }

}
