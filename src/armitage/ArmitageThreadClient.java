package armitage;

public interface ArmitageThreadClient {
	/** return -1 to stop the thread, return >=0 value to have the thread call this client again */
    long execute();
}
