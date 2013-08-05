/**
 * 
 */
package de.clusteval.utils;

import java.util.regex.Pattern;

import utils.parse.Parser;
import de.clusteval.utils.regexp.AndRegExp;
import de.clusteval.utils.regexp.BraketRegExp;
import de.clusteval.utils.regexp.OptionalRegExp;
import de.clusteval.utils.regexp.OrRegExp;
import de.clusteval.utils.regexp.RegExp;
import de.clusteval.utils.regexp.Token;

/**
 * @author Christian Wiwie
 * 
 */
public class FormatCompatibilityParser extends Parser<RegExp> {

	protected SyntaxNode root;

	public FormatCompatibilityParser(final String input) {
		super(input);
	}

	public RegExp parse() {
		this.pos = 0;
		endReached = false;
		root = new SyntaxNode("S1");
		s1(root);
		if (!endReached)
			throw new IllegalArgumentException("Blabla");
		RegExp exp = toRegExpr(root);
		return exp;
	}

	protected RegExp toRegExpr(final SyntaxNode node) {
		if (node.label.equals("S1")) {
			SyntaxNode child = node.getChildAt(1);
			if (child.getChildCount() == 2) {
				return new OrRegExp(toRegExpr(node.getChildAt(0)),
						toRegExpr(child.getChildAt(1)));
			}
			return toRegExpr(node.getChildAt(0));
		} else if (node.label.equals("S2")) {
			SyntaxNode child = node.getChildAt(1);
			if (child.getChildCount() == 2) {
				return new AndRegExp(toRegExpr(node.getChildAt(0)),
						toRegExpr(child.getChildAt(1)));
			}
			return toRegExpr(node.getChildAt(0));
		} else if (node.label.equals("S3")) {
			SyntaxNode child = node.getChildAt(1);
			if (child.getChildCount() == 1) {
				return new OptionalRegExp(toRegExpr(node.getChildAt(0)));
			}
			return toRegExpr(node.getChildAt(0));
		} else if (node.label.equals("S4")) {
			if (node.getChildCount() == 1) {
				return toRegExpr(node.getChildAt(0));
			}
			return new BraketRegExp(toRegExpr(node.getChildAt(1)));
		}
		// S4'
		else {
			if (node.getChildCount() == 2) {
				Token t1 = new Token(node.getChildAt(0).label);
				Token t2 = (Token) toRegExpr(node.getChildAt(1));
				if (t2 != null)
					return new Token(t1.getToken() + t2.getToken());
				return t1;
			}
			return null;
		}
	}

	protected void s1(SyntaxNode node) {
		SyntaxNode child1 = new SyntaxNode("S2");
		SyntaxNode child2 = new SyntaxNode("S1'");
		node.add(child1);
		node.add(child2);
		s2(child1);
		s1_strich(child2);
	}

	protected void s1_strich(SyntaxNode node) {
		if (!endReached && input.charAt(pos) == '|') {
			node.add(new SyntaxNode("|"));
			SyntaxNode child = new SyntaxNode("S1");
			node.add(child);
			match('|');
			s1(child);
		} else {
			// epsilon
		}
	}

	protected void s2(SyntaxNode node) {
		SyntaxNode child1 = new SyntaxNode("S3");
		SyntaxNode child2 = new SyntaxNode("S2'");
		node.add(child1);
		node.add(child2);
		s3(child1);
		s2_strich(child2);
	}

	protected void s2_strich(SyntaxNode node) {
		if (!endReached && input.charAt(pos) == '&') {
			node.add(new SyntaxNode("&"));
			SyntaxNode child1 = new SyntaxNode("S2");
			node.add(child1);
			match('&');
			s2(child1);
		} else {
			// epsilon
		}
	}

	protected void s3(SyntaxNode node) {
		SyntaxNode child1 = new SyntaxNode("S4");
		SyntaxNode child2 = new SyntaxNode("S3'");
		node.add(child1);
		node.add(child2);
		s4(child1);
		s3_strich(child2);
	}

	protected void s3_strich(SyntaxNode node) {
		if (!endReached && input.charAt(pos) == '?') {
			node.add(new SyntaxNode("?"));
			match('?');
		} else {
			// epsilon
		}
	}

	protected Pattern p = Pattern.compile("[a-zA-Z]");

	protected void s4(SyntaxNode node) {
		if (!endReached && input.charAt(pos) == '(') {
			node.add(new SyntaxNode("("));
			SyntaxNode child1 = new SyntaxNode("S1");
			node.add(child1);
			node.add(new SyntaxNode(")"));
			match('(');
			s1(child1);
			match(')');
		} else {
			SyntaxNode child1 = new SyntaxNode("S4'");
			node.add(child1);
			s4_strich(child1);
		}
	}

	protected void s4_strich(SyntaxNode node) {
		if (!endReached && p.matcher(input.charAt(pos) + "").matches()) {
			node.add(new SyntaxNode("" + input.charAt(pos)));
			SyntaxNode child1 = new SyntaxNode("S4'");
			node.add(child1);
			match(input.charAt(pos));
			s4_strich(child1);
		} else {
			// epsilon
		}
	}
}
