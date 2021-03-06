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

package cz.neumimto.rpg.common.configuration;

import com.typesafe.config.*;
import cz.neumimto.rpg.api.Rpg;
import cz.neumimto.rpg.api.configuration.AttributeConfig;
import cz.neumimto.rpg.api.configuration.ItemString;
import cz.neumimto.rpg.api.configuration.SkillItemCost;
import cz.neumimto.rpg.api.configuration.SkillTreeDao;
import cz.neumimto.rpg.api.entity.players.IActiveCharacter;
import cz.neumimto.rpg.api.gui.ISkillTreeInterfaceModel;
import cz.neumimto.rpg.api.localization.LocalizationService;
import cz.neumimto.rpg.api.logging.Log;
import cz.neumimto.rpg.api.skills.*;
import cz.neumimto.rpg.api.skills.mods.ActiveSkillPreProcessorWrapper;
import cz.neumimto.rpg.api.skills.scripting.ScriptedSkillNodeDescription;
import cz.neumimto.rpg.api.skills.tree.SkillTree;
import cz.neumimto.rpg.api.skills.utils.SkillLoadingErrors;
import cz.neumimto.rpg.api.utils.FileUtils;
import cz.neumimto.rpg.api.utils.MathUtils;
import cz.neumimto.rpg.api.utils.Pair;
import cz.neumimto.rpg.common.skills.SkillConfigLoader;
import cz.neumimto.rpg.common.skills.SkillConfigLoaders;
import cz.neumimto.rpg.common.skills.preprocessors.SkillPreprocessorFactories;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.*;
import java.util.stream.Collectors;

import static cz.neumimto.rpg.api.logging.Log.*;

/**
 * Created by NeumimTo on 24.7.2015.
 */
//todo use nightconfig
@Singleton
public class SkillTreeLoaderImpl implements SkillTreeDao {

    @Inject
    private SkillService skillService;

    @Inject
    private LocalizationService localizationService;

