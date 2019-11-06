package contextualegonetwork;

import java.util.logging.Logger;

public class Utils {
	public static final boolean development = true;
	private static Logger logger = Logger.getGlobal();
	
	/**
	 * If development is enabled, throws the given Throwable, otherwise logs its message.
	 * @param exception the given Throwable
	 * @throws ExceptionType
	 */
	public static <ExceptionType extends Throwable> void error(ExceptionType exception) throws ExceptionType {
		if(development)
			throw exception;
		logger.warning(exception.getLocalizedMessage());
	}

	/**
	 * If development is enabled, throws an Exception with the given
	 * message, otherwise logs that message.
	 * @param message the message of the raised Exception
	 * @throws RuntimeException
	 */
	public static void error(String message) throws RuntimeException {
		error(new RuntimeException(message));
	}
	
	/**
	 * If development is enabled, throws the given Throwable,
	 * otherwise logs its message and returns the given default value.
	 * @param exception
	 * @param defaultValue
	 * @return the given defaultValue
	 * @throws ExceptionType
	 */
	public static <ExceptionType extends Throwable, ValueType> ValueType error(ExceptionType exception, ValueType defaultValue) throws ExceptionType {
		error(exception);
		return defaultValue;
	}

	/**
	 * If development is enabled, throws an Exception with the given message,
	 * otherwise logs that message and returns the given default value.
	 * @param message the message of the raised Exception
	 * @param defaultValue
	 * @return the given defaultValue
	 * @throws RuntimeException
	 */
	public static <ValueType> ValueType error(String message, ValueType defaultValue) throws RuntimeException {
		error(message);
		return defaultValue;
	}
	
}
