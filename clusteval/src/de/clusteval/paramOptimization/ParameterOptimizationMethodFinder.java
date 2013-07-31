/**
 * 
 */
package de.clusteval.paramOptimization;

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
public class ParameterOptimizationMethodFinder
		extends
			JARFinder<ParameterOptimizationMethod> {

	/**
	 * Instantiates a new clustering quality measure finder.
	 * 
	 * @param repository
	 *            The repository to register the new data configurations at.
	 * @throws RegisterException
	 */
	public ParameterOptimizationMethodFinder(final Repository repository)
			throws RegisterException {
		super(repository, System.currentTimeMillis(), new File(
				repository.getParameterOptimizationMethodsBasePath()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.Finder#getBaseDir()
	 */
	@Override
	protected File getBaseDir() {
		return new File(
				this.repository.getParameterOptimizationMethodsBasePath());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.Finder#checkFile(java.io.File)
	 */
	@Override
	protected boolean checkFile(File file) {
		return file.getName().endsWith("ParameterOptimizationMethod.jar");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.Finder#getClassToFind()
	 */
	@Override
	protected Class<?> getClassToFind() {
		return ParameterOptimizationMethod.class;
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
	 * @see utils.JARFinder#classNameForJARFile(java.io.File)
	 */
	@Override
	protected String[] classNamesForJARFile(File f) {
		return new String[]{"de.clusteval.paramOptimization."
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
						.isParameterOptimizationMethodRegistered(classNamesForJARFile(f)[0]);
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
		return new ParameterOptimizationMethodURLClassLoader(this,
				new URL[]{url}, parent);
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
	 * @see utils.JARFinder#getRegisteredObjectSet()
	 */
	@Override
	protected Collection<Class<? extends ParameterOptimizationMethod>> getRegisteredObjectSet() {
		return this.repository.getParameterOptimizationMethodClasses();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.JARFinder#removeOldObject(java.lang.Class)
	 */
	@Override
	protected void removeOldObject(
			Class<? extends ParameterOptimizationMethod> object) {
		this.repository.unregisterParameterOptimizationMethodClass(object);
	}
}

class ParameterOptimizationMethodURLClassLoader extends URLClassLoader {

	protected ParameterOptimizationMethodFinder parent;

	/**
	 * @param urls
	 * @param parent
	 * @param loaderParent
	 */
	public ParameterOptimizationMethodURLClassLoader(
			ParameterOptimizationMethodFinder parent, URL[] urls,
			ClassLoader loaderParent) {
		super(urls, loaderParent);
		this.parent = parent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.net.URLClassLoader#findClass(java.lang.String)
	 */
	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		Class<?> result = super.findClass(name);

		// if the class to load has parent annotation, we have to make sure,
		// that the parent is loaded
		if (result.isAnnotationPresent(LoadableClassParentAnnotation.class)) {
			// get the annotation
			LoadableClassParentAnnotation anno = result
					.getAnnotation(LoadableClassParentAnnotation.class);
			try {
				// try to load the parent of the class
				this.loadClass("de.clusteval.paramOptimization."
						+ anno.parent());
			} catch (ClassNotFoundException e) {
				// throw the NoClassDefFoundError, which is handled in
				// JARFinder.loadJAR()
				throw new NoClassDefFoundError(anno.parent());
			}
		}

		if (name.startsWith("de.clusteval.paramOptimization")
				&& !name.equals("de.clusteval.paramOptimization.ParameterOptimizationMethod")
				&& !name.equals("de.clusteval.paramOptimization.IDivergingParameterOptimizationMethod")) {
			if (name.endsWith("ParameterOptimizationMethod")) {
				@SuppressWarnings("unchecked")
				Class<? extends ParameterOptimizationMethod> parameterOptimizationMethod = (Class<? extends ParameterOptimizationMethod>) result;

				if (this.parent.getRepository()
						.registerParameterOptimizationMethodClass(
								parameterOptimizationMethod))
					this.parent.getLog().info(
							"ParameterOptimizationMethod " + name + " loaded");
			}
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.ClassLoader#loadClass(java.lang.String)
	 */
	// @Override
	// public Class<?> loadClass(String name) throws ClassNotFoundException {
	// Class<?> result = super.loadClass(name, true);
	// // Class<?> result = super.loadClass(name, false);
	//
	// if (name.startsWith("de.clusteval.paramOptimization")
	// &&
	// !name.equals("de.clusteval.paramOptimization.ParameterOptimizationMethod")
	// &&
	// !name.equals("de.clusteval.paramOptimization.IDivergingParameterOptimizationMethod"))
	// {
	// if (name.endsWith("ParameterOptimizationMethod")) {
	// @SuppressWarnings("unchecked")
	// Class<? extends ParameterOptimizationMethod> parameterOptimizationMethod
	// = (Class<? extends ParameterOptimizationMethod>) result;
	//
	// if (this.parent.getRepository()
	// .registerParameterOptimizationMethodClass(
	// parameterOptimizationMethod))
	// this.parent.getLog().info(
	// "ParameterOptimizationMethod " + name + " loaded");
	// }
	// }
	// return result;
	// }
}
