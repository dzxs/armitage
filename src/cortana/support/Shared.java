package cortana.support;

import cortana.core.EventManager;
import sleep.bridges.BridgeUtilities;
import sleep.bridges.SleepClosure;
import sleep.interfaces.Function;
import sleep.interfaces.Loadable;
import sleep.runtime.Scalar;
import sleep.runtime.ScriptInstance;
import sleep.runtime.SleepUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/* an abstraction for tracking data shared between Cortana and Armitage */
public class Shared implements Function, Loadable {
	public void scriptLoaded(ScriptInstance script) {
		/* this is a very weird thing to call a function in Armitage from Cortana.
		   the armitage function must register itself though */
		script.getScriptEnvironment().getEnvironment().put("&_call_", this);
		script.getScriptEnvironment().getEnvironment().put("&_call_async_", this);
		script.getScriptEnvironment().getEnvironment().put("&_call_later_", this);
	}

	public void scriptUnloaded(ScriptInstance script) {
	}

	public Scalar evaluate(String name, ScriptInstance script, Stack args) {
		final String function = BridgeUtilities.getString(args, "");
		if (values.containsKey(function)) {
			switch (name) {
				case "&_call_": {
					SleepClosure f = (SleepClosure) values.get(function);
					return SleepUtils.runCode(f, function, f.getOwner(), EventManager.shallowCopy(args));
				}
				case "&_call_async_": {
					final SleepClosure f = (SleepClosure) values.get(function);
					final Stack argz = EventManager.shallowCopy(args);
					new Thread(() -> SleepUtils.runCode(f, function, f.getOwner(), argz)).start();
					return SleepUtils.getEmptyScalar();
				}
				case "&_call_later_": {
					final SleepClosure f = (SleepClosure) values.get(function);
					final Stack argz = EventManager.shallowCopy(args);
					javax.swing.SwingUtilities.invokeLater(() -> SleepUtils.runCode(f, function, f.getOwner(), argz));
					return SleepUtils.getEmptyScalar();
				}
			}
		}

		throw new RuntimeException("'" + function + "' does not exist");
	}

	/*
	 * Some generic stuff to share
	 */
	protected Map values = new HashMap();

	public void put(String key, Object value) {
		synchronized (this) {
			values.put(key, value);
		}
	}

	public Object get(String key) {
		synchronized (this) {
			return values.get(key);
		}
	}

	/*
	 *  Shared PrintStreams for logging purposes.
	 */
	protected Map logs = new HashMap();

	public PrintStream getLogger(String key) {
		if (!logs.containsKey(key)) {
			try {
				logs.put(key, new PrintStream(new FileOutputStream(key, true), true, StandardCharsets.UTF_8));
			}
			catch (IOException ex) {
				throw new RuntimeException(ex);
			}
		}
		return (PrintStream)logs.get(key);
	}
}
