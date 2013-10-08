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
package de.clusteval.serverclient;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.cli.Options;

import utils.Pair;
import ch.qos.logback.classic.Level;
import de.clusteval.run.RUN_STATUS;

/**
 * An interface for the backend server. This interface contains all command a
 * server can take from a client, e.g. starting, stopping, resuming of runs or
 * shutting down the server.
 * 
 * @author Christian Wiwie
 */
public interface IBackendServer extends Remote {

	/**
	 * 
	 * @return A collection with the names of all runs contained in the
	 *         repository of this server.
	 * @throws RemoteException
	 */
	public Collection<String> getRuns() throws RemoteException;

	/**
	 * @return A collection with the names of all run result directories
	 *         contained in the repository of this server. Those run result
	 *         directories can be resumed, if they were terminated before.
	 * @throws RemoteException
	 */
	public Collection<String> getRunResumes() throws RemoteException;

	/**
	 * @return A collection with the names of all programs contained in the
	 *         repository of this server.
	 * @throws RemoteException
	 */
	public Collection<String> getPrograms() throws RemoteException;

	/**
	 * 
	 * @return A collection with the names of all dataset generators registered
	 *         at the repository of this server.
	 * @throws RemoteException
	 */
	public Collection<String> getDataSetGenerators() throws RemoteException;

	/**
	 * 
	 * @param generatorName
	 *            The simple name of the class of the dataset generator.
	 * @return A wrapper objects keeping all the options of the specified
	 *         dataset generator.
	 * @throws RemoteException
	 */
	public Options getOptionsForDataSetGenerator(final String generatorName)
			throws RemoteException;

	/**
	 * @param generatorName
	 *            The simple name of the class of the dataset generator to use
	 *            to generate the new dataset.
	 * @param args
	 *            The arguments to pass on to the dataset generator.
	 * @return True, if the dataset (and goldstandard) has been generated
	 *         successfully.
	 * @throws RemoteException
	 */
	public boolean generateDataSet(final String generatorName,
			final String[] args) throws RemoteException;

	/**
	 * @return A collection with the names of all datasets contained in the
	 *         repository of this server.
	 * @throws RemoteException
	 *             the remote exception
	 */
	public Collection<String> getDataSets() throws RemoteException;

	/**
	 * @param uniqueRunIdentifier
	 *            The unique run identifier of a run result stored in the
	 *            corresponding directory of the repository.
	 * @return The run results for the given unique run identifier.
	 * @throws RemoteException
	 */
	public Map<Pair<String, String>, Map<String, Double>> getRunResults(
			final String uniqueRunIdentifier) throws RemoteException;

	/**
	 * @return A collection with the names of those run result directories
	 *         contained in the repository of this server, that contain a
	 *         clusters subfolder and at least one *.complete file containing
	 *         results (can be slow if many run result folders are present).
	 * @throws RemoteException
	 */
	public Collection<String> getRunResults() throws RemoteException;

	/**
	 * 
	 * @return A collection with the names of all runs and run results that are
	 *         currently enqueued but not yet running.
	 * @throws RemoteException
	 */
	public Collection<String> getQueue() throws RemoteException;

	/**
	 * This method tells the framework that a certain client wants to perform
	 * the run with the given name.
	 * 
	 * @param clientId
	 *            The id of the client, that wants to perform the run.
	 * @param runId
	 *            The name of the run that should be performed.
	 * @return true, if successful
	 * @throws RemoteException
	 */
	public boolean performRun(String clientId, String runId)
			throws RemoteException;

	/**
	 * This method tells the framework that a certain client wants to resume the
	 * run result with the given unique identifier.
	 * 
	 * @param clientId
	 *            The id of the client, that wants to perform the run.
	 * @param uniqueRunIdentifier
	 *            The unique identifier of the run result that should be
	 *            resumed.
	 * @return true, if successful
	 * @throws RemoteException
	 */
	public boolean resumeRun(final String clientId,
			final String uniqueRunIdentifier) throws RemoteException;

	/**
	 * This method tells the framework that a certain client wants to terminate
	 * the run with the given name.
	 * 
	 * <p>
	 * This operation is only allowed if the client id is the same, as the one
	 * that performed the run before.
	 * 
	 * @param clientId
	 *            The id of the client, that wants to perform the run.
	 * @param runId
	 *            The name of the run that should be terminated.
	 * @return true, if successful
	 * @throws RemoteException
	 */
	public boolean terminateRun(final String clientId, final String runId)
			throws RemoteException;

	/**
	 * This method tells the framework to shutdown. The framework will terminate
	 * the supervisor thread, which then in turn terminates all other threads he
	 * is oversees.
	 * 
	 * @param clientId
	 *            The id of the client, that wants to shutdown the framework.
	 * @param timeOut
	 *            The timeout how long the server will wait for threads until
	 *            forcing the shutdown.
	 * @throws RemoteException
	 */
	public void shutdown(final String clientId, final long timeOut)
			throws RemoteException;

	/**
	 * This is a factory method which returns the next free unused client id.
	 * Client ids are needed for the clients to communicate with the server and
	 * give certain commands.
	 * 
	 * @return The next free client id.
	 * @throws RemoteException
	 */
	public String getClientId() throws RemoteException;

	/**
	 * This method returns the status and percentage of any run performed by the
	 * client with the given id.
	 * 
	 * @param clientId
	 *            The client id for which this method returns the status of its
	 *            runs.
	 * @return The status and percentage of all runs of this client.
	 * @throws RemoteException
	 */
	public Map<String, Pair<RUN_STATUS, Float>> getRunStatusForClientId(
			String clientId) throws RemoteException;

	// /**
	// * @param clientId
	// * @return
	// * @throws RemoteException
	// */
	// TODO
	// public Map<String, Pair<Pair<RUN_STATUS, Float>, Map<Pair<String,
	// String>, Map<String, Pair<Map<String, Double>, Double>>>>>
	// getOptimizationRunStatusForClientId(
	// String clientId) throws RemoteException;

	/**
	 * This method allows to set the log level of this server.
	 * <p>
	 * Possible values are
	 * <ul>
	 * <li><b>0</b>: ALL</li>
	 * <li><b>1</b>: TRACE</li>
	 * <li><b>2</b>: DEBUG</li>
	 * <li><b>3</b>: INFO</li>
	 * <li><b>4</b>: WARN</li>
	 * <li><b>5</b>: ERROR</li>
	 * <li><b>6</b>: OFF</li>
	 * </ul>
	 * See {@link Level} for explanations of the log levels.
	 * 
	 * @param logLevel
	 *            The new log level of this server as an integer value.
	 * @throws RemoteException
	 */
	public void setLogLevel(Level logLevel) throws RemoteException;
}
