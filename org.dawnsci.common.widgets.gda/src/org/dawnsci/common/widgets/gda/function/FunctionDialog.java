/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.common.widgets.gda.function;

//import org.dawb.common.ui.plot.function.FunctionType;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IFunction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.fitting.functions.Polynomial;

/**
 * A dialog for editing a Mathematical Function. Uses FunctionViewer table.
 *
 */
public class FunctionDialog extends Dialog {

	private static final Logger logger = LoggerFactory.getLogger(FunctionDialog.class);
	private static final String POLYNOMIAL = "Polynomial";

	private FunctionEditTable functionEditor;
	private CCombo functionType;
	private Spinner polynomialDegree;
	private Label labelDegree;

	String[] fittingFunctionNames;

	public FunctionDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	public Control createDialogArea(Composite parent) {

		final Composite main = (Composite)super.createDialogArea(parent);
		main.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		final Composite top= new Composite(main, SWT.NONE);
		top.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		top.setLayout(new GridLayout(2, false));

		final Label label = new Label(top, SWT.NONE);
		label.setText("Function type    ");
		label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		functionType = new CCombo(top, SWT.READ_ONLY|SWT.BORDER);

		fittingFunctionNames = FunctionExtensionFactory.getFunctionExtensionFactory().getFittingFunctionNames();
		functionType.setItems(fittingFunctionNames);
		functionType.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		functionType.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					//IFunction myFunction = FunctionType.createNew(functionType.getSelectionIndex());
					IFunction myFunction = FunctionExtensionFactory.getFunctionExtensionFactory().getFittingFunction(functionType.getText());
					functionEditor.setFunction(myFunction, null);
					if(functionType.getText().equals(POLYNOMIAL)){
						labelDegree.setVisible(true);
						polynomialDegree.setVisible(true);
					}else{
						labelDegree.setVisible(false);
						polynomialDegree.setVisible(false);
					}
				} catch (CoreException e1) {
					logger.error("Cannot create function "+ functionType.getText(), e1);
				}
			}
		});

		labelDegree = new Label(top, SWT.NONE);
		labelDegree.setText("Polynomial degree ");
		labelDegree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		labelDegree.setVisible(false);

		polynomialDegree = new Spinner(top, SWT.NONE);
		polynomialDegree.setToolTipText("Polynomial degree");
		polynomialDegree.setMinimum(1);
		polynomialDegree.setMaximum(100);
		polynomialDegree.setVisible(false);
		polynomialDegree.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					IFunction myFunction = FunctionExtensionFactory.getFunctionExtensionFactory().getFittingFunction(POLYNOMIAL);
					Polynomial polynom = (Polynomial)myFunction;
					polynom.setDegree(polynomialDegree.getSelection()-1);
					functionEditor.setFunction(myFunction, null);
				} catch (Exception e1) {
					logger.error("Cannot create function "+ POLYNOMIAL, e1);
				}
			}
		});

		this.functionEditor = new FunctionEditTable();
		functionEditor.createPartControl(main);

		return main;
	}

	public void setFunction(IFunction function) {
		String name = function.getName();
		int index = -1;
		for (int i = 0; i < fittingFunctionNames.length; i++) {
			String fName = fittingFunctionNames[i];
			if (name.equals(fName)){
				index = i;
			}
		}
		functionType.select(index);
		functionEditor.setFunction(function, null);
	}

	public IFunction getFunction() {
		return functionEditor.getFunction();
	}
}
