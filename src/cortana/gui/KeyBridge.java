package cortana.gui;

import armitage.ArmitageApplication;
import sleep.bridges.BridgeUtilities;
import sleep.bridges.SleepClosure;
import sleep.engine.Block;
import sleep.interfaces.Environment;
import sleep.interfaces.Function;
import sleep.interfaces.Loadable;
import sleep.runtime.Scalar;
import sleep.runtime.ScriptInstance;
import sleep.runtime.SleepUtils;
import ui.KeyHandler;

import java.util.Stack;

/* an API to bind new keys in Armitage */
public class KeyBridge implements Loadable, Function, Environment {
	protected ArmitageApplication armitage;

	public KeyBridge(ArmitageApplication a) {
		armitage = a;
	}

	private static class Binding implements KeyHandler {
		protected SleepClosure code;

		public Binding(SleepClosure c) {
			code = c;
		}

		public void key_pressed(String description) {
			if (code != null && code.getOwner().isLoaded()) {
				SleepUtils.runCode(code, description, null, new Stack());
			}
			else {
				code = null;
			}
		}
	}

	protected void registerKey(String combination, SleepClosure closure) {
		Binding b = new Binding(closure);
		armitage.bindKey(combination, b);
	}

	public void bindFunction(ScriptInstance si, String type, String event, Block body) {
		SleepClosure f = new SleepClosure(si, body);
		registerKey(event, f);
	}

	public Scalar evaluate(String name, ScriptInstance script, Stack args) {
		String desc = BridgeUtilities.getString(args, "");
		SleepClosure f = BridgeUtilities.getFunction(args, script);
		registerKey(desc, f);
		return SleepUtils.getEmptyScalar();
	}

	public void scriptLoaded(ScriptInstance si) {
		si.getScriptEnvironment().getEnvironment().put("bind", this);
		si.getScriptEnvironment().getEnvironment().put("&bind",  this);
	}

	public void scriptUnloaded(ScriptInstance si) {
	}
}
