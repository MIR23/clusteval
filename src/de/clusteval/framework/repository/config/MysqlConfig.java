/*******************************************************************************
 * Copyright (c) 2013 Christian Wiwie.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Christian Wiwie - initial API and implementation
 ******************************************************************************/
/**
 * 
 */
package de.clusteval.framework.repository.config;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Wrapper class to store a mysql connection configuration.
 * 
 * @author Christian Wiwie
 * 
 */
public class MysqlConfig {

	protected boolean usesMysql;
	protected String mysqlUsername;
	protected String mysqlDatabase;
	protected String mysqlHost;

	/**
	 * @param usesMysql
	 * @param mysqlUsername
	 * @param mysqlDatabase
	 * @param mysqlHost
	 */
	public MysqlConfig(final boolean usesMysql, final String mysqlUsername,
			final String mysqlDatabase, final String mysqlHost) {
		super();
		this.usesMysql = usesMysql;
		this.mysqlUsername = mysqlUsername;
		this.mysqlDatabase = mysqlDatabase;
		this.mysqlHost = mysqlHost;
	}

	/**
	 * @return A boolean indicating, whether the repository that this mysql
	 *         configuration corresponds to uses mysql.
	 */
	public boolean usesMysql() {
		return this.usesMysql;
	}

	/**
	 * 
	 * @return The username used to connect to the mysql database.
	 */
	public String getUsername() {
		return this.mysqlUsername;
	}

	/**
	 * @return The password used to connect to the mysql database.
	 */
	public String getPassword() {
		String mysqlPassword;
		Console c = System.console();
		if (c != null) {
			mysqlPassword = new String(System.console().readPassword(
					"MySQL password for '%s'@%s: ", mysqlUsername, mysqlHost));
		}
		// handling for eclipse launching
		else {
			mysqlPassword = "";
			System.out.printf("MySQL password for '%s'@%s: ", mysqlUsername,
					mysqlHost);
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(System.in));
			try {
				mysqlPassword = bufferedReader.readLine();
			} catch (IOException e) {
				// Ignore
			}
		}
		return mysqlPassword;
	}

	/**
	 * 
	 * @return The name of the database to connect to.
	 */
	public String getDatabase() {
		return this.mysqlDatabase;
	}

	/**
	 * 
	 * @return The host address to connect to.
	 */
	public String getHost() {
		return this.mysqlHost;
	}
}
