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

package cz.neumimto.rpg.players;

import cz.neumimto.rpg.players.groups.ConfigClass;

/**
 * Created by NeumimTo on 28.7.2015.
 */
public class ExtendedNClass {

	public static ExtendedNClass Default;

	static {
		Default = new ExtendedNClass(null) {
			@Override
			public boolean takesExp() {
				return false;
			}
		};
		Default.setConfigClass(ConfigClass.Default);
		Default.setPrimary(true);
	}

	private ConfigClass configClass;
	private double experiencesFromLevel;
	private int slot;
	private int level;
	private ActiveCharacter activeCharacter;

	public ExtendedNClass(ActiveCharacter activeCharacter) {
		this.activeCharacter = activeCharacter;
	}


	public boolean takesExp() {
		return getExperiences() <= configClass.getTotalExp();
	}

	public ConfigClass getConfigClass() {
		return configClass;
	}

	public void setConfigClass(ConfigClass configClass) {
		this.configClass = configClass;
	}

	public double getExperiences() {
		return activeCharacter.getCharacterBase().getCharacterClass(getConfigClass()).getExperiences();
	}

	public void setExperiences(double experiences) {
		activeCharacter.getCharacterBase().getCharacterClass(getConfigClass()).setExperiences(experiences);
	}

	public boolean isPrimary() {
		return slot == 1;
	}

	public void setPrimary(boolean isPrimary) {
		this.slot = 1;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public double getExperiencesFromLevel() {
		return experiencesFromLevel;
	}

	public void setExperiencesFromLevel(double experiencesFromLevel) {
		this.experiencesFromLevel = experiencesFromLevel;
	}

	public int getSlot() {
		return slot;
	}

	public void setSlot(int slot) {
		this.slot = slot;
	}
}
