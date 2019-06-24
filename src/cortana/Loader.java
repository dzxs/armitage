package cortana;

import sleep.error.RuntimeWarningWatcher;
import sleep.error.YourCodeSucksException;
import sleep.interfaces.Loadable;
import sleep.runtime.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

/** The Loader creates an isolated script instance for each Cortana script that we load */
public class Loader implements Loadable {
	protected ScriptLoader    loader;
	protected Hashtable       shared  = new Hashtable();
	protected ScriptVariables vars    = new ScriptVariables();
	protected Object[]        passMe  = new Object[4];
	protected List		  scripts = new LinkedList();

	public void unsetDebugLevel(int flag) {
		for (Object o : scripts) {
			ScriptInstance script = (ScriptInstance) o;
			int flags = script.getDebugFlags() & ~flag;
			script.setDebugFlags(flags);
		}
	}

	public void printProfile(OutputStream out) {
		for (Object o : scripts) {
			ScriptInstance script = (ScriptInstance) o;
			script.printProfileStatistics(out);
			return;
		}
	}

	public void setDebugLevel(int flag) {
		for (Object o : scripts) {
			ScriptInstance script = (ScriptInstance) o;
			int flags = script.getDebugFlags() | flag;
			script.setDebugFlags(flags);
		}
	}

	public boolean isReady() {
		synchronized (this) {
			return passMe != null;
		}
	}

	public void passObjects(Object o, Object p, Object q, Object r) {
		synchronized (this) {
			passMe[0] = o;
			passMe[1] = p;
			passMe[2] = q;
			passMe[3] = r;
		}
	}

	public Object[] getPassedObjects() {
		synchronized (this) {
			return passMe;
		}
	}

	public void setGlobal(String name, Scalar value) {
		vars.getGlobalVariables().putScalar(name, value);
	}

	public ScriptLoader getScriptLoader() {
		return loader;
	}

	protected RuntimeWarningWatcher watcher;

	public Loader(RuntimeWarningWatcher watcher) {
		loader = new ScriptLoader();
		loader.addSpecificBridge(this);
		this.watcher = watcher;
	}

	public void scriptLoaded(ScriptInstance i) {
		i.setScriptVariables(vars);
		i.addWarningWatcher(watcher);
		scripts.add(i);

		/* store a hashcode in metadata so we have a unique way of marking
		   this script and its children forks */
		i.getMetadata().put("%scriptid%", i.hashCode());
	}

	public void unload() {
		for (Object script : scripts) {
			ScriptInstance temp = (ScriptInstance) script;
			temp.setUnloaded();
		}

		scripts = null;
		vars = null;
		shared = null;
		passMe = null;
		loader = null;
	}

	public void scriptUnloaded(ScriptInstance i) {

	}

	public Object loadInternalScript(String file, Object cache) {
		try {
			/* we cache the compiled version of internal scripts to conserve some memory */
			if (cache == null) {
				InputStream i = this.getClass().getClassLoader().getResourceAsStream(file);
				if (i == null)
					throw new RuntimeException("resource " + file + " does not exist");

				cache = loader.compileScript(file, i);
			}

			ScriptInstance script = loader.loadScript(file, (sleep.engine.Block)cache, shared);
			script.runScript();
		}
		catch (IOException ex) {
			System.err.println("*** Could not load: " + file + " - " + ex.getMessage());
		}
		catch (YourCodeSucksException ex) {
			ex.printErrors(System.out);
		}
		return cache;
	}

	public ScriptInstance loadScript(String file) throws IOException {
		setGlobal("$__script__", SleepUtils.getScalar(file));
		ScriptInstance script = loader.loadScript(file, shared);
		script.runScript();
		return script;
	}
}
