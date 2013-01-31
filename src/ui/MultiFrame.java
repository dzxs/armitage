package ui;

import javax.swing.*;
import javax.swing.event.*;

import java.awt.*;
import java.awt.event.*;

import java.util.*;

import armitage.ArmitageApplication;

/* A class to host multiple Armitage instances in one frame. Srsly */
public class MultiFrame extends JFrame implements KeyEventDispatcher {
	protected JToolBar            toolbar;
	protected JPanel              content;
	protected CardLayout          cards;
	protected LinkedList          buttons;

	private static class ArmitageInstance {
		public ArmitageApplication app;
		public JToggleButton       button;
	}

	public void setTitle(ArmitageApplication app, String title) {
		if (active == app)
			setTitle(title);
	}

	protected ArmitageApplication active;

	/* is localhost running? */
	public boolean checkLocal() {
		Iterator i = buttons.iterator();
		while (i.hasNext()) {
			ArmitageInstance temp = (ArmitageInstance)i.next();
			if ("localhost".equals(temp.button.getText())) {
				return true;
			}
		}
		return false;
	}

	public boolean dispatchKeyEvent(KeyEvent ev) {
		if (active != null) {
			return active.getBindings().dispatchKeyEvent(ev);
		}
		return false;
	}

	public static final void setupLookAndFeel() {
		try {
			for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		}
		catch (Exception e) {
		}
	}

	public void quit() {
		ArmitageInstance temp = null;
		content.remove(active);
		Iterator i = buttons.iterator();
		while (i.hasNext()) {
			temp = (ArmitageInstance)i.next();
			if (temp.app == active) {
				toolbar.remove(temp.button);
				i.remove();
				break;
			}
		}

		if (buttons.size() == 0) {
			System.exit(0);
		}
		else if (buttons.size() == 1) {
			remove(toolbar);
			validate();
		}

		if (i.hasNext()) {
			temp = (ArmitageInstance)i.next();
		}
		else {
			temp = (ArmitageInstance)buttons.getFirst();
		}

		set(temp.button);
	}

	public MultiFrame() {
		super("");

		setLayout(new BorderLayout());

		/* setup our toolbar */
		toolbar = new JToolBar();

		/* content area */
		content = new JPanel();
		cards   = new CardLayout();
		content.setLayout(cards);

		/* setup our stuff */
		add(content, BorderLayout.CENTER);

		/* buttons?!? :) */
		buttons = new LinkedList();

		/* do this ... */
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		/* some basic setup */
		setSize(800, 600);
		setExtendedState(JFrame.MAXIMIZED_BOTH);

		/* all your keyboard shortcuts are belong to me */
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this);
	}

	protected void set(JToggleButton button) {
		/* set all buttons to the right state */
		Iterator i = buttons.iterator();
		while (i.hasNext()) {
			ArmitageInstance temp = (ArmitageInstance)i.next();
			if (temp.button.getText().equals(button.getText())) {
				temp.button.setSelected(true);
				active = temp.app;
				setTitle(active.getTitle());
			}
			else {
				temp.button.setSelected(false);
			}
		}

		/* show our cards? */
		cards.show(content, button.getText());
		active.touch();
	}

	public void addButton(String title, ArmitageApplication component) {
		ArmitageInstance a = new ArmitageInstance();
		a.button = new JToggleButton(title);
		a.app    = component;

		a.button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				set((JToggleButton)ev.getSource());
			}
		});

		toolbar.add(a.button);
		content.add(component, title);
		buttons.add(a);
		set(a.button);

		if (buttons.size() == 1) {
			show();
		}
		else if (buttons.size() == 2) {
			add(toolbar, BorderLayout.SOUTH);
		}
		validate();
	}
}