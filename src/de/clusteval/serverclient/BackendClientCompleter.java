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

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import jline.console.completer.Completer;

import org.apache.commons.cli.ParseException;

import de.clusteval.run.RUN_STATUS;

import utils.ArraysExt;
import utils.Pair;

/**
 * This class is used by the backend client as a tab completer for the command
 * line.
 * 
 * <p>
 * Objects of this class communicate directly with the backend server to receive
 * information, that then in turn are shown in the command line as tab
 * completion candidates.
 * 
 * @author Christian Wiwie
 * 
 */
public class BackendClientCompleter implements Completer {

	/**
	 * The backend client that uses this completer.
	 */
	protected BackendClient client;

	/**
	 * The id of the client needed for communication between this completer and
	 * the server.
	 */
	protected String clientId;

	/**
	 * The parameters of the client (e.g. server ip and port) for communication
	 * between this completer and the server.
	 */
	protected String[] newArgs;

	/**
	 * A temporary variable holding the runs after they were retrieved from the
	 * server.
	 */
	protected TreeSet<String> runs;

	/**
	 * A temporary variable holding the dataset generators after they were
	 * retrieved from the server.
	 */
	protected TreeSet<String> dataSetGenerators;

	/**
	 * A temporary variable holding the run resumes after they were retrieved
	 * from the server.
	 */
	protected TreeSet<String> runResumes;

	/**
	 * A temporary variable holding the run results after they were retrieved
	 * from the server.
	 */
	protected TreeSet<String> runResults;

	/**
	 * A temporary variable holding the run status after they were retrieved
	 * from the server.
	 */
	protected Map<String, Pair<RUN_STATUS, Float>> runStatus;

	/**
	 * A temporary variable holding the active runs after they were retrieved
	 * from the server.
	 */
	protected TreeSet<String> runningRuns;

