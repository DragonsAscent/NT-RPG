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

package cz.neumimto.rpg;

import cz.neumimto.configuration.ConfigMapper;
import cz.neumimto.configuration.ConfigurationContainer;
import cz.neumimto.core.PersistentContext;
import cz.neumimto.core.PluginCore;
import cz.neumimto.core.Repository;
import cz.neumimto.core.ioc.Inject;
import cz.neumimto.core.ioc.IoC;
import cz.neumimto.core.ioc.Singleton;
import cz.neumimto.core.localization.LocalizationService;
import cz.neumimto.core.localization.ResourceBundle;
import cz.neumimto.core.localization.ResourceBundles;
import cz.neumimto.rpg.commands.CommandBase;
import cz.neumimto.rpg.commands.CommandService;
import cz.neumimto.rpg.effects.EffectService;
import cz.neumimto.rpg.effects.IGlobalEffect;
import cz.neumimto.rpg.effects.model.EffectModelFactory;
import cz.neumimto.rpg.effects.model.EffectModelMapper;
import cz.neumimto.rpg.players.properties.PropertyContainer;
import cz.neumimto.rpg.players.properties.PropertyService;
import cz.neumimto.rpg.players.properties.attributes.ICharacterAttribute;
import cz.neumimto.rpg.scripting.JSLoader;
import cz.neumimto.rpg.scripting.JsBinding;
import cz.neumimto.rpg.skills.ISkill;
import cz.neumimto.rpg.skills.SkillService;
import org.apache.commons.io.FileUtils;
import org.spongepowered.api.Game;
import org.spongepowered.api.text.Text;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static cz.neumimto.rpg.Log.error;
import static cz.neumimto.rpg.Log.info;
import static cz.neumimto.rpg.NtRpgPlugin.pluginConfig;

/**
 * Created by NeumimTo on 27.12.2014.
 */
@Singleton
public class ResourceLoader {

	private final static String INNERCLASS_SEPARATOR = "$";

	public static File classDir, raceDir, guildsDir, addonDir, skilltreeDir, addonLoadDir;

	private static IoC ioc;

