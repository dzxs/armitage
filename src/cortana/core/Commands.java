package cortana.core;

import sleep.bridges.BridgeUtilities;
import sleep.bridges.SleepClosure;
import sleep.engine.Block;
import sleep.interfaces.Environment;
import sleep.interfaces.Function;
import sleep.interfaces.Loadable;
import sleep.runtime.Scalar;
import sleep.runtime.ScriptInstance;
import sleep.runtime.SleepUtils;

import java.util.*;

public class Commands implements Function, Environment, Loadable {
	protected CommandManager manager;

	public void scriptLoaded(ScriptInstance si) {
		Hashtable environment = si.getScriptEnvironment().getEnvironment();

		environment.put("&command", this);
		environment.put("command", this);

		environment.put("&fire_command", this);
	}

	public void scriptUnloaded(ScriptInstance si) {
	}

	public void bindFunction(ScriptInstance si, String type, String command, Block body) {
		SleepClosure f = new SleepClosure(si, body);
		manager.registerCommand(command, f);
	}

	public Scalar evaluate(String name, ScriptInstance script, Stack args) {
		String command = BridgeUtilities.getString(args, "");
		if (name.equals("&fire_command")) {
			StringBuilder arstring = new StringBuilder();
			LinkedList l = new LinkedList(args);
			l.add(command);
			Collections.reverse(l);
			Iterator i = l.iterator();
			while (i.hasNext()) {
				arstring.append(i.next());
				if (i.hasNext())
					arstring.append(" ");
			}

			manager.fireCommand(command, arstring + "", args);
			return SleepUtils.getEmptyScalar();
		}
		else {
			SleepClosure f = BridgeUtilities.getFunction(args, script);
			manager.registerCommand(command, f);
			return SleepUtils.getEmptyScalar();
		}
	}

	public Commands(CommandManager m) {
		this.manager = m;
	}
}
