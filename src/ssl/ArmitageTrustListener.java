package ssl;

public interface ArmitageTrustListener {
	boolean trust(String fingerprint);
}
