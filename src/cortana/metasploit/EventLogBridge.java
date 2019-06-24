package cortana.metasploit;

import armitage.ConsoleCallback;
import armitage.ConsoleClient;
import cortana.core.EventManager;
import cortana.core.FilterManager;
import msf.RpcConnection;
import sleep.bridges.BridgeUtilities;
import sleep.interfaces.Function;
import sleep.interfaces.Loadable;
import sleep.runtime.Scalar;
import sleep.runtime.ScriptInstance;
import sleep.runtime.SleepUtils;

import java.util.Stack;

/* add an API for interfacing with the event log... */
public class EventLogBridge implements Loadable, Function, ConsoleCallback {
	protected EventManager  events;
	protected FilterManager filters;
	protected RpcConnection client;
	protected RpcConnection dserver;

	protected ConsoleClient console;

        public void sessionRead(String sessionid, String text) {
		String[] lines = text.trim().split("\n");

            for (String line : lines) {
                Stack args = new Stack();
                args.push(SleepUtils.getScalar(line));
                events.fireEvent("event_read", args);
            }
	}

        public void sessionWrote(String sessionid, String text) {
		Stack args = new Stack();
		args.push(SleepUtils.getScalar(sessionid));
		args.push(SleepUtils.getScalar(text.trim()));

		events.fireEvent("event_write", args);
	}

	public ConsoleClient start(console.Console window) {
		if (dserver != client) {
			console = new ConsoleClient(window, dserver, "armitage.poll", "armitage.push", null, "", false);
			console.addSessionListener(this);
			return console;
		}
		return null;
	}

	public EventLogBridge(RpcConnection client, RpcConnection dserver, EventManager events, FilterManager filters) {
		this.client  = client;
		this.dserver = dserver;
		this.events  = events;
		this.filters = filters;
	}

	public Scalar evaluate(String name, ScriptInstance script, Stack args) {
		if (dserver == client)
			return SleepUtils.getEmptyScalar();

		String text = BridgeUtilities.getString(args, "");

		try {
			if (name.equals("&elog")) {
				Object[] arg = new Object[1];
				arg[0] = text;
				dserver.execute("armitage.log", arg);
				sessionWrote("log", text + "\n");
			}
			else if (name.equals("&say")) {
				console.sendString(text + "\n");
			}
		}
		catch (java.io.IOException ex) {
			throw new RuntimeException(ex);
		}

		return SleepUtils.getEmptyScalar();
	}

	public void scriptLoaded(ScriptInstance si) {
		si.getScriptEnvironment().getEnvironment().put("&elog", this);
		si.getScriptEnvironment().getEnvironment().put("&say",  this);
	}

	public void scriptUnloaded(ScriptInstance si) {
	}
}
