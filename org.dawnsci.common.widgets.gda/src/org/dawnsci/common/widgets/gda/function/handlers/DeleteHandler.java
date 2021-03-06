/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.gda.function.handlers;

import org.dawnsci.common.widgets.gda.function.IFunctionViewer;
import org.dawnsci.common.widgets.gda.function.internal.model.FunctionModel;
import org.dawnsci.common.widgets.gda.function.internal.model.FunctionModelElement;
import org.dawnsci.common.widgets.gda.function.internal.model.OperatorModel;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

public class DeleteHandler extends BaseHandler {

	public DeleteHandler(IFunctionViewer viewer) {
		super(viewer, true);
	}

	@Override
	protected boolean isSelectionValid(FunctionModelElement model) {
		return model instanceof FunctionModel || model instanceof OperatorModel;
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		FunctionModelElement model = viewer.getSelectedFunctionModel();
		if (model != null && isSelectionValid(model)) {
			model.deleteFunction();
			viewer.refresh(model.getParentOperator());
		}

		return null;
	}
}
