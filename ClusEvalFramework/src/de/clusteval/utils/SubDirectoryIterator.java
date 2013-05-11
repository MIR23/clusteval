/**
 * 
 */
package de.clusteval.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Christian Wiwie
 * 
 */
public class SubDirectoryIterator implements Iterator<File> {

	protected List<File> files;
	protected int pos;

	/**
	 * @param file
	 */
	public SubDirectoryIterator(final File file) {
		this.files = new ArrayList<File>();
		this.pos = 0;
		for (File child : file.listFiles())
			if (child.isDirectory())
				for (File subChild : child.listFiles())
					this.files.add(subChild);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext() {
		return this.pos < this.files.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Iterator#next()
	 */
	@Override
	public File next() {
		return this.files.get(this.pos++);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Iterator#remove()
	 */
	@Override
	public void remove() {
		// not supported
	}

}
