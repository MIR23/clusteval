/**
 * 
 */
package de.clusteval.framework.threading;

import java.util.Map;

import de.clusteval.cluster.paramOptimization.ParameterOptimizationMethodFinderThread;
import de.clusteval.cluster.quality.ClusteringQualityMeasure;
import de.clusteval.cluster.quality.ClusteringQualityMeasureFinderThread;
import de.clusteval.context.ContextFinderThread;
import de.clusteval.data.DataConfig;
import de.clusteval.data.DataConfigFinderThread;
import de.clusteval.data.dataset.DataSet;
import de.clusteval.data.dataset.DataSetConfig;
import de.clusteval.data.dataset.DataSetConfigFinderThread;
import de.clusteval.data.dataset.DataSetFinderThread;
import de.clusteval.data.dataset.format.DataSetFormat;
import de.clusteval.data.dataset.format.DataSetFormatFinderThread;
import de.clusteval.data.dataset.generator.DataSetGenerator;
import de.clusteval.data.dataset.generator.DataSetGeneratorFinderThread;
import de.clusteval.data.dataset.type.DataSetType;
import de.clusteval.data.dataset.type.DataSetTypeFinderThread;
import de.clusteval.data.distance.DistanceMeasure;
import de.clusteval.data.distance.DistanceMeasureFinderThread;
import de.clusteval.data.goldstandard.GoldStandardConfig;
import de.clusteval.data.goldstandard.GoldStandardConfigFinderThread;
import de.clusteval.data.preprocessing.DataPreprocessorFinderThread;
import de.clusteval.data.statistics.DataStatistic;
import de.clusteval.data.statistics.DataStatisticFinderThread;
import de.clusteval.framework.ClustevalBackendServer;
import de.clusteval.framework.repository.Repository;
import de.clusteval.program.ProgramConfig;
import de.clusteval.program.ProgramConfigFinderThread;
import de.clusteval.program.r.RProgram;
import de.clusteval.program.r.RProgramFinderThread;
import de.clusteval.run.Run;
import de.clusteval.run.RunFinderThread;
import de.clusteval.run.result.RunResult;
import de.clusteval.run.result.RunResultFinderThread;
import de.clusteval.run.result.format.RunResultFormat;
import de.clusteval.run.result.format.RunResultFormatFinderThread;
import de.clusteval.run.statistics.RunDataStatistic;
import de.clusteval.run.statistics.RunDataStatisticFinderThread;
import de.clusteval.run.statistics.RunStatistic;
import de.clusteval.run.statistics.RunStatisticFinderThread;

/**
 * A type of supervisor thread which creates the standard set of threads
 * responsible for the initialization of a standard {@link Repository}. In
 * detail threads of the following type are created, started and kept alive:
 * 
 * <ul>
 * <li><b>DataSetFormatFinderThread</b>: A thread which checks
 * {@link Repository#dataSetFormatBasePath} for new dataset formats (see
 * {@link DataSetFormat}).</li>
 * <li><b>DataSetTypeFinderThread</b>: A thread which checks
 * {@link Repository#dataSetTypeBasePath} for new dataset types (see
 * {@link DataSetType}).</li>
 * <li><b>DataSetFinderThread</b>: A thread which checks
 * {@link Repository#dataSetBasePath} for new datasets (see {@link DataSet}).</li>
 * <li><b>DistanceMeasureFinderThread</b>: A thread which checks
 * {@link Repository#distanceMeasureBasePath} for new distance measures (see
 * {@link DistanceMeasure}).</li>
 * <li><b>DataStatisticFinderThread</b>: A thread which checks
 * {@link Repository#dataStatisticBasePath} for new data statistics (see
 * {@link DataStatistic}).</li>
 * <li><b>RunStatisticFinderThread</b>: A thread which checks
 * {@link Repository#runStatisticBasePath} for new run statistics (see
 * {@link RunStatistic}).</li>
 * <li><b>RunDataStatisticFinderThread</b>: A thread which checks
 * {@link Repository#runDataStatisticBasePath} for new run data statistics (see
 * {@link RunDataStatistic}).</li>
 * <li><b>RunResultFormatFinderThread</b>: A thread which checks
 * {@link Repository#runResultFormatBasePath} for new runresult formats (see
 * {@link RunResultFormat}).</li>
 * <li><b>ClusteringQualityMeasureFinderThread</b>: A thread which checks
 * {@link Repository#clusteringQualityMeasureBasePath} for new clustering
 * quality measures (see {@link ClusteringQualityMeasure}).</li>
 * <li><b>ParameterOptimizationMethodFinderThread</b>: A thread which checks
 * {@link Repository#parameterOptimizationMethodBasePath} for new parameter
 * optimization methods (see {@link DataSetFormat}).</li>
 * <li><b>DataSetConfigFinderThread</b>: A thread which checks
 * {@link Repository#dataSetConfigBasePath} for new dataset configurations (see
 * {@link DataSetConfig}).</li>
 * <li><b>GoldStandardConfigFinderThread</b>: A thread which checks
 * {@link Repository#goldStandardConfigBasePath} for new goldstandard
 * configurations (see {@link GoldStandardConfig}).</li>
 * <li><b>DataConfigFinderThread</b>: A thread which checks
 * {@link Repository#dataConfigBasePath} for new data configurations (see
 * {@link DataConfig}).</li>
 * <li><b>RProgramFinderThread</b>: A thread which checks
 * {@link Repository#programBasePath} for new RPrograms (see {@link RProgram}).</li>
 * <li><b>ProgramConfigFinderThread</b>: A thread which checks
 * {@link Repository#programConfigBasePath} for new program configurations (see
 * {@link ProgramConfig}).</li>
 * <li><b>DataSetGeneratorFinderThread</b>: A thread which checks
 * {@link Repository#dataSetGeneratorBasePath} for new dataset generators (see
 * {@link DataSetGenerator}).</li>
 * <li><b>RunFinderThread</b>: A thread which checks
 * {@link Repository#runBasePath} for new runs (see {@link Run}).</li>
 * <li><b>RunSchedulerThread</b>: A thread which is responsible for scheduling,
 * starting and terminating runs. It can be controlled with its
 * {@link RunSchedulerThread#schedule(String, String)},
 * {@link RunSchedulerThread#scheduleResume(String, String)} and
 * {@link RunSchedulerThread#terminate(String, String)} methods.</li>
 * </ul>
 * 
 * If the boolean parameter <b>checkForRunResults</b> is true, additionally the
 * following threads are started and kept alive:
 * 
 * <ul>
 * <li><b>RunResultFinderThread</b>: A thread which checks
 * {@link Repository#runResultBasePath} for new run results (see
 * {@link RunResult}).</li>
 * </ul>
 * 
 * @author Christian Wiwie
 * 
 */
