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
package de.clusteval.tools;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.Pair;
import utils.ProgressPrinter;
import utils.Triple;
import utils.parse.TextFileParser;
import utils.parse.TextFileParser.OUTPUT_MODE;
import ch.qos.logback.classic.Level;
import de.clusteval.context.IncompatibleContextException;
import de.clusteval.context.UnknownContextException;
import de.clusteval.data.DataConfig;
import de.clusteval.data.DataConfigNotFoundException;
import de.clusteval.data.DataConfigurationException;
import de.clusteval.data.dataset.DataSetConfigNotFoundException;
import de.clusteval.data.dataset.DataSetConfigurationException;
import de.clusteval.data.dataset.DataSetNotFoundException;
import de.clusteval.data.dataset.IncompatibleDataSetConfigPreprocessorException;
import de.clusteval.data.dataset.NoDataSetException;
import de.clusteval.data.dataset.format.InvalidDataSetFormatVersionException;
import de.clusteval.data.dataset.format.UnknownDataSetFormatException;
import de.clusteval.data.dataset.type.UnknownDataSetTypeException;
import de.clusteval.data.distance.UnknownDistanceMeasureException;
import de.clusteval.data.goldstandard.GoldStandardConfigNotFoundException;
import de.clusteval.data.goldstandard.GoldStandardConfigurationException;
import de.clusteval.data.goldstandard.GoldStandardNotFoundException;
import de.clusteval.data.goldstandard.format.UnknownGoldStandardFormatException;
import de.clusteval.data.preprocessing.UnknownDataPreprocessorException;
import de.clusteval.data.statistics.UnknownDataStatisticException;
import de.clusteval.framework.ClustevalBackendServer;
import de.clusteval.framework.repository.InvalidRepositoryException;
import de.clusteval.framework.repository.NoRepositoryFoundException;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.framework.repository.RepositoryAlreadyExistsException;
import de.clusteval.framework.repository.RunResultRepository;
import de.clusteval.framework.repository.config.RepositoryConfigNotFoundException;
import de.clusteval.framework.repository.config.RepositoryConfigurationException;
import de.clusteval.graphmatching.GraphMatching;
import de.clusteval.paramOptimization.IncompatibleParameterOptimizationMethodException;
import de.clusteval.paramOptimization.InvalidOptimizationParameterException;
import de.clusteval.paramOptimization.UnknownParameterOptimizationMethodException;
import de.clusteval.program.NoOptimizableProgramParameterException;
import de.clusteval.program.ParameterSet;
import de.clusteval.program.ProgramConfig;
import de.clusteval.program.UnknownParameterType;
import de.clusteval.program.UnknownProgramParameterException;
import de.clusteval.program.UnknownProgramTypeException;
import de.clusteval.program.r.UnknownRProgramException;
import de.clusteval.quality.QualityMeasure;
import de.clusteval.quality.QualitySet;
import de.clusteval.quality.UnknownQualityMeasureException;
import de.clusteval.run.InvalidRunModeException;
import de.clusteval.run.ParameterOptimizationRun;
import de.clusteval.run.RunException;
import de.clusteval.run.result.ExecutionRunResult;
import de.clusteval.run.result.ParameterOptimizationResult;
import de.clusteval.run.result.RunResult;
import de.clusteval.run.result.RunResultParseException;
import de.clusteval.run.result.format.UnknownRunResultFormatException;
import de.clusteval.run.statistics.UnknownRunDataStatisticException;
import de.clusteval.run.statistics.UnknownRunStatisticException;
import de.clusteval.utils.ClustEvalException;
import de.clusteval.utils.InvalidConfigurationFileException;
import file.FileUtils;

/**
 * @author Christian Wiwie
 * 
 */
public class StandaloneQualityEval {

	protected Repository repo;
	protected ProgressPrinter printer;

