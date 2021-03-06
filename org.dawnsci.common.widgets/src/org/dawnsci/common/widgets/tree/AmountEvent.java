/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.tree;

import java.util.EventObject;

import javax.measure.quantity.Quantity;

import org.jscience.physics.amount.Amount;

public class AmountEvent<E extends Quantity> extends EventObject {
	private static final long serialVersionUID = -7433184477752660193L;

	private Amount<E> amount;

	public AmountEvent(Object source, Amount<E> amount) {
		super(source);
		this.amount = amount;
	}

	public Amount<E> getAmount() {
		return amount;
	}

	public void setAmount(Amount<E> amount) {
		this.amount = amount;
	}
}
