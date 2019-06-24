package msf;

public interface Async {
	void execute_async(String methodName);
	void execute_async(String methodName, Object[] args);
	void execute_async(String methodName, Object[] args, RpcCallback callback);
	boolean isConnected();
	void disconnect();
	boolean isResponsive();
}
