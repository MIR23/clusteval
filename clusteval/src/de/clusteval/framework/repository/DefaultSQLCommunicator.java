/**
 * 
 */
package de.clusteval.framework.repository;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import utils.Pair;
import de.clusteval.paramOptimization.ParameterOptimizationMethod;
import de.clusteval.context.Context;
import de.clusteval.data.DataConfig;
import de.clusteval.data.dataset.DataSet;
import de.clusteval.data.dataset.DataSetConfig;
import de.clusteval.data.dataset.format.DataSetFormat;
import de.clusteval.data.dataset.type.DataSetType;
import de.clusteval.data.goldstandard.GoldStandard;
import de.clusteval.data.goldstandard.GoldStandardConfig;
import de.clusteval.data.statistics.DataStatistic;
import de.clusteval.framework.repository.config.MysqlConfig;
import de.clusteval.graphmatching.GraphMatching;
import de.clusteval.program.DoubleProgramParameter;
import de.clusteval.program.IntegerProgramParameter;
import de.clusteval.program.ParameterSet;
import de.clusteval.program.Program;
import de.clusteval.program.ProgramConfig;
import de.clusteval.program.ProgramParameter;
import de.clusteval.program.StringProgramParameter;
import de.clusteval.quality.QualityMeasure;
import de.clusteval.quality.QualitySet;
import de.clusteval.run.AnalysisRun;
import de.clusteval.run.ClusteringRun;
import de.clusteval.run.DataAnalysisRun;
import de.clusteval.run.ExecutionRun;
import de.clusteval.run.InternalParameterOptimizationRun;
import de.clusteval.run.ParameterOptimizationRun;
import de.clusteval.run.Run;
import de.clusteval.run.RunAnalysisRun;
import de.clusteval.run.RunDataAnalysisRun;
import de.clusteval.run.result.AnalysisRunResult;
import de.clusteval.run.result.GraphMatchingRunResult;
import de.clusteval.run.result.DataAnalysisRunResult;
import de.clusteval.run.result.ExecutionRunResult;
import de.clusteval.run.result.ParameterOptimizationResult;
import de.clusteval.run.result.RunAnalysisRunResult;
import de.clusteval.run.result.RunDataAnalysisRunResult;
import de.clusteval.run.result.RunResult;
import de.clusteval.run.result.format.RunResultFormat;
import de.clusteval.run.statistics.RunDataStatistic;
import de.clusteval.run.statistics.RunStatistic;
import de.clusteval.utils.Statistic;

