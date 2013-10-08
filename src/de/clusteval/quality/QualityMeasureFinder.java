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
package de.clusteval.quality;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Iterator;

import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.utils.JARFinder;
import de.clusteval.utils.RecursiveSubDirectoryIterator;

/**
 * @author Christian Wiwie
 */
public class QualityMeasureFinder extends JARFinder<QualityMeasure> {

	/**
	 * Instantiates a new clustering quality measure finder.
	 * 
	 * @param repository
	 *            The repository to register the new data configurations at.
	 * @param changeDate
	 * @param absPath
	 * @throws RegisterException
	 */
	public QualityMeasureFinder(final Repository repository,
			final long changeDate, final File absPath) throws RegisterException {
		super(repository, changeDate, absPath);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.JARFinder#getRegisteredObjectSet()
	 */
	@Override
	protected Collection<Class<? extends QualityMeasure>> getRegisteredObjectSet() {
		return this.repository.getClusteringQualityMeasureClasses();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.JARFinder#removeOldObject(java.lang.Class)
	 */
	@Override
	protected void removeOldObject(Class<? extends QualityMeasure> object) {
		this.repository.unregisterClusteringQualityMeasureClass(object);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.JARFinder#classForJARFile(java.io.File)
	 */
	@Override
	protected String[] classNamesForJARFile(File f) {
		return new String[]{"de.clusteval.quality."
				+ f.getName().replace(".jar", "")};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.JARFinder#getURLClassLoader0(java.io.File)
	 */
	@Override
	protected URLClassLoader getURLClassLoader0(File f, final ClassLoader parent)
			throws MalformedURLException {
		URL url = f.toURI().toURL();
		return new QualityMeasureURLClassLoader(this, new URL[]{url}, parent);
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
						.isQualityMeasureRegistered(classNamesForJARFile(f)[0]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.Finder#checkFile(java.io.File)
	 */
	@Override
	protected boolean checkFile(File file) {
		return file.getName().endsWith("QualityMeasure.jar");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.Finder#getBaseDir()
	 */
	@Override
	protected File getBaseDir() {
		return new File(this.repository.getClusteringQualityMeasuresBasePath());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.Finder#getClassToFind()
	 */
	@Override
	protected Class<?> getClassToFind() {
		return QualityMeasure.class;
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
	 * @see utils.RepositoryObject#register()
	 */
	@Override
	public boolean register() {
		return repository.register(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.RepositoryObject#unregister()
	 */
	@Override
	public boolean unregister() {
		return this.repository.unregister(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (this.getClass().toString() + this.repository).hashCode();
	}
}

class QualityMeasureURLClassLoader extends URLClassLoader {

	protected QualityMeasureFinder parent;

	/**
	 * @param urls
	 * @param parent
	 * @param loaderParent
	 */
	public QualityMeasureURLClassLoader(QualityMeasureFinder parent,
			URL[] urls, ClassLoader loaderParent) {
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
		Class<?> result = super.loadClass(name, true);

		if (name.startsWith("de.clusteval.quality")) {
			if (!name.equals("de.clusteval.quality.QualityMeasure")
					&& !name.equals("de.clusteval.quality.QualityMeasureR")
					&& name.endsWith("QualityMeasure")) {
				@SuppressWarnings("unchecked")
				Class<? extends QualityMeasure> qualityMeasure = (Class<? extends QualityMeasure>) result;

				if (this.parent.getRepository()
						.registerClusteringQualityMeasureClass(qualityMeasure))
					this.parent.getLog().info(
							"QualityMeasure " + name + " loaded");
			}
		}
		return result;
	}
}
