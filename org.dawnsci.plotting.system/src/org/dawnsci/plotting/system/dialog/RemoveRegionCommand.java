/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.system.dialog;

import java.util.ArrayList;
import java.util.List;

import org.dawnsci.plotting.draw2d.swtxy.XYRegionGraph;
import org.dawnsci.plotting.draw2d.swtxy.selection.AbstractSelectionRegion;
import org.eclipse.nebula.visualization.xygraph.undo.IUndoableCommand;

/**The undoable command to remove an annotation.
 * @author Xihui Chen
 *
 */
public class RemoveRegionCommand implements IUndoableCommand {
	
	private XYRegionGraph xyGraph;
	private List<AbstractSelectionRegion<?>> regions;
	
	public RemoveRegionCommand(XYRegionGraph xyGraph, AbstractSelectionRegion<?> region) {
		this.xyGraph = xyGraph;
		this.regions = new ArrayList<AbstractSelectionRegion<?>>();
		regions.add(region);
	}

	public RemoveRegionCommand(XYRegionGraph xyGraph, List<AbstractSelectionRegion<?>> regions) {
		this.xyGraph = xyGraph;
		this.regions = regions;
	}

	public void redo() {
		for (AbstractSelectionRegion<?> region : regions)  xyGraph.removeRegion(region);
	}

	public void undo() {		
		for (AbstractSelectionRegion<?> region : regions)  xyGraph.addRegion(region);
	}
	
	@Override
	public String toString() {
		return "Remove Region(s)";
	}

}