	static {
		classDir = new File(NtRpgPlugin.workingDir + File.separator + "classes");
		raceDir = new File(NtRpgPlugin.workingDir + File.separator + "races");
		guildsDir = new File(NtRpgPlugin.workingDir + File.separator + "guilds");
		addonDir = new File(NtRpgPlugin.workingDir + File.separator + "addons");
		addonLoadDir = new File(NtRpgPlugin.workingDir + File.separator + ".deployed");
		skilltreeDir = new File(NtRpgPlugin.workingDir + File.separator + "skilltrees");
		classDir.mkdirs();
		raceDir.mkdirs();
		guildsDir.mkdirs();
		skilltreeDir.mkdirs();
		addonDir.mkdirs();
		ioc = IoC.get();

		try {
			FileUtils.deleteDirectory(addonLoadDir);
			FileUtils.copyDirectory(addonDir, addonLoadDir);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Inject
	private SkillService skillService;

	@Inject
	private ClassService classService;

	@Inject
	private EffectService effectService;

	@Inject
	private PropertyService propertyService;


	@Inject
	private CommandService commandService;

	private ConfigMapper configMapper;

	@Inject
	private ClassGenerator classGenerator;

	@Inject
	private LocalizationService localizationService;

	private Map<String, URLClassLoader> classLoaderMap = new HashMap<>();

	private URLClassLoader configClassLaoder;

	public ResourceLoader() {
		ConfigMapper.init("NtRPG", Paths.get(NtRpgPlugin.workingDir));
		configMapper = ConfigMapper.get("NtRPG");
		configClassLaoder = new URLClassLoader(new URL[]{}, this.getClass().getClassLoader());

	}

	private static <T> T newInstance(Class<T> excepted, Class<?> clazz) {
		T t = null;
		try {
			t = (T) clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return t;
	}

	public void loadExternalJars() {
		Path dir = addonLoadDir.toPath();
		for (File f : dir.toFile().listFiles()) {
			loadJarFile(f, false);
		}
	}

	public void loadJarFile(File f, boolean main) {
		if (f == null) {
			return;
		}
		JarFile file = null;
		try {
			file = new JarFile(f);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		info("Loading jarfile " + file.getName());
		Enumeration<JarEntry> entries = file.entries();
		JarEntry next = null;

		if (!main) {
			URLClassLoader classLoader = classLoaderMap.get(f.getName());
			if (classLoader == null) {
		/*		classLoader = (URLClassLoader) NtRpgPlugin.class.getClassLoader();
				classLoaderMap.put(f.getName(), classLoader);
				PluginCore.loadJarFile(f);*/

			try {

				classLoader = new ResourceClassLoader(f.toPath().getFileName().toString().trim(),
							new URL[]{f.toURI().toURL()},
							PluginCore.getClassLoader());

					classLoaderMap.put(f.getName(), classLoader);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}

			}

		}
		while (entries.hasMoreElements()) {
			next = entries.nextElement();
			if (next.isDirectory() || !next.getName().endsWith(".class")) {
				continue;
			}
			if (main && !next.getName().startsWith("cz/neumimto")) {
				continue;
			}
			//todo place this into each modules
			if (next.getName().startsWith("org")
					|| next.getName().startsWith("spark")
					|| next.getName().startsWith("javax")) {
				continue;
			}
			if (next.getName().lastIndexOf(INNERCLASS_SEPARATOR) > 1) {
				continue;
			}
			String className = next.getName().substring(0, next.getName().length() - 6);
			className = className.replace('/', '.');
			Class<?> clazz = null;
			try {
				if (!main) {
					ClassLoader classLoader = classLoaderMap.get(f.getName());
					clazz = classLoader.loadClass(className);
					info("ClassLoader for "
							+ Console.GREEN_BOLD + classLoader +
							Console.RESET + " loaded class " +
							Console.GREEN + clazz.getSimpleName() + Console.RESET, pluginConfig
							.DEBUG);
					loadClass(clazz, classLoader);
				} else {
					clazz = Class.forName(className);
					loadClass(clazz, this.getClass().getClassLoader());
				}
			} catch (Exception e) {
				error("Could not load the class [" + className + "]" + e.getMessage(), e);
				continue;
			}
		}
		info("Finished loading of jarfile " + file.getName());
	}

	public Object loadClass(Class<?> clazz, ClassLoader classLoader) throws IllegalAccessException, InstantiationException {
		//Properties
		if (clazz == IGlobalEffect.class) {
			return null;
		}
		Object container = null;
		if (clazz.isInterface() && clazz.getAnnotations().length == 0) {
			return null;
		}
		if (clazz.isAnnotationPresent(Singleton.class)) {
			ioc.build(clazz);
		}
		if (clazz.isAnnotationPresent(ListenerClass.class)) {
			info("Registering listener class" + clazz.getName(), pluginConfig.DEBUG);
			container = ioc.build(clazz);
			ioc.build(Game.class).getEventManager().registerListeners(ioc.build(NtRpgPlugin.class), container);
		}
		if (clazz.isAnnotationPresent(Command.class)) {
			container = ioc.build(clazz);
			info("registering command class" + clazz.getName(), pluginConfig.DEBUG);
			commandService.registerCommand((CommandBase) container);
		}
		if (clazz.isAnnotationPresent(Skill.class)) {
			container = ioc.build(clazz);
			info("registering skill " + clazz.getName(), pluginConfig.DEBUG);
			ISkill skill = (ISkill) container;
			Skill sk = clazz.getAnnotation(Skill.class);
			if (sk.dynamicLocalizationNodes()) {
				String[] split = sk.value().split(":");
				String key = split[0] + ".skills." + split[1];
				skill.setLocalizableName(localizationService.getText(key + ".name"));
				skill.setDescription(localizationService.getTextList(key + ".desc"));
				skill.setLore(localizationService.getTextList(key + ".lore"));
			}
			if (skill.getLocalizableName().isEmpty()) {
				String name = skill.getClass().getSimpleName();
				name = name.startsWith("Skill") ? name.substring("Skill".length()) : name;
				skill.setLocalizableName(Text.of(name));
			}
			skillService.registerAdditionalCatalog(skill);
		}
		if (clazz.isAnnotationPresent(ConfigurationContainer.class)) {
			configMapper.loadClass(clazz);
			info("Found configuration container class " + clazz.getName(), pluginConfig.DEBUG);
		}
		if (clazz.isAnnotationPresent(PropertyContainer.class)) {
			info("Found Property container class" + clazz.getName(), pluginConfig.DEBUG);
			propertyService.processContainer(clazz);
		}
		if (clazz.isAnnotationPresent(Attribute.class)) {
			propertyService.registerAttribute((ICharacterAttribute) clazz.newInstance());
		}
		if (clazz.isAnnotationPresent(JsBinding.class)) {
			IoC.get().build(JSLoader.class).getDataToBind().put(clazz, clazz.getAnnotation(JsBinding.class).value());
		}
		if (clazz.isAnnotationPresent(ResourceBundles.class)) {
			ResourceBundles annotation = clazz.getAnnotation(ResourceBundles.class);
			for (ResourceBundle resourceBundle : annotation.value()) {
				localizationService.loadResourceBundle(resourceBundle.value(), Locale.forLanguageTag(pluginConfig.LOCALE), clazz.getClassLoader());
			}
		}
		if (IGlobalEffect.class.isAssignableFrom(clazz)) {
			container = newInstance(IGlobalEffect.class, clazz);
			effectService.registerGlobalEffect((IGlobalEffect) container);
		}
		if (clazz.isAnnotationPresent(ModelMapper.class)) {
			EffectModelMapper o = (EffectModelMapper) clazz.newInstance();
			EffectModelFactory.typeMappers.put(o.getType(), o);
		}
		if (clazz.isAnnotationPresent(Repository.class)) {
			container = ioc.build(clazz);
			boolean f = false;
			for (Field field : container.getClass().getDeclaredFields()) {
				field.setAccessible(true);
				if (field.isAnnotationPresent(PersistentContext.class)) {
					field.set(container, PluginCore.Instance.getSessionFactoryByName(field.getAnnotation(PersistentContext.class).value()));
					f = true;
				}
			}
			if (!f) {
				Log.info("No SessionFactory injected for " + container.getClass().getCanonicalName());
			}
		}
		return container;
	}

	public URLClassLoader getConfigClassLaoder() {
		return configClassLaoder;
	}

	public Map<String, URLClassLoader> getClassLoaderMap() {
		return Collections.unmodifiableMap(classLoaderMap);
	}

	@Retention(RetentionPolicy.RUNTIME)
	public @interface ListenerClass {

	}

	@Retention(RetentionPolicy.RUNTIME)
	public @interface Skill {

		String value();

		boolean dynamicLocalizationNodes() default true;
	}

	@Retention(RetentionPolicy.RUNTIME)
	public @interface Command {

	}

	@Retention(RetentionPolicy.RUNTIME)
	public @interface Attribute {

	}

	@Retention(RetentionPolicy.RUNTIME)
	public @interface ModelMapper {

	}
}
