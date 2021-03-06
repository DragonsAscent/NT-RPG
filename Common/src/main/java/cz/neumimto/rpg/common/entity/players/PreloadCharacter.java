/*
 *     Copyright (c) 2015, NeumimTo https://github.com/NeumimTo
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package cz.neumimto.rpg.common.entity.players;

import cz.neumimto.rpg.api.Rpg;
import cz.neumimto.rpg.api.effects.IEffect;
import cz.neumimto.rpg.api.effects.IEffectContainer;
import cz.neumimto.rpg.api.entity.EntityHand;
import cz.neumimto.rpg.api.entity.IReservable;
import cz.neumimto.rpg.api.entity.players.IActiveCharacter;
import cz.neumimto.rpg.api.entity.players.classes.ClassDefinition;
import cz.neumimto.rpg.api.entity.players.classes.PlayerClassData;
import cz.neumimto.rpg.api.entity.players.party.IParty;
import cz.neumimto.rpg.api.inventory.RpgInventory;
import cz.neumimto.rpg.api.items.RpgItemStack;
import cz.neumimto.rpg.api.items.RpgItemType;
import cz.neumimto.rpg.api.persistance.model.CharacterBase;
import cz.neumimto.rpg.api.persistance.model.EquipedSlot;
import cz.neumimto.rpg.api.skills.ISkill;
import cz.neumimto.rpg.api.skills.PlayerSkillContext;
import cz.neumimto.rpg.api.skills.preprocessors.InterruptableSkillPreprocessor;
import cz.neumimto.rpg.api.skills.tree.SkillTreeSpecialization;
import cz.neumimto.rpg.common.entity.PropertyServiceImpl;

import java.util.*;


/**
 * Created by NeumimTo on 23.7.2015.
 */
public abstract class PreloadCharacter<T, P extends IParty> implements IActiveCharacter<T, P> {

    private static float[] characterProperties = new float[PropertyServiceImpl.LAST_ID];
    protected UUID uuid;

    private boolean isusinggui;

    public PreloadCharacter(UUID uuid) {
        this.uuid = uuid;
    }


    @Override
    public void setChanneledSkill(InterruptableSkillPreprocessor o) {

    }

    @Override
    public Optional<InterruptableSkillPreprocessor> getChanneledSkill() {
        return Optional.empty();
    }

    @Override
    public boolean isFriendlyTo(IActiveCharacter character) {
        return false;
    }

    @Override
    public void setCharacterLevelProperty(int index, float value) {

    }

    @Override
    public float[] getSecondaryProperties() {
        return characterProperties;
    }

    @Override
    public void setSecondaryProperties(float[] arr) {

    }

    @Override
    public Map<String, Integer> getTransientAttributes() {
        return null;
    }

    @Override
    public boolean isInvulnerable() {
        return Rpg.get().getPluginConfig().ALLOW_COMBAT_FOR_CHARACTERLESS_PLAYERS;
    }

    @Override
    public void setInvulnerable(boolean b) {

    }

    @Override
    public float getCharacterPropertyWithoutLevel(int index) {
        return 0;
    }

    @Override
    public double getBaseWeaponDamage(RpgItemType type) {
        return 0;
    }

    @Override
    public String getName() {
        return "None";
    }

    @Override
    public int getLevel() {
        return 0;
    }

    @Override
    public boolean isStub() {
        return true;
    }

    @Override
    public float[] getPrimaryProperties() {
        return characterProperties;
    }

    @Override
    public boolean canUse(RpgItemType weaponItemType, EntityHand type) {
        return false;
    }

    @Override
    public double getWeaponDamage() {
        return 0;
    }

    @Override
    public void setWeaponDamage(double damage) {

    }

    @Override
    public double getArmorValue() {
        return 0;
    }

    @Override
    public void setArmorValue(double value) {

    }

    @Override
    public boolean hasPreferedDamageType() {
        return false;
    }

    @Override
    public String getDamageType() {
        return "none";
    }

    @Override
    public void setDamageType(String damageType) {

    }

    @Override
    public void updateLastKnownLocation(int x, int y, int z, java.lang.String name) {

    }

    @Override
    public Map<java.lang.String, IEffectContainer<Object, IEffect<Object>>> getEffectMap() {
        return Collections.emptyMap();
    }

    @Override
    public float getProperty(int index) {
        return 0;
    }

    @Override
    public void setProperty(int index, float value) {

    }

    @Override
    public IReservable getMana() {
        return null;
    }

    @Override
    public void setMana(IReservable mana) {

    }

    @Override
    public IReservable getHealth() {
        return null;
    }

    @Override
    public void setHealth(IReservable health) {

    }

    @Override
    public int getAttributePoints() {
        return 0;
    }

    @Override
    public void setAttributePoints(int attributePoints) {

    }

    @Override
    public int getAttributeValue(String name) {
        return 0;
    }

