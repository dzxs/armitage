package armitage;

public interface ConsoleCallback {
	void sessionRead(String sessionid, String text);
	void sessionWrote(String sessionid, String text);
}
