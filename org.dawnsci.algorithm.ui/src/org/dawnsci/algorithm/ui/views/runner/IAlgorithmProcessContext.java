package org.dawnsci.algorithm.ui.views.runner;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.ISourceProvider;

/**
 * This class provides resources for running a algorithm/workflow in a custom way.
 * @author fcp94556
 *
 */
public interface IAlgorithmProcessContext {

	/**
	 * 
	 * @return a list of the data bound UI used in the custom algorithm
	 *         or null if none were defined.
	 */
	public ISourceProvider[] getSourceProviders();
	
    /**
     * Runs the algorithm at the given path. If sameVm is
     * true the current running VM is used (faster but more dangerous!)
     * 
     * No job is used, call the run method from a job unless you want the UI to
     * block.
     * 
     * This method is thread safe, you can call it from a Job.
     * 
     * @param filePath (to moml file).
     * @param sameVm
     * @param monitor, may be null
     * @throws Exception if anything goes wrong with the run, or it is stopped.
     */
	public void execute(final String filePath, boolean sameVm, IProgressMonitor monitor) throws Exception;

	/**
	 * Call this method to attempt to stop any running algorithm
	 */
	public void stop() throws Exception;

	/**
	 * 
	 * @return true if something is being run.
	 */
	public boolean isRunning();

	/**
	 * Sets the file path for the file containing the algorithm, e.g.
	 * the moml file for workflows or the py file for python or none.
	 * @param filePath
	 */
	public void setFilePath(String filePath);

	/**
	 * Gets the file path for the file containing the algorithm, e.g.
	 * the moml file for workflows or the py file for python or none.
	 * @return workflowFileName
	 */
	public String getFilePath();
}