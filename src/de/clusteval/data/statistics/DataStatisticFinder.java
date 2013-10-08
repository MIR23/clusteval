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
package de.clusteval.data.statistics;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.utils.JARFinder;
import de.clusteval.utils.RecursiveSubDirectoryIterator;

/**
 * @author Christian Wiwie
 */
public class DataStatisticFinder extends JARFinder<DataStatistic> {

	/**
	 * Instantiates a new data set format finder.
	 * 
	 * @param repository
	 *            the repository
	 * @param changeDate
	 * @param absPath
	 * @throws RegisterException
	 */
	public DataStatisticFinder(final Repository repository,
			final long changeDate, final File absPath) throws RegisterException {
		super(repository, changeDate, absPath);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.JARFinder#getRegisteredObjectSet()
	 */
	@Override
	protected Collection<Class<? extends DataStatistic>> getRegisteredObjectSet() {
		return this.repository.getDataStatisticClasses();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.JARFinder#removeOldObject(java.lang.Class)
	 */
	@Override
	protected void removeOldObject(Class<? extends DataStatistic> object) {
		this.repository.unregisterDataStatisticClass(object);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.JARFinder#getURLClassLoader0(java.io.File)
	 */
	@SuppressWarnings("unused")
	@Override
	protected URLClassLoader getURLClassLoader0(File f, final ClassLoader parent)
			throws MalformedURLException {
		// add URLS for JARs into list
		List<URL> urls = this.search(new File(this.repository
				.getDataStatisticBasePath()));
		// load corresponding classes of URLs in list
		return new DataStatisticURLClassLoader(this, urls.toArray(new URL[0]),
				parent);
	}

	protected List<URL> search(final File f) throws MalformedURLException {
		List<URL> result = new ArrayList<URL>();
		if (f.isDirectory())
			for (File child : f.listFiles())
				result.addAll(search(child));
		else if (f.getName().endsWith("DataStatistic.jar"))
			result.add(f.toURI().toURL());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.Finder#checkFile(java.io.File)
	 */
	@Override
	protected boolean checkFile(File file) {
		return file.getName().endsWith("DataStatistic.jar");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.JARFinder#classNamesForJARFile(java.io.File)
	 */
	@Override
	protected String[] classNamesForJARFile(File f) {
		return new String[]{
				"de.clusteval.data.statistics."
						+ f.getName().replace(".jar", ""),
				"de.clusteval.data.statistics."
						+ f.getName().replace(".jar", "Calculator")};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.Finder#getBaseDir()
	 */
	@Override
	protected File getBaseDir() {
		return new File(this.repository.getDataStatisticBasePath());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.Finder#getClassToFind()
	 */
	@Override
	protected Class<?> getClassToFind() {
		return DataStatistic.class;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.Finder#getIterator()
	 */
	@Override
	protected Iterator<File> getIterator() {
		return new RecursiveSubDirectoryIterator(getBaseDir());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.JARFinder#isJARLoaded(java.io.File)
	 */
	@Override
	protected boolean isJARLoaded(File f) {
		return super.isJARLoaded(f)
				&& this.repository
						.isDataStatisticRegistered(classNamesForJARFile(f)[0]);
	}
}

class DataStatisticURLClassLoader extends URLClassLoader {

	protected DataStatisticFinder parent;

	/**
	 * @param urls
	 * @param parent
	 * @param loaderParent
	 */
	public DataStatisticURLClassLoader(DataStatisticFinder parent, URL[] urls,
			ClassLoader loaderParent) {
		super(urls, loaderParent);
		this.parent = parent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.ClassLoader#loadClass(java.lang.String)
	 */
	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		Class<?> result = super.loadClass(name);

		if (name.startsWith("de.clusteval.data.statistics")
				&& !name.equals("de.clusteval.data.statistics.DataStatistic")
				&& !name.equals("de.clusteval.data.statistics.DoubleValueDataStatistic")) {
			if (name.endsWith("DataStatistic")) {
				@SuppressWarnings("unchecked")
				Class<? extends DataStatistic> dataStatistic = (Class<? extends DataStatistic>) result;
				if (this.parent.getRepository().registerDataStatisticClass(
						dataStatistic))
					this.parent.getLog().info(
							"DataStatistic " + name + " loaded");

			} else if (name.endsWith("DataStatisticCalculator")
					&& !name.equals("DataStatisticRCalculator")) {
				@SuppressWarnings("unchecked")
				Class<? extends DataStatisticCalculator<? extends DataStatistic>> dataStatisticCalculator = (Class<? extends DataStatisticCalculator<? extends DataStatistic>>) result;
				this.parent.getRepository().registerDataStatisticCalculator(
						dataStatisticCalculator);
			}
		}
		return result;
	}
}
