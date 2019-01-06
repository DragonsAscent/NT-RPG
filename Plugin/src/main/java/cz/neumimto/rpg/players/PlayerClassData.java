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

import cz.neumimto.rpg.players.groups.ClassDefinition;

/**
 * Created by NeumimTo on 28.7.2015.
 */
public class PlayerClassData {

	public static PlayerClassData Default;

	static {
		Default = new PlayerClassData(null) {
			@Override
			public boolean takesExp() {
				return false;
			}
		};
		Default.setClassDefinition(ConfigClass.Default);
		Default.setPrimary(true);
	}

	private ActiveCharacter activeCharacter;
	private ClassDefinition classDefinition;

	private double experiencesFromLevel;
	private int level;


	public PlayerClassData(ActiveCharacter activeCharacter) {
		this.activeCharacter = activeCharacter;
	}


	public boolean takesExp() {
		return getExperiences() <= classDefinition.getTotalExp();
	}

	public ClassDefinition getClassDefinition() {
		return classDefinition;
	}

	public void setClassDefinition(ClassDefinition classDefinition) {
		this.classDefinition = classDefinition;
	}

	public double getExperiences() {
		return activeCharacter.getCharacterBase().getCharacterClass(getClassDefinition()).getExperiences();
	}

	public void setExperiences(double experiences) {
		activeCharacter.getCharacterBase().getCharacterClass(getClassDefinition()).setExperiences(experiences);
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
}
