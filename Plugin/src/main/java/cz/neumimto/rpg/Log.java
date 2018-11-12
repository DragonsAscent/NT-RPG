package cz.neumimto.rpg;


import static cz.neumimto.rpg.NtRpgPlugin.pluginConfig;

import cz.neumimto.rpg.configuration.DebugLevel;
import org.slf4j.Logger;

/**
 * Created by NeumimTo on 19.8.2018.
 */
public class Log {

	protected static Logger logger;

	public static void info(String message) {
		logger.info(message);
	}

	public static void info(String message, DebugLevel debugLevel) {
		if (debugLevel.getLevel() >= pluginConfig.DEBUG.getLevel()) {
			logger.info(message);
		}
	}

	public static void warn(String message) {
		logger.warn(message);
	}

	public static void error(String message, Throwable t) {
		logger.error(message, t);
	}

	public static void error(String message) {
		logger.error(message);
	}
}
