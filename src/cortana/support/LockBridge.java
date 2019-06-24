package cortana.support;

import cortana.core.EventManager;
import msf.Async;
import msf.RpcConnection;
import sleep.bridges.BridgeUtilities;
import sleep.interfaces.Function;
import sleep.interfaces.Loadable;
import sleep.runtime.Scalar;
import sleep.runtime.ScriptInstance;
import sleep.runtime.SleepUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class LockBridge implements Function, Loadable, Runnable {
	protected EventManager  events;
	protected RpcConnection connection;
	protected List          locks = new LinkedList();

	private class LockMinion {
		protected String name;
		protected ScriptInstance script;
		protected boolean keep;
		protected Stack         args;

		public LockMinion(String name, ScriptInstance script, boolean keep, Stack args) {
			this.name   = name;
			this.script = script;
			this.keep   = keep;
			this.args   = args;
		}

		public boolean grab() {
			if (acquireLock(name, script)) {
				events.fireEvent("locked_" + name, args, script);
				if (!keep)
					releaseLock(name);
				return true;
			}
			return false;
		}
	}

	public LockBridge(RpcConnection connection, EventManager events) {
		this.connection = connection;
		this.events     = events;
		new Thread(this).start();
	}

	public boolean acquireLock(String name, ScriptInstance script) {
		try {
			Map temp = (Map)connection.execute("armitage.lock", new Object[] { name, script.getName() });
			if (!temp.containsKey("error"))
				return true;
		}
		catch (Exception ignored) {
		}
		return false;
	}

	public void releaseLock(String name) {
		try {
			((Async)connection).execute_async("armitage.unlock", new Object[] { name });
		}
		catch (Exception ignored) {
		}
	}

	public void run() {
		while (true) {
			List templ;
			synchronized (this) {
				templ = new LinkedList(locks);
			}

			try {
				if (templ.size() == 0) {
					Thread.sleep(2000);
				}
				else {
					List tempr = new LinkedList();

					/* loop through our locks (in order) and try to acquire them and release them */
                    for (Object o : templ) {
                        LockMinion m = (LockMinion) o;
                        if (m.grab())
                            tempr.add(m);
                        Thread.sleep(100);
                    }

					/* now, take any locks that were successful and remove them from oust list */
					synchronized (this) {
						for (Object o : tempr) {
							locks.remove(o);
						}
					}

					/* now, sleep... */
					Thread.sleep(1000);
				}
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public Scalar evaluate(String name, ScriptInstance script, Stack args) {
		String lname = BridgeUtilities.getString(args, "");
		if (name.equals("&lock")) {
			Scalar last = BridgeUtilities.getScalar(args);
			synchronized (this) {
				locks.add( new LockMinion( lname, script, SleepUtils.isTrueScalar(last), (Stack)args.clone() ) );
			}
		}
		else {
			releaseLock(lname);
		}
		return SleepUtils.getEmptyScalar();
	}

	public void scriptLoaded(ScriptInstance script) {
		script.getScriptEnvironment().getEnvironment().put("&lock", this);
		script.getScriptEnvironment().getEnvironment().put("&unlock", this);
	}

	public void scriptUnloaded(ScriptInstance script) {
	}
}
