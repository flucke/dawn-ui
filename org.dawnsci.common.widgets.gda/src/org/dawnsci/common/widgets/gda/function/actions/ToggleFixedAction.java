/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.gda.function.actions;

import org.dawnsci.common.widgets.gda.Activator;
import org.dawnsci.common.widgets.gda.function.FunctionTreeViewer;
import org.dawnsci.common.widgets.gda.function.IFunctionViewer;
import org.dawnsci.common.widgets.gda.function.internal.model.FunctionModelElement;
import org.dawnsci.common.widgets.gda.function.internal.model.ParameterModel;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IParameter;
import org.eclipse.jface.action.Action;

public class ToggleFixedAction extends Action {
	private FunctionTreeViewer viewer;

	public ToggleFixedAction(IFunctionViewer viewer) {
		super("Toggle fixed parameter setting", Activator.getImageDescriptor("icons/copy.gif"));
		if (!(viewer instanceof FunctionTreeViewer)) {
			throw new UnsupportedOperationException(
					"viewer must be a FunctionTreeViewer");
		}
		this.viewer = (FunctionTreeViewer) viewer;
		setToolTipText("Toggle fixed parameter setting");
	}

	@Override
	public void run() {
		FunctionModelElement model = viewer.getSelectedFunctionModel();
		if (model instanceof ParameterModel) {
			ParameterModel parameterModel = (ParameterModel) model;
			IParameter parameter = parameterModel.getParameter();
			parameterModel.setParameterFixed(!parameter.isFixed());
			viewer.refresh(parameter);
		}
	}
}
