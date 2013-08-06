/**
 * 
 */
package de.clusteval.framework.repository.config;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalINIConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.clusteval.framework.repository.Repository;

/**
 * A repository configuration determines certain settings and options for a
 * {@link Repository} and also for the complete backend. This includes for
 * example whether an sql database should be used or how often the supervising
 * threads of the repository should scan for changes.
 * 
 * @author Christian Wiwie
 * 
 */
public class RepositoryConfig {

	/**
	 * This method parses a repository configuration from the file at the given
	 * absolute path.
	 * 
	 * <p>
	 * A repository configuration contains several sections and possible
	 * options:
	 * <ul>
	 * <li><b>[mysql]</b></li>
	 * <ul>
	 * <li><b>host</b>: The ip address of the mysql host server.</li>
	 * <li><b>database</b>: The mysql database name.</li>
	 * <li><b>user</b>: The username used to connect to the database.</li>
	 * <li><b>password</b>: The mysql password used to connect to the database.
	 * The password is prompted from the console and not parsed from the file.</li>
	 * </ul>
	 * <li><b>[threading]</b></li>
	 * <li><b>NameOfTheThreadSleepTime</b>: Sleeping time of the thread
	 * 'NameOfTheThread'. This option can be used to control the frequency, with
	 * which the threads check for changes on the filesystem.</li> </ul>
	 * 
	 * @param absConfigPath
	 *            The absolute path of the repository configuration file.
	 * @return The parsed repository configuration.
	 * @throws RepositoryConfigNotFoundException
	 * @throws RepositoryConfigurationException
	 */
	public static RepositoryConfig parseFromFile(final File absConfigPath)
			throws RepositoryConfigNotFoundException,
			RepositoryConfigurationException {
		if (!absConfigPath.exists())
			throw new RepositoryConfigNotFoundException("Repository config \""
					+ absConfigPath + "\" does not exist!");

		Logger log = LoggerFactory.getLogger(RepositoryConfig.class);

		log.debug("Parsing repository config \"" + absConfigPath + "\"");

		try {

			HierarchicalINIConfiguration props = new HierarchicalINIConfiguration(
					absConfigPath);
			props.setThrowExceptionOnMissing(true);

			boolean usesMysql = false;
			MysqlConfig mysqlConfig = null;

			if (props.getSections().contains("mysql")) {
				usesMysql = true;
				String mysqlUsername, mysqlDatabase, mysqlHost;
				SubnodeConfiguration mysql = props.getSection("mysql");
				mysqlUsername = mysql.getString("user");

				mysqlDatabase = mysql.getString("database");
				mysqlHost = mysql.getString("host");
				mysqlConfig = new MysqlConfig(usesMysql, mysqlUsername,
						mysqlDatabase, mysqlHost);
			} else
				mysqlConfig = new MysqlConfig(usesMysql, "", "", "");

			Map<String, Long> threadingSleepTimes = new HashMap<String, Long>();

			long methodMaxTime = -1;

			if (props.getSections().contains("threading")) {
				SubnodeConfiguration threading = props.getSection("threading");
				Iterator<String> it = threading.getKeys();
				while (it.hasNext()) {
					String key = it.next();
					if (key.equals("methodMaxTime")) {
						methodMaxTime = threading.getLong(key);
					} else if (key.endsWith("SleepTime")) {
						String subKey = key.substring(0,
								key.indexOf("SleepTime"));
						try {
							threadingSleepTimes.put(subKey,
									threading.getLong(key));
						} catch (Exception e) {
							// in case anything goes wrong, we just ignore this
							// option
							e.printStackTrace();
						}
					}
				}
			}

			return new RepositoryConfig(mysqlConfig, threadingSleepTimes,
					methodMaxTime);
		} catch (ConfigurationException e) {
			throw new RepositoryConfigurationException(e.getMessage());
		} catch (NoSuchElementException e) {
			throw new RepositoryConfigurationException(e.getMessage());
		}
	}

	/**
	 * This map holds the sleeping times for all threads that check the
	 * repository for changes.
	 */
	protected Map<String, Long> threadingSleepingTimes;

	/**
	 * The configuration of the mysql connection of the repository.
	 */
	protected MysqlConfig mysqlConfig;

	protected long methodMaxTime;

	/**
	 * Creates a new repository configuration.
	 * 
	 * @param mysqlConfig
	 *            The mysql configuration for the repository.
	 * @param threadingSleepTimes
	 *            The sleep times of the threads created for the repository.
	 * @param methodMaxTime
	 */
	public RepositoryConfig(final MysqlConfig mysqlConfig,
			final Map<String, Long> threadingSleepTimes,
			final long methodMaxTime) {
		super();
		this.mysqlConfig = mysqlConfig;
		this.threadingSleepingTimes = threadingSleepTimes;
		this.methodMaxTime = methodMaxTime;
	}

	/**
	 * @return The mysql configuration of this repository.
	 */
	public MysqlConfig getMysqlConfig() {
		return this.mysqlConfig;
	}

	/**
	 * Override the mysql configuration of this repository.
	 * 
	 * @param mysqlConfig
	 *            The new mysql configuration.
	 */
	public void setMysqlConfig(final MysqlConfig mysqlConfig) {
		this.mysqlConfig = mysqlConfig;
	}

	/**
	 * @return The thread sleep times for the repository.
	 * @see #threadingSleepingTimes
	 */
	public Map<String, Long> getThreadSleepTimes() {
		return this.threadingSleepingTimes;
	}
	
	public long getMethodMaxTime() {
		return this.methodMaxTime;
	}

}
