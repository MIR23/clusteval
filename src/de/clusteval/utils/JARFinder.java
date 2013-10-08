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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.framework.repository.RepositoryObject;
import file.FileUtils;

/**
 * @author Christian Wiwie
 * @param <T>
 * 
 */
public abstract class JARFinder<T extends RepositoryObject> extends Finder {

	protected Map<URL, URLClassLoader> classLoaders;

	protected Map<File, List<File>> waitingFiles;

	protected Map<String, Long> loadedJarFileChangeDates;

	/**
	 * @param repository
	 * @param changeDate
	 * @param absPath
	 * @throws RegisterException
	 */
	public JARFinder(Repository repository, long changeDate, File absPath)
			throws RegisterException {
		super(repository, changeDate, absPath);
		this.classLoaders = this.repository.getJARFinderClassLoaders();
		this.waitingFiles = this.repository.getJARFinderWaitingFiles();
		this.loadedJarFileChangeDates = this.repository
				.getFinderLoadedJarFileChangeDates();
	}

	protected abstract String[] classNamesForJARFile(final File f);

	protected Class<?> loadClass(final String className,
			final URLClassLoader loader) throws ClassNotFoundException {
		return Class.forName(className, true, loader);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.Finder#doOnFileFound(java.io.File)
	 */
	@Override
	protected void doOnFileFound(File file) throws Exception {
		loadJAR(file);
	}

	/**
	 * Load jar.
	 * 
	 * @param f
	 *            the f
	 * @throws MalformedURLException
	 *             the malformed url exception
	 * @throws InstantiationException
	 *             the instantiation exception
	 * @throws IllegalAccessException
	 *             the illegal access exception
	 */
	protected void loadJAR(final File f) throws MalformedURLException {
		URLClassLoader loader = getURLClassLoaderAndStore(f);
		loadJAR(f, loader);
	}

	protected void loadJAR(final File f, final URLClassLoader loader)
			throws MalformedURLException {
		if (!isJARLoaded(f)) {
			// assume that the classname is equivalent to the jar-filename
			String[] classNames = classNamesForJARFile(f);
			for (String className : classNames) {
				try {
					loadClass(className, loader);

					// Clear the exceptions associated to this object
					knownExceptions.remove(className);
				} catch (Throwable e) {
					// check if we already logged a warning for this
					boolean known = false;
					if (knownExceptions.containsKey(className)) {
						for (Throwable other : knownExceptions.get(className))
							if ((e.getMessage() == null && other.getMessage() == null)
									|| (e.getMessage() != null
											&& other.getMessage() != null && other
											.getMessage()
											.equals(e.getMessage()))) {
								known = true;
								break;
							}
					}
					if (known) {
						if (e instanceof ClassNotFoundException) {
							this.log.debug("Could not load class " + className
									+ " in file " + f);
						} else if (e instanceof NoClassDefFoundError) {
							this.log.debug("Could not load class " + className
									+ " in file " + f
									+ " because a required class is missing:"
									+ e.getMessage());
						}
					} else {
						if (!knownExceptions.containsKey(className))
							knownExceptions.put(className,
									new ArrayList<Throwable>());
						knownExceptions.get(className).add(e);

						if (e instanceof ClassNotFoundException) {
							this.log.warn("Could not load class " + className
									+ " in file " + f);
						} else if (e instanceof NoClassDefFoundError) {
							// check whether the class has been loaded with a
							// different classlader
							String[] missingMessageSplit = e.getMessage()
									.split("/");
							String missingClassName = missingMessageSplit[missingMessageSplit.length - 1];
							File potentialParentFile = new File(
									FileUtils.buildPath(f.getParentFile()
											.getAbsolutePath(),
											missingClassName + ".jar"));
							URL url = potentialParentFile.toURI().toURL();
							if (classLoaders.containsKey(url)) {
								this.log.debug("Trying to load "
										+ className
										+ " with self-guessed parent class loader from file "
										+ f);
								loadJAR(f,
										getURLClassLoaderAndStore(f,
												classLoaders.get(url)));
							} else {
								this.log.info("Could not load class "
										+ className
										+ " in file "
										+ f
										+ " because a required class is missing:"
										+ e.getMessage());
								// add this file to the waiting files
								if (!waitingFiles
										.containsKey(potentialParentFile))
									waitingFiles.put(potentialParentFile,
											new ArrayList<File>());
								waitingFiles.get(potentialParentFile).add(f);
							}
						}
					}
				}
			}
			loadedJarFileChangeDates.put(f.getAbsolutePath(), f.lastModified());

			// process files depending on this file
			if (waitingFiles.containsKey(f)) {
				for (File waiting : waitingFiles.get(f))
					loadJAR(waiting,
							getURLClassLoader0(waiting,
									classLoaders.get(f.toURI().toURL())));
				waitingFiles.remove(f);
			}
		}
	}

	protected final URLClassLoader getURLClassLoaderAndStore(final File f)
			throws MalformedURLException {
		URLClassLoader loader = getURLClassLoader0(f);
		classLoaders.put(f.toURI().toURL(), loader);
		return loader;
	}

	protected final URLClassLoader getURLClassLoaderAndStore(final File f,
			final ClassLoader parent) throws MalformedURLException {
		URLClassLoader loader = getURLClassLoader0(f, parent);
		classLoaders.put(f.toURI().toURL(), loader);
		return loader;
	}

	protected URLClassLoader getURLClassLoader0(final File f)
			throws MalformedURLException {
		return getURLClassLoader0(f, ClassLoader.getSystemClassLoader());
	}

	protected abstract URLClassLoader getURLClassLoader0(final File f,
			final ClassLoader parent) throws MalformedURLException;

	/**
	 * We check whether the jar file has been loaded, using its modification
	 * date. The jar is only assumed do be loaded, if the modification dates are
	 * equal.
	 * 
	 * @param f
	 *            The jar file we want to load.
	 * @return true, if the jar file has been loaded, false otherwise.
	 */
	protected boolean isJARLoaded(final File f) {
		return loadedJarFileChangeDates.containsKey(f.getAbsolutePath())
				&& loadedJarFileChangeDates.get(f.getAbsolutePath()) == f
						.lastModified();
	}

	protected abstract Collection<Class<? extends T>> getRegisteredObjectSet();

	protected void validateRegisteredObjects() {
		/*
		 * First check whether all registered objects currently in the
		 * repository do still exist
		 */
		Collection<Class<? extends T>> toRemove = new HashSet<Class<? extends T>>();
		for (Class<? extends T> object : getRegisteredObjectSet())
			if (!new File(FileUtils.buildPath(getBaseDir().getAbsolutePath(),
					object.getSimpleName() + ".jar")).exists()) {
				toRemove.add(object);
			}

		for (Class<? extends T> object : toRemove) {
			removeOldObject(object);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.Finder#findAndRegisterObjects()
	 */
	@Override
	public void findAndRegisterObjects() throws RegisterException {
		validateRegisteredObjects();
		super.findAndRegisterObjects();
	}

	protected abstract void removeOldObject(Class<? extends T> object);
}
