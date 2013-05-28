package org.dawnsci.algorithm.ui.updater;

public class WorkflowUpdaterCreator {

	/**
	 * 
	 * @param modelFilePath
	 * @return
	 *       a workflow updater
	 */
	public static IWorkflowUpdater createWorkflowUpdater(String modelFilePath){
		return new WorkflowUpdaterImpl(modelFilePath);
	}
}