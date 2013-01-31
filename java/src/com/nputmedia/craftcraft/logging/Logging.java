package com.nputmedia.craftcraft.logging;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Logging {
	private final Logger logger;
	private final Level DEBUG = Level.FINE;

	private Logging(String loggerName) {
		logger = Logger.getLogger(loggerName);
	}

	public static Logging createLogger(Object obj) {
		return new Logging(obj.toString());
	}

	public boolean isDebugEnabled() {
		return logger.isLoggable(DEBUG);
	}

	public void debug(String message) {
		logger.log(DEBUG, message);
	}

	public void error(String message, Throwable e) {
		logger.log(Level.SEVERE, message, e);
	}
}