public class RepositorySupervisorThread extends SupervisorThread {

	/**
	 * @param repository
	 *            The repository this thread belongs to.
	 * @param threadSleepTimes
	 *            The sleep times of the created threads.
	 * @param checkOnce
	 *            A boolean indicating, whether this thread should only check
	 *            once and terminate afterwards.
	 * @param checkForRunResults
	 *            Whether this thread should check for run results in the
	 *            repository.
	 */
	@SuppressWarnings({"unchecked"})
	public RepositorySupervisorThread(final Repository repository,
			Map<String, Long> threadSleepTimes, final boolean checkOnce,
			final boolean checkForRunResults) {
		super(repository, checkForRunResults
				? createList(DataSetFormatFinderThread.class,
						RunResultFormatFinderThread.class,
						ContextFinderThread.class,
						DataSetTypeFinderThread.class,
						DataSetFinderThread.class,
						DataPreprocessorFinderThread.class,
						DistanceMeasureFinderThread.class,
						DataStatisticFinderThread.class,
						RunStatisticFinderThread.class,
						RunDataStatisticFinderThread.class,
						ClusteringQualityMeasureFinderThread.class,
						ParameterOptimizationMethodFinderThread.class,
						DataSetConfigFinderThread.class,
						GoldStandardConfigFinderThread.class,
						DataConfigFinderThread.class,
						RProgramFinderThread.class,
						ProgramConfigFinderThread.class, RunFinderThread.class,
						DataSetGeneratorFinderThread.class,
						RunResultFinderThread.class) : createList(
						// normal elements
						ContextFinderThread.class,
						DataSetFormatFinderThread.class,
						DataSetTypeFinderThread.class,
						DataSetFinderThread.class,
						DataPreprocessorFinderThread.class,
						DistanceMeasureFinderThread.class,
						DataStatisticFinderThread.class,
						RunStatisticFinderThread.class,
						RunDataStatisticFinderThread.class,
						RunResultFormatFinderThread.class,
						ClusteringQualityMeasureFinderThread.class,
						ParameterOptimizationMethodFinderThread.class,
						DataSetConfigFinderThread.class,
						GoldStandardConfigFinderThread.class,
						DataConfigFinderThread.class,
						RProgramFinderThread.class,
						ProgramConfigFinderThread.class, RunFinderThread.class,
						DataSetGeneratorFinderThread.class), threadSleepTimes,
				checkOnce);

		this.threads.put(RunSchedulerThread.class, new RunSchedulerThread(this,
				this.repository, ClustevalBackendServer
						.getBackendServerConfiguration().getNumberOfThreads()));
		this.start();
	}
}