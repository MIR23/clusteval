/**
 * 
 */
package de.clusteval.run.runnable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.ProgressPrinter;
import de.clusteval.paramOptimization.NoParameterSetFoundException;
import de.clusteval.data.dataset.format.IncompatibleDataSetFormatException;
import de.clusteval.data.dataset.format.InvalidDataSetFormatVersionException;
import de.clusteval.data.dataset.format.UnknownDataSetFormatException;
import de.clusteval.data.goldstandard.IncompleteGoldStandardException;
import de.clusteval.data.goldstandard.format.UnknownGoldStandardFormatException;
import de.clusteval.framework.RLibraryNotLoadedException;
import de.clusteval.framework.repository.RegisterException;
import de.clusteval.framework.threading.RunSchedulerThread;
import de.clusteval.run.RUN_STATUS;
import de.clusteval.run.Run;
import de.clusteval.run.result.NoRunResultFormatParserException;
import de.clusteval.utils.InternalAttributeException;
import de.clusteval.utils.RNotAvailableException;

/**
 * An abstract class that corresponds to a smaller atomic part of a {@link Run}.
 * A runnable is executed asynchronously and no order among runnables is
 * guaranteed.
 * 
 * <p>
 * Objects of subclasses of this class are created in
 * {@link Run#perform(RunSchedulerThread)} (in subclasses) and later executed by
 * the RunScheduler. One instance represents the execution of one subunit of the
 * overall run. The results are added and stored in {@link Run#results} in a
 * synchronized way.
 * 
 * <p>
 * In the class hierarchy this class corresponds to the {@link Run} class.
 * 
 * @author Christian Wiwie
 */
public abstract class RunRunnable implements Runnable {

	/**
	 * The run this runnable object was created by.
	 */
	protected Run run;

	/**
	 * If exceptions are thrown during the execution it is stored in the
	 * following attributes. It will not been thrown automatically, to avoid
	 * disrupting the successive optimization iterations. If one wants to check
	 * for these exceptions afterwards, one can use the corresponding getter
	 * methods.
	 */
	protected List<Throwable> exceptions;

	/**
	 * Keep track of the progress of this runnable. In case of parameter
	 * optimization mode, it will increase by one after every percent reached of
	 * the parameter sets to evaluate.
	 */
	protected ProgressPrinter progress;

	/**
	 * This attribute indicates, whether this run is a resumption of a previous
	 * execution or a completely new execution.
	 */
	protected boolean isResume;

	/**
	 * A logger that keeps track of all actions done by the runnable.
	 */
	protected Logger log;

	/**
	 * This object can be used to get the status of the runnable thread.
	 */
	protected Future<?> future;

	/**
	 * True, if this runnable is paused, false otherwise.
	 */
	protected boolean paused;

	/**
	 * This attribute holds the running time after finishing the runnable.
	 */
	protected long runningTime;

	/**
	 * This attribute is used to store the last start time in case this runnable
	 * is paused and resumed.
	 */
	protected long lastStartTime;

	/**
	 * The unique identification string of the run which is used to store the
	 * results in a unique folder to avoid overwriting.
	 */
	protected String runThreadIdentString;

	/**
	 * Instantiates a new run runnable.
	 * 
	 * @param run
	 *            The run this runnable belongs to.
	 * @param runIdentString
	 *            The unique identification string of the run which is used to
	 *            store the results in a unique folder to avoid overwriting.
	 * @param isResume
	 *            True, if this run is a resumption of a previous execution or a
	 *            completely new execution.
	 */
	public RunRunnable(final Run run, final String runIdentString,
			final boolean isResume) {
		super();
		this.run = run;
		this.isResume = isResume;
		this.exceptions = new ArrayList<Throwable>();
		this.runThreadIdentString = runIdentString;
		this.log = LoggerFactory.getLogger(this.getClass());
	}

	/**
	 * Checks whether this runnable thread has been interrupted. If yes, it
	 * prints out a simple log statement and.
	 * 
	 * @return True, if this thread was interrupted, false otherwise.
	 */
	protected final boolean checkForInterrupted() {
		if (isInterrupted()) {
			this.log.info("Caught the signal to terminate. Terminating as soon as possible...");
			return true;
		}
		return false;
	}

