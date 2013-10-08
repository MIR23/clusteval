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
package de.clusteval.framework.repository;

/**
 * @author Christian Wiwie
 * 
 */
public interface RepositoryListener {

	/**
	 * This method is invoked either by a listener itself when it wants to
	 * inform all other objects listening about its removal from the repository
	 * or when another object wants to notify this object about repositoral
	 * changes.
	 * 
	 * @param event
	 * @throws RegisterException
	 */
	public void notify(RepositoryEvent event) throws RegisterException;
}
