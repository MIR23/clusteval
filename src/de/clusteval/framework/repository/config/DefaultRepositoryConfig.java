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
