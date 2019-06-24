package cortana.gui;

import sleep.bridges.SleepClosure;
import sleep.runtime.SleepUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Stack;

public class ScriptedMenuItem extends JMenuItem implements ActionListener {
	protected String       label;
	protected SleepClosure code;
	protected MenuBridge   bridge;
	protected Stack        args;

	public ScriptedMenuItem(String label, SleepClosure code, MenuBridge bridge) {
		if (label.indexOf('&') > -1) {
			setText( label.substring(0, label.indexOf('&')) +
					label.substring(label.indexOf('&') + 1, label.length())
				);
			setMnemonic(label.charAt(label.indexOf('&') + 1));
		}
		else {
			setText(label);
		}

		this.code   = code;
		this.bridge = bridge;
		this.label  = label;
		args = bridge.getArguments();
		addActionListener(this);
	}

	public void actionPerformed(ActionEvent ev) {
		SleepUtils.runCode(code, label, null, cortana.core.EventManager.shallowCopy(args));
	}
}
