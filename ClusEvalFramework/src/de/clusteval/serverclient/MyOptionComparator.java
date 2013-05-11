/**
 * 
 */
package de.clusteval.serverclient;

import java.util.Comparator;

import org.apache.commons.cli.Option;

/**
 * This comparator is needed, to sort options by their required property first
 * and second after their name.
 * 
 * @author Christian Wiwie
 * 
 */
@SuppressWarnings("rawtypes")
public class MyOptionComparator implements Comparator {

	@Override
	public int compare(Object o1, Object o2) {
		Option opt1 = (Option) o1;
		Option opt2 = (Option) o2;

		if (opt1.isRequired() && !opt2.isRequired())
			return -1;
		else if (!opt1.isRequired() && opt2.isRequired())
			return 1;
		return opt1.getOpt().compareToIgnoreCase(opt2.getOpt());
	}

}
