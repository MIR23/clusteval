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
package de.clusteval.run.result;

import java.io.File;
import java.util.List;

import utils.Triple;
import de.clusteval.data.DataConfig;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.program.ParameterSet;
import de.clusteval.program.ProgramConfig;
import de.clusteval.program.ProgramParameter;
import de.clusteval.quality.QualityMeasure;
import de.clusteval.quality.QualitySet;
import de.clusteval.run.ExecutionRun;
import de.clusteval.run.Run;
import file.FileUtils;

/**
 * @author Christian Wiwie
 * 
 */
public abstract class ExecutionRunResult extends RunResult {

	/** The data config. */
	protected DataConfig dataConfig;

	/** The program config. */
	protected ProgramConfig programConfig;

	/**
	 * @param repository
	 * @param changeDate
	 * @param absPath
	 * @param runIdentString
	 * @param run
	 * @param dataConfig
	 * @param programConfig
	 * @throws RegisterException
	 */
	public ExecutionRunResult(Repository repository, long changeDate,
			File absPath, String runIdentString, final Run run,
			final DataConfig dataConfig, final ProgramConfig programConfig)
			throws RegisterException {
		super(repository, changeDate, absPath, runIdentString, run);
		this.dataConfig = dataConfig;
		this.programConfig = programConfig;
	}

	/**
	 * The copy constructor of run results.
	 * 
	 * @param other
	 *            The object to clone.
	 * @throws RegisterException
	 */
	public ExecutionRunResult(final ExecutionRunResult other)
			throws RegisterException {
		super(other);
		this.dataConfig = other.dataConfig.clone();
		this.programConfig = other.programConfig.clone();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see run.result.RunResult#clone()
	 */
	@Override
	public abstract ExecutionRunResult clone();

	/**
	 * @return The program configuration wrapping the program that produced this
	 *         runresult.
	 */
	public ProgramConfig getProgramConfig() {
		return this.programConfig;
	}

	/**
	 * @return The data configuration wrapping the dataset on which this
	 *         runresult was produced.
	 */
	public DataConfig getDataConfig() {
		return this.dataConfig;
	}

	/**
	 * A helper method to write a header into the complete quality output in the
	 * beginning.
	 * 
	 * <p>
	 * If at all, then this method is invoked by {@link #beginRun()} before
	 * anything has been executed by the runnable.
	 */
	public void writeHeaderIntoCompleteFile() {
		StringBuilder sb = new StringBuilder();
		// 04.04.2013: adding iteration numbers into complete file
		sb.append("iteration\t");
		for (int p = 0; p < programConfig.getOptimizableParams().size(); p++) {
			ProgramParameter<?> param = programConfig.getOptimizableParams()
					.get(p);
			if (p > 0)
				sb.append(",");
			sb.append(param);
		}
		sb.append("\t");
		for (QualityMeasure measure : ((ExecutionRun) this.getRun())
				.getQualityMeasures()) {
			sb.append(measure.getClass().getSimpleName());
			sb.append("\t");
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.append("\n");

		FileUtils.appendStringToFile(this.getAbsolutePath(), sb.toString());
	}

	/**
	 * Helper method of {@link #assessQualities(GraphMatchingRunResult)},
	 * invoked to write the assessed clustering qualities into files.
	 * 
	 * @param qualities
	 *            A list containing pairs of parameter sets and corresponding
	 *            clustering qualities of different measures.
	 */
	public void writeQualitiesToFiles(
			List<Triple<ParameterSet, QualitySet, Long>> qualities) {
		// 04.04.2013: adding iteration number into first column
		for (Triple<ParameterSet, QualitySet, Long> clustSet : qualities) {
			/*
			 * Write the qualities into the complete file
			 */
			StringBuilder sb = new StringBuilder();
			sb.append(clustSet.getThird());
			sb.append("\t");
			for (int p = 0; p < programConfig.getOptimizableParams().size(); p++) {
				ProgramParameter<?> param = programConfig
						.getOptimizableParams().get(p);
				if (p > 0)
					sb.append(",");
				sb.append(clustSet.getFirst().get(param.getName()));
			}
			sb.append("\t");
			for (QualityMeasure measure : ((ExecutionRun) this.getRun())
					.getQualityMeasures()) {
				sb.append(clustSet.getSecond().get(measure));
				sb.append("\t");
			}
			sb.deleteCharAt(sb.length() - 1);
			sb.append("\n");

			FileUtils.appendStringToFile(this.getAbsolutePath(), sb.toString());

			// write into individual files
			final String qualityFile = this.getAbsolutePath().replace(
					".results.qual.complete",
					"." + clustSet.getThird() + ".results.matching.conv.qual");
			if (new File(qualityFile).exists())
				new File(qualityFile).delete();
			for (QualityMeasure qualityMeasure : clustSet.getSecond().keySet()) {
				FileUtils.appendStringToFile(qualityFile, qualityMeasure
						.getClass().getSimpleName()
						+ "\t"
						+ clustSet.getSecond().get(qualityMeasure) + "\n");
			}

			this.log.info(this.getRun() + " (" + this.programConfig + ","
					+ this.dataConfig + ") " + clustSet.getSecond().toString());
		}
	}
}