    @Override
    public Map<String, SkillTree> getAll() {
        Path dir = Paths.get(Rpg.get().getWorkingDirectory(), "Skilltrees");
        FileUtils.createDirectoryIfNotExists(dir);
        Map<String, SkillTree> map = new HashMap<>();
        try (DirectoryStream<Path> paths = Files.newDirectoryStream(dir, "*.conf")) {
            paths.forEach(path -> {
                info("Loading skilltree from a file " + path.getFileName());
                Config config = ConfigFactory.parseFile(path.toFile());
                populateMap(map, config);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    public void populateMap(Map<String, SkillTree> map, Config config) {
        SkillTree skillTree = new SkillTree();
        if (loadTree(config, skillTree)) {
            return;
        }
        loadAsciiMaps(config, skillTree);
        map.put(skillTree.getId(), skillTree);
    }

    public void loadAsciiMaps(Config config, SkillTree skillTree) {
        try {
            List<String> asciiMap = config.getStringList("AsciiMap");
            Optional<String> max = asciiMap.stream().max(Comparator.comparingInt(String::length));
            if (max.isPresent()) {
                int length = max.get().length();
                int rows = asciiMap.size();

                short[][] array = new short[rows][length];

                int i = 0;
                int j = 0;
                StringBuilder num = new StringBuilder();
                for (String s : asciiMap) {
                    for (char c1 : s.toCharArray()) {
                        if (Character.isDigit(c1)) {
                            num.append(c1);
                            continue;
                        } else if (c1 == 'X') {
                            skillTree.setCenter(new Pair<>(i, j));
                            j++;
                            continue;
                        }
                        if (!num.toString().equals("")) {
                            array[i][j] = Short.parseShort(num.toString());
                            j++;
                        }
                        ISkillTreeInterfaceModel guiModelByCharacter = skillService.getGuiModelByCharacter(c1);
                        if (guiModelByCharacter != null) {
                            array[i][j] = guiModelByCharacter.getId();
                        }
                        num = new StringBuilder();
                        j++;
                    }
                    j = 0;
                    i++;
                }
                skillTree.setSkillTreeMap(array);
            }
        } catch (ConfigException | ArrayIndexOutOfBoundsException ignored) {
            error("Could not read ascii map in the skilltree " + skillTree.getId(), ignored);
            skillTree.setSkillTreeMap(new short[][]{});
        }
    }


    protected boolean loadTree(Config config, SkillTree skillTree) {

        try {
            skillTree.setId(config.getString("Name"));
        } catch (ConfigException e) {
            warn("Missing \"Name\" skipping to another file");
            return true;
        }

        try {
            List<? extends ConfigObject> skills = config.getObjectList("Skills");
            createConfigSkills(skills, skillTree);
            loadSkills(skills, skillTree);
        } catch (ConfigException e) {
            error("Could not read \"Skills\" section. skipping", e);

        }
        return false;
    }

    private void createConfigSkills(List<? extends ConfigObject> sub, SkillTree skillTree) {
        for (ConfigObject co : sub) {
            Config c = co.toConfig();
            createConfigSkill(skillTree, c);
        }
    }

    private void createConfigSkill(SkillTree skillTree, Config c) {
        String type = null;
        try {
            type = c.getString("Type");
        } catch (ConfigException.Missing ignored) {
            return;
        }

        try {
            Config parent = c.getConfig("Parent");
            createConfigSkill(skillTree, parent);
        } catch (ConfigException.Missing ignored) {}

        String id = c.getString("SkillId");

        ISkill skill = Rpg.get().getSkillService().getSkills().get(id.toLowerCase());
        if (skill == null) {
            try {
                final String cType =type;
                SkillConfigLoader loader = SkillConfigLoaders.getById(type)
                        .orElseThrow(() ->
                                new IllegalArgumentException("Unknown skill type " + cType + " in a skiltree " + skillTree.getId())
                        );

                skill = loader.build(id.toLowerCase());

            } catch (ConfigException.Missing ignored) {
            }

        } else {
            Log.warn("Found duplicit Config skill " + id + ", will be skipped try to avoid such use cases.");
        }
    }

    private void loadSkills(List<? extends ConfigObject> sub, SkillTree skillTree) {
        for (ConfigObject co : sub) {
            loadSkill(skillTree, co.toConfig());
        }
    }

    protected void loadSkill(SkillTree skillTree, Config c) {
        String skillId = c.getString("SkillId");
        try {
            Config parent = c.getConfig("Parent");

            SkillData info = createSkillInfo(skillTree, skillId);

            loadSkill(skillTree, parent, info);
        } catch (ConfigException ignored) {}

        SkillData info = getSkillInfo(skillId, skillTree);
        loadSkill(skillTree, c, info);
    }

    private void loadSkill(SkillTree skillTree, Config c, SkillData info) {
        try {
            info.setMaxSkillLevel(c.getInt("MaxSkillLevel"));
        } catch (ConfigException e) {
            info.setMaxSkillLevel(1);
            warn("Missing \"MaxSkillLevel\" node for a skill \"" + info.getSkillId() + "\", setting to 1");
        }
        try {
            String combination = c.getString("Combination");
            combination = combination.trim();
            if (!"".equals(combination)) {
                info.setCombination(combination);
            }
        } catch (ConfigException e) {
        }

        try {
            Integer modelId = c.getInt("ModelId");
            info.setModelId(modelId);
        } catch (ConfigException e) {
        }

        try {
            String icon = c.getString("Icon");
            info.setIcon(icon);
        } catch (ConfigException e) {

        }

        try {
            info.setMinPlayerLevel(c.getInt("MinPlayerLevel"));
        } catch (ConfigException e) {
            info.setMinPlayerLevel(1);
            warn("Missing \"MinPlayerLevel\" node for a skill \"" + info.getSkillId() + "\", setting to 1");
        }

        try {
            info.setLevelGap(c.getInt("LevelGap"));
        } catch (ConfigException e) {
            info.setLevelGap(0);
            warn("Missing \"LevelGap\" node for a skill \"" + info.getSkillId() + "\", setting to 1");
        }

        ISkillNodeDescription skillNodeDescription = null;
        try {
            List<String> description = c.getStringList("Description");
            skillNodeDescription = new SkillNodeDescription(description);
        } catch (ConfigException e) {
            try {
                Config description = c.getConfig("Description");

                List<String> template = description.getStringList("Template");
                ScriptedSkillNodeDescription scriptedSkillNodeDescription = new ScriptedSkillNodeDescription();
                scriptedSkillNodeDescription.setTemplate(template);

                scriptedSkillNodeDescription.setJSFunction(description.getString("Function"));

                skillNodeDescription = scriptedSkillNodeDescription;
            } catch (ConfigException ee) {
                skillNodeDescription = character -> Collections.emptyList();
            }
        }
        info.setDescription(skillNodeDescription);

        try {
            Config reagent = c.getConfig("InvokeCost");
            SkillCost itemCost = new SkillCost();
            info.setInvokeCost(itemCost);
            List<? extends ConfigObject> list = reagent.getObjectList("Items");

            for (ConfigObject configObject : list) {
                try {
                    SkillItemCost q = new SkillItemCost();
                    q.setAmount(Integer.parseInt(configObject.get("Amount").unwrapped().toString()));
                    String type = configObject.get("Item").unwrapped().toString();
                    boolean consume = Boolean.valueOf(configObject.get("Consume").unwrapped().toString());
                    q.setConsumeItems(consume);
                    ItemString parse = ItemString.parse(type);
                    q.setItemType(parse);
                    itemCost.getItemCost().add(q);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            list = reagent.getObjectList("Insufficient");
            for (ConfigObject configObject : list) {
                Set<ActiveSkillPreProcessorWrapper> preprocessors = itemCost.getInsufficientProcessors();
                loadPreprocessor(configObject, preprocessors);
            }
        } catch (Exception e) {

        }

        try {
            for (String conflicts : c.getStringList("Conflicts")) {
                info.getConflicts().add(getSkillInfo(conflicts, skillTree));
            }
        } catch (ConfigException ignored) {
        }

        try {
            Config softDepends = c.getConfig("SoftDepends");
            for (Map.Entry<String, ConfigValue> entry : softDepends.entrySet()) {
                String skillId = entry.getKey().replaceAll("\"", "");
                String render = entry.getValue().render();
                int i = Integer.parseInt(render);
                SkillData skill = getSkillInfo(skillId, skillTree);
                info.getSoftDepends().add(new SkillDependency(skill, i));
                skill.getDepending().add(info);
            }
        } catch (ConfigException ignored) {
        }
        try {
            Config softDepends = c.getConfig("HardDepends");
            for (Map.Entry<String, ConfigValue> entry : softDepends.entrySet()) {
                String skillId = entry.getKey();
                String render = entry.getValue().render();
                int i = Integer.parseInt(render);
                SkillData skill = getSkillInfo(skillId, skillTree);
                info.getHardDepends().add(new SkillDependency(skill, i));
                skill.getDepending().add(info);
            }
        } catch (ConfigException ignored) {
        }

        try {
            info.setSkillTreeId(c.getInt("SkillTreeId"));
            skillTree.addSkillTreeId(info);
        } catch (ConfigException ignored) {
            info(" - Skill " + info.getSkillId() + " missing SkillTreeId, it wont be possible to reference this skill in the ascii map");
        }

        String id = info.getSkill().getId();
        try {
            info.setSkillName(c.getString("Name"));
            info(" - Alternate name defined for skill " + id + " > " + info.getSkillName());
            Rpg.get().getSkillService().registerSkillAlternateName(info.getSkillName(), info.getSkill());
        } catch (ConfigException missing) {
            info.setSkillName(id);
            warn(" - No alternate name defined for skill " + id + " !!falling back to default!! > " + info.getSkillName());
        }

        try {
            List<ActiveSkillPreProcessorWrapper> skillPreprocessors = info.getSkillPreprocessors();
            List<? extends ConfigObject> preprocessors = c.getObjectList("Preprocessors");
            for (ConfigObject preprocessor : preprocessors) {
                loadPreprocessor(preprocessor, skillPreprocessors);
            }
        } catch (ConfigException e) {

        }

        SkillSettings skillSettings = new SkillSettings();
        try {
            Config settings = c.getConfig("SkillSettings");
            Collection<AttributeConfig> attributes = Rpg.get().getPropertyService().getAttributes().values();
            outer:
            for (Map.Entry<String, ConfigValue> e : settings.entrySet()) {
                if (e.getKey().endsWith(SkillSettings.BONUS_SUFFIX)) {
                    continue;
                }
                String val = e.getValue().render();
                if (MathUtils.isNumeric(val)) {
                    float norm = Float.parseFloat(val);
                    for (AttributeConfig attribute : attributes) {
                        String s = "_per_" + attribute.getId();
                        if (e.getKey().endsWith(s)) {
                            String stripped = e.getKey().substring(0, e.getKey().length() - s.length());
                            skillSettings.addAttributeNode(stripped, attribute, norm);
                            continue outer;
                        }
                    }


                    String name = e.getKey();
                    skillSettings.addNode(name, norm);
                    name = name + SkillSettings.BONUS_SUFFIX;
                    float bonus = 0f;
                    try {
                        bonus = Float.parseFloat(settings.getString(name));
                    } catch (ConfigException ignored) {
                    }
                    skillSettings.addNode(name, bonus);
                } else {
                    skillSettings.addObjectNode(e.getKey(), val);
                }
            }
            addRequiredIfMissing(skillSettings);
        } catch (ConfigException ignored) {
            warn(" - missing SkillSettings section " + info.getSkillId());
        }
        info.setSkillSettings(skillSettings);

        SkillSettings defaultSkillSettings = info.getSkill().getDefaultSkillSettings();
        if (defaultSkillSettings != null && defaultSkillSettings.getNodes() != null) {
            Iterator<Map.Entry<String, Float>> iterator = defaultSkillSettings.getNodes().entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Float> next = iterator.next();
                Float value = next.getValue();
                String key = next.getKey();
                if (key.endsWith(SkillSettings.BONUS_SUFFIX)) {
                    continue;
                }
                if (!skillSettings.getNodes().containsKey(key)) {
                    Float val2 = defaultSkillSettings.getNodes().get(key + SkillSettings.BONUS_SUFFIX);
                    skillSettings.addNode(key, value, val2);
                    warn(" - Missing settings node " + key + " for a skill " + info.getSkillId() + " - inherited from default: " + value + " / " + val2);
                }
            }
        }

        SkillLoadingErrors errors = new SkillLoadingErrors(skillTree.getId());
        try {
            info.getSkill().loadSkillData(info, skillTree, errors, c);
        } catch (ConfigException e) {

        }
        for (String s : errors.getErrors()) {
            info(s);
        }


        skillTree.getSkills().put(info.getSkillId().toLowerCase(), info);
    }

    private void loadPreprocessor(ConfigObject configObject, Collection<ActiveSkillPreProcessorWrapper> preprocessors) {
        String preprocessorFactoryId = configObject.get("Id").unwrapped().toString();
        Optional<SkillPreProcessorFactory> id = SkillPreprocessorFactories.getById(preprocessorFactoryId);
        if (id.isPresent()) {
            SkillPreProcessorFactory skillPreProcessorFactory = id.get();
            ActiveSkillPreProcessorWrapper parse = skillPreProcessorFactory.parse(configObject);
            preprocessors.add(parse);
        } else {
            warn("- Unknown processor type " + configObject.get("Id").render() + ", use one of: " +
                    SkillPreprocessorFactories.getAll()
                    .stream()
                    .map(SkillPreProcessorFactory::getId)
                    .collect(Collectors.joining(", ")));
        }
    }

    private void addRequiredIfMissing(SkillSettings skillSettings) {
        Map.Entry<String, Float> q = skillSettings.getFloatNodeEntry(SkillNodes.HPCOST.name());
        if (q == null) {
            skillSettings.addNode(SkillNodes.HPCOST, 0, 0);
        }
        q = skillSettings.getFloatNodeEntry(SkillNodes.MANACOST.name());
        if (q == null) {
            skillSettings.addNode(SkillNodes.MANACOST, 0, 0);
        }
        q = skillSettings.getFloatNodeEntry(SkillNodes.COOLDOWN.name());
        if (q == null) {
            skillSettings.addNode(SkillNodes.COOLDOWN, 0, 0);
        }
    }

    private SkillData getSkillInfo(String id, SkillTree tree) {
        final String lowercased = id.toLowerCase();
        SkillData info = tree.getSkills().get(lowercased);
        if (info == null) {
            info = createSkillInfo(tree, lowercased);
        }
        return info;
    }

    private SkillData createSkillInfo(SkillTree tree, String lowercased) {

        ISkill skill = Rpg.get().getSkillService().getSkills().get(lowercased);
        if (skill == null) {
            throw new IllegalStateException("Could not find a skill " + lowercased + " referenced in the skilltree " + tree.getId());
        }
        SkillData info = skill.constructSkillData();
        info.setSkill(skill);
        tree.addSkill(info);
        return info;
    }
}
