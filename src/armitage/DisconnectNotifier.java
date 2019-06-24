package armitage;

import java.util.LinkedList;
import java.util.List;

/* an object to track listeners for disconnects... */
public class DisconnectNotifier {
	public interface DisconnectListener {
		void disconnected(String reason);
	}

	protected List listeners    = new LinkedList();
	protected boolean connected = true;

	public void addDisconnectListener(DisconnectListener l) {
		synchronized (listeners) {
			listeners.add(l);
		}
	}

	public void fireDisconnectEvent(final String reason) {
		new Thread(() -> {
			synchronized (listeners) {
				if (!connected)
					return;

                for (Object listener : listeners) {
                    DisconnectListener l = (DisconnectListener) listener;
                    l.disconnected(reason);
                }

				connected = false;
			}
		}).start();
	}
}
