package cz.neumimto.rpg.api.skills.mods;

import cz.neumimto.rpg.api.skills.ISkill;
import cz.neumimto.rpg.api.skills.ISkillNode;
import cz.neumimto.rpg.api.skills.PlayerSkillContext;
import cz.neumimto.rpg.api.skills.SkillResult;
import cz.neumimto.rpg.api.skills.types.IActiveSkill;
import cz.neumimto.rpg.api.entity.players.IActiveCharacter;

import java.util.*;


/**
 * Created by fs on 20.10.16.
 */
public class SkillContext {

    protected final ArrayList<ActiveSkillPreProcessorWrapper> wrappers = new ArrayList<>();
    protected PlayerSkillContext esi;
    protected int cursor;
    private SkillResult result;
    protected boolean continueExecution;
    private float finalCooldown;
    private boolean sorted = false;

    public SkillContext(IActiveSkill activeSkill, PlayerSkillContext esi) {
        this.esi = esi;
        resetCursor();
        continueExecution = true;
        wrappers.add(new ActiveSkillPreProcessorWrapper(PreProcessorTarget.EXECUTION) {
            @Override
            public void doNext(IActiveCharacter character, PlayerSkillContext info, SkillContext skillResult) {
                activeSkill.cast(character, info, SkillContext.this);
            }
        });
    }

    public SkillContext() {

    }

    public void resetCursor() {
        this.cursor = -1;
    }

    public ISkill getSkill() {
        return esi.getSkill();
    }

    public void sort() {
        sorted = true;
        wrappers.sort(Comparator.comparing(ActiveSkillPreProcessorWrapper::getTarget));
    }

    public boolean isSorted() {
        return sorted;
    }

    public void next(IActiveCharacter consumer, PlayerSkillContext info, SkillContext skillResult) {
        cursor++;
        if (result == SkillResult.CANCELLED || skillResult.continueExecution) {
            wrappers.get(cursor).doNext(consumer, info, skillResult);
        }
    }

    public void endWith(IActiveCharacter consumer, PlayerSkillContext info, SkillContext context) {
        wrappers.get(wrappers.size() - 1).doNext(consumer, info, context);
    }

    public void next(IActiveCharacter consumer, PlayerSkillContext info, SkillResult skillResult) {
        next(consumer, info, result(skillResult));
    }


    public SkillContext result(SkillResult result) {
        this.result = result;
        return this;
    }

    public SkillResult getResult() {
        return result;
    }

    public boolean continueExecution() {
        return continueExecution;
    }

    public SkillContext continueExecution(boolean continueExecution) {
        this.continueExecution = continueExecution;
        return this;
    }

    public void addExecutor(ActiveSkillPreProcessorWrapper proc) {
        wrappers.add(proc);
    }

    public void addExecutor(Collection<ActiveSkillPreProcessorWrapper> set) {
        wrappers.addAll(set);
    }

    public Map<String, Float> getSkillNodes() {
        return esi.getCachedComputedSkillSettings();
    }

    public void overrideNode(String key, Float value) {
		/*todo
		if (!copy) {
			copy = true;
			skillNodes = new HashMap<>(skillNodes);
		}
		skillNodes.put(key, value);
		*/
    }

    private float getLevelNodeValue(String s) {
        return esi.getCachedComputedSkillSettings().getFloat(s);
    }

    public float getFloatNodeValue(ISkillNode node) {
        return getFloatNodeValue(node.value());
    }

    public float getFloatNodeValue(String node) {
        return getLevelNodeValue(node);
    }

    public int getIntNodeValue(ISkillNode node) {
        return getIntNodeValue(node.value());
    }

    public int getIntNodeValue(String node) {
        return (int) getLevelNodeValue(node);
    }

    public long getLongNodeValue(ISkillNode node) {
        return getLongNodeValue(node.value());
    }

    public long getLongNodeValue(String node) {
        return (long) getLevelNodeValue(node);
    }

    public double getDoubleNodeValue(String node) {
        return getLevelNodeValue(node);
    }

    public double getDoubleNodeValue(ISkillNode node) {
        return getDoubleNodeValue(node.value());
    }

    public void setFinalCooldown(float finalCooldown) {
        this.finalCooldown = finalCooldown;
    }

    public float getFinalCooldown() {
        return finalCooldown;
    }
}