    @Override
    public Map<String, Long> getCooldowns() {
        return Collections.emptyMap();
    }

    @Override
    public boolean hasCooldown(String thing) {
        return true;
    }


    @Override
    public Set<RpgItemType> getAllowedArmor() {
        return Collections.emptySet();
    }

    @Override
    public boolean canWear(RpgItemType armor) {
        return false;
    }

    @Override
    public Map<RpgItemType, Double> getAllowedWeapons() {
        return Collections.emptyMap();
    }

    @Override
    public Map<String, Double> getProjectileDamages() {
        return Collections.emptyMap();
    }

    @Override
    public IActiveCharacter updateItemRestrictions() {
        return this;
    }

    @Override
    public CharacterBase getCharacterBase() {
        return null;
    }

    @Override
    public PlayerClassData getPrimaryClass() {
        return null;
    }

    @Override
    public double getBaseProjectileDamage(String id) {
        return 0;
    }

    @Override
    public Collection<IEffectContainer<Object, IEffect<Object>>> getEffects() {
        return Collections.emptySet();
    }

    @Override
    public boolean hasEffect(String cl) {
        return false;
    }

    @Override
    public void addEffect(IEffect effect) {

    }

    @Override
    public void addEffect(IEffectContainer<Object, IEffect<Object>> IEffectContainer) {

    }

    @Override
    public void removeEffect(String cl) {

    }


    @Override
    public Map<String, PlayerSkillContext> getSkills() {
        return Collections.emptyMap();
    }

    @Override
    public PlayerSkillContext getSkillInfo(ISkill skill) {
        return PlayerSkillContext.EMPTY;
    }

    @Override
    public boolean hasSkill(String name) {
        return false;
    }

    @Override
    public PlayerSkillContext getSkillInfo(String s) {
        return PlayerSkillContext.EMPTY;
    }

    @Override
    public boolean isSilenced() {
        return true;
    }

    @Override
    public void addSkill(String name, PlayerSkillContext info) {

    }

    @Override
    public PlayerSkillContext getSkill(String skillName) {
        return PlayerSkillContext.EMPTY;
    }

    @Override
    public void removeAllSkills() {

    }

    @Override
    public boolean hasParty() {
        return false;
    }

    @Override
    public boolean isInPartyWith(IActiveCharacter character) {
        return false;
    }


    @Override
    public Map<String, PlayerClassData> getClasses() {
        return Collections.EMPTY_MAP;
    }

    @Override
    public P getParty() {
        return null;
    }

    @Override
    public void setParty(P party) {

    }

    @Override
    public P getPendingPartyInvite() {
        return null;
    }

    @Override
    public void setPendingPartyInvite(P party) {

    }

    @Override
    public boolean isUsingGuiMod() {
        return isusinggui;
    }

    @Override
    public void setUsingGuiMod(boolean b) {
        isusinggui = b;
    }

    @Override
    public boolean isPartyLeader() {
        return false;
    }

    @Override
    public boolean hasClass(ClassDefinition configClass) {
        return false;
    }

    @Override
    public List<Integer> getSlotsToReinitialize() {
        return Collections.emptyList();
    }

    @Override
    public void setSlotsToReinitialize(List<Integer> slotsToReinitialize) {

    }


    @Override
    public boolean isDetached() {
        return true;
    }

    @Override
    public void addSkillTreeSpecialization(SkillTreeSpecialization specialization) {

    }

    @Override
    public double getExperienceBonusFor(String name, String type) {
        return 0;
    }

    @Override
    public void addClass(PlayerClassData playerClassData) {

    }

    @Override
    public void removeSkillTreeSpecialization(SkillTreeSpecialization specialization) {

    }

    @Override
    public boolean hasSkillTreeSpecialization(SkillTreeSpecialization specialization) {
        return false;
    }

    @Override
    public Set<SkillTreeSpecialization> getSkillTreeSpecialization() {
        return Collections.emptySet();
    }

    @Override
    public Set<EquipedSlot> getSlotsCannotBeEquiped() {
        return Collections.emptySet();
    }

    @Override
    public RpgItemStack getMainHand() {
        return null;
    }

    @Override
    public void setMainHand(RpgItemStack customItem, int slot) {

    }

    @Override
    public int getMainHandSlotId() {
        return -1;
    }

    @Override
    public RpgItemStack getOffHand() {
        return null;
    }

    @Override
    public void setOffHand(RpgItemStack customItem) {

    }

    @Override
    public void restartAttributeGuiSession() {

    }

    @Override
    public boolean requiresDamageRecalculation() {
        return false;
    }

    @Override
    public void setRequiresDamageRecalculation(boolean k) {

    }

    @Override
    public int getLastHotbarSlotInteraction() {
        return 0;
    }

    @Override
    public void setLastHotbarSlotInteraction(int last) {

    }

    @Override
    public Map<Class<?>, RpgInventory> getManagedInventory() {
        return Collections.emptyMap();
    }
}