	/**
	 * Checks whether this runnable thread has been interrupted.
	 * 
	 * @return True, if this thread was interrupted, false otherwise.
	 */
	protected final boolean isInterrupted() {
		return this.future.isCancelled();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public final void run() {
		this.run.setStatus(RUN_STATUS.RUNNING);
		try {
			beforeRun();
			doRun();
		} catch (Throwable e) {
			this.exceptions.add(e);
		} finally {
			afterRun();
		}
	}

	/**
	 * This method is invoked by {@link #run()} before anything else is done. It
	 * can be overwritten and used in subclasses to do any precalculations or
	 * requirements like copying or moving files.
	 * 
	 * @throws IOException
	 * @throws IllegalArgumentException
	 * @throws InvalidDataSetFormatVersionException
	 * @throws UnknownDataSetFormatException
	 * @throws RegisterException
	 * @throws InternalAttributeException
	 * @throws IncompatibleDataSetFormatException
	 * @throws UnknownGoldStandardFormatException
	 * @throws IncompleteGoldStandardException
	 * @throws RNotAvailableException
	 */

	@SuppressWarnings("unused")
	protected void beforeRun() throws UnknownDataSetFormatException,
			InvalidDataSetFormatVersionException, IllegalArgumentException,
			IOException, RegisterException, InternalAttributeException,
			IncompatibleDataSetFormatException,
			UnknownGoldStandardFormatException,
			IncompleteGoldStandardException, RNotAvailableException {

	}

	/**
	 * This method is invoked by {@link #run()} after {@link #beforeRun()} has
	 * finished and is responsible for the operation and execution of the
	 * runnable itself.
	 * 
	 * @throws NoParameterSetFoundException
	 * @throws NoRunResultFormatParserException
	 * @throws IOException
	 * @throws RegisterException
	 * @throws InternalAttributeException
	 * @throws REXPMismatchException
	 * @throws REngineException
	 * @throws RLibraryNotLoadedException
	 * @throws RNotAvailableException
	 */
	protected abstract void doRun() throws InternalAttributeException,
			RegisterException, IOException, NoRunResultFormatParserException,
			NoParameterSetFoundException, RNotAvailableException,
			RLibraryNotLoadedException, REngineException, REXPMismatchException;

	/**
	 * This method is invoked by {@link #run()} after {@link #doRun()} has
	 * finished. It is responsible for cleaning up all files, folders and for
	 * doing all kinds of postcalculations.
	 */
	protected void afterRun() {
		// print exceptions
		if (this.exceptions.size() > 0) {
			this.log.warn("During the execution of this run runnable exceptions were thrown:");
			for (Throwable t : this.exceptions) {
				this.log.warn(t.toString());
			}
		}
	}

	/**
	 * This method causes the caller to wait for this runnable's thread to
	 * finish its execution.
	 * 
	 * <p>
	 * This method also waits in case this runnable has not yet been started
	 * (the future object has not yet been initialized).
	 * 
	 * @throws InterruptedException
	 * @throws CancellationException
	 * @throws ExecutionException
	 */
	public final void waitFor() throws InterruptedException, ExecutionException {
		boolean nullPointerException = true;
		while (nullPointerException) {
			try {
				this.future.get();
			} catch (NullPointerException e) {
				continue;
			} catch (CancellationException e) {
			}
			nullPointerException = false;
		}
	}

	/**
	 * @return True, if this runnable's thread has been paused.
	 */
	public boolean isPaused() {
		return this.paused;
	}

	/**
	 * @return True, if this runnable's thread has been cancelled.
	 */
	public boolean isCancelled() {
		if (this.future != null)
			return this.future.isCancelled();
		return false;
	}

	/**
	 * @return True, if this runnable's thread has finished its execution.
	 */
	public boolean isDone() {
		if (this.future != null)
			return this.future.isDone();
		return false;
	}

	/**
	 * This method pauses this runnable's thread until {@link #resume()} is
	 * invoked. If it was paused before this invocation will be ignored.
	 */
	public void pause() {
		this.paused = true;
	}

	/**
	 * This method resumes this runnable's thread, after it has been paused. If
	 * it wasn't paused before this invocation will be ignored.
	 */
	public void resume() {
		this.paused = false;
	}

	/**
	 * The future object of a runnable is only initialized, when it has been
	 * started.
	 * 
	 * @return The future object of this runnable.
	 * @see #future
	 */
	public Future<?> getFuture() {
		return this.future;
	}

	/**
	 * @return A list with all exceptions thrown during execution of this
	 *         runnable.
	 * @see #exceptions
	 */
	public List<Throwable> getExceptions() {
		return this.exceptions;
	}

	/**
	 * @return The progress printer of this runnable.
	 * @see #progress
	 */
	public ProgressPrinter getProgressPrinter() {
		return this.progress;
	}

	/**
	 * @return The run this runnable belongs to.
	 */
	public Run getRun() {
		return this.run;
	}
}

/**
 * This class is responsible for reading and emptying the streams of the started
 * thread. This has to be done in order to avoid overflowing streams and thus
 * possible termination of the thread by the operating system.
 * 
 */
class StreamGobbler extends Thread {

	InputStream is;
	BufferedWriter bw;

	public StreamGobbler(InputStream is, BufferedWriter bw) {
		super();
		this.setName(this.getName().replace("Thread",
				this.getClass().getSimpleName()));
		// TODO this.setPriority(NORM_PRIORITY-1);
		this.is = is;
		this.bw = bw;
	}

	@Override
	public void run() {
		try {
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			while ((line = br.readLine()) != null)
				synchronized (bw) {
					bw.append(line);
					bw.newLine();
					bw.flush();
				}
		} catch (IOException ioe) {
		}
	}
}