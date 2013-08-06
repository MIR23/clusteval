/**
 * 
 */
package de.clusteval.framework.repository.config;

import java.util.HashMap;

/**
 * @author Christian Wiwie
 * 
 */
public class DefaultRepositoryConfig extends RepositoryConfig {

	/**
	 */
	public DefaultRepositoryConfig() {
		super(new MysqlConfig(false, null, null, null),
				new HashMap<String, Long>(), -1);
	}
}
