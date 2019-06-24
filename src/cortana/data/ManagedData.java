package cortana.data;

import sleep.runtime.Scalar;

public abstract class ManagedData {
	protected boolean        initial = true;
	protected Scalar         cache   = null;

	public boolean isInitial() {
		return initial;
	}

	public abstract Scalar getScalar();

	public void reset() {
		initial = true;
	}
}
