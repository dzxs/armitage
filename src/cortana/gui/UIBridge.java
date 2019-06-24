package cortana.gui;

import armitage.ArmitageApplication;
import cortana.core.EventManager;
import sleep.bridges.BridgeUtilities;
import sleep.bridges.SleepClosure;
import sleep.interfaces.Function;
import sleep.interfaces.Loadable;
import sleep.runtime.Scalar;
import sleep.runtime.ScriptInstance;
import sleep.runtime.SleepUtils;

import javax.swing.*;
import java.util.Stack;

/* some methods to help out with user interface stuff */
public class UIBridge implements Loadable, Function {
	protected ArmitageApplication armitage;

	public UIBridge(ArmitageApplication a) {
		armitage = a;
	}

	public Scalar evaluate(String name, ScriptInstance script, Stack args) {
		if (name.equals("&later")) {
			final SleepClosure f = BridgeUtilities.getFunction(args, script);
			final Stack argz = EventManager.shallowCopy(args);
			if (SwingUtilities.isEventDispatchThread()) {
				SleepUtils.runCode(f, "laterz", null, argz);
			}
			else {
				SwingUtilities.invokeLater(() -> SleepUtils.runCode(f, "laterz", null, argz));
			}
		}

		return SleepUtils.getEmptyScalar();
	}

	public void scriptLoaded(ScriptInstance si) {
		si.getScriptEnvironment().getEnvironment().put("&later",  this);
	}

	public void scriptUnloaded(ScriptInstance si) {
	}
}
