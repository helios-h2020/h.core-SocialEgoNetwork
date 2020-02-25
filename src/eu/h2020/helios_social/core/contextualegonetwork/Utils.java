package eu.h2020.helios_social.core.contextualegonetwork;

import java.util.logging.Logger;

/**
 * This class implements static error parsing and logging methods, with the ability to suppress errors that can be silently handled during deployment.
 */
public class Utils {
	public static boolean development = true;
	private static Logger logger = Logger.getGlobal();
	
	/**
	 * If development is enabled, throws the given Throwable, otherwise logs its message.
	 * @param exception the given Throwable
	 * @throws ExceptionType The given exception if Utils.development is set to true
	 */
	public synchronized static <ExceptionType extends Throwable> void error(ExceptionType exception) throws ExceptionType {
		if(development)
			throw exception;
		logger.warning(exception.getLocalizedMessage());
	}

	/**
	 * Logs the given objects by calling their {@link Object#toString()} values.
	 * @param messages Comma separated objects to write in one line of the log file
	 */
	public static void log(Object... messages) {
		if(development) {
			StringBuilder message = new StringBuilder();
			if(messages.length>1)
				message.append("|");
			for(Object obj : messages) {
				message.append(obj.toString());
				if(messages.length>1)
					message.append("\t|");
			}
			logger.info(message.toString());
		}
	}

	/**
	 * If development is enabled, throws an Exception with the given
	 * message, otherwise logs that message.
	 * @param message the message of the raised Exception
	 * @throws RuntimeException The given exception if Utils.development is set to true
	 */
	public synchronized static void error(String message) throws RuntimeException {
		error(new RuntimeException(message));
	}
	
	/**
	 * If development is enabled, throws the given Throwable,
	 * otherwise logs its message and returns the given default value.
	 * @param exception The exception to potentially throw
	 * @param defaultValue The default value to return if an exception is not thrown
	 * @return The given defaultValue
	 * @throws ExceptionType The given exception if Utils.development is set to true
	 */
	public synchronized static <ExceptionType extends Throwable, ValueType> ValueType error(ExceptionType exception, ValueType defaultValue) throws ExceptionType {
		error(exception);
		return defaultValue;
	}

	/**
	 * If development is enabled, throws an Exception with the given message,
	 * otherwise logs that message and returns the given default value.
	 * @param message The message of the thrown Exception
	 * @param defaultValue The default value to return if an exception is not thrown
	 * @return The given defaultValue
	 * @throws RuntimeException The given exception if Utils.development is set to true
	 */
	public synchronized static <ValueType> ValueType error(String message, ValueType defaultValue) throws RuntimeException {
		error(message);
		return defaultValue;
	}
	
	/**
	 * @return A UNIX timestamp representing the current time
	 */
	public static long getCurrentTimestamp() {
		return System.currentTimeMillis();
	}
}
