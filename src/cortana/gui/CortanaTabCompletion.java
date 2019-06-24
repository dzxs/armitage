package cortana.gui;

import armitage.GenericTabCompletion;
import console.Console;

import java.util.Collection;

/* scriptable tab completion... */
public class CortanaTabCompletion extends GenericTabCompletion {
	public interface Completer {
		Collection getOptions(String text);
	}

	protected Completer completer;

	public CortanaTabCompletion(Console window, Completer c) {
		super(window);
		this.completer = c;
	}

	public Collection getOptions(String text) {
		return completer.getOptions(text);
	}
}
