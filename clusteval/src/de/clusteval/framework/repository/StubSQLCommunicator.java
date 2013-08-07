/**
 * 
 */
package de.clusteval.framework.repository;

import java.sql.SQLException;

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
import de.clusteval.program.DoubleProgramParameter;
import de.clusteval.program.IntegerProgramParameter;
import de.clusteval.program.Program;
import de.clusteval.program.ProgramConfig;
import de.clusteval.program.ProgramParameter;
import de.clusteval.program.StringProgramParameter;
import de.clusteval.quality.QualityMeasure;
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
 * @author Christian Wiwie
 * 
 */
@SuppressWarnings({"unused", "rawtypes"})
public class StubSQLCommunicator extends SQLCommunicator {

	/**
	 * @param repository
	 */
	public StubSQLCommunicator(Repository repository) {
		super(repository);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see framework.repository.SQLCommunicator#initDB()
	 */
	@Override
	public void initDB() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getServer()
	 */
	@Override
	protected String getServer() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getDatabase()
	 */
	@Override
	protected String getDatabase() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getDBUsername()
	 */
	@Override
	protected String getDBUsername() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getDBPassword()
	 */
	@Override
	protected String getDBPassword() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableRepositories()
	 */
	@Override
	protected String getTableRepositories() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableClusteringQualityMeasures()
	 */
	@Override
	protected String getTableClusteringQualityMeasures() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableDataConfigs()
	 */
	@Override
	protected String getTableDataConfigs() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableDataSetConfigs()
	 */
	@Override
	protected String getTableDataSetConfigs() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableDataSetFormats()
	 */
	@Override
	protected String getTableDataSetFormats() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableDatasets()
	 */
	@Override
	protected String getTableDatasets() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableGoldStandardConfigs()
	 */
	@Override
	protected String getTableGoldStandardConfigs() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableOptimizableProgramParameters()
	 */
	@Override
	protected String getTableOptimizableProgramParameters() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableParameterOptimizationMethods()
	 */
	@Override
	protected String getTableParameterOptimizationMethods() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableProgramConfigs()
	 */
	@Override
	protected String getTableProgramConfigs() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableProgramParameter()
	 */
	@Override
	protected String getTableProgramParameter() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTablePrograms()
	 */
	@Override
	protected String getTablePrograms() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableProgramsCompatibleDataSetFormats()
	 */
	@Override
	protected String getTableProgramConfigsCompatibleDataSetFormats() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableRunsAnalysis()
	 */
	@Override
	protected String getTableRunsAnalysis() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableRunsAnalysisData()
	 */
	@Override
	protected String getTableRunsAnalysisData() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableRunsAnalysisDataDataIdentifiers()
	 */
	@Override
	protected String getTableRunsAnalysisDataDataIdentifiers() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableRunsAnalysisRun()
	 */
	@Override
	protected String getTableRunsAnalysisRun() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableRunsAnalysisRunRunIdentifiers()
	 */
	@Override
	protected String getTableRunsAnalysisRunRunIdentifiers() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableRunsAnalysisRunData()
	 */
	@Override
	protected String getTableRunsAnalysisRunData() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableRunsAnalysisStatistics()
	 */
	@Override
	protected String getTableRunsAnalysisStatistics() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableRunsClustering()
	 */
	@Override
	protected String getTableRunsClustering() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableRunsExecution()
	 */
	@Override
	protected String getTableRunsExecution() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableRunsExecutionDataConfigs()
	 */
	@Override
	protected String getTableRunsExecutionDataConfigs() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableRunsExecutionParameterValues()
	 */
	@Override
	protected String getTableRunsExecutionParameterValues() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableRunsExecutionProgramConfigs()
	 */
	@Override
	protected String getTableRunsExecutionProgramConfigs() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableRunsExecutionQualityMeasures()
	 */
	@Override
	protected String getTableRunsExecutionQualityMeasures() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableRunsInternalParameterOptimization()
	 */
	@Override
	protected String getTableRunsInternalParameterOptimization() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableRunsParameterOptimization()
	 */
	@Override
	protected String getTableRunsParameterOptimization() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableRunsParameterOptimizationMethods()
	 */
	@Override
	protected String getTableRunsParameterOptimizationMethods() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableRunsParameterOptimizationParameters()
	 */
	@Override
	protected String getTableRunsParameterOptimizationParameters() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableRunResultsExecution()
	 */
	@Override
	protected String getTableRunResultsExecution() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableRunResultFormats()
	 */
	@Override
	protected String getTableRunResultFormats() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableRunResultsParameterOptimization()
	 */
	@Override
	protected String getTableRunResultsParameterOptimization() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableRunResults()
	 */
	@Override
	protected String getTableRunResults() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableRunResultsDataAnalysis()
	 */
	@Override
	protected String getTableRunResultsDataAnalysis() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableRunResultsRunAnalysis()
	 */
	@Override
	protected String getTableRunResultsRunAnalysis() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableRuns()
	 */
	@Override
	protected String getTableRuns() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableRunTypes()
	 */
	@Override
	protected String getTableRunTypes() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableStatistics()
	 */
	@Override
	protected String getTableStatistics() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableStatisticsData()
	 */
	@Override
	protected String getTableStatisticsData() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableStatisticsRun()
	 */
	@Override
	protected String getTableStatisticsRun() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableStatisticsRunData()
	 */
	@Override
	protected String getTableStatisticsRunData() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#register(run.config.Run)
	 */
	@Override
	protected boolean register(Run run, final boolean updateOnly) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#register(program.ProgramConfig)
	 */
	@Override
	protected boolean register(ProgramConfig object, final boolean updateOnly) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#register(program.Program)
	 */
	@Override
	protected boolean register(Program object, final boolean updateOnly) {
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
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#register(data.goldstandard.GoldStandard)
	 */
	@Override
	protected boolean register(GoldStandard object, final boolean updateOnly) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#register(program.DoubleProgramParameter)
	 */
	@Override
	protected boolean register(DoubleProgramParameter object) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#register(program.IntegerProgramParameter)
	 */
	@Override
	protected boolean register(IntegerProgramParameter object) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#register(program.StringProgramParameter)
	 */
	@Override
	protected boolean register(StringProgramParameter object) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#register(data.dataset.DataSet)
	 */
	@Override
	protected boolean register(DataSet object, final boolean updateOnly) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#register(data.DataConfig)
	 */
	@Override
	protected boolean register(DataConfig object, final boolean updateOnly) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#register(data.dataset.DataSetConfig)
	 */
	@Override
	protected boolean register(DataSetConfig object, final boolean updateOnly) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#unregisterRunResultFormat(java.lang.Class)
	 */
	@Override
	protected boolean unregisterRunResultFormat(
			Class<? extends RunResultFormat> object) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#unregister(program.ProgramConfig)
	 */
	@Override
	protected boolean unregister(ProgramConfig object) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#unregister(program.Program)
	 */
	@Override
	protected boolean unregister(Program object) {
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
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#unregister(data.goldstandard.GoldStandard)
	 */
	@Override
	protected boolean unregister(GoldStandard object) {
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
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#unregister(data.dataset.DataSet)
	 */
	@Override
	protected boolean unregister(DataSet object) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#unregister(data.DataConfig)
	 */
	@Override
	protected boolean unregister(DataConfig object) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#unregister(data.dataset.DataSetConfig)
	 */
	@Override
	protected boolean unregister(DataSetConfig object) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * utils.SQLCommunicator#getClusteringQualityMeasureId(java.lang.String)
	 */
	@Override
	protected int getClusteringQualityMeasureId(String name)
			throws SQLException {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getDataConfigId(java.lang.String)
	 */
	@Override
	protected int getDataConfigId(DataConfig dataConfig) throws SQLException {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getDataSetConfigId(java.lang.String)
	 */
	@Override
	protected int getDataSetConfigId(final DataSetConfig dataSetConfig)
			throws SQLException {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getDataSetFormatId(java.lang.String)
	 */
	@Override
	protected int getDataSetFormatId(String dataSetFormatClassSimpleName)
			throws SQLException {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getDataSetId(java.lang.String)
	 */
	@Override
	protected int getDataSetId(final DataSet dataSet) throws SQLException {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getGoldStandardConfigId(java.lang.String)
	 */
	@Override
	protected int getGoldStandardConfigId(
			final GoldStandardConfig goldStandardConfig) throws SQLException {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getGoldStandardId(java.lang.String)
	 */
	@Override
	protected int getGoldStandardId(final GoldStandard goldStandard)
			throws SQLException {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * utils.SQLCommunicator#getParameterOptimizationMethodId(java.lang.String)
	 */
	@Override
	protected int getParameterOptimizationMethodId(String name)
			throws SQLException {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getProgramConfigId(java.lang.String)
	 */
	@Override
	protected int getProgramConfigId(final ProgramConfig programConfig)
			throws SQLException {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getProgramId(java.lang.String)
	 */
	@Override
	protected int getProgramId(String name) throws SQLException {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getRunAnalysisDataId(int)
	 */
	@Override
	protected int getRunAnalysisDataId(int runAnalysisId) throws SQLException {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getRunAnalysisId(int)
	 */
	@Override
	protected int getRunAnalysisId(int runId) throws SQLException {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getRunAnalysisRunId(int)
	 */
	@Override
	protected int getRunAnalysisRunId(int runAnalysisId) throws SQLException {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getRunExecutionId(int)
	 */
	@Override
	protected int getRunExecutionId(int runId) throws SQLException {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getRunParameterOptimizationId(int)
	 */
	@Override
	protected int getRunParameterOptimizationId(int runExecutionId)
			throws SQLException {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getRunResultExecutionId(int)
	 */
	@Override
	protected int getRunResultExecutionId(int runResultId) throws SQLException {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getRunResultFormatId(java.lang.String)
	 */
	@Override
	protected int getRunResultFormatId(String runResultFormatSimpleName)
			throws SQLException {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getRunResultId(java.lang.String)
	 */
	@Override
	protected int getRunResultId(String uniqueRunIdentifier)
			throws SQLException {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getRunTypeId(java.lang.String)
	 */
	@Override
	protected int getRunTypeId(String name) throws SQLException {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getStatisticId(java.lang.String)
	 */
	@Override
	protected int getStatisticId(String statisticsName) throws SQLException {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#commitDB()
	 */
	@Override
	public void commitDB() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#updateStatusOfRun(run.Run, java.lang.String)
	 */
	@Override
	public boolean updateStatusOfRun(Run run, String runStatus) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#register(run.result.RunResult)
	 */
	@Override
	public boolean register(RunResult object) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.SQLCommunicator#getTableRepositoryTypes()
	 */
	@Override
	protected String getTableRepositoryTypes() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see framework.repository.SQLCommunicator#register(run.result.
	 * ParameterOptimizationResult)
	 */
	@Override
	public boolean register(ParameterOptimizationResult object) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see framework.repository.SQLCommunicator#register(run.result.
	 * DataAnalysisRunResult)
	 */
	@Override
	public boolean register(DataAnalysisRunResult object) {
		return false;
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
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see framework.repository.SQLCommunicator#getTableRunResultsClustering()
	 */
	@Override
	protected String getTableRunResultsClustering() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * framework.repository.SQLCommunicator#register(run.result.ClusteringRunResult
	 * )
	 */
	@Override
	public boolean register(GraphMatchingRunResult object) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see framework.repository.SQLCommunicator#getTableRunResultsAnalysis()
	 */
	@Override
	protected String getTableRunResultsAnalysis() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see framework.repository.SQLCommunicator#getRunResultAnalysisId(int)
	 */
	@Override
	protected int getRunResultAnalysisId(int runResultId) throws SQLException {
		return 0;
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
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * framework.repository.SQLCommunicator#getTableRunResultsRunDataAnalysis()
	 */
	@Override
	protected String getTableRunResultsRunDataAnalysis() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see framework.repository.SQLCommunicator#getRunResultRunAnalysisId(int)
	 */
	@Override
	protected int getRunResultRunAnalysisId(int runResultAnalysisId)
			throws SQLException {
		return 0;
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
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see framework.repository.SQLCommunicator#getRunAnalysisRunDataId(int)
	 */
	@Override
	protected int getRunAnalysisRunDataId(int runAnalysisRunId)
			throws SQLException {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see framework.repository.SQLCommunicator#register(run.config.
	 * DataAnalysisRun)
	 */
	@Override
	protected boolean register(DataAnalysisRun run, final boolean updateOnly) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * framework.repository.SQLCommunicator#register(run.config.RunAnalysisRun )
	 */
	@Override
	protected boolean register(RunAnalysisRun run, final boolean updateOnly) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see framework.repository.SQLCommunicator#register(run.config.
	 * RunDataAnalysisRun)
	 */
	@Override
	protected boolean register(RunDataAnalysisRun run, final boolean updateOnly) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * framework.repository.SQLCommunicator#register(run.config.ExecutionRun )
	 */
	@Override
	protected boolean register(ExecutionRun run, final boolean updateOnly) {
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
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see framework.repository.SQLCommunicator#register(run.config.
	 * ParameterOptimizationRun)
	 */
	@Override
	protected boolean register(ParameterOptimizationRun run,
			final boolean updateOnly) {
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
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see framework.repository.SQLCommunicator#getTableProgramParameterType()
	 */
	@Override
	protected String getTableProgramParameterType() {
		return null;
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
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see framework.repository.SQLCommunicator#getTableParameterSets()
	 */
	@Override
	protected String getTableParameterSets() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * framework.repository.SQLCommunicator#getTableParameterSetParameters()
	 */
	@Override
	protected String getTableParameterSetParameterValues() {
		return null;
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
		return null;
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
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * framework.repository.SQLCommunicator#getParameterSetParameterValuesId
	 * (int, int, int)
	 */
	@Override
	protected int getParameterSetParameterValuesId(int parameterSetId,
			int parameterId, int iteration) throws SQLException {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * framework.repository.SQLCommunicator#getRepositoryId(java.lang.String)
	 */
	@Override
	protected int getRepositoryId(String absPath) throws SQLException {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * framework.repository.SQLCommunicator#getRunResultParameterOptimizationId
	 * (java.lang.String)
	 */
	@Override
	protected int getRunResultParameterOptimizationId(String absPath)
			throws SQLException {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * framework.repository.SQLCommunicator#getTableParameterSetParameters()
	 */
	@Override
	protected String getTableParameterSetParameters() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see framework.repository.SQLCommunicator#getParameterSetParameterId(int,
	 * int)
	 */
	@Override
	protected int getParameterSetParameterId(int parameterSetId, int parameterId)
			throws SQLException {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see framework.repository.SQLCommunicator#getProgramParameterId(program.
	 * ProgramParameter)
	 */
	@Override
	protected int getProgramParameterId(ProgramParameter<?> programParameterName)
			throws SQLException {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see framework.repository.SQLCommunicator#
	 * getTableRunsParameterOptimizationQualityMeasures()
	 */
	@Override
	protected String getTableRunsParameterOptimizationQualityMeasures() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see framework.repository.SQLCommunicator#getRunId(run.Run)
	 */
	@Override
	protected int getRunId(Run run) throws SQLException {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * framework.repository.SQLCommunicator#getTableParameterSetIterations()
	 */
	@Override
	protected String getTableParameterSetIterations() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see framework.repository.SQLCommunicator#unregister(program.
	 * DoubleProgramParameter)
	 */
	@Override
	protected boolean unregister(ProgramParameter<?> programParameter) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see framework.repository.SQLCommunicator#getTableClusterings()
	 */
	@Override
	protected String getTableClusterings() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see framework.repository.SQLCommunicator#getTableClusters()
	 */
	@Override
	protected String getTableClusters() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see framework.repository.SQLCommunicator#getTableClusterObjects()
	 */
	@Override
	protected String getTableClusterObjects() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * framework.repository.SQLCommunicator#getClusteringId(java.lang.String)
	 */
	@Override
	protected int getClusteringId(String name) throws SQLException {
		return 0;
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
		return 0;
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
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see framework.repository.SQLCommunicator#getTableGoldStandards()
	 */
	@Override
	protected String getTableGoldStandards() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see framework.repository.SQLCommunicator#getTableDataSetTypes()
	 */
	@Override
	protected String getTableDataSetTypes() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * framework.repository.SQLCommunicator#getDataSetTypeId(java.lang.String)
	 */
	@Override
	protected int getDataSetTypeId(String dataSetTypeClassSimpleName)
			throws SQLException {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see framework.repository.SQLCommunicator#unregister(run.Run)
	 */
	@Override
	protected boolean unregister(Run object) {
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
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see framework.repository.SQLCommunicator#register(run.AnalysisRun,
	 * boolean)
	 */
	@Override
	protected boolean register(AnalysisRun<Statistic> run, boolean updateOnly) {
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
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * framework.repository.SQLCommunicator#registerDataSetFormatClass(java.
	 * lang.Class)
	 */
	@Override
	protected boolean registerDataSetFormatClass(
			Class<? extends DataSetFormat> object) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * framework.repository.SQLCommunicator#registerParameterOptimizationMethodClass
	 * (java.lang.Class)
	 */
	@Override
	protected boolean registerParameterOptimizationMethodClass(
			Class<? extends ParameterOptimizationMethod> paramOptMethod) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * framework.repository.SQLCommunicator#registerClusteringQualityMeasureClass
	 * (java.lang.Class)
	 */
	@Override
	protected boolean registerClusteringQualityMeasureClass(
			Class<? extends QualityMeasure> clusteringQualityMeasure) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * framework.repository.SQLCommunicator#registerDataStatisticClass(java.
	 * lang.Class)
	 */
	@Override
	protected boolean registerDataStatisticClass(
			Class<? extends DataStatistic> dataStatistic) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * framework.repository.SQLCommunicator#registerRunStatisticClass(java.lang
	 * .Class)
	 */
	@Override
	protected boolean registerRunStatisticClass(
			Class<? extends RunStatistic> runStatistic) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * framework.repository.SQLCommunicator#registerRunDataStatisticClass(java
	 * .lang.Class)
	 */
	@Override
	protected boolean registerRunDataStatisticClass(
			Class<? extends RunDataStatistic> runDataStatistic) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * framework.repository.SQLCommunicator#registerRunResultFormatClass(java
	 * .lang.Class)
	 */
	@Override
	protected boolean registerRunResultFormatClass(
			Class<? extends RunResultFormat> runResultFormat) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * framework.repository.SQLCommunicator#registerDataSetTypeClass(java.lang
	 * .Class)
	 */
	@Override
	protected boolean registerDataSetTypeClass(
			Class<? extends DataSetType> object) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see framework.repository.SQLCommunicator#
	 * getTableRunsAnalysisRunDataRunIdentifiers()
	 */
	@Override
	protected String getTableRunsAnalysisRunDataRunIdentifiers() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.clusteval.framework.repository.SQLCommunicator#
	 * getTableRunResultsClusteringsQuality()
	 */
	@Override
	protected String getTableRunResultsClusteringsQuality() {
		return null;
	}

	@Override
	protected boolean registerContextClass(Class<? extends Context> object) {
		return false;
	}

	@Override
	protected boolean unregisterContextClass(Class<? extends Context> object) {
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
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.clusteval.framework.repository.SQLCommunicator#getRepositoryTypeId
	 * (java.lang.String)
	 */
	@Override
	protected int getRepositoryTypeId(String repositoryType)
			throws SQLException {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.clusteval.framework.repository.SQLCommunicator#
	 * getTableDatasetConfigsDataSets()
	 */
	@Override
	protected String getTableDatasetConfigsDataSets() {
		return null;
	}
}
