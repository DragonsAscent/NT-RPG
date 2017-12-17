package cz.neumimto.rpg.skills;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;

/**
 * Created by NeumimTo on 16.8.17.
 */
public class SkillPathData extends SkillData {

    private List<String> enterCommands  = new ArrayList<>();
    private List<String> exitCommands  = new ArrayList<>();
    private int tier;
    private int skillPointsRequired;
    private Map<String, Integer> skillBonus = new HashMap<>();

    public SkillPathData(String name) {
        super(name);
    }

    public List<String> getEnterCommands() {
        return enterCommands;
    }

    public void ListEnterCommands(List<String> enterCommands) {
        this.enterCommands = enterCommands;
    }

    public List<String> getExitCommands() {
        return exitCommands;
    }

    public void ListExitCommands(List<String> exitCommands) {
        this.exitCommands = exitCommands;
    }

    public int getTier() {
        return tier;
    }

    public void setEnterCommands(List<String> enterCommands) {
        this.enterCommands = enterCommands;
    }

    public void setExitCommands(List<String> exitCommands) {
        this.exitCommands = exitCommands;
    }

    public void setTier(int tier) {
        this.tier = tier;
    }

    public void setSkillPointsRequired(int skillPointsRequired) {
        this.skillPointsRequired = skillPointsRequired;
    }

    public void setSkillBonus(Map<String, Integer> skillBonus) {
        this.skillBonus = skillBonus;
    }

    public void ListTier(int tier) {
        this.tier = tier;
    }

    public int getSkillPointsRequired() {
        return skillPointsRequired;
    }

    public void ListSkillPointsRequired(int skillPointsRequired) {
        this.skillPointsRequired = skillPointsRequired;
    }

    public void addSkillBonus(String skill, int levels) {
        skillBonus.put(skill, levels);
    }

    public Map<String, Integer> getSkillBonus() {
        return skillBonus;
    }


}
