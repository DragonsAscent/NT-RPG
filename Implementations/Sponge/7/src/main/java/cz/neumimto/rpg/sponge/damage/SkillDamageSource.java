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
package cz.neumimto.rpg.sponge.damage;

import cz.neumimto.rpg.api.effects.IEffect;
import cz.neumimto.rpg.api.skills.ISkill;
import cz.neumimto.rpg.api.entity.IEntity;
import org.spongepowered.api.event.cause.entity.damage.source.common.AbstractEntityDamageSource;

/**
 * Created by NeumimTo on 29.12.2015.
 */
public class SkillDamageSource extends AbstractEntityDamageSource implements ISkillDamageSource {

    private final ISkill skill;
    private final IEntity nSource;
    private final IEffect effect;

    public SkillDamageSource(SkillDamageSourceBuilder builder) {
        super(builder);
        this.skill = builder.getSkill();
        this.nSource = builder.getSource();
        this.effect = builder.getEffect();
    }

    @Override
    public final ISkill getSkill() {
        return this.skill;
    }

    @Override
    public IEntity getSourceIEntity() {
        return nSource;
    }

    @Override
    public IEffect getEffect() {
        return effect;
    }
}