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