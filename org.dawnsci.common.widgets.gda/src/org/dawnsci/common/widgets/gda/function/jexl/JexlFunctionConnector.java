/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.gda.function.jexl;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;

import uk.ac.diamond.scisoft.analysis.fitting.functions.Gaussian;

/**
 * Reference implementation for a function to be accessible in jexl function expression
 */
public class JexlFunctionConnector {

	public static double Gaussian(double x, double p, double w, double a) {
		return new Gaussian(p, w, a).val(x);
	}
	
	/**
	 * Dataset implementation for Tracy
	 * 
	 * @param x
	 * @param p
	 * @param w
	 * @param a
	 * @return gaussian
	 */
	public static IDataset Gaussian2(IDataset x, double p, double w, double a) {
		Gaussian g = new Gaussian(p, w, a);
		return g.calculateValues(x);
		
		
	}

}
