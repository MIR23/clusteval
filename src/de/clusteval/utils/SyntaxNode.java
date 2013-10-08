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
package de.clusteval.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Christian Wiwie
 * 
 */
public class SyntaxNode {

	protected String label;
	protected List<SyntaxNode> childs;

	public SyntaxNode(final String label) {
		super();
		this.label = label;
		this.childs = new ArrayList<SyntaxNode>();
	}

	public void add(final SyntaxNode child) {
		this.childs.add(child);
	}

	public SyntaxNode getChildAt(final int pos) {
		return this.childs.get(pos);
	}

	public int getChildCount() {
		return this.childs.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.label;
	}
}
