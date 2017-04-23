package cz.neumimto.rpg.inventory;

import cz.neumimto.rpg.NtRpgPlugin;
import cz.neumimto.rpg.configuration.Localization;
import cz.neumimto.rpg.effects.EffectSourceType;
import cz.neumimto.rpg.effects.IEffectSource;
import cz.neumimto.rpg.effects.IGlobalEffect;
import cz.neumimto.rpg.gui.Gui;
import cz.neumimto.rpg.players.IActiveCharacter;
import cz.neumimto.rpg.utils.ItemStackUtils;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by NeumimTo on 16.1.2016.
 */
public class Charm extends HotbarObject {

    private Map<IGlobalEffect, String> effects;
    private Map<ItemRestriction,Object> restrictionSet = new HashMap<>();


    public Charm() {
        type = HotbarObjectTypes.CHARM;
        this.effects = new HashMap<>();
    }


    public Map<ItemRestriction,Object> getRestrictions() {
        return restrictionSet;
    }

    @Override
    public void onRightClick(IActiveCharacter character) {
        Gui.sendMessage(character, Localization.CHARM_INFO);
    }

    @Override
    public void onLeftClick(IActiveCharacter character) {
        onRightClick(character);
    }

    @Override
    public void onEquip(ItemStack is, IActiveCharacter character) {
        super.onEquip(is, character);
        if (effects == null) {
            effects = NtRpgPlugin.GlobalScope.inventorySerivce.getItemEffects(is);
        }
        NtRpgPlugin.GlobalScope.effectService.applyGlobalEffectsAsEnchantments(effects, character, this);
    }

    @Override
    public void onUnEquip(IActiveCharacter character) {
        if (effects != null) {
            NtRpgPlugin.GlobalScope.effectService.removeGlobalEffectsAsEnchantments(effects, character, this);
        }
    }

    public Map<IGlobalEffect, String> getEffects() {
        return effects;
    }

    public void setEffects(Map<IGlobalEffect, String> effects) {
        this.effects = effects;
    }

    @Override
    public IEffectSource getType() {
        return EffectSourceType.CHARM;
    }
}
