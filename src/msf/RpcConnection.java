package msf;

import java.io.IOException;

/**
 * This is a modification of msfgui/RpcConnection.java by scriptjunkie. Taken from 
 * the Metasploit Framework Java GUI. 
 */
public interface RpcConnection {
	Object execute(String methodName) throws IOException;
	Object execute(String methodName, Object[] params) throws IOException;
}
