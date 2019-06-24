package ui;

import javax.swing.*;
import javax.swing.tree.TreeNode;

public class ATree extends JTree {
	public ATree(TreeNode root) {
		super(root);
	}

	public boolean getScrollableTracksViewportWidth() {
		return true;
	}
}
