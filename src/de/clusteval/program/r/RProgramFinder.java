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
package de.clusteval.program.r;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Iterator;

import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.program.ProgramConfig;
import de.clusteval.utils.JARFinder;

import utils.ArrayIterator;

/**
 * Objects of this class look for new RPrograms in the program-directory defined
 * in the corresponding repository (see {@link Repository#programBasePath}).
 * 
 * @author Christian Wiwie
 * 
 * 
 */
public class RProgramFinder extends JARFinder<RProgram> {

	/**
	 * Instantiates a new RProgram finder.
	 * 
	 * @param repository
	 *            the repository
	 * @param changeDate
	 * @param absPath
	 * @throws RegisterException
	 */
	public RProgramFinder(final Repository repository, final long changeDate,
			final File absPath) throws RegisterException {
		super(repository, changeDate, absPath);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.JARFinder#getRegisteredObjectSet()
	 */
	@Override
	protected Collection<Class<? extends RProgram>> getRegisteredObjectSet() {
		return this.repository.getRProgramClasses();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.JARFinder#removeOldObject(java.lang.Class)
	 */
	@Override
	protected void removeOldObject(Class<? extends RProgram> object) {
		this.repository.unregisterRProgramClass(object);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.Finder#getBaseDir()
	 */
	@Override
	protected File getBaseDir() {
		return new File(this.repository.getProgramBasePath());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.Finder#checkFile(java.io.File)
	 */
	@Override
	protected boolean checkFile(File file) {
		return file.isFile() && file.getName().endsWith("RProgram.jar");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.Finder#getIterator()
	 */
	@Override
	protected Iterator<File> getIterator() {
		return new ArrayIterator<File>(getBaseDir().listFiles());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.Finder#getClassToFind()
	 */
	@Override
	protected Class<?> getClassToFind() {
		return ProgramConfig.class;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.JARFinder#classNamesForJARFile(java.io.File)
	 */
	@Override
	protected String[] classNamesForJARFile(File f) {
		return new String[]{"de.clusteval.program.r."
				+ f.getName().replace(".jar", "")};
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
						.isRProgramRegistered(classNamesForJARFile(f)[0]);
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
		return new RProgramURLClassLoader(this, new URL[]{url}, parent);
	}
}

class RProgramURLClassLoader extends URLClassLoader {

	protected RProgramFinder parent;

	/**
	 * @param urls
	 * @param parent
	 * @param loaderParent
	 */
	public RProgramURLClassLoader(RProgramFinder parent, URL[] urls,
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

		if (name.startsWith("de.clusteval.program.r")
				&& !name.equals("de.clusteval.program.r.RProgram")
				&& !name.equals("de.clusteval.program.r.RelativeDataRProgram")
				&& !name.equals("de.clusteval.program.r.AbsoluteDataRProgram")) {
			if (name.endsWith("RProgram")) {
				@SuppressWarnings("unchecked")
				Class<? extends RProgram> rProgram = (Class<? extends RProgram>) result;

				if (this.parent.getRepository().registerRProgramClass(rProgram))
					this.parent.getLog().info("RProgram " + name + " loaded");

				RProgram program;
				try {
					program = RProgram.parseFromString(
							this.parent.getRepository(),
							rProgram.getSimpleName());
					program.register();
				} catch (UnknownRProgramException e) {
					e.printStackTrace();
				} catch (RegisterException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}
}