	public StandaloneQualityEval(final String absRepoPath,
			final String... qualityMeasures)
			throws RepositoryAlreadyExistsException,
			InvalidRepositoryException, RepositoryConfigNotFoundException,
			RepositoryConfigurationException, UnknownQualityMeasureException,
			UnknownDataSetFormatException,
			InvalidDataSetFormatVersionException, IllegalArgumentException,
			IOException, UnknownGoldStandardFormatException,
			GoldStandardNotFoundException, GoldStandardConfigurationException,
			DataSetConfigurationException, DataSetNotFoundException,
			DataSetConfigNotFoundException,
			GoldStandardConfigNotFoundException, NoDataSetException,
			DataConfigurationException, DataConfigNotFoundException,
			RunResultParseException, ConfigurationException, RegisterException,
			UnknownContextException, UnknownRunResultFormatException,
			InvalidRunModeException,
			UnknownParameterOptimizationMethodException,
			NoOptimizableProgramParameterException,
			UnknownProgramParameterException,
			InvalidConfigurationFileException, NoRepositoryFoundException,
			InvalidOptimizationParameterException, RunException,
			UnknownDataStatisticException, UnknownProgramTypeException,
			UnknownRProgramException,
			IncompatibleParameterOptimizationMethodException,
			UnknownDistanceMeasureException, UnknownRunStatisticException,
			UnknownDataSetTypeException, UnknownRunDataStatisticException,
			UnknownDataPreprocessorException,
			IncompatibleDataSetConfigPreprocessorException,
			IncompatibleContextException, UnknownParameterType {
		super();
		ClustevalBackendServer.logLevel(Level.INFO);
		Repository parent = new Repository(new File(absRepoPath)
				.getParentFile().getParent(), null);
		parent.initialize();
		this.repo = new RunResultRepository(absRepoPath, parent);
		this.repo.initialize();

		List<ExecutionRunResult> results = new ArrayList<ExecutionRunResult>();
		ParameterOptimizationRun run = (ParameterOptimizationRun) RunResult
				.parseFromRunResultFolder(parent, new File(absRepoPath),
						results, true, true);

		// new measures to assess
		final Set<QualityMeasure> measures = new HashSet<QualityMeasure>();
		for (String measure : qualityMeasures)
			measures.add(QualityMeasure.parseFromString(parent, measure));
		// remove measures already assessed earlier
		measures.removeAll(new HashSet<QualityMeasure>(run.getQualityMeasures()));

		run.getQualityMeasures().addAll(measures);

		for (ExecutionRunResult result : results) {
			result.loadIntoMemory();
			ParameterOptimizationResult optR = (ParameterOptimizationResult) result;

			List<Long> iterationNumbers = optR.getIterationNumbers();
			List<Pair<ParameterSet, QualitySet>> quals = optR
					.getOptimizationQualities();
			List<Pair<ParameterSet, GraphMatching>> matchings = optR
					.getOptimizationClusterings();

			final DataConfig dataConfig = optR.getDataConfig();
			final ProgramConfig programConfig = optR.getProgramConfig();

			this.printer = new MyProgressPrinter(iterationNumbers.size(), true);
			((ch.qos.logback.classic.Logger) LoggerFactory
					.getLogger(Logger.ROOT_LOGGER_NAME))
					.info("Assessing qualities of graph matchings for "
							+ dataConfig + " and " + programConfig + "...");

			final String completeQualityFile = FileUtils.buildPath(
					repo.getClusterResultsBasePath(), programConfig.getName()
							+ "_" + dataConfig.getName()
							+ ".results.qual.complete.new");
			if (new File(completeQualityFile).exists())
				new File(completeQualityFile).delete();

			ParameterOptimizationResult newResult = optR.clone();
			newResult.setAbsolutePath(new File(completeQualityFile));
			newResult.writeHeaderIntoCompleteFile();

			newResult.getDataConfig().getDatasetConfig().loadIntoMemory();

			List<Triple<ParameterSet, QualitySet, Long>> newQualities = new ArrayList<Triple<ParameterSet, QualitySet, Long>>();
			for (long l : iterationNumbers) {
				Pair<ParameterSet, QualitySet> q = quals.get((int) l - 1);
				Pair<ParameterSet, GraphMatching> matching = matchings
						.get((int) l - 1);
				QualitySet tmpQualities = matching.getSecond().assessQuality(
						newResult.getDataConfig(),
						new ArrayList<QualityMeasure>(measures));
				for (QualityMeasure m : tmpQualities.keySet())
					q.getSecond().put(m, tmpQualities.get(m));

				newQualities.add(Triple.getTriple(q.getFirst(), q.getSecond(),
						l));
			}
			newResult.writeQualitiesToFiles(newQualities);
			// this.printer.update(this.printer.getCurrentPos() + 1);
			result.unloadFromMemory();
		}

		for (File f : new File(repo.getClusterResultsBasePath())
				.listFiles(new FilenameFilter() {

					@Override
					public boolean accept(File dir, String name) {
						return name.endsWith(".new");
					}
				})) {
			if (new File(f.getAbsolutePath().replace(".new", "")).exists())
				new File(f.getAbsolutePath().replace(".new", "")).delete();
			Files.move(f.toPath(),
					new File(f.getAbsolutePath().replace(".new", "")).toPath());
		}
		TextFileParser parser = new TextFileParser(run.getAbsolutePath(),
				new int[0], new int[0], false, "\t", run.getAbsolutePath()
						+ ".new", OUTPUT_MODE.STREAM) {

			@Override
			protected void processLine(String[] key, String[] value) {
			}

			protected String getLineOutput(String[] key, String[] value) {
				if (value[0].contains("qualityMeasures")) {
					StringBuilder sb = new StringBuilder(value[0]);
					for (QualityMeasure m : measures) {
						sb.append(",");
						sb.append(m);
					}
					sb.append(System.getProperty("line.separator"));
					return sb.toString();
				}
				return value[0] + System.getProperty("line.separator");
			};
		}.process();
		new File(run.getAbsolutePath()).delete();
		Files.move(new File(run.getAbsolutePath() + ".new").toPath(), new File(
				run.getAbsolutePath()).toPath());
		System.exit(0);
	}

	public static void main(String[] args) throws ClustEvalException,
			IllegalArgumentException, ConfigurationException, IOException {
		new StandaloneQualityEval(args[0], Arrays.copyOfRange(args, 1,
				args.length));
	}
}

class MyProgressPrinter extends ProgressPrinter {

	/**
	 * 
	 */
	public MyProgressPrinter(final long upperLimit,
			final boolean printOnNewPercent) {
		super(upperLimit, printOnNewPercent);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.ProgressPrinter#log(java.lang.String)
	 */
	@Override
	protected void log(String message) {
		this.log.info(message);
	}
}
