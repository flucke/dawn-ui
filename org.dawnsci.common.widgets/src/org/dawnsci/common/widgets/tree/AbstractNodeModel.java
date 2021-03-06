/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.tree;

import java.util.Map;
import java.util.TreeMap;

import javax.swing.tree.TreeNode;

import org.eclipse.jface.viewers.TreeViewer;

public class AbstractNodeModel {

	protected boolean isDisposed;
	protected Map<String, TreeNode> nodeMap;
	protected LabelNode   root;
	protected TreeViewer  viewer;

	protected AbstractNodeModel() {
	    this.root    = new LabelNode();
	    this.nodeMap = new TreeMap<String, TreeNode>();
	}

	public LabelNode getRoot() {
		return root;
	}

	protected void registerNode(LabelNode node) {
		final String labelPath = node.getPath();
		// System.out.println(labelPath);
		if (labelPath!=null && nodeMap!=null) {
			this.nodeMap.put(labelPath, node);
		}
	}
	
	/**
	 * Get any node from the tree. Useful when running algorithms with the model.
	 * @param labelPath
	 * @return
	 */
	public TreeNode getNode(final String labelPath) {
		return nodeMap.get(labelPath.toLowerCase());
	}

	/**
	 * Set any node from the tree, if the label path does not exist does nothing
	 * @param node
	 * @param labelPath
	 */
	public void setNode(TreeNode node, String labelPath) {
		if (nodeMap.get(labelPath.toLowerCase()) == null)
			return;
		nodeMap.put(labelPath.toLowerCase(), node);
	}

	public void dispose() {
		root.dispose();
		nodeMap.clear();
		nodeMap = null;
		isDisposed = true;
		root   = null;
		viewer = null;

	}
	
	public void reset() {
		reset(root);
	}

	private void reset(TreeNode node) {
		if (node instanceof NumericNode) {
			((NumericNode<?>)node).reset();
		}
		for (int i = 0; i < node.getChildCount(); i++) {
			reset(node.getChildAt(i));
		}
	}

	public TreeViewer getViewer() {
		return viewer;
	}

	public void setViewer(TreeViewer viewer) {
		this.viewer = viewer;
	}

}