	/**
	 * @param clientId
	 *            The id of the client needed for communication between this
	 *            completer and the server.
	 * @param args
	 *            The parameters of the client (e.g. server ip and port) for
	 *            communication between this completer and the server.
	 */
	public BackendClientCompleter(final String clientId, final String[] args) {
		super();

		this.clientId = clientId;
		try {
			newArgs = ArraysExt
					.merge(args, new String[]{"-clientId", clientId});
			client = new BackendClient(newArgs);
		} catch (ConnectException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method updates the {@link #runs} attribute by retrieving the
	 * available runs from the server.
	 */
	protected void updateRuns() throws RemoteException {
		runs = new TreeSet<String>(client.getRuns());
	}

	/**
	 * This method updates the {@link #dataSetGenerators} attribute by
	 * retrieving the availble dataset generators from the server.
	 * 
	 * @throws RemoteException
	 */
	protected void updateDataSetGenerators() throws RemoteException {
		dataSetGenerators = new TreeSet<String>(client.getDataSetGenerators());
	}

	/**
	 * This method updates the {@link #runResumes} attribute by retrieving the
	 * available run resumes from the server.
	 */
	protected void updateRunResumes() throws RemoteException {
		runResumes = new TreeSet<String>(client.getRunResumes());
	}

	/**
	 * This method updates the {@link #runResults} attribute by retrieving the
	 * available run results from the server.
	 */
	protected void updateRunResults() throws RemoteException {
		runResults = new TreeSet<String>(client.getRunResults());
	}

	/**
	 * This method updates the {@link #runningRuns} attribute by retrieving the
	 * currently executed runs from the server.
	 */
	protected void updateRunningRuns() throws RemoteException {
		runStatus = client.getMyRunStatus();
		runningRuns = new TreeSet<String>();
		for (String run : runStatus.keySet())
			if (runStatus.get(run).getFirst().equals(RUN_STATUS.RUNNING)
					|| runStatus.get(run).getFirst()
							.equals(RUN_STATUS.SCHEDULED))
				runningRuns.add(run);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jline.console.completer.Completer#complete(java.lang.String, int,
	 * java.util.List)
	 */
	@SuppressWarnings("unused")
	@Override
	public int complete(String buffer, int cursor, List<CharSequence> candidates) {
		SortedSet<String> strings = new TreeSet<String>();
		strings.add("performRun");
		strings.add("resumeRun");
		strings.add("terminateRun");
		strings.add("getRunStatus");
		strings.add("shutdown");
		strings.add("getRuns");
		strings.add("getQueue");
		strings.add("getRunResumes");
		strings.add("getDataSets");
		strings.add("getPrograms");
		strings.add("getRunResults");
		strings.add("generateDataSet");

		boolean exception = true;
		while (exception) {
			exception = false;
			try {
				if (buffer == null) {
					candidates.addAll(strings);
				} else if (buffer.equals("performRun ")) {
					this.updateRuns();
					candidates.addAll(runs);
					return buffer.length();
				} else if (buffer.equals("resumeRun ")) {
					this.updateRunResumes();
					candidates.addAll(runResumes);
					return buffer.length();
				} else if (buffer.equals("getRunResults ")) {
					this.updateRunResults();
					candidates.addAll(runResults);
					return buffer.length();
				} else if (buffer.equals("getRunResumes ")) {
					this.updateRunResumes();
					candidates.addAll(runResumes);
					return buffer.length();
				} else if (buffer.equals("terminateRun ")
						|| buffer.equals("getRunStatus ")) {
					this.updateRunningRuns();
					candidates.addAll(runningRuns);
					return buffer.length();
				} else if (buffer.startsWith("performRun ")) {
					updateRuns();
					int posSpace = buffer.indexOf(' ');
					for (String match : runs.tailSet(buffer
							.substring(posSpace + 1))) {
						if (!match.startsWith(buffer.substring(posSpace + 1))) {
							break;
						}

						candidates.add(match);
					}

					return posSpace + 1;
				} else if (buffer.startsWith("resumeRun ")) {
					this.updateRunResumes();
					int posSpace = buffer.indexOf(' ');
					for (String match : runResumes.tailSet(buffer
							.substring(posSpace + 1))) {
						if (!match.startsWith(buffer.substring(posSpace + 1))) {
							break;
						}

						candidates.add(match);
					}

					return posSpace + 1;
				} else if (buffer.startsWith("getRunResults ")) {
					this.updateRunResults();
					int posSpace = buffer.indexOf(' ');
					for (String match : runResults.tailSet(buffer
							.substring(posSpace + 1))) {
						if (!match.startsWith(buffer.substring(posSpace + 1))) {
							break;
						}

						candidates.add(match);
					}

					return posSpace + 1;
				} else if (buffer.startsWith("getRunResumes ")) {
					this.updateRunResumes();
					int posSpace = buffer.indexOf(' ');
					for (String match : runResumes.tailSet(buffer
							.substring(posSpace + 1))) {
						if (!match.startsWith(buffer.substring(posSpace + 1))) {
							break;
						}

						candidates.add(match);
					}

					return posSpace + 1;
				} else if (buffer.startsWith("generateDataSet ")) {
					this.updateDataSetGenerators();
					int posSpace = buffer.indexOf(' ');
					for (String match : dataSetGenerators.tailSet(buffer
							.substring(posSpace + 1))) {
						if (!match.startsWith(buffer.substring(posSpace + 1))) {
							break;
						}

						candidates.add(match);
					}

					return posSpace + 1;
				} else if (buffer.startsWith("generateDataSet ")) {
					this.updateDataSetGenerators();
					candidates.addAll(dataSetGenerators);
					return buffer.length();
				} else if (buffer.startsWith("terminateRun ")
						|| buffer.startsWith("getRunStatus ")) {
					this.updateRunningRuns();
					int posSpace = buffer.indexOf(' ');
					for (String match : runningRuns.tailSet(buffer
							.substring(posSpace + 1))) {
						if (!match.startsWith(buffer.substring(posSpace + 1))) {
							break;
						}

						candidates.add(match);
					}

					return posSpace + 1;
				} else {
					for (String match : strings.tailSet(buffer)) {
						if (!match.startsWith(buffer)) {
							break;
						}

						candidates.add(match);
					}
				}
			} catch (RemoteException e) {
				exception = true;
				try {
					// client = new EvalClient(new String[]{"-clientId",
					// clientId});
					client = new BackendClient(newArgs);
				} catch (ConnectException e1) {
					// e1.printStackTrace();
				} catch (ParseException e1) {
					// e1.printStackTrace();
				}
				// return -1;
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}

			if (candidates.size() == 1) {
				candidates.set(0, candidates.get(0) + " ");
			}
		}

		return candidates.isEmpty() ? -1 : 0;
	}
}
