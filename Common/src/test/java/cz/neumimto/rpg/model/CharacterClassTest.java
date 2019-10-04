package cz.neumimto.rpg.model;

import cz.neumimto.rpg.api.persistance.model.CharacterBase;
import cz.neumimto.rpg.api.persistance.model.CharacterClass;

/**
 * Created by ja on 8.10.2016.
 */
public class CharacterClassTest extends TimestampEntityTest implements CharacterClass {

    private long classId;
    private double experiences;
    private int level;
    private String name;
    private int usedSkillPoints;
    private int skillPoints;

    private CharacterBase characterBase;

    @Override
    public Long getId() {
        return classId;
    }

    @Override
    public void setId(Long id) {
        this.classId = id;
    }

    @Override
    public CharacterBase getCharacterBase() {
        return characterBase;
    }

    @Override
    public void setCharacterBase(CharacterBase characterBase) {
        this.characterBase = characterBase;
    }

    @Override
    public double getExperiences() {
        return experiences;
    }

    @Override
    public void setExperiences(double experiences) {
        this.experiences = experiences;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int getSkillPoints() {
        return skillPoints;
    }

    @Override
    public void setSkillPoints(int skillPoints) {
        this.skillPoints = skillPoints;
    }

    @Override
    public int getUsedSkillPoints() {
        return usedSkillPoints;
    }

    @Override
    public void setUsedSkillPoints(int usedSkillPoints) {
        this.usedSkillPoints = usedSkillPoints;
    }


    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public void setLevel(int level) {
        this.level = level;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CharacterClassTest that = (CharacterClassTest) o;

        return name.equals(that.name);

    }

}
