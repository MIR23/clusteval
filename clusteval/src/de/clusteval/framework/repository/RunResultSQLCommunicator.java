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
package de.clusteval.framework.repository;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import de.clusteval.data.DataConfig;
import de.clusteval.data.dataset.DataSet;
import de.clusteval.data.dataset.DataSetConfig;
import de.clusteval.data.goldstandard.GoldStandard;
import de.clusteval.data.goldstandard.GoldStandardConfig;
import de.clusteval.framework.repository.config.MysqlConfig;
import de.clusteval.program.ProgramConfig;
import de.clusteval.program.ProgramParameter;
import de.clusteval.run.Run;

/**
 * @author Christian Wiwie
 * 
 */
public class RunResultSQLCommunicator extends DefaultSQLCommunicator {

	/**
	 * @param repository
	 * @param mysqlConfig
	 */
	public RunResultSQLCommunicator(Repository repository,
			final MysqlConfig mysqlConfig) {
		super(repository, mysqlConfig);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * utils.DefaultSQLCommunicator#getClusteringQualityMeasureId(java.lang.
	 * String)
	 */
	@Override
	protected int getClusteringQualityMeasureId(String name)
			throws SQLException {
		return repository.getParent().getSqlCommunicator()
				.getClusteringQualityMeasureId(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * utils.DefaultSQLCommunicator#getParameterOptimizationMethodId(java.lang
	 * .String)
	 */
	@Override
	protected int getParameterOptimizationMethodId(String name)
			throws SQLException {
		return repository.getParent().getSqlCommunicator()
				.getParameterOptimizationMethodId(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.DefaultSQLCommunicator#getDataSetFormatId(java.lang.String)
	 */
	@Override
	protected int getDataSetFormatId(String dataSetFormatClassSimpleName)
			throws SQLException {
		return this.repository.getParent().getSqlCommunicator()
				.getDataSetFormatId(dataSetFormatClassSimpleName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.DefaultSQLCommunicator#getDataConfigId(java.lang.String)
	 */
	@Override
	protected int getDataConfigId(DataConfig dataConfig) throws SQLException {
		try {
			return super.getDataConfigId(dataConfig);
		} catch (SQLException e) {
			return this.repository.getParent().getSqlCommunicator()
					.getDataConfigId(dataConfig);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.DefaultSQLCommunicator#getDataSetConfigId(java.lang.String)
	 */
	@Override
	protected int getDataSetConfigId(final DataSetConfig dataSetConfig)
			throws SQLException {
		try {
			return super.getDataSetConfigId(dataSetConfig);
		} catch (SQLException e) {
			return this.repository.getParent().getSqlCommunicator()
					.getDataSetConfigId(dataSetConfig);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.DefaultSQLCommunicator#getDataSetId(java.lang.String)
	 */
	@Override
	protected int getDataSetId(final DataSet dataSet) throws SQLException {
		try {
			return super.getDataSetId(dataSet);
		} catch (SQLException e) {
			return this.repository.getParent().getSqlCommunicator()
					.getDataSetId(dataSet);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * utils.DefaultSQLCommunicator#getGoldStandardConfigId(java.lang.String)
	 */
	@Override
	protected int getGoldStandardConfigId(
			final GoldStandardConfig goldStandardConfig) throws SQLException {
		try {
			return super.getGoldStandardConfigId(goldStandardConfig);
		} catch (SQLException e) {
			return this.repository.getParent().getSqlCommunicator()
					.getGoldStandardConfigId(goldStandardConfig);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.DefaultSQLCommunicator#getGoldStandardId(java.lang.String)
	 */
	@Override
	protected int getGoldStandardId(final GoldStandard goldStandard)
			throws SQLException {
		try {
			return super.getGoldStandardId(goldStandard);
		} catch (SQLException e) {
			return this.repository.getParent().getSqlCommunicator()
					.getGoldStandardId(goldStandard);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.DefaultSQLCommunicator#getProgramConfigId(java.lang.String)
	 */
	@Override
	protected int getProgramConfigId(final ProgramConfig programConfig)
			throws SQLException {
		try {
			return super.getProgramConfigId(programConfig);
		} catch (SQLException e) {
			return this.repository.getParent().getSqlCommunicator()
					.getProgramConfigId(programConfig);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see framework.repository.SQLCommunicator#initDB()
	 */
	@Override
	public void initDB() {
		try {
			if (conn == null) {
				conn = DriverManager.getConnection("jdbc:mysql://"
						+ getServer() + "/" + getDatabase() + "?",
						getDBUsername(), getDBPassword());
				conn.setAutoCommit(false);
			}
			Statement stmt = conn.createStatement();

			ResultSet rs = stmt.executeQuery("SELECT `id` FROM `"
					+ this.getDatabase() + "`.`" + this.getTableRepositories()
					+ "` WHERE `basePath`='"
					+ this.repository.getParent().getBasePath() + "';");
			rs.first();
			int parent_repository_id = rs.getInt("id");

			String repositoryType = this.repository.getClass().getSimpleName();
			// Get repository_type_id
			rs = stmt.executeQuery("SELECT `id` FROM `" + this.getDatabase()
					+ "`.`" + this.getTableRepositoryTypes()
					+ "` WHERE `name`='" + repositoryType + "';");
			rs.first();
			int repository_type_id = rs.getInt("id");

			try {
				// Get repositoryId
				rs = stmt.executeQuery("SELECT `id` FROM `"
						+ this.getDatabase() + "`.`"
						+ this.getTableRepositories() + "` WHERE `basePath`='"
						+ this.repository.getBasePath() + "';");
				if (!rs.last() || rs.getRow() == 0) {
					stmt.execute("INSERT INTO `"
							+ this.getDatabase()
							+ "`.`"
							+ this.getTableRepositories()
							+ "` (`basePath`,`repository_type_id`,`repository_id`) VALUES ('"
							+ this.repository.getBasePath() + "','"
							+ repository_type_id + "','" + parent_repository_id
							+ "');");
				}

				rs = stmt.executeQuery("SELECT `id` FROM `"
						+ this.getDatabase() + "`.`"
						+ this.getTableRepositories() + "` WHERE `basePath`='"
						+ this.repository.getBasePath() + "';");

				rs.first();
				this.setRepositoryId(rs.getInt("id"));
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
			conn.commit();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.DefaultSQLCommunicator#register(program.ProgramConfig)
	 */
	@Override
	protected boolean register(ProgramConfig object, final boolean updateOnly) {
		return super.register(object, updateOnly);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.DefaultSQLCommunicator#getProgramId(java.lang.String)
	 */
	@Override
	protected int getProgramId(String name) throws SQLException {
		return this.repository.getParent().getSqlCommunicator()
				.getProgramId(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.DefaultSQLCommunicator#getProgramParameterId(int,
	 * java.lang.String)
	 */
	@Override
	protected int getProgramParameterId(
			final ProgramParameter<?> programParameter) throws SQLException {
		try {
			return super.getProgramParameterId(programParameter);
		} catch (SQLException e) {
			return this.repository.getParent().getSqlCommunicator()
					.getProgramParameterId(programParameter);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.DefaultSQLCommunicator#getRunAnalysisDataId(int)
	 */
	@Override
	protected int getRunAnalysisDataId(int runAnalysisId) throws SQLException {
		try {
			return super.getRunAnalysisDataId(runAnalysisId);
		} catch (SQLException e) {
			return this.repository.getParent().getSqlCommunicator()
					.getRunAnalysisDataId(runAnalysisId);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.DefaultSQLCommunicator#getRunAnalysisId(int)
	 */
	@Override
	protected int getRunAnalysisId(int runId) throws SQLException {
		try {
			return super.getRunAnalysisId(runId);
		} catch (SQLException e) {
			return this.repository.getParent().getSqlCommunicator()
					.getRunAnalysisId(runId);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.DefaultSQLCommunicator#getRunAnalysisRunId(int)
	 */
	@Override
	protected int getRunAnalysisRunId(int runAnalysisId) throws SQLException {
		try {
			return super.getRunAnalysisRunId(runAnalysisId);
		} catch (SQLException e) {
			return this.repository.getParent().getSqlCommunicator()
					.getRunAnalysisRunId(runAnalysisId);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.DefaultSQLCommunicator#getRunExecutionId(int)
	 */
	@Override
	protected int getRunExecutionId(int runId) throws SQLException {
		try {
			return super.getRunExecutionId(runId);
		} catch (SQLException e) {
			return this.repository.getParent().getSqlCommunicator()
					.getRunExecutionId(runId);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.DefaultSQLCommunicator#getRunId(java.lang.String)
	 */
	@Override
	protected int getRunId(final Run run) throws SQLException {
		try {
			return super.getRunId(run);
		} catch (SQLException e) {
			return this.repository.getParent().getSqlCommunicator()
					.getRunId(run);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.DefaultSQLCommunicator#getRunParameterOptimizationId(int)
	 */
	@Override
	protected int getRunParameterOptimizationId(int runExecutionId)
			throws SQLException {
		try {
			return super.getRunParameterOptimizationId(runExecutionId);
		} catch (SQLException e) {
			return this.repository.getParent().getSqlCommunicator()
					.getRunParameterOptimizationId(runExecutionId);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.DefaultSQLCommunicator#getRunResultExecutionId(int)
	 */
	@Override
	protected int getRunResultExecutionId(int runResultId) throws SQLException {
		return this.repository.getParent().getSqlCommunicator()
				.getRunResultExecutionId(runResultId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.DefaultSQLCommunicator#getRunResultFormatId(java.lang.String)
	 */
	@Override
	protected int getRunResultFormatId(String runResultFormatSimpleName)
			throws SQLException {
		return this.repository.getParent().getSqlCommunicator()
				.getRunResultFormatId(runResultFormatSimpleName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.DefaultSQLCommunicator#getRunResultId(java.lang.String)
	 */
	@Override
	protected int getRunResultId(String uniqueRunIdentifier)
			throws SQLException {
		return this.repository.getParent().getSqlCommunicator()
				.getRunResultId(uniqueRunIdentifier);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.DefaultSQLCommunicator#getRunTypeId(java.lang.String)
	 */
	@Override
	protected int getRunTypeId(String name) throws SQLException {
		return this.repository.getParent().getSqlCommunicator()
				.getRunTypeId(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.DefaultSQLCommunicator#getStatisticId(java.lang.String)
	 */
	@Override
	protected int getStatisticId(String statisticsName) throws SQLException {
		return this.repository.getParent().getSqlCommunicator()
				.getStatisticId(statisticsName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.DefaultSQLCommunicator#getDataSetFormatId(java.lang.String)
	 */
	@Override
	protected int getDataSetTypeId(String dataSetTypeClassSimpleName)
			throws SQLException {
		return this.repository.getParent().getSqlCommunicator()
				.getDataSetTypeId(dataSetTypeClassSimpleName);
	}
}
