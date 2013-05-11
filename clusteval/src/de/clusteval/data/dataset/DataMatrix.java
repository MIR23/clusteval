/**
 * 
 */
package de.clusteval.data.dataset;

import utils.MySparseDoubleMatrix2D;

/**
 * This is a wrapper class for absolute data that needs to be stored in memory.
 * The absolute coordinate values are stored as double values in a sparse
 * matrix. That means default values of the matrix are not stored in memory.
 * 
 * @author Christian Wiwie
 * 
 */
public class DataMatrix {

	protected String[] ids;
	protected MySparseDoubleMatrix2D sparseMatrix;

	/**
	 * @param ids
	 * @param data
	 */
	public DataMatrix(final String[] ids, final double[][] data) {
		super();
		this.ids = ids;
		this.sparseMatrix = new MySparseDoubleMatrix2D(data);
	}

	/**
	 * @return The object ids contained in this matrix.
	 */
	public String[] getIds() {
		return this.ids;
	}

	/**
	 * @return The absolute coordinates of the objects contained in this matrix.
	 */
	public double[][] getData() {
		return this.sparseMatrix.toArray();
	}
}
