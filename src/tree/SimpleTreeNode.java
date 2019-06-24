package tree;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import java.util.*;

/** implements a TreeNode that:
	1. stores children in sorted order
	2. requires unique children
	3. provides a thread safe add(), mark(), and reset() operations

	
*/
public class SimpleTreeNode implements TreeNode {
	protected TreeNode parent;
	protected Object   data;

	protected List     children = new LinkedList();
	protected Set      marked = null;

	protected TreeModel model;

	public SimpleTreeNode(TreeModel model, TreeNode parent, Object data) {
		this.model  = model;
		this.parent = parent;
		this.data = data;
	}

	public void setModel(TreeModel model) {
		this.model = model;
	}

	public SimpleTreeNode(TreeNode parent, Object data) {
		this.parent = parent;
		this.data = data;
	}

	public Enumeration children() {
		return Collections.enumeration(children);
	}

	public void add(final TreeNode node) {
		if (SwingUtilities.isEventDispatchThread()) {
			_add(node);
		}
		else {
			SwingUtilities.invokeLater(() -> _add(node));
		}
	}

	public TreeNode findChild(Object o) {
		for (Object child : children) {
			SimpleTreeNode node = (SimpleTreeNode) child;
			if (o.equals(node.getData()))
				return node;
		}
		System.err.println("Child not found: " + o + " vs. " + children);
		return null;
	}

	public Object getData() {
		return data;
	}

	public void mark() {
		synchronized (this) {
			marked = new HashSet();
		}
	}

	public void reset() {
		if (SwingUtilities.isEventDispatchThread()) {
			_reset();
		}
		else {
			SwingUtilities.invokeLater(this::_reset);
		}
	}

	public void _reset() {
		children.removeIf(node -> !marked.contains(node));

		if (model instanceof DefaultTreeModel)
			((DefaultTreeModel)model).nodeStructureChanged(this);
	}

	public void _add(TreeNode node) {
		ListIterator i = children.listIterator();
		String represent = node.toString().toLowerCase();
		while (i.hasNext()) {
			Object next = i.next();
			if (next.toString().toLowerCase().equals(represent)) {
				synchronized(this) {
					marked.add(next);
				}
				return;
			}
			else if (represent.compareTo(next.toString().toLowerCase()) < 0) {
				synchronized (this) {
					i.add(node);
					marked.add(node);
				}
				return;
			} 
		}
		children.add(node);
		synchronized (this) {
			marked.add(node);
		}
	}

	public boolean getAllowsChildren() {
		return true;
	}

	public TreeNode getChildAt(int index) {
		return (TreeNode)children.get(index);
	}

	public int getChildCount() {
		return children.size();
	}

	public int getIndex(TreeNode node) {
		return children.indexOf(node);
	}

	public TreeNode getParent() {
		return parent;
	}

	public boolean isLeaf() {
		return false;
	}

	public String toString() {
		return data.toString();
	}
}
