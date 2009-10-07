package com.googlecode.jaev;

import java.util.concurrent.TimeUnit;

/**
 * The timeout capable <code>ValidatorFactoryBuilder</code> defines some
 * additional configuration featutes for the <code>ValiatorFactory</code>.
 */
public interface TimeoutCapableValidatorFactoryBuilder extends ValidatorFactoryBuilder {

	/**
	 * Configures the time out
	 * 
	 * @param timeout
	 *            The time out
	 * @param unit
	 *            The time out unit
	 * @return The <code>TimeoutCapableValidatorFactoryBuilder</code> instance
	 */
	public TimeoutCapableValidatorFactoryBuilder withTimeout(long timeout, TimeUnit unit);

	/**
	 * Defines the core and maximum pool size for the executor service
	 * 
	 * @param core
	 *            The core pool size
	 * @param max
	 *            The maximal pool size
	 * @return The <code>TimeoutCapableValidatorFactoryBuilder</code> instance
	 */
	public TimeoutCapableValidatorFactoryBuilder withPool(int core, int max);
}