/**
 * A default sql communicator is the standard implementation of the abstract
 * {@link SQLCommunicator} and is intended as a default for standard
 * repositories of type {@link Repository}.
 * 
 * <p>
 * Subclasses of {@link Repository}, e.g. {@link RunResultRepository} have their
 * own sql communicator implementations.
 * 
 * @author Christian Wiwie
 * 
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class DefaultSQLCommunicator extends SQLCommunicator {

	/**
	 * A sql communicator needs a mysql configuration to know, how to connect to
	 * the database server.
	 */
	protected MysqlConfig mysqlConfig;

	/**
	 * @param repository
	 *            The repository this communicator belongs to.
	 * @param mysqlConfig
	 *            The mysql configuration this communicator should use.
	 */
	public DefaultSQLCommunicator(Repository repository,
			final MysqlConfig mysqlConfig) {
		super(repository);
		this.mysqlConfig = mysqlConfig;

		initDB();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getServer()
	 */
	@Override
	protected String getServer() {
		return this.mysqlConfig.getHost();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getDatabase()
	 */
	@Override
	protected String getDatabase() {
		return this.mysqlConfig.getDatabase();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getDBUsername()
	 */
	@Override
	protected String getDBUsername() {
		return this.mysqlConfig.getUsername();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getDBPassword()
	 */
	@Override
	protected String getDBPassword() {
		return this.mysqlConfig.getPassword();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableRepositories()
	 */
	@Override
	protected String getTableRepositories() {
		return "repositories";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see framework.repository.SQLCommunicator#getTableClusterings()
	 */
	@Override
	protected String getTableClusterings() {
		return "clusterings";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see framework.repository.SQLCommunicator#getTableClusterObjects()
	 */
	@Override
	protected String getTableClusterObjects() {
		return "cluster_objects";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see framework.repository.SQLCommunicator#getTableClusters()
	 */
	@Override
	protected String getTableClusters() {
		return "clusters";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableClusteringQualityMeasures()
	 */
	@Override
	protected String getTableClusteringQualityMeasures() {
		return "clustering_quality_measures";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableDataConfigs()
	 */
	@Override
	protected String getTableDataConfigs() {
		return "data_configs";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableDataets()
	 */
	@Override
	protected String getTableDatasets() {
		return "datasets";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableDataSetConfigs()
	 */
	@Override
	protected String getTableDataSetConfigs() {
		return "dataset_configs";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableDataSetFormats()
	 */
	@Override
	protected String getTableDataSetFormats() {
		return "dataset_formats";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableGoldStandardConfigs()
	 */
	@Override
	protected String getTableGoldStandardConfigs() {
		return "goldstandard_configs";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableGoldStandards()
	 */
	@Override
	protected String getTableGoldStandards() {
		return "goldstandards";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableOptimizableProgramParameters()
	 */
	@Override
	protected String getTableOptimizableProgramParameters() {
		return "optimizable_program_parameters";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableParameterOptimizationMethod()
	 */
	@Override
	protected String getTableParameterOptimizationMethods() {
		return "parameter_optimization_methods";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableProgramConfigs()
	 */
	@Override
	protected String getTableProgramConfigs() {
		return "program_configs";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableProgramParameter()
	 */
	@Override
	protected String getTableProgramParameter() {
		return "program_parameters";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTablePrograms()
	 */
	@Override
	protected String getTablePrograms() {
		return "programs";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableProgramsCompatibleDataSetFormats()
	 */
	@Override
	protected String getTableProgramConfigsCompatibleDataSetFormats() {
		return "program_configs_compatible_dataset_formats";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableRunsAnalysis()
	 */
	@Override
	protected String getTableRunsAnalysis() {
		return "run_analyses";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableRunsAnalysisData()
	 */
	@Override
	protected String getTableRunsAnalysisData() {
		return "run_data_analyses";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableRunsAnalysisDataDataIdentifiers()
	 */
	@Override
	protected String getTableRunsAnalysisDataDataIdentifiers() {
		return "run_data_analysis_data_configs";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableRunsAnalysisRun()
	 */
	@Override
	protected String getTableRunsAnalysisRun() {
		return "run_run_analyses";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableRunsAnalysisRunRunIdentifiers()
	 */
	@Override
	protected String getTableRunsAnalysisRunRunIdentifiers() {
		return "run_run_analysis_run_identifiers";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableRunsAnalysisRunData()
	 */
	@Override
	protected String getTableRunsAnalysisRunData() {
		return "run_run_data_analyses";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableRunsAnalysisStatistics()
	 */
	@Override
	protected String getTableRunsAnalysisStatistics() {
		return "run_analysis_statistics";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableRunsClustering()
	 */
	@Override
	protected String getTableRunsClustering() {
		return "run_clusterings";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableRunsExecution()
	 */
	@Override
	protected String getTableRunsExecution() {
		return "run_executions";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableRunsExecutionDataConfigs()
	 */
	@Override
	protected String getTableRunsExecutionDataConfigs() {
		return "run_execution_data_configs";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableRunsExecutionParameterValues()
	 */
	@Override
	protected String getTableRunsExecutionParameterValues() {
		return "run_execution_parameter_values";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableRunsExecutionProgramConfigs()
	 */
	@Override
	protected String getTableRunsExecutionProgramConfigs() {
		return "run_execution_program_configs";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableRunsExecutionQualityMeasures()
	 */
	@Override
	protected String getTableRunsExecutionQualityMeasures() {
		return "run_execution_quality_measures";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableRunsInternalParameterOptimization()
	 */
	@Override
	protected String getTableRunsInternalParameterOptimization() {
		return "run_internal_parameter_optimizations";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableRunsParameterOptimization()
	 */
	@Override
	protected String getTableRunsParameterOptimization() {
		return "run_parameter_optimizations";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableRunsParameterOptimizationMethods()
	 */
	@Override
	protected String getTableRunsParameterOptimizationMethods() {
		return "run_parameter_optimization_methods";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see framework.repository.SQLCommunicator#
	 * getTableRunsParameterOptimizationQualityMeasures()
	 */
	@Override
	protected String getTableRunsParameterOptimizationQualityMeasures() {
		return "run_parameter_optimization_quality_measures";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableRunsParameterOptimizationParameters()
	 */
	@Override
	protected String getTableRunsParameterOptimizationParameters() {
		return "run_parameter_optimization_parameters";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableRunResultExecution()
	 */
	@Override
	protected String getTableRunResultsExecution() {
		return "run_results_executions";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableRunResultFormats()
	 */
	@Override
	protected String getTableRunResultFormats() {
		return "run_result_formats";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableRunResultsClustering()
	 */
	@Override
	protected String getTableRunResultsClustering() {
		return "run_results_clusterings";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableRunResultParameterOptimization()
	 */
	@Override
	protected String getTableRunResultsParameterOptimization() {
		return "run_results_parameter_optimizations";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see framework.repository.SQLCommunicator#getTableParameterSets()
	 */
	@Override
	protected String getTableParameterSets() {
		return "run_results_parameter_optimizations_parameter_sets";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * framework.repository.SQLCommunicator#getTableParameterSetParameters()
	 */
	@Override
	protected String getTableParameterSetParameters() {
		return "run_results_parameter_optimizations_parameter_set_parameters";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * framework.repository.SQLCommunicator#getTableParameterSetIterations()
	 */
	@Override
	protected String getTableParameterSetIterations() {
		return "run_results_parameter_optimizations_parameter_set_iterations";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * framework.repository.SQLCommunicator#getTableParameterSetParameterValues
	 * ()
	 */
	@Override
	protected String getTableParameterSetParameterValues() {
		return "run_results_parameter_optimizations_parameter_values";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * framework.repository.SQLCommunicator#getTableParameterOptimizationQualities
	 * ()
	 */
	@Override
	protected String getTableParameterOptimizationQualities() {
		return "run_results_parameter_optimizations_qualities";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableRunResults()
	 */
	@Override
	protected String getTableRunResults() {
		return "run_results";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableRunResultsDataAnalysis()
	 */
	@Override
	protected String getTableRunResultsDataAnalysis() {
		return "run_results_data_analyses";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableRunResultsRunAnalysis()
	 */
	@Override
	protected String getTableRunResultsRunAnalysis() {
		return "run_results_run_analyses";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableRunResultsRunDataAnalysis()
	 */
	@Override
	protected String getTableRunResultsRunDataAnalysis() {
		return "run_results_run_data_analyses";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableRuns()
	 */
	@Override
	protected String getTableRuns() {
		return "runs";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableRunTypes()
	 */
	@Override
	protected String getTableRunTypes() {
		return "run_types";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableStatistics()
	 */
	@Override
	protected String getTableStatistics() {
		return "statistics";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableStatisticsData()
	 */
	@Override
	protected String getTableStatisticsData() {
		return "statistics_data";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableStatisticsRun()
	 */
	@Override
	protected String getTableStatisticsRun() {
		return "statistics_runs";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableStatisticsRunData()
	 */
	@Override
	protected String getTableStatisticsRunData() {
		return "statistics_run_data";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#registerRunResultFormat(java.lang.Class)
	 */
	@Override
	protected boolean registerRunResultFormatClass(
			final Class<? extends RunResultFormat> object) {

		try {
			insert(this.getTableRunResultFormats(),
					new String[]{"repository_id", "name"},
					new String[]{"" + this.updateRepositoryId(),
							"" + object.getSimpleName()});
			return true;
		} catch (SQLException e) {
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#register(run.config.Run)
	 */
	@Override
	protected boolean register(Run object, final boolean updateOnly) {
		try {
			String type;
			if (object instanceof AnalysisRun) {
				if (object instanceof DataAnalysisRun)
					type = "Data Analysis";
				else if (object instanceof RunDataAnalysisRun)
					type = "Run-Data Analysis";
				else
					type = "Run Analysis";
			} else {
				if (object instanceof ClusteringRun)
					type = "Clustering";
				else if (object instanceof ParameterOptimizationRun)
					type = "Parameter Optimization";
				else
					type = "Internal Parameter Optimization";
			}

			int run_type_id = this.getRunTypeId(type);

			try {
				String[] columns;
				String[] values;
				if (object.getRepository() instanceof RunResultRepository) {
					columns = new String[]{"repository_id", "absPath", "name",
							"run_type_id", "run_id", "status"};
					values = new String[]{
							this.updateRepositoryId() + "",
							object.getAbsolutePath(),
							new File(object.getAbsolutePath()).getName()
									.replace(".run", ""),
							"" + run_type_id,
							""
									+ getRunId(object.getRepository()
											.getParent()
											.getRunWithName(object.getName())),
							"" + object.getStatus()};
				} else {
					columns = new String[]{"repository_id", "absPath", "name",
							"run_type_id", "status"};
					values = new String[]{
							this.updateRepositoryId() + "",
							object.getAbsolutePath(),
							new File(object.getAbsolutePath()).getName()
									.replace(".run", ""), "" + run_type_id,
							"" + object.getStatus()};
				}
				if (updateOnly) {
					// TODO
					/*
					 * in case of an update to this run config in the
					 * repository, we need to delete all references to this
					 * object "upstream", that means subtypes of this run in the
					 * database
					 */
					int rowId = getRunId(object);

					delete(getTableRunsExecution(), rowId);
					delete(getTableRunsAnalysis(), rowId);

					update(this.getTableRuns(), columns, values, rowId);
				} else
					insert(this.getTableRuns(), columns, values);
			} catch (SQLException e) {

				e.printStackTrace();
			}
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see framework.repository.SQLCommunicator#register(run.config.AnalysisRun
	 * )
	 */
	@Override
	protected boolean register(AnalysisRun<Statistic> object,
			final boolean updateOnly) {
		try {
			this.register((Run) object, updateOnly);
			int run_id = getRunId(object);

			int run_analysis_id = insert(this.getTableRunsAnalysis(),
					new String[]{"repository_id", "run_id"}, new String[]{
							"" + this.updateRepositoryId(), "" + run_id});

			for (Statistic st : object.getStatistics()) {
				int statistic_id = getStatisticId(st.getIdentifier());
				insert(this.getTableRunsAnalysisStatistics(), new String[]{
						"repository_id", "run_analysis_id", "statistic_id"},
						new String[]{"" + this.updateRepositoryId(),
								"" + run_analysis_id, "" + statistic_id});
			}

			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see framework.repository.SQLCommunicator#register(run.config.
	 * DataAnalysisRun)
	 */
	@Override
	protected boolean register(DataAnalysisRun object, final boolean updateOnly) {
		try {
			this.register((AnalysisRun) object, updateOnly);
			int run_id = getRunId(object);
			int run_analysis_id = getRunAnalysisId(run_id);

			int run_data_analysis_id = insert(this.getTableRunsAnalysisData(),
					new String[]{"repository_id", "run_analysis_id"},
					new String[]{"" + this.updateRepositoryId(),
							"" + run_analysis_id});

			DataAnalysisRun dataAnalysis = object;

			for (DataConfig dataConfig : dataAnalysis.getDataConfigs()) {
				insert(this.getTableRunsAnalysisDataDataIdentifiers(),
						new String[]{"repository_id", "run_data_analysis_id",
								"data_config_id"},
						new String[]{"" + this.updateRepositoryId(),
								"" + run_data_analysis_id,
								"" + this.getDataConfigId(dataConfig)});
			}
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * framework.repository.SQLCommunicator#register(run.config.RunAnalysisRun )
	 */
	@Override
	protected boolean register(RunAnalysisRun object, final boolean updateOnly) {
		try {
			this.register((AnalysisRun) object, updateOnly);

			int run_id = getRunId(object);
			int run_analysis_id = getRunAnalysisId(run_id);

			int run_run_analysis_id = insert(this.getTableRunsAnalysisRun(),
					new String[]{"repository_id", "run_analysis_id"},
					new String[]{"" + this.updateRepositoryId(),
							"" + run_analysis_id});

			RunAnalysisRun runAnalysis = object;

			for (String uniqueRunIdentifier : runAnalysis
					.getUniqueRunAnalysisRunIdentifiers()) {
				insert(this.getTableRunsAnalysisRunRunIdentifiers(),
						new String[]{"repository_id", "run_run_analysis_id",
								"runIdentifier"},
						new String[]{"" + this.updateRepositoryId(),
								"" + run_run_analysis_id,
								"" + uniqueRunIdentifier});
			}
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see framework.repository.SQLCommunicator#register(run.config.
	 * RunDataAnalysisRun)
	 */
	@Override
	protected boolean register(RunDataAnalysisRun object,
			final boolean updateOnly) {
		try {
			this.register((AnalysisRun) object, updateOnly);
			int run_id = getRunId(object);
			int run_analysis_id = getRunAnalysisId(run_id);
			// int run_run_analysis_id = getRunAnalysisRunId(run_analysis_id);

			int run_run_data_analysis_id = insert(
					this.getTableRunsAnalysisRunData(), new String[]{
							"repository_id", "run_analysis_id"}, new String[]{
							"" + this.updateRepositoryId(),
							"" + run_analysis_id});

			RunDataAnalysisRun runDataAnalysis = object;

			for (String uniqueDataIdentifier : runDataAnalysis
					.getUniqueDataAnalysisRunIdentifiers()) {
				insert(this.getTableRunsAnalysisRunDataDataIdentifiers(),
						new String[]{"repository_id",
								"run_run_data_analysis_id", "dataIdentifier"},
						new String[]{"" + this.updateRepositoryId(),
								"" + run_run_data_analysis_id,
								"" + uniqueDataIdentifier});
			}

			for (String uniqueRunIdentifier : runDataAnalysis
					.getUniqueRunAnalysisRunIdentifiers()) {
				insert(this.getTableRunsAnalysisRunDataRunIdentifiers(),
						new String[]{"repository_id",
								"run_run_data_analysis_id", "runIdentifier"},
						new String[]{"" + this.updateRepositoryId(),
								"" + run_run_data_analysis_id,
								"" + uniqueRunIdentifier});
			}
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * framework.repository.SQLCommunicator#register(run.config.ExecutionRun )
	 */
	@Override
	protected boolean register(ExecutionRun object, final boolean updateOnly) {
		try {
			this.register((Run) object, updateOnly);
			int run_id = getRunId(object);

			int run_execution_id;
			try {
				run_execution_id = insert(this.getTableRunsExecution(),
						new String[]{"repository_id", "run_id"}, new String[]{
								"" + this.updateRepositoryId(), "" + run_id});
			} catch (SQLException e) {
				run_execution_id = getRunExecutionId(run_id);
			}

			ExecutionRun execRun = object;

			// insert dataConfigs into DB
			for (DataConfig dataConfig : execRun.getDataConfigs()) {
				int data_config_id = this.getDataConfigId(dataConfig);

				try {
					insert(this.getTableRunsExecutionDataConfigs(),
							new String[]{"repository_id", "run_execution_id",
									"data_config_id"},
							new String[]{"" + this.updateRepositoryId(),
									"" + run_execution_id, "" + data_config_id});
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

			for (int i = 0; i < execRun.getQualityMeasures().size(); i++) {
				int clustering_quality_measure_id = this
						.getClusteringQualityMeasureId(execRun
								.getQualityMeasures().get(i).getClass()
								.getSimpleName());

				try {
					insert(this.getTableRunsExecutionQualityMeasures(),
							new String[]{"repository_id", "run_execution_id",
									"clustering_quality_measure_id"},
							new String[]{"" + this.updateRepositoryId(),
									"" + run_execution_id,
									"" + clustering_quality_measure_id});
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

			// insert programConfigs and parameter values into DB
			for (int i = 0; i < execRun.getProgramConfigs().size(); i++) {
				ProgramConfig programConfig = execRun.getProgramConfigs()
						.get(i);

				int program_config_id = this.getProgramConfigId(programConfig);
				try {
					insert(this.getTableRunsExecutionProgramConfigs(),
							new String[]{"repository_id", "run_execution_id",
									"program_config_id"}, new String[]{
									"" + this.updateRepositoryId(),
									"" + run_execution_id,
									"" + program_config_id});
				} catch (SQLException e) {
					e.printStackTrace();
				}

				Map<ProgramParameter<?>, String> paramValues = execRun
						.getParameterValues().get(i);

				for (ProgramParameter<?> param : paramValues.keySet()) {

					int paramId = this.getProgramParameterId(param);

					try {
						insert(this.getTableRunsExecutionParameterValues(),
								new String[]{"repository_id",
										"run_execution_id",
										"program_config_id",
										"program_parameter_id", "value"},
								new String[]{"" + this.updateRepositoryId(),
										"" + run_execution_id,
										"" + program_config_id, "" + paramId,
										"" + paramValues.get(param)});
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * framework.repository.SQLCommunicator#register(run.config.ClusteringRun )
	 */
	@Override
	protected boolean register(ClusteringRun run, final boolean updateOnly) {
		try {
			this.register((ExecutionRun) run, updateOnly);
			int run_id = getRunId(run);
			int run_execution_id = getRunExecutionId(run_id);
			insert(this.getTableRunsClustering(), new String[]{"repository_id",
					"run_execution_id"},
					new String[]{"" + this.updateRepositoryId(),
							"" + run_execution_id});
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see framework.repository.SQLCommunicator#register(run.config.
	 * InternalParameterOptimizationRun)
	 */
	@Override
	protected boolean register(InternalParameterOptimizationRun run,
			final boolean updateOnly) {
		try {
			this.register((ExecutionRun) run, updateOnly);
			int run_id = getRunId(run);
			int run_execution_id = getRunExecutionId(run_id);
			insert(this.getTableRunsInternalParameterOptimization(),
					new String[]{"repository_id", "run_execution_id"},
					new String[]{"" + this.updateRepositoryId(),
							"" + run_execution_id});
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see framework.repository.SQLCommunicator#register(run.config.
	 * ParameterOptimizationRun)
	 */
	@Override
	protected boolean register(ParameterOptimizationRun object,
			final boolean updateOnly) {
		try {
			this.register((ExecutionRun) object, updateOnly);
			int run_id = getRunId(object);
			int run_execution_id = getRunExecutionId(run_id);

			int runParamOptId = insert(
					this.getTableRunsParameterOptimization(), new String[]{
							"repository_id", "run_execution_id"}, new String[]{
							"" + this.updateRepositoryId(),
							"" + run_execution_id});

			ParameterOptimizationRun paramOpt = object;

			for (int i = 0; i < paramOpt.getProgramConfigs().size(); i++) {
				int program_config_id = this.getProgramConfigId(paramOpt
						.getProgramConfigs().get(i));

				ParameterOptimizationMethod method = paramOpt
						.getOptimizationMethods().get(i);

				int methodId = this.getParameterOptimizationMethodId(method
						.getClass().getSimpleName());

				int clustering_quality_measure_id = this
						.getClusteringQualityMeasureId(method
								.getOptimizationCriterion().getClass()
								.getSimpleName());

				insert(this.getTableRunsParameterOptimizationMethods(),
						new String[]{"repository_id",
								"run_parameter_optimization_id",
								"program_config_id",
								"parameter_optimization_method_id",
								"clustering_quality_measure_id"}, new String[]{
								"" + this.updateRepositoryId(),
								"" + runParamOptId, "" + program_config_id,
								"" + methodId,
								"" + clustering_quality_measure_id});

				for (ProgramParameter<?> param : paramOpt
						.getOptimizationParameters().get(i)) {

					int paramId = this.getProgramParameterId(param);

					insert(this.getTableRunsParameterOptimizationParameters(),
							new String[]{"repository_id",
									"run_parameter_optimization_id",
									"program_config_id", "program_parameter_id"},
							new String[]{"" + this.updateRepositoryId(),
									"" + runParamOptId, "" + program_config_id,
									"" + paramId});
				}
			}

			// insert clustering quality measures into DB
			for (QualityMeasure measure : object.getQualityMeasures()) {

				int measureId = getClusteringQualityMeasureId(measure
						.toString());

				insert(this.getTableRunsParameterOptimizationQualityMeasures(),
						new String[]{"repository_id",
								"run_parameter_optimization_id",
								"clustering_quality_measure_id"}, new String[]{
								"" + this.updateRepositoryId(),
								"" + runParamOptId, "" + measureId});
			}

			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	// /*
	// * (non-Javadoc)
	// *
	// * @see utils.SQLCommunicator#register(run.Run)
	// */
	// @Override
	// protected boolean register(Run object, final boolean updateOnly) {
	// try {
	// int run_id = this.getRunId(object.getRun());
	//
	// String type;
	// if (object instanceof AnalysisRun) {
	// if (object instanceof DataAnalysisRun)
	// type = "Data Analysis";
	// else if (object instanceof RunDataAnalysisRun)
	// type = "Run-Data Analysis";
	// else
	// type = "Run Analysis";
	// } else {
	// if (object instanceof ClusteringRun)
	// type = "Clustering";
	// else if (object instanceof ParameterOptimizationRun)
	// type = "Parameter Optimization";
	// else
	// type = "Internal Parameter Optimization";
	// }
	//
	// int run_type_id = this.getRunTypeId(type);
	//
	// String[] columns;
	// String[] values;
	// if (object.getRepository() instanceof RunResultRepository) {
	// columns = new String[]{"repository_id", "run_id",
	// "run_type_id", "absPath", "name", "status", "runOrigId"};
	// values = new String[]{
	// "" + this.repositoryId,
	// "" + run_id,
	// "" + run_type_id,
	// object.getAbsolutePath(),
	// new File(object.getAbsolutePath()).getName().replace(
	// ".run", ""),
	// "" + object.getStatus(),
	// ""
	// + getRunId(object
	// .getRepository()
	// .getParent()
	// .getRunWithName(
	// object.getAbsolutePath()
	// .substring(
	// object.getAbsolutePath()
	// .lastIndexOf(
	// "/") + 1)))};
	// } else {
	// columns = new String[]{"repository_id", "run_id",
	// "run_type_id", "absPath", "name", "status"};
	// values = new String[]{
	// "" + this.repositoryId,
	// "" + run_id,
	// "" + run_type_id,
	// object.getAbsolutePath(),
	// new File(object.getAbsolutePath()).getName().replace(
	// ".run", ""), "" + object.getStatus()};
	// }
	// if (updateOnly) {
	// int rowId = getRunId(object);
	// update(this.getTableRuns(), columns, values, rowId);
	// } else
	// insert(this.getTableRuns(), columns, values);
	// return true;
	// } catch (SQLException e) {
	//
	// e.printStackTrace();
	// }
	// return false;
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#register(program.ProgramConfig)
	 */
	@Override
	protected boolean register(ProgramConfig object, final boolean updateOnly) {
		// TODO
		try {
			int program_id = getProgramId(object.getProgram().getAbsolutePath());

			int run_result_format_id = getRunResultFormatId(object
					.getOutputFormat().getClass().getSimpleName());

			String[] columns;
			String[] values;
			if (object.getRepository() instanceof RunResultRepository) {
				columns = new String[]{
						"repository_id",
						"program_id",
						"run_result_format_id",
						"name",
						"absPath",
						"invocationFormat",
						"invocationFormatWithoutGoldStandard",
						"invocationFormatParameterOptimization",
						"invocationFormatParameterOptimizationWithoutGoldStandard",
						"program_config_id"};
				values = new String[]{
						"" + this.updateRepositoryId(),
						"" + program_id,
						"" + run_result_format_id,
						"" + object.getName(),
						"" + replaceNull(object.getAbsolutePath(), ""),
						"" + replaceNull(object.getInvocationFormat(false), ""),
						"" + replaceNull(object.getInvocationFormat(true), ""),
						""
								+ replaceNull(
										object.getInvocationFormatParameterOptimization(false),
										""),
						""
								+ replaceNull(
										object.getInvocationFormatParameterOptimization(true),
										""),
						""
								+ getProgramConfigId(object
										.getRepository()
										.getParent()
										.getProgramConfigWithName(
												object.getAbsolutePath()
														.substring(
																object.getAbsolutePath()
																		.lastIndexOf(
																				"/") + 1)))};
			} else {
				columns = new String[]{"repository_id", "program_id",
						"run_result_format_id", "name", "absPath",
						"invocationFormat",
						"invocationFormatWithoutGoldStandard",
						"invocationFormatParameterOptimization",
						"invocationFormatParameterOptimizationWithoutGoldStandard"};
				values = new String[]{
						"" + this.updateRepositoryId(),
						"" + program_id,
						"" + run_result_format_id,
						"" + object.getName(),
						"" + replaceNull(object.getAbsolutePath(), ""),
						"" + replaceNull(object.getInvocationFormat(false), ""),
						"" + replaceNull(object.getInvocationFormat(true), ""),
						""
								+ replaceNull(
										object.getInvocationFormatParameterOptimization(false),
										""),
						""
								+ replaceNull(
										object.getInvocationFormatParameterOptimization(true),
										"")};
			}

			int rowId;
			if (updateOnly) {
				rowId = getProgramConfigId(object);

				delete(this.getTableOptimizableProgramParameters(), rowId,
						"program_config_id");

				delete(this.getTableProgramConfigsCompatibleDataSetFormats(),
						rowId, "program_config_id");

				update(this.getTableProgramConfigs(), columns, values, rowId);
			} else
				rowId = insert(this.getTableProgramConfigs(), columns, values);

			// TODO
//			for (DataSetFormat dsFormat : object.getCompatibleDataSetFormats()) {
//				int dataset_format_id = getDataSetFormatId(dsFormat.getClass()
//						.getSimpleName());
//
//				insert(this.getTableProgramConfigsCompatibleDataSetFormats(),
//						new String[]{"repository_id", "program_config_id",
//								"dataset_format_id"}, new String[]{
//								"" + updateRepositoryId(), "" + rowId,
//								"" + dataset_format_id});
//			}

			for (ProgramParameter<?> param : object.getOptimizableParams()) {
				int program_config_id = getProgramConfigId(object);
				int program_parameter_id = getProgramParameterId(param);

				insert(this.getTableOptimizableProgramParameters(),
						new String[]{"repository_id", "program_config_id",
								"program_parameter_id"},
						new String[]{"" + this.updateRepositoryId(),
								"" + program_config_id,
								"" + program_parameter_id});
			}
			return true;
		} catch (SQLException e) {

			e.printStackTrace();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getRunId(java.lang.String)
	 */
	@Override
	protected int getRunId(final Run run) throws SQLException {
		Statement stmt = conn.createStatement();
		try {
			ResultSet rs = stmt.executeQuery("SELECT `id` FROM `"
					+ this.getDatabase() + "`.`" + this.getTableRuns()
					+ "` WHERE `repository_id`='"
					+ getRepositoryId(run.getRepository().getBasePath())
					+ "' AND `absPath`='" + run.getAbsolutePath() + "';");
			rs.first();
			return rs.getInt("id");
		} finally {
			stmt.close();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * framework.repository.SQLCommunicator#getClusteringId(java.lang.String)
	 */
	@Override
	protected int getClusteringId(String absPath) throws SQLException {
		Statement stmt = conn.createStatement();
		try {
			ResultSet rs = stmt.executeQuery("SELECT `id` FROM `"
					+ this.getDatabase() + "`.`" + this.getTableClusterings()
					+ "` WHERE `absPath`='" + absPath + "';");
			rs.first();
			return rs.getInt("id");
		} finally {
			stmt.close();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see framework.repository.SQLCommunicator#getClusterId(int,
	 * java.lang.String)
	 */
	@Override
	protected int getClusterId(int clusteringId, String name)
			throws SQLException {
		Statement stmt = conn.createStatement();
		try {
			ResultSet rs = stmt.executeQuery("SELECT `id` FROM `"
					+ this.getDatabase() + "`.`" + this.getTableClusters()
					+ "` WHERE `repository_id`='" + this.updateRepositoryId()
					+ "' AND `clustering_id`='" + clusteringId
					+ "' AND `name`='" + name + "';");
			rs.first();
			return rs.getInt("id");
		} finally {
			stmt.close();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see framework.repository.SQLCommunicator#getClusterObjectId(int,
	 * java.lang.String)
	 */
	@Override
	protected int getClusterObjectId(int clusterId, String name)
			throws SQLException {
		Statement stmt = conn.createStatement();
		try {
			ResultSet rs = stmt.executeQuery("SELECT `id` FROM `"
					+ this.getDatabase() + "`.`"
					+ this.getTableClusterObjects()
					+ "` WHERE `repository_id`='" + this.updateRepositoryId()
					+ "' AND `cluster_id`='" + clusterId + "' AND `name`='"
					+ name + "';");
			rs.first();
			return rs.getInt("id");
		} finally {
			stmt.close();
		}
	}

	@Override
	protected int getClusteringQualityMeasureId(final String name)
			throws SQLException {
		Statement stmt = conn.createStatement();
		try {
			ResultSet rs = stmt.executeQuery("SELECT `id` FROM `"
					+ this.getDatabase() + "`.`"
					+ this.getTableClusteringQualityMeasures()
					+ "` WHERE `repository_id`='" + this.updateRepositoryId()
					+ "' AND `name`='" + name + "';");
			rs.first();
			return rs.getInt("id");
		} catch (SQLException e) {
			e.printStackTrace();
			throw e;
		} finally {
			stmt.close();
		}
	}

	@Override
	protected int getDataConfigId(final DataConfig dataConfig)
			throws SQLException {
		Statement stmt = conn.createStatement();
		try {
			ResultSet rs = stmt
					.executeQuery("SELECT `id` FROM `"
							+ this.getDatabase()
							+ "`.`"
							+ this.getTableDataConfigs()
							+ "` WHERE `repository_id`='"
							+ getRepositoryId(dataConfig.getRepository()
									.getBasePath()) + "' AND `absPath`='"
							+ dataConfig.getAbsolutePath() + "';");
			rs.first();
			return rs.getInt("id");
		} catch (SQLException e) {
			System.out.println("bla");
			throw e;
		} finally {
			stmt.close();
		}
	}

	@Override
	protected int getDataSetConfigId(final DataSetConfig dataSetConfig)
			throws SQLException {
		Statement stmt = conn.createStatement();
		try {
			ResultSet rs = stmt.executeQuery("SELECT `id` FROM `"
					+ this.getDatabase() + "`.`"
					+ this.getTableDataSetConfigs() + "` WHERE `absPath`='"
					+ dataSetConfig.getAbsolutePath() + "';");
			rs.first();
			return rs.getInt("id");
		} finally {
			stmt.close();
		}
	}

	@Override
	protected int getDataSetTypeId(final String dataSetTypeClassSimpleName)
			throws SQLException {
		Statement stmt = conn.createStatement();
		try {
			ResultSet rs = stmt.executeQuery("SELECT `id` FROM `"
					+ this.getDatabase() + "`.`" + this.getTableDataSetTypes()
					+ "` WHERE `repository_id`='" + this.updateRepositoryId()
					+ "' AND `name`='" + dataSetTypeClassSimpleName + "';");
			rs.first();
			return rs.getInt("id");
		} catch (SQLException e) {
			throw e;
		} finally {
			stmt.close();
		}
	}

	@Override
	protected int getDataSetId(final DataSet dataSet) throws SQLException {
		Statement stmt = conn.createStatement();
		try {
			ResultSet rs = stmt.executeQuery("SELECT `id` FROM `"
					+ this.getDatabase() + "`.`" + this.getTableDatasets()
					+ "` WHERE `absPath`='" + dataSet.getAbsolutePath() + "';");
			rs.first();
			return rs.getInt("id");
		} finally {
			stmt.close();
		}
	}

	@Override
	protected int getRepositoryTypeId(final String repositoryType)
			throws SQLException {
		Statement stmt = conn.createStatement();
		try {
			ResultSet rs = stmt.executeQuery("SELECT `id` FROM `"
					+ this.getDatabase() + "`.`"
					+ this.getTableRepositoryTypes() + "` WHERE `name`='"
					+ repositoryType + "';");
			rs.first();
			return rs.getInt("id");
		} finally {
			stmt.close();
		}
	}

	@Override
	protected int getGoldStandardConfigId(
			final GoldStandardConfig goldStandardConfig) throws SQLException {
		Statement stmt = conn.createStatement();
		try {
			ResultSet rs = stmt.executeQuery("SELECT `id` FROM `"
					+ this.getDatabase() + "`.`"
					+ this.getTableGoldStandardConfigs()
					+ "` WHERE `absPath`='"
					+ goldStandardConfig.getAbsolutePath() + "';");
			rs.first();
			return rs.getInt("id");
		} finally {
			stmt.close();
		}
	}

	@Override
	protected int getGoldStandardId(final GoldStandard goldStandard)
			throws SQLException {
		Statement stmt = conn.createStatement();
		try {
			ResultSet rs = stmt.executeQuery("SELECT `id` FROM `"
					+ this.getDatabase() + "`.`" + this.getTableGoldStandards()
					+ "` WHERE `absPath`='" + goldStandard.getAbsolutePath()
					+ "';");
			rs.first();
			return rs.getInt("id");
		} finally {
			stmt.close();
		}
	}

	@Override
	protected int getParameterOptimizationMethodId(final String name)
			throws SQLException {
		Statement stmt = conn.createStatement();
		try {
			ResultSet rs = stmt.executeQuery("SELECT `id` FROM `"
					+ this.getDatabase() + "`.`"
					+ this.getTableParameterOptimizationMethods()
					+ "` WHERE `repository_id`='" + this.updateRepositoryId()
					+ "' AND `name`='" + name + "';");
			rs.first();
			return rs.getInt("id");
		} finally {
			stmt.close();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * framework.repository.SQLCommunicator#getParameterSetId(java.lang.String)
	 */
	@Override
	protected int getParameterSetId(final int runResultParamOptId)
			throws SQLException {
		Statement stmt = conn.createStatement();
		try {
			ResultSet rs = stmt.executeQuery("SELECT `id` FROM `"
					+ this.getDatabase() + "`.`" + this.getTableParameterSets()
					+ "` WHERE `repository_id`='" + this.updateRepositoryId()
					+ "' AND `run_results_parameter_optimization_id`='"
					+ runResultParamOptId + "';");
			rs.first();
			return rs.getInt("id");
		} finally {
			stmt.close();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see framework.repository.SQLCommunicator#getParameterSetParameterId(int,
	 * int)
	 */
	@Override
	protected int getParameterSetParameterId(
			int run_results_parameter_optimizations_parameter_set_id,
			int program_parameter_id) throws SQLException {
		Statement stmt = conn.createStatement();
		try {
			ResultSet rs = stmt
					.executeQuery("SELECT `id` FROM `"
							+ this.getDatabase()
							+ "`.`"
							+ this.getTableParameterSetParameters()
							+ "` WHERE `run_results_parameter_optimizations_parameter_set_id`='"
							+ run_results_parameter_optimizations_parameter_set_id
							+ "' AND `program_parameter_id`='"
							+ program_parameter_id + "';");
			rs.first();
			return rs.getInt("id");
		} finally {
			stmt.close();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * framework.repository.SQLCommunicator#getParameterSetId(java.lang.String)
	 */
	@Override
	protected int getParameterSetParameterValuesId(
			final int run_results_parameter_optimizations_parameter_set_id,
			final int program_parameter_id, final int iteration)
			throws SQLException {
		Statement stmt = conn.createStatement();
		try {
			ResultSet rs = stmt
					.executeQuery("SELECT `id` FROM `"
							+ this.getDatabase()
							+ "`.`"
							+ this.getTableParameterSetParameterValues()
							+ "` WHERE `repository_id`='"
							+ this.updateRepositoryId()
							+ "' AND `run_results_parameter_optimizations_parameter_set_id`='"
							+ run_results_parameter_optimizations_parameter_set_id
							+ "' AND `program_parameter_id`='"
							+ program_parameter_id + "' AND `iteration`='"
							+ iteration + "';");
			rs.first();
			return rs.getInt("id");
		} finally {
			stmt.close();
		}
	}

	@Override
	protected int getProgramConfigId(final ProgramConfig programConfig)
			throws SQLException {
		Statement stmt = conn.createStatement();
		try {
			ResultSet rs = stmt.executeQuery("SELECT `id` FROM `"
					+ this.getDatabase()
					+ "`.`"
					+ this.getTableProgramConfigs()
					+ "` WHERE `repository_id`='"
					+ getRepositoryId(programConfig.getRepository()
							.getBasePath()) + "' AND  `absPath`='"
					+ programConfig.getAbsolutePath() + "';");
			rs.first();
			return rs.getInt("id");
		} finally {
			stmt.close();
		}
	}

	@Override
	protected int getProgramId(final String absPath) throws SQLException {
		Statement stmt = conn.createStatement();
		try {
			ResultSet rs = stmt.executeQuery("SELECT `id` FROM `"
					+ this.getDatabase() + "`.`" + this.getTablePrograms()
					+ "` WHERE `absPath`='" + absPath + "';");
			rs.first();
			return rs.getInt("id");
		} finally {
			stmt.close();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getRunTypeId(java.lang.String)
	 */
	@Override
	protected int getRunTypeId(String name) throws SQLException {
		Statement stmt = conn.createStatement();
		try {
			ResultSet rs = stmt.executeQuery("SELECT `id` FROM `"
					+ this.getDatabase() + "`.`" + this.getTableRunTypes()
					+ "` WHERE `name`='" + name + "';");
			rs.first();
			return rs.getInt("id");
		} finally {
			stmt.close();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getRepositoryId(java.lang.String)
	 */
	@Override
	protected int getRepositoryId(String absPath) throws SQLException {
		Statement stmt = conn.createStatement();
		try {
			ResultSet rs = stmt.executeQuery("SELECT `id` FROM `"
					+ this.getDatabase() + "`.`" + this.getTableRepositories()
					+ "` WHERE `basePath`='" + absPath + "';");
			rs.first();
			return rs.getInt("id");
		} finally {
			stmt.close();
		}
	}

	@Override
	protected int getProgramParameterId(
			final ProgramParameter<?> programParameter) throws SQLException {
		Statement stmt = conn.createStatement();
		try {

			int program_config_id = getProgramConfigId(programParameter
					.getProgramConfig());

			ResultSet rs = stmt.executeQuery("SELECT `id` FROM `"
					+ this.getDatabase()
					+ "`.`"
					+ this.getTableProgramParameter()
					+ "` WHERE `program_config_id`='"
					+ program_config_id
					+ "' AND `name`='"
					+ programParameter.getName()
					+ "' AND `repository_id`='"
					+ getRepositoryId(programParameter.getRepository()
							.getBasePath()) + "';");
			rs.first();
			return rs.getInt("id");
		} catch (SQLException e) {
			throw e;
		} finally {
			stmt.close();
		}
	}

	@Override
	protected int getRunAnalysisDataId(final int run_analysis_id)
			throws SQLException {
		Statement stmt = conn.createStatement();
		try {
			ResultSet rs = stmt.executeQuery("SELECT `id` FROM `"
					+ this.getDatabase() + "`.`"
					+ this.getTableRunsAnalysisData()
					+ "` WHERE `repository_id`='" + this.updateRepositoryId()
					+ "' AND `run_analysis_id`='" + run_analysis_id + "';");
			rs.first();
			return rs.getInt("id");
		} finally {
			stmt.close();
		}
	}

	@Override
	protected int getRunAnalysisId(final int run_id) throws SQLException {
		Statement stmt = conn.createStatement();
		try {
			ResultSet rs = stmt.executeQuery("SELECT `id` FROM `"
					+ this.getDatabase() + "`.`" + this.getTableRunsAnalysis()
					+ "` WHERE `run_id`='" + run_id + "';");
			rs.first();
			return rs.getInt("id");
		} finally {
			stmt.close();
		}
	}

	@Override
	protected int getRunAnalysisRunId(final int run_analysis_id)
			throws SQLException {
		Statement stmt = conn.createStatement();
		try {
			ResultSet rs = stmt.executeQuery("SELECT `id` FROM `"
					+ this.getDatabase() + "`.`"
					+ this.getTableRunsAnalysisRun()
					+ "` WHERE `run_analysis_id`='" + run_analysis_id + "';");
			rs.first();
			return rs.getInt("id");
		} finally {
			stmt.close();
		}
	}

	@Override
	protected int getRunAnalysisRunDataId(final int run_run_analysis_id)
			throws SQLException {
		Statement stmt = conn.createStatement();
		try {
			ResultSet rs = stmt.executeQuery("SELECT `id` FROM `"
					+ this.getDatabase() + "`.`"
					+ this.getTableRunsAnalysisRunData()
					+ "` WHERE `run_run_analysis_id`='" + run_run_analysis_id
					+ "';");
			rs.first();
			return rs.getInt("id");
		} finally {
			stmt.close();
		}
	}

	@Override
	protected int getRunExecutionId(final int run_id) throws SQLException {
		Statement stmt = conn.createStatement();
		try {
			ResultSet rs = stmt.executeQuery("SELECT `id` FROM `"
					+ this.getDatabase() + "`.`" + this.getTableRunsExecution()
					+ "` WHERE `repository_id`='" + this.updateRepositoryId()
					+ "' AND `run_id`='" + run_id + "';");
			rs.first();
			return rs.getInt("id");
		} finally {
			stmt.close();
		}
	}

	@Override
	protected int getRunParameterOptimizationId(final int run_execution_id)
			throws SQLException {
		Statement stmt = conn.createStatement();
		try {
			ResultSet rs = stmt.executeQuery("SELECT `id` FROM `"
					+ this.getDatabase() + "`.`"
					+ this.getTableRunsParameterOptimization()
					+ "` WHERE `repository_id`='" + this.updateRepositoryId()
					+ "' AND `run_execution_id`='" + run_execution_id + "';");
			rs.first();
			return rs.getInt("id");
		} finally {
			stmt.close();
		}
	}

	@Override
	protected int getRunResultExecutionId(final int run_results_id)
			throws SQLException {
		Statement stmt = conn.createStatement();
		try {
			ResultSet rs = stmt.executeQuery("SELECT `id` FROM `"
					+ this.getDatabase() + "`.`"
					+ this.getTableRunResultsExecution()
					+ "` WHERE `repository_id`='" + this.updateRepositoryId()
					+ "' AND `run_result_id`='" + run_results_id + "';");
			rs.first();
			return rs.getInt("id");
		} finally {
			stmt.close();
		}
	}

	@Override
	protected int getRunResultParameterOptimizationId(final String absPath)
			throws SQLException {
		Statement stmt = conn.createStatement();
		try {
			ResultSet rs = stmt.executeQuery("SELECT `id` FROM `"
					+ this.getDatabase() + "`.`"
					+ this.getTableRunResultsParameterOptimization()
					+ "` WHERE `absPath`='" + absPath + "';");
			rs.first();
			return rs.getInt("id");
		} finally {
			stmt.close();
		}
	}

	@Override
	protected int getRunResultFormatId(final String runResultFormatSimpleName)
			throws SQLException {
		Statement stmt = conn.createStatement();
		try {
			ResultSet rs = stmt.executeQuery("SELECT `id` FROM `"
					+ this.getDatabase() + "`.`"
					+ this.getTableRunResultFormats()
					+ "` WHERE `repository_id`='" + this.updateRepositoryId()
					+ "' AND `name`='" + runResultFormatSimpleName + "';");
			rs.first();
			return rs.getInt("id");
		} finally {
			stmt.close();
		}
	}

	@Override
	protected int getRunResultId(final String uniqueRunIdentifier)
			throws SQLException {
		Statement stmt = conn.createStatement();
		try {
			ResultSet rs = stmt.executeQuery("SELECT `id` FROM `"
					+ this.getDatabase() + "`.`" + this.getTableRunResults()
					+ "` WHERE `repository_id`='" + this.updateRepositoryId()
					+ "' AND `uniqueRunIdentifier`='" + uniqueRunIdentifier
					+ "';");
			rs.first();
			return rs.getInt("id");
		} finally {
			stmt.close();
		}
	}

	@Override
	protected int getStatisticId(final String statisticsName)
			throws SQLException {

		Statement stmt = conn.createStatement();
		try {
			ResultSet rs = stmt.executeQuery("SELECT `id` FROM `"
					+ this.getDatabase() + "`.`" + this.getTableStatistics()
					+ "` WHERE `repository_id`='" + this.updateRepositoryId()
					+ "' AND `name`='" + statisticsName + "';");
			rs.first();
			return rs.getInt("id");
		} finally {
			stmt.close();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#register(program.Program)
	 */
	@Override
	protected boolean register(Program object, final boolean updateOnly) {
		try {
			// in case of a update, we do nothing
			if (updateOnly)
				return true;
			insert(this.getTablePrograms(),
					new String[]{"repository_id", "absPath", "alias"},
					new String[]{"" + this.updateRepositoryId(),
							"" + object.getAbsolutePath(), object.getAlias()});

			return true;
		} catch (SQLException e) {

			e.printStackTrace();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#register(data.goldstandard.GoldStandardConfig)
	 */
	@Override
	protected boolean register(GoldStandardConfig object,
			final boolean updateOnly) {
		try {
			int goldstandard_id = getGoldStandardId(object.getGoldstandard());

			String[] columns;
			String[] values;
			if (object.getRepository() instanceof RunResultRepository) {
				columns = new String[]{"repository_id", "absPath", "name",
						"goldstandard_id", "goldstandard_config_id"};
				values = new String[]{
						"" + this.updateRepositoryId(),
						"" + object.getAbsolutePath(),
						new File(object.getAbsolutePath()).getName().replace(
								".gsconfig", ""),
						"" + goldstandard_id,
						""
								+ getGoldStandardConfigId(object
										.getRepository()
										.getParent()
										.getGoldStandardConfigWithName(
												object.getAbsolutePath()
														.substring(
																object.getAbsolutePath()
																		.lastIndexOf(
																				"/") + 1)))};
			} else {
				columns = new String[]{"repository_id", "absPath", "name",
						"goldstandard_id"};
				values = new String[]{
						"" + this.updateRepositoryId(),
						"" + object.getAbsolutePath(),
						new File(object.getAbsolutePath()).getName().replace(
								".gsconfig", ""), "" + goldstandard_id};
			}
			if (updateOnly) {
				int rowId = getGoldStandardConfigId(object);
				update(this.getTableGoldStandardConfigs(), columns, values,
						rowId);
			} else
				insert(this.getTableGoldStandardConfigs(), columns, values);
			return true;
		} catch (SQLException e) {

			e.printStackTrace();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#register(data.goldstandard.GoldStandard)
	 */
	@Override
	protected boolean register(GoldStandard object, final boolean updateOnly) {
		try {
			String[] columns;
			String[] values;
			if (object.getRepository() instanceof RunResultRepository) {
				columns = new String[]{"repository_id", "absPath",
						"goldstandard_id"};
				values = new String[]{
						"" + this.updateRepositoryId(),
						"" + object.getAbsolutePath(),
						""
								+ getGoldStandardId(object
										.getRepository()
										.getParent()
										.getGoldStandardWithName(
												object.getFullName()))};
			} else {
				columns = new String[]{"repository_id", "absPath"};
				values = new String[]{"" + this.updateRepositoryId(),
						"" + object.getAbsolutePath()};
			}
			if (updateOnly) {
				int rowId = getGoldStandardId(object);
				update(this.getTableGoldStandards(), columns, values, rowId);
			} else
				insert(this.getTableGoldStandards(), columns, values);
			return true;
		} catch (SQLException e) {

			e.printStackTrace();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#registerDataSetFormat(java.lang.Class)
	 */
	@Override
	protected boolean registerDataSetFormatClass(
			Class<? extends DataSetFormat> object) {
		try {
			DataSetFormat format = object.getConstructor(Repository.class,
					boolean.class, long.class, File.class, int.class)
					.newInstance(repository, false, System.currentTimeMillis(),
							new File(object.getSimpleName()), 1);
			insert(this.getTableDataSetFormats(),
					new String[]{"repository_id", "name", "alias"},
					new String[]{"" + this.updateRepositoryId(),
							"" + object.getSimpleName(), format.getAlias()});
			return true;
		} catch (SQLException e) {
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#registerDataSetFormat(java.lang.Class)
	 */
	@Override
	protected boolean registerDataSetTypeClass(
			Class<? extends DataSetType> object) {
		try {
			DataSetType type = object.getConstructor(Repository.class,
					boolean.class, long.class, File.class).newInstance(
					repository, false, System.currentTimeMillis(),
					new File(object.getSimpleName()));
			insert(this.getTableDataSetTypes(),
					new String[]{"repository_id", "name", "alias"},
					new String[]{"" + this.updateRepositoryId(),
							"" + object.getSimpleName(), type.getAlias()});
			return true;
		} catch (SQLException e) {
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		return false;
	}

	protected boolean isInfinity(final double d) {
		if (d == Double.POSITIVE_INFINITY || d == Double.NEGATIVE_INFINITY)
			return true;
		return false;
	}

	protected String replaceInfinity(final double d) {
		if (isInfinity(d))
			return null;
		return "" + (float) d;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * utils.SQLCommunicator#registerClusteringQualityMeasure(java.lang.Class)
	 */
	@Override
	protected boolean registerClusteringQualityMeasureClass(
			Class<? extends QualityMeasure> object) {
		try {
			QualityMeasure measure = object.getConstructor(
					Repository.class, boolean.class, long.class, File.class)
					.newInstance(repository, false, System.currentTimeMillis(),
							new File(object.getSimpleName()));
			insert(this.getTableClusteringQualityMeasures(),
					new String[]{"repository_id", "name", "minValue",
							"maxValue", "requiresGoldStandard", "alias"},
					new String[]{"" + this.updateRepositoryId(),
							"" + object.getSimpleName(),
							replaceInfinity(measure.getMinimum()),
							replaceInfinity(measure.getMaximum()),
							"" + (measure.requiresGoldstandard() ? 1 : 0),
							measure.getAlias()});
			return true;
		} catch (SQLException e) {

			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * utils.SQLCommunicator#registerParameterOptimizationMethod(java.lang.Class
	 * )
	 */
	@Override
	protected boolean registerParameterOptimizationMethodClass(
			Class<? extends ParameterOptimizationMethod> object) {

		try {
			insert(this.getTableParameterOptimizationMethods(),
					new String[]{"repository_id", "name"},
					new String[]{"" + this.updateRepositoryId(),
							"" + object.getSimpleName()});
			return true;
		} catch (SQLException e) {

			e.printStackTrace();
		}
		return false;
	}

	@Override
	protected boolean registerDataStatisticClass(
			Class<? extends DataStatistic> object) {

		try {
			int statisticId;
			try {
				DataStatistic statistic = object
						.getConstructor(Repository.class, boolean.class,
								long.class, File.class).newInstance(repository,
								false, System.currentTimeMillis(),
								new File(object.getSimpleName()));
				statisticId = insert(
						this.getTableStatistics(),
						new String[]{"repository_id", "name", "alias"},
						new String[]{"" + this.updateRepositoryId(),
								"" + object.getSimpleName(),
								statistic.getAlias()});

				insert(this.getTableStatisticsData(), new String[]{
						"repository_id", "statistic_id"}, new String[]{
						"" + this.updateRepositoryId(), "" + statisticId});
			} catch (SQLException e) {
				e.printStackTrace();
				statisticId = getStatisticId(object.getSimpleName());
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
			return true;
		} catch (SQLException e) {

			e.printStackTrace();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#registerRunStatistic(java.lang.Class)
	 */
	@Override
	protected boolean registerRunStatisticClass(
			Class<? extends RunStatistic> object) {

		try {
			int statisticId;
			try {
				RunStatistic runStatistic = object
						.getConstructor(Repository.class, boolean.class,
								long.class, File.class).newInstance(repository,
								false, System.currentTimeMillis(),
								new File(object.getSimpleName()));
				statisticId = insert(
						this.getTableStatistics(),
						new String[]{"repository_id", "name", "alias"},
						new String[]{"" + this.updateRepositoryId(),
								"" + object.getSimpleName(),
								runStatistic.getAlias()});

				insert(this.getTableStatisticsRun(), new String[]{
						"repository_id", "statistic_id"}, new String[]{
						"" + this.updateRepositoryId(), "" + statisticId});
			} catch (SQLException e) {
				e.printStackTrace();
				statisticId = getStatisticId(object.getSimpleName());
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
			return true;
		} catch (SQLException e) {

			e.printStackTrace();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#registerRunDataStatistic(java.lang.Class)
	 */
	@Override
	protected boolean registerRunDataStatisticClass(
			Class<? extends RunDataStatistic> object) {
		try {
			int statisticId;
			try {
				RunDataStatistic runDataStatistic = object
						.getConstructor(Repository.class, boolean.class,
								long.class, File.class).newInstance(repository,
								false, System.currentTimeMillis(),
								new File(object.getSimpleName()));
				statisticId = insert(
						this.getTableStatistics(),
						new String[]{"repository_id", "name", "alias"},
						new String[]{"" + this.updateRepositoryId(),
								"" + object.getSimpleName(),
								runDataStatistic.getAlias()});
				insert(this.getTableStatisticsRunData(), new String[]{
						"repository_id", "statistic_id"}, new String[]{
						"" + this.updateRepositoryId(), "" + statisticId});
			} catch (SQLException e) {
				e.printStackTrace();
				statisticId = getStatisticId(object.getSimpleName());
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
			return true;
		} catch (SQLException e) {

			e.printStackTrace();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#register(program.DoubleProgramParameter)
	 */
	@Override
	protected boolean register(DoubleProgramParameter object) {

		ProgramConfig programConfig = object.getProgramConfig();
		int program_config_id = -1;
		try {

			program_config_id = getProgramConfigId(programConfig);

			int parameterTypeId = getProgramParameterTypeId(object.getClass()
					.getSimpleName());

			insert(this.getTableProgramParameter(),
					new String[]{"repository_id", "program_config_id", "name",
							"description", "minValue", "maxValue", "def",
							"program_parameter_type_id"},
					new String[]{"" + this.updateRepositoryId(),
							"" + program_config_id,
							"" + replaceNull(object.getName(), ""),
							"" + replaceNull(object.getDescription(), ""),
							"" + replaceNull(object.getMinValue(), ""),
							"" + replaceNull(object.getMaxValue(), ""),
							"" + replaceNull(object.getDefault(), ""),
							"" + parameterTypeId});
			return true;
		} catch (SQLException e) {
			System.out.println(program_config_id);
			e.printStackTrace();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#register(program.IntegerProgramParameter)
	 */
	@Override
	protected boolean register(IntegerProgramParameter object) {

		ProgramConfig programConfig = object.getProgramConfig();

		try {
			int program_config_id = getProgramConfigId(programConfig);

			int parameterTypeId = getProgramParameterTypeId(object.getClass()
					.getSimpleName());

			insert(this.getTableProgramParameter(),
					new String[]{"repository_id", "program_config_id", "name",
							"description", "minValue", "maxValue", "def",
							"program_parameter_type_id"},
					new String[]{"" + this.updateRepositoryId(),
							"" + program_config_id,
							"" + replaceNull(object.getName(), ""),
							"" + replaceNull(object.getDescription(), ""),
							"" + replaceNull(object.getMinValue(), ""),
							"" + replaceNull(object.getMaxValue(), ""),
							"" + replaceNull(object.getDefault(), ""),
							"" + parameterTypeId});
			return true;
		} catch (SQLException e) {

			e.printStackTrace();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#register(program.StringProgramParameter)
	 */
	@Override
	protected boolean register(StringProgramParameter object) {

		ProgramConfig programConfig = object.getProgramConfig();

		try {
			int program_config_id = getProgramConfigId(programConfig);

			int parameterTypeId = getProgramParameterTypeId(object.getClass()
					.getSimpleName());

			insert(this.getTableProgramParameter(),
					new String[]{"repository_id", "program_config_id", "name",
							"description", "minValue", "maxValue", "def",
							"program_parameter_type_id"},
					new String[]{"" + this.updateRepositoryId(),
							"" + program_config_id,
							"" + replaceNull(object.getName(), ""),
							"" + replaceNull(object.getDescription(), ""),
							"" + replaceNull(object.getMinValue(), ""),
							"" + replaceNull(object.getMaxValue(), ""),
							"" + replaceNull(object.getDefault(), ""),
							"" + parameterTypeId});
			return true;
		} catch (SQLException e) {

			e.printStackTrace();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#register(data.dataset.DataSet)
	 */
	@Override
	protected boolean register(DataSet object, final boolean updateOnly) {

		try {
			int dataset_format_id = getDataSetFormatId(object
					.getDataSetFormat().getClass().getSimpleName());

			String[] columns;
			String[] values;
			if (object.getRepository() instanceof RunResultRepository) {
				columns = new String[]{"repository_id", "absPath",
						"dataset_format_id", "dataset_id", "checksum",
						"dataset_type_id", "alias"};
				values = new String[]{
						"" + this.updateRepositoryId(),
						"" + object.getAbsolutePath(),
						"" + dataset_format_id,
						""
								+ getDataSetId(object
										.getRepository()
										.getParent()
										.getDataSetWithName(
												object.getFullName())),
						object.getChecksum() + "",
						getDataSetTypeId(object.getDataSetType().getClass()
								.getSimpleName())
								+ "", object.getAlias()};
			} else {
				columns = new String[]{"repository_id", "absPath",
						"dataset_format_id", "checksum", "dataset_type_id",
						"alias"};
				values = new String[]{
						"" + this.updateRepositoryId(),
						"" + object.getAbsolutePath(),
						"" + dataset_format_id,
						object.getChecksum() + "",
						getDataSetTypeId(object.getDataSetType().getClass()
								.getSimpleName())
								+ "", object.getAlias()};
			}
			if (updateOnly) {
				int rowId = getDataSetId(object);
				update(this.getTableDatasets(), columns, values, rowId);
			} else
				insert(this.getTableDatasets(), columns, values);
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#register(data.DataConfig)
	 */
	@Override
	protected boolean register(DataConfig object, final boolean updateOnly) {

		try {
			int dataset_config_id = getDataSetConfigId(object
					.getDatasetConfig());

			String[] columns;
			String[] values;
			if (object.getRepository() instanceof RunResultRepository) {
				if (object.hasGoldStandardConfig()) {
					int goldstandard_config_id = getGoldStandardConfigId(object
							.getGoldstandardConfig());

					columns = new String[]{"repository_id", "absPath", "name",
							"dataset_config_id", "goldstandard_config_id",
							"data_config_id"};
					values = new String[]{
							"" + this.updateRepositoryId(),
							"" + object.getAbsolutePath(),
							new File(object.getAbsolutePath()).getName()
									.replace(".dataconfig", ""),
							"" + dataset_config_id,
							"" + goldstandard_config_id,
							""
									+ getDataConfigId(object
											.getRepository()
											.getParent()
											.getDataConfigWithName(
													object.getAbsolutePath()
															.substring(
																	object.getAbsolutePath()
																			.lastIndexOf(
																					"/") + 1)))};

				} else {
					columns = new String[]{"repository_id", "absPath", "name",
							"dataset_config_id", "data_config_id"};
					values = new String[]{
							"" + this.updateRepositoryId(),
							"" + object.getAbsolutePath(),
							new File(object.getAbsolutePath()).getName()
									.replace(".dataconfig", ""),
							"" + dataset_config_id,
							""
									+ getDataConfigId(object
											.getRepository()
											.getParent()
											.getDataConfigWithName(
													object.getAbsolutePath()
															.substring(
																	object.getAbsolutePath()
																			.lastIndexOf(
																					"/") + 1)))};
				}
			} else {
				if (object.hasGoldStandardConfig()) {
					int goldstandard_config_id = getGoldStandardConfigId(object
							.getGoldstandardConfig());

					columns = new String[]{"repository_id", "absPath", "name",
							"dataset_config_id", "goldstandard_config_id"};
					values = new String[]{
							"" + this.updateRepositoryId(),
							"" + object.getAbsolutePath(),
							new File(object.getAbsolutePath()).getName()
									.replace(".dataconfig", ""),
							"" + dataset_config_id, "" + goldstandard_config_id};

				} else {
					columns = new String[]{"repository_id", "absPath", "name",
							"dataset_config_id"};
					values = new String[]{
							"" + this.updateRepositoryId(),
							"" + object.getAbsolutePath(),
							new File(object.getAbsolutePath()).getName()
									.replace(".dataconfig", ""),
							"" + dataset_config_id};
				}
			}

			if (updateOnly) {
				int rowId = getDataConfigId(object);
				update(this.getTableDataConfigs(), columns, values, rowId);
			} else
				insert(this.getTableDataConfigs(), columns, values);
			return true;
		} catch (SQLException e) {

			e.printStackTrace();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#register(data.dataset.DataSetConfig)
	 */
	@Override
	protected boolean register(DataSetConfig object, final boolean updateOnly) {
		// TODO: change database structure
		return true;
		//
		// try {
		// int dataset_id = getDataSetId(object.getDataSet());
		//
		// String[] columns;
		// String[] values;
		// if (object.getRepository() instanceof RunResultRepository) {
		// columns = new String[]{"repository_id", "absPath", "name",
		// "dataset_id", "dataset_config_id"};
		// values = new String[]{
		// "" + this.updateRepositoryId(),
		// "" + object.getAbsolutePath(),
		// new File(object.getAbsolutePath()).getName().replace(
		// ".dsconfig", ""),
		// "" + dataset_id,
		// ""
		// + getDataSetConfigId(object
		// .getRepository()
		// .getParent()
		// .getDataSetConfigWithName(
		// object.getAbsolutePath()
		// .substring(
		// object.getAbsolutePath()
		// .lastIndexOf(
		// "/") + 1)))};
		// } else {
		// columns = new String[]{"repository_id", "absPath", "name",
		// "dataset_id"};
		// values = new String[]{
		// "" + this.updateRepositoryId(),
		// "" + object.getAbsolutePath(),
		// new File(object.getAbsolutePath()).getName().replace(
		// ".dsconfig", ""), "" + dataset_id};
		// }
		// if (updateOnly) {
		// int rowId = getDataSetConfigId(object);
		// update(this.getTableDataSetConfigs(), columns, values, rowId);
		// } else
		// insert(this.getTableDataSetConfigs(), columns, values);
		// return true;
		// } catch (SQLException e) {
		//
		// e.printStackTrace();
		// }
		//
		// return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#unregisterRunResultFormat(java.lang.Class)
	 */
	@Override
	protected boolean unregisterRunResultFormat(
			Class<? extends RunResultFormat> object) {
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			stmt.execute("DELETE FROM `" + this.getDatabase() + "`.`"
					+ this.getTableRunResultFormats()
					+ "` WHERE `repository_id`='" + this.updateRepositoryId()
					+ "' AND `name`='" + object.getSimpleName() + "';");
			stmt.close();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			if (stmt != null)
				stmt.close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#unregister(program.ProgramConfig)
	 */
	@Override
	protected boolean unregister(ProgramConfig object) {
		Statement stmt = null;
		try {
			stmt = conn.createStatement();

			int program_id = getProgramId(object.getProgram().getAbsolutePath());

			for (ProgramParameter<?> param : object.getOptimizableParams()) {
				int program_config_id = getProgramConfigId(object);
				int program_parameter_id = getProgramParameterId(param);

				stmt.execute("DELETE FROM `" + this.getDatabase() + "`.`"
						+ this.getTableOptimizableProgramParameters()
						+ "` WHERE `program_config_id`='" + program_config_id
						+ "' AND `program_parameter_id`='"
						+ program_parameter_id + "';");
			}

			stmt.execute("DELETE FROM `" + this.getDatabase() + "`.`"
					+ this.getTableProgramConfigs() + "` WHERE `program_id`='"
					+ program_id + "' AND `name`='" + object.getName() + "';");
			stmt.close();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			if (stmt != null)
				stmt.close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#unregister(program.Program)
	 */
	@Override
	protected boolean unregister(Program object) {
		Statement stmt = null;
		try {
			stmt = conn.createStatement();

			stmt.execute("DELETE FROM `" + this.getDatabase() + "`.`"
					+ this.getTablePrograms() + "` WHERE `absPath`='"
					+ object.getAbsolutePath() + "';");
			stmt.close();
			return true;
		} catch (SQLException e) {

			e.printStackTrace();
		}
		try {
			if (stmt != null)
				stmt.close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * utils.SQLCommunicator#unregister(data.goldstandard.GoldStandardConfig)
	 */
	@Override
	protected boolean unregister(GoldStandardConfig object) {
		Statement stmt = null;
		try {
			stmt = conn.createStatement();

			stmt.execute("DELETE FROM `" + this.getDatabase() + "`.`"
					+ this.getTableGoldStandardConfigs()
					+ "` WHERE `absPath`='" + object.getAbsolutePath() + "';");
			stmt.close();
			return true;
		} catch (SQLException e) {

			e.printStackTrace();
		}
		try {
			if (stmt != null)
				stmt.close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#unregister(data.goldstandard.GoldStandard)
	 */
	@Override
	protected boolean unregister(GoldStandard object) {
		Statement stmt = null;
		try {
			stmt = conn.createStatement();

			stmt.execute("DELETE FROM `" + this.getDatabase() + "`.`"
					+ this.getTableGoldStandards() + "` WHERE `absPath`='"
					+ object.getAbsolutePath() + "';");
			stmt.close();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			if (stmt != null)
				stmt.close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#unregisterDataSetFormat(java.lang.Class)
	 */
	@Override
	protected boolean unregisterDataSetFormatClass(
			Class<? extends DataSetFormat> object) {
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			stmt.execute("DELETE FROM `" + this.getDatabase() + "`.`"
					+ this.getTableDataSetFormats()
					+ "` WHERE `repository_id`='" + this.updateRepositoryId()
					+ "' AND  `name`='" + object.getSimpleName() + "';");
			stmt.close();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			if (stmt != null)
				stmt.close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#unregister(data.dataset.DataSet)
	 */
	@Override
	protected boolean unregister(DataSet object) {
		Statement stmt = null;
		try {
			stmt = conn.createStatement();

			stmt.execute("DELETE FROM `" + this.getDatabase() + "`.`"
					+ this.getTableDatasets() + "` WHERE `absPath`='"
					+ object.getAbsolutePath() + "';");
			stmt.close();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			if (stmt != null)
				stmt.close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#unregister(data.DataConfig)
	 */
	@Override
	protected boolean unregister(DataConfig object) {
		Statement stmt = null;
		try {
			stmt = conn.createStatement();

			stmt.execute("DELETE FROM `" + this.getDatabase() + "`.`"
					+ this.getTableDataConfigs() + "` WHERE `absPath`='"
					+ object.getAbsolutePath() + "';");
			stmt.close();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			if (stmt != null)
				stmt.close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#unregister(data.dataset.DataSetConfig)
	 */
	@Override
	protected boolean unregister(DataSetConfig object) {
		Statement stmt = null;
		try {
			stmt = conn.createStatement();

			stmt.execute("DELETE FROM `" + this.getDatabase() + "`.`"
					+ this.getTableDataSetConfigs() + "` WHERE `absPath`='"
					+ object.getAbsolutePath() + "';");
			stmt.close();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			if (stmt != null)
				stmt.close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#updateStatusOfRun(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public boolean updateStatusOfRun(final Run run, String runStatus) {
		Statement stmt = null;
		try {
			stmt = conn.createStatement();

			ResultSet rs = stmt.executeQuery("SELECT `id` FROM `"
					+ this.getDatabase() + "`.`" + this.getTableRuns()
					+ "` WHERE `repository_id`='" + this.updateRepositoryId()
					+ "' AND `absPath`='" + run.getAbsolutePath() + "';");
			rs.first();
			int runId = rs.getInt("id");

			stmt.execute("UPDATE `" + this.getDatabase() + "`.`"
					+ this.getTableRuns() + "` SET `status`='" + runStatus
					+ "' WHERE `id`='" + runId + "';");
			stmt.close();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			if (stmt != null)
				stmt.close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#register(run.result.RunResult)
	 */
	@Override
	public boolean register(RunResult object) {
		String type = null;
		String resultsPath = null;
		if (object instanceof GraphMatchingRunResult) {
			type = "Clustering";
			resultsPath = object.absPath.getParentFile().getParentFile()
					.getAbsolutePath();
		} else if (object instanceof ParameterOptimizationResult) {
			type = "Parameter Optimization";
			resultsPath = object.absPath.getParentFile().getParentFile()
					.getAbsolutePath();
		} else if (object instanceof DataAnalysisRunResult) {
			type = "Data Analysis";
			resultsPath = object.absPath.getParentFile().getAbsolutePath();
		} else if (object instanceof RunDataAnalysisRunResult) {
			type = "Run-Data Analysis";
			resultsPath = object.absPath.getParentFile().getAbsolutePath();
		} else if (object instanceof RunAnalysisRunResult) {
			type = "Run Analysis";
			resultsPath = object.absPath.getParentFile().getAbsolutePath();
		}

		String identifier = object.getIdentifier();
		String dateString = identifier.substring(0, 19);
		try {
			dateString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
					.format(new SimpleDateFormat("MM_dd_yyyy-HH_mm_ss")
							.parse(dateString));
		} catch (ParseException e) {
			e.printStackTrace();
		}

		try {
			int run_type_id = getRunTypeId(type);
			int run_id = getRunId(object.getRun());

			// parameter optimization runs may already been inserted before,
			// thus the corresponding run result also.
			// because of this we use tryInsert instead of insert
			tryInsert(this.getTableRunResults(), new String[]{"repository_id",
					"uniqueRunIdentifier", "run_type_id", "run_id", "date",
					"absPath"}, new String[]{"" + this.updateRepositoryId(),
					"" + object.getIdentifier(), "" + run_type_id, "" + run_id,
					"" + dateString, resultsPath});
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return super.register(object);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * framework.repository.SQLCommunicator#register(run.result.ExecutionRunResult
	 * )
	 */
	@Override
	public boolean register(ExecutionRunResult object) {
		// this.register((RunResult) object);

		try {
			int run_results_id = getRunResultId(object.getIdentifier());

			// parameter optimization runs may already been inserted before,
			// thus the corresponding execution run result also.
			// because of this we use tryInsert instead of insert
			tryInsert(this.getTableRunResultsExecution(), new String[]{
					"repository_id", "run_result_id"},
					new String[]{"" + this.updateRepositoryId(),
							"" + run_results_id});
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#register(run.result.ClusteringRunResult)
	 */
	@Override
	public boolean register(GraphMatchingRunResult object) {
		this.register((ExecutionRunResult) object);

		try {
			int run_results_id = getRunResultId(object.getIdentifier());
			int run_results_execution_id = getRunResultExecutionId(run_results_id);

			int clusteringId;
			try {
				clusteringId = insert(
						this.getTableRunResultsClustering(),
						new String[]{"repository_id",
								"run_results_execution_id", "absPath"},
						new String[]{"" + this.updateRepositoryId(),
								"" + run_results_execution_id,
								"" + object.getAbsolutePath()});
			} catch (SQLException e) {
				clusteringId = select(
						this.getTableRunResultsClustering(),
						"id",
						new String[]{"repository_id",
								"run_results_execution_id", "absPath"},
						new String[]{"" + this.updateRepositoryId(),
								"" + run_results_execution_id,
								"" + object.getAbsolutePath()});
			}

			int dataConfigId = getDataConfigId(object.getDataConfig());
			int programConfigId = getProgramConfigId(object.getProgramConfig());

			final Pair<ParameterSet, GraphMatching> pair = object.getGraphMatching();
// TODO
//			final QualitySet qualities = pair.getSecond()
//					.getQualities();
//
//			final StringBuilder paramString = new StringBuilder();
//			for (String param : pair.getFirst().keySet()) {
//				paramString.append(param);
//				paramString.append("=");
//				paramString.append(pair.getFirst().get(param));
//				paramString.append(",");
//			}
//			for (QualityMeasure measure : qualities.keySet()) {
//
//				paramString.deleteCharAt(paramString.length() - 1);
//				insert(this.getTableRunResultsClusteringsQuality(),
//						new String[]{"repository_id",
//								"run_results_clustering_id", "data_config_id",
//								"program_config_id",
//								"clustering_quality_measure_id", "paramString",
//								"quality"},
//						new String[]{
//								"" + this.updateRepositoryId(),
//								"" + clusteringId,
//								"" + dataConfigId,
//								"" + programConfigId,
//								""
//										+ getClusteringQualityMeasureId(measure
//												.toString()),
//								paramString.toString(),
//								"" + qualities.get(measure).getValue()});
//			}

			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * utils.SQLCommunicator#register(run.result.ParameterOptimizationResult)
	 */
	@Override
	public boolean register(ParameterOptimizationResult object) {
		this.register((ExecutionRunResult) object);

		try {
			int repository_id;
			try {
				repository_id = getRepositoryId(Repository
						.getRepositoryForPath(object.getAbsolutePath()).basePath);
			} catch (NoRepositoryFoundException e1) {
				e1.printStackTrace();
				repository_id = this.updateRepositoryId();
			}
			int run_results_id = getRunResultId(object.getIdentifier());
			int run_results_execution_id = getRunResultExecutionId(run_results_id);

			int runResultParamOptId;
			try {
				runResultParamOptId = insert(
						this.getTableRunResultsParameterOptimization(),
						new String[]{"repository_id",
								"run_results_execution_id", "absPath",
								"data_config_id", "program_config_id"},
						new String[]{
								"" + repository_id,
								"" + run_results_execution_id,
								"" + object.getAbsolutePath(),
								getDataConfigId(object.getDataConfig()) + "",
								getProgramConfigId(object.getProgramConfig())
										+ ""});
			} catch (SQLException e) {
				e.printStackTrace();
				runResultParamOptId = getRunResultParameterOptimizationId(object
						.getAbsolutePath());
			}

			/*
			 * Insert parameterset
			 */

			int run_results_parameter_optimizations_parameter_set_id = insert(
					this.getTableParameterSets(), new String[]{"repository_id",
							"run_results_parameter_optimization_id"},
					new String[]{repository_id + "", "" + runResultParamOptId});
			/*
			 * Insert parameters
			 */
			for (ProgramParameter<?> optParam : object.getMethod()
					.getOptimizationParameter()) {
				int program_parameter_id = getProgramParameterId(optParam);
				insert(this.getTableParameterSetParameters(),
						new String[]{
								"repository_id",
								"run_results_parameter_optimization_id",
								"run_results_parameter_optimizations_parameter_set_id",
								"program_parameter_id"},
						new String[]{
								"" + repository_id,
								"" + runResultParamOptId,
								""
										+ run_results_parameter_optimizations_parameter_set_id,
								"" + program_parameter_id});
			}

			/*
			 * For every iteration in the result insert the parameter values
			 */
			List<ParameterSet> paramSets = object.getParameterSets();
			List<Long> iterationNumbers = object.getIterationNumbers();
			for (int i = 0; i < paramSets.size(); i++) {
				Pair<ParameterSet, QualitySet> pair = Pair.getPair(
						paramSets.get(i), object.get(paramSets.get(i)));
				StringBuilder paramSetAsString = new StringBuilder();
				for (Map.Entry<String, String> paramValue : paramSets.get(i)
						.entrySet()) {
					paramSetAsString.append(paramValue.getKey());
					paramSetAsString.append("=");
					paramSetAsString.append(paramValue.getValue());
					paramSetAsString.append(",");
				}
				paramSetAsString.deleteCharAt(paramSetAsString.length() - 1);

				// int clustering_id = getClusteringId(object.getAbsolutePath()
				// .replace("results.qual.complete",
				// iteration + ".results.conv"));
				/*
				 * insert iteration
				 */
				int iteration_id = insert(
						this.getTableParameterSetIterations(),
						new String[]{
								"repository_id",
								"run_results_parameter_optimizations_parameter_set_id",
								"iteration", "paramSetAsString"
						// ,"clustering_id"
						}, new String[]{
								"" + repository_id,
								run_results_parameter_optimizations_parameter_set_id
										+ "", iterationNumbers.get(i) + "",
								paramSetAsString.toString()
						// , "" + clustering_id
						});

				for (ProgramParameter<?> optParam : object.getMethod()
						.getOptimizationParameter()) {
					int program_parameter_id = getProgramParameterId(optParam);
					int run_results_parameter_optimizations_parameter_sets_parameter_id = getParameterSetParameterId(
							run_results_parameter_optimizations_parameter_set_id,
							program_parameter_id);
					insert(this.getTableParameterSetParameterValues(),
							new String[]{
									"repository_id",
									"run_results_parameter_optimization_id",
									"run_results_parameter_optimizations_parameter_set_parameter_id",
									"run_results_parameter_optimizations_parameter_set_iteration_id",
									"value"},
							new String[]{
									"" + repository_id,
									"" + runResultParamOptId,
									""
											+ run_results_parameter_optimizations_parameter_sets_parameter_id,
									"" + iteration_id,
									""
											+ pair.getFirst().get(
													optParam.getName())});
				}

				for (QualityMeasure measure : pair.getSecond()
						.keySet()) {
					int clustering_quality_measure_id = getClusteringQualityMeasureId(measure
							.toString());
					try {
						if (pair.getSecond().get(measure).isTerminated()
								&& !Double.isNaN(pair.getSecond().get(measure)
										.getValue())
								&& !isInfinity(pair.getSecond().get(measure)
										.getValue()))
							insert(this
									.getTableParameterOptimizationQualities(),
									new String[]{
											"repository_id",
											"run_results_parameter_optimization_id",
											"run_results_parameter_optimizations_parameter_set_iteration_id",
											"clustering_quality_measure_id",
											"quality"}, new String[]{
											"" + repository_id,
											"" + runResultParamOptId,
											"" + iteration_id,
											"" + clustering_quality_measure_id,
											replaceInfinity(pair.getSecond()
													.get(measure).getValue())});
					} catch (NullPointerException e) {
						System.out.println(iterationNumbers.get(i));
					}
				}

			}

			conn.commit();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * framework.repository.SQLCommunicator#register(run.result.AnalysisRunResult
	 * )
	 */
	@Override
	public boolean register(AnalysisRunResult object) {
		// this.register((RunResult) object);

		try {
			int run_results_id = getRunResultId(object.getIdentifier());

			insert(this.getTableRunResultsAnalysis(), new String[]{
					"repository_id", "run_result_id"},
					new String[]{"" + this.updateRepositoryId(),
							"" + run_results_id});

			conn.commit();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * utils.SQLCommunicator#register(run.result.ParameterOptimizationResult)
	 */
	@Override
	public boolean register(DataAnalysisRunResult object) {
		this.register((AnalysisRunResult) object);

		try {
			int run_results_id = getRunResultId(object.getIdentifier());
			int run_results_analysis_id = getRunResultAnalysisId(run_results_id);

			insert(this.getTableRunResultsDataAnalysis(), new String[]{
					"repository_id", "run_results_analysis_id"}, new String[]{
					"" + this.updateRepositoryId(),
					"" + run_results_analysis_id});

			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableRepositoryTypes()
	 */
	@Override
	protected String getTableRepositoryTypes() {
		return "repository_types";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see framework.repository.SQLCommunicator#getTableRunResultsAnalysis()
	 */
	@Override
	protected String getTableRunResultsAnalysis() {
		return "run_results_analyses";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see framework.repository.SQLCommunicator#getRunResultAnalysisId(int)
	 */
	@Override
	protected int getRunResultAnalysisId(int run_results_id)
			throws SQLException {
		Statement stmt = conn.createStatement();
		try {
			ResultSet rs = stmt.executeQuery("SELECT `id` FROM `"
					+ this.getDatabase() + "`.`"
					+ this.getTableRunResultsAnalysis()
					+ "` WHERE `repository_id`='" + this.updateRepositoryId()
					+ "' AND `run_result_id`='" + run_results_id + "';");
			rs.first();
			return rs.getInt("id");
		} finally {
			stmt.close();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see framework.repository.SQLCommunicator#getRunResultRunAnalysisId(int)
	 */
	@Override
	protected int getRunResultRunAnalysisId(int run_results_analysis_id)
			throws SQLException {
		Statement stmt = conn.createStatement();
		try {
			ResultSet rs = stmt.executeQuery("SELECT `id` FROM `"
					+ this.getDatabase() + "`.`"
					+ this.getTableRunResultsRunAnalysis()
					+ "` WHERE `repository_id`='" + this.updateRepositoryId()
					+ "' AND `run_results_analysis_id`='"
					+ run_results_analysis_id + "';");
			rs.first();
			return rs.getInt("id");
		} finally {
			stmt.close();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * framework.repository.SQLCommunicator#register(run.result.RunAnalysisRunResult
	 * )
	 */
	@Override
	public boolean register(RunAnalysisRunResult object) {
		this.register((AnalysisRunResult) object);

		try {
			int run_results_id = getRunResultId(object.getIdentifier());
			int run_results_analysis_id = getRunResultAnalysisId(run_results_id);
			insert(this.getTableRunResultsRunAnalysis(), new String[]{
					"repository_id", "run_results_analysis_id"}, new String[]{
					"" + this.updateRepositoryId(),
					"" + run_results_analysis_id});

			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see framework.repository.SQLCommunicator#register(run.result.
	 * RunDataAnalysisRunResult)
	 */
	@Override
	public boolean register(RunDataAnalysisRunResult object) {
		this.register((AnalysisRunResult) object);

		try {
			int run_results_id = getRunResultId(object.getIdentifier());
			int run_results_analysis_id = getRunResultAnalysisId(run_results_id);
			int runResultRunAnalysisId = getRunResultRunAnalysisId(run_results_analysis_id);

			insert(this.getTableRunResultsRunDataAnalysis(), new String[]{
					"repository_id", "run_results_analysis_id"},
					new String[]{"" + this.updateRepositoryId(),
							"" + runResultRunAnalysisId});

			conn.commit();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see framework.repository.SQLCommunicator#
	 * getTableRunsAnalysisRunDataDataIdentifiers()
	 */
	@Override
	protected String getTableRunsAnalysisRunDataDataIdentifiers() {
		return "run_run_data_analysis_data_identifiers";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see framework.repository.SQLCommunicator#
	 * getTableRunsAnalysisRunDataRunIdentifiers()
	 */
	@Override
	protected String getTableRunsAnalysisRunDataRunIdentifiers() {
		return "run_run_data_analysis_run_identifiers";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see framework.repository.SQLCommunicator#getTableProgramParameterType()
	 */
	@Override
	protected String getTableProgramParameterType() {
		return "program_parameter_types";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * framework.repository.SQLCommunicator#getProgramParameterTypeId(java.lang
	 * .String)
	 */
	@Override
	protected int getProgramParameterTypeId(String typeName)
			throws SQLException {
		Statement stmt = conn.createStatement();
		try {
			ResultSet rs = stmt.executeQuery("SELECT `id` FROM `"
					+ this.getDatabase() + "`.`"
					+ this.getTableProgramParameterType() + "` WHERE `name`='"
					+ typeName + "';");
			rs.first();
			return rs.getInt("id");
		} finally {
			stmt.close();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see framework.repository.SQLCommunicator#unregister(program.
	 * ProgramParameter)
	 */
	@Override
	protected boolean unregister(ProgramParameter<?> programParameter) {
		try {
			this.delete(this.getTableProgramParameter(),
					this.getProgramParameterId(programParameter));
			return true;
		} catch (SQLException e) {
		}
		return false;
	}

	@Override
	protected int getDataSetFormatId(final String dataSetFormatClassSimpleName)
			throws SQLException {
		Statement stmt = conn.createStatement();
		try {
			ResultSet rs = stmt.executeQuery("SELECT `id` FROM `"
					+ this.getDatabase() + "`.`"
					+ this.getTableDataSetFormats()
					+ "` WHERE `repository_id`='" + this.updateRepositoryId()
					+ "' AND `name`='" + dataSetFormatClassSimpleName + "';");
			rs.first();
			return rs.getInt("id");
		} finally {
			stmt.close();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableDataSetFormats()
	 */
	@Override
	protected String getTableDataSetTypes() {
		return "dataset_types";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see framework.repository.SQLCommunicator#unregister(run.Run)
	 */
	@Override
	protected boolean unregister(Run object) {
		Statement stmt = null;
		try {
			stmt = conn.createStatement();

			stmt.execute("DELETE FROM `" + this.getDatabase() + "`.`"
					+ this.getTableRuns() + "` WHERE `absPath`='"
					+ object.getAbsolutePath() + "';");
			stmt.close();
			return true;
		} catch (SQLException e) {

			e.printStackTrace();
		}
		try {
			if (stmt != null)
				stmt.close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.clusteval.framework.repository.SQLCommunicator#unregister(de.clusteval
	 * .run.result.ParameterOptimizationResult)
	 */
	@Override
	protected boolean unregister(ParameterOptimizationResult object) {
		Statement stmt = null;
		try {
			stmt = conn.createStatement();

			stmt.execute("DELETE FROM `"
					+ this.getDatabase()
					+ "`.`"
					+ this.getTableRunResultsParameterOptimization()
					+ "` WHERE `repository_id`='"
					+ this.getRepositoryId(this.repository.getParent()
							.getBasePath()) + "' AND  `absPath`='"
					+ object.getAbsolutePath() + "';");
			stmt.close();
			return true;
		} catch (SQLException e) {

			e.printStackTrace();
		}
		try {
			if (stmt != null)
				stmt.close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * framework.repository.SQLCommunicator#unregister(run.result.RunResult)
	 */
	@Override
	protected boolean unregister(RunResult object) {

		Statement stmt = null;
		try {
			stmt = conn.createStatement();

			stmt.execute("DELETE FROM `"
					+ this.getDatabase()
					+ "`.`"
					+ this.getTableRunResults()
					+ "` WHERE `repository_id`='"
					+ this.getRepositoryId(this.repository.getParent()
							.getBasePath()) + "' AND  `uniqueRunIdentifier`='"
					+ object.getIdentifier() + "';");
			// remove the repository for the runresult
			if (object instanceof ParameterOptimizationResult) {
				try {
					stmt.execute("DELETE FROM `"
							+ this.getDatabase()
							+ "`.`"
							+ this.getTableRepositories()
							+ "` WHERE `id`='"
							+ this.getRepositoryId(object.getRepository()
									.getBasePath()) + "';");
				} catch (SQLException e) {
					// might have been deleted by another parameter optimization
					// run result contained in that folder
				}
			}
			stmt.close();
			return true;
		} catch (SQLException e) {

			e.printStackTrace();
		}
		try {
			if (stmt != null)
				stmt.close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see framework.repository.SQLCommunicator#
	 * unregisterParameterOptimizationMethodClass(java.lang.Class)
	 */
	@Override
	protected boolean unregisterParameterOptimizationMethodClass(
			Class<? extends ParameterOptimizationMethod> object) {
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			stmt.execute("DELETE FROM `" + this.getDatabase() + "`.`"
					+ this.getTableParameterOptimizationMethods()
					+ "` WHERE `repository_id`='" + this.updateRepositoryId()
					+ "' AND `name`='" + object.getSimpleName() + "';");
			stmt.close();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			if (stmt != null)
				stmt.close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * framework.repository.SQLCommunicator#unregisterClusteringQualityMeasureClass
	 * (java.lang.Class)
	 */
	@Override
	protected boolean unregisterClusteringQualityMeasureClass(
			Class<? extends QualityMeasure> object) {
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			stmt.execute("DELETE FROM `" + this.getDatabase() + "`.`"
					+ this.getTableClusteringQualityMeasures()
					+ "` WHERE `repository_id`='" + this.updateRepositoryId()
					+ "' AND `name`='" + object.getSimpleName() + "';");
			stmt.close();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			if (stmt != null)
				stmt.close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * framework.repository.SQLCommunicator#unregisterDataStatisticClass(java
	 * .lang.Class)
	 */
	@Override
	protected boolean unregisterDataStatisticClass(
			Class<? extends DataStatistic> object) {
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			int statistic_id = getStatisticId(object.getSimpleName());
			stmt.execute("DELETE FROM `" + this.getDatabase() + "`.`"
					+ this.getTableStatisticsData()
					+ "` WHERE `repository_id`='" + this.updateRepositoryId()
					+ "' AND `statistic_id` = '" + statistic_id + "';");
			stmt.close();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			if (stmt != null)
				stmt.close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * framework.repository.SQLCommunicator#unregisterRunStatisticClass(java
	 * .lang.Class)
	 */
	@Override
	protected boolean unregisterRunStatisticClass(
			Class<? extends RunStatistic> object) {
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			int statistic_id = getStatisticId(object.getSimpleName());
			stmt.execute("DELETE FROM `" + this.getDatabase() + "`.`"
					+ this.getTableStatisticsRun()
					+ "` WHERE `repository_id`='" + this.updateRepositoryId()
					+ "' AND `statistic_id`='" + statistic_id + "';");
			stmt.close();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			if (stmt != null)
				stmt.close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * framework.repository.SQLCommunicator#unregisterRunDataStatisticClass(
	 * java.lang.Class)
	 */
	@Override
	protected boolean unregisterRunDataStatisticClass(
			Class<? extends RunDataStatistic> object) {
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			int statistic_id = getStatisticId(object.getSimpleName());
			stmt.execute("DELETE FROM `" + this.getDatabase() + "`.`"
					+ this.getTableStatisticsRunData()
					+ "` WHERE `repository_id`='" + this.updateRepositoryId()
					+ "' AND `statistic_id`='" + statistic_id + "';");
			stmt.close();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			if (stmt != null)
				stmt.close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * framework.repository.SQLCommunicator#unregisterDataSetTypeClass(java.
	 * lang.Class)
	 */
	@Override
	protected boolean unregisterDataSetTypeClass(
			Class<? extends DataSetType> object) {
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			stmt.execute("DELETE FROM `" + this.getDatabase() + "`.`"
					+ this.getTableDataSetTypes() + "` WHERE `repository_id`='"
					+ this.updateRepositoryId() + "' AND  `name`='"
					+ object.getSimpleName() + "';");
			stmt.close();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			if (stmt != null)
				stmt.close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.clusteval.framework.repository.SQLCommunicator#
	 * getTableRunResultsClusteringsQuality()
	 */
	@Override
	protected String getTableRunResultsClusteringsQuality() {
		return "run_results_clustering_qualities";
	}

	@SuppressWarnings("unused")
	@Override
	protected boolean registerContextClass(Class<? extends Context> object) {
		// TODO think about how to integrate contexts into the website &
		// database
		return false;
	}

	@SuppressWarnings("unused")
	@Override
	protected boolean unregisterContextClass(Class<? extends Context> object) {
		// TODO think about how to integrate contexts into the website &
		// database
		return false;
	}
}
