package de.clusteval.data;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;

import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.framework.repository.RepositoryObject;
import de.clusteval.utils.FileFinder;

import utils.ArrayIterator;

/**
 * Objects of this class look for new data configuration-files in the directory
 * defined in the corresponding repository (see
 * {@link Repository#dataConfigBasePath}).
 * 
 * @author Christian Wiwie
 * 
 * 
 */
public class DataConfigFinder extends FileFinder {

	/**
	 * Instantiates a new data configuration finder.
	 * 
	 * @param repository
	 *            The repository to register the new data configurations at.
	 * @throws RegisterException
	 */
	public DataConfigFinder(final Repository repository)
			throws RegisterException {
		super(repository, System.currentTimeMillis(), new File(
				repository.getDataConfigBasePath()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.Finder#getBaseDir()
	 */
	@Override
	protected File getBaseDir() {
		return new File(this.repository.getDataConfigBasePath());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.Finder#checkFile(java.io.File)
	 */
	@Override
	protected boolean checkFile(File file) {
		return file.isFile() && file.getName().endsWith(".dataconfig");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.Finder#getClassToFind()
	 */
	@Override
	protected Class<?> getClassToFind() {
		return DataConfig.class;
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
	 * @see utils.FileFinder#getRegisteredObjectSet()
	 */
	@Override
	protected Collection<? extends RepositoryObject> getRegisteredObjectSet() {
		return this.repository.getDataConfigs();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.FileFinder#parseObjectFromFile(java.io.File)
	 */
	@Override
	protected RepositoryObject parseObjectFromFile(File file) throws Exception {
		return DataConfig.parseFromFile(file);
	}
}
