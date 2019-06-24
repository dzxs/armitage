package msf;

import armitage.ArmitageBuffer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * This is a modification of msfgui/RpcConnection.java by scriptjunkie. Taken from 
 * the Metasploit Framework Java GUI.
 */
public abstract class RpcConnectionImpl implements RpcConnection, Async {
	protected String rpcToken;
	private Map callCache = new HashMap();
	protected RpcConnection database = null;
	protected Map hooks = new HashMap();
	protected RpcQueue queue = null;
	protected boolean connected = true;

	public boolean isConnected() {
		return connected;
	}

	public boolean isResponsive() {
		return true;
	}

	public void disconnect() {
		connected = false;
	}

	public void addHook(String name, RpcConnection hook) {
		hooks.put(name, hook);
	}

	public void setDatabase(RpcConnection connection) {
		database = connection;
	}

	public RpcConnectionImpl() {
		/* empty */
	}

	/** Constructor sets up a connection and authenticates. */
	public RpcConnectionImpl(String username, String password, String host, int port, boolean secure, boolean debugf) {
	}

	/** Method that sends a call to the server and received a response; only allows one at a time */
	protected Map exec (String methname, Object[] params) {
		try {
			synchronized(this) {
				writeCall(methname, params);
				Object response = readResp();
				if (response instanceof Map) {
					return (Map)response;
				}
				else {
					Map temp = new HashMap();
					temp.put("response", response);
					return temp;
				}
			}
		}
		catch (RuntimeException rex) {
			if ("module.execute".equals(methname)) {
				/* give my code a chance to respond to the error for payload generation */
				Map temp = new HashMap();
				temp.put("error", rex.getMessage());
				return temp;
			}
			else {
				/* other code (e.g., session and console threads) depend on an exception
				   to know when they may stop */
				throw rex;
			}
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public void execute_async(String methodName) {
		execute_async(methodName, new Object[]{}, null);
	}

	public void execute_async(String methodName, Object[] args) {
		execute_async(methodName, args, null);
	}

	public void execute_async(String methodName, Object[] args, RpcCallback callback) {
		if (queue == null) {
			queue = new RpcQueue(this);
		}
		queue.execute(methodName, args, callback);
	}

	/** Runs command with no args */
	public Object execute(String methodName) throws IOException {
		return execute(methodName, new Object[]{});
	}

	protected HashMap locks = new HashMap();
	protected String  address = "";
	protected HashMap buffers = new HashMap();

	/* help implement our remote buffer API for PQS primitives */
	public ArmitageBuffer getABuffer(String key) {
		synchronized (buffers) {
			ArmitageBuffer buffer;
			if (buffers.containsKey(key)) {
				buffer = (ArmitageBuffer)buffers.get(key);
			}
			else {
				buffer = new ArmitageBuffer(16384);
				buffers.put(key, buffer);
			}
			return buffer;
		}
	}

	public String getLocalAddress() {
		return address;
	}

	/** Adds token, runs command, and notifies logger on call and return */
	public Object execute(String methodName, Object[] params) throws IOException {
		if (database != null && "db.".equals(methodName.substring(0, 3))) {
			return database.execute(methodName, params);
		} else if (hooks.containsKey(methodName)) {
            RpcConnection con = (RpcConnection)hooks.get(methodName);
            return con.execute(methodName, params);
        }
        switch (methodName) {
            case "armitage.ping":
                try {
                    long time = System.currentTimeMillis() - Long.parseLong(params[0] + "");

                    HashMap res = new HashMap();
                    res.put("result", time + "");
                    return res;
                } catch (Exception ex) {
                    HashMap res = new HashMap();
                    res.put("result", "0");
                    return res;
                }
            case "armitage.my_ip":
                HashMap res = new HashMap();
                res.put("result", address);
                return res;
            case "armitage.set_ip":
                address = params[0] + "";
                return new HashMap();
            case "armitage.lock":
                if (locks.containsKey(params[0] + "")) {
                    Map ress = new HashMap();
                    ress.put("error", "session already locked\n" + locks.get(params[0] + ""));
                    return ress;
                } else {
                    locks.put(params[0] + "", params[1]);
                }
                return new HashMap();
            case "armitage.unlock":
                locks.remove(params[0] + "");
                return new HashMap();
            case "armitage.publish": {
                ArmitageBuffer buffer = getABuffer(params[0] + "");
                buffer.put(params[1] + "");
                return new HashMap();
            }
            case "armitage.query": {
                ArmitageBuffer buffer = getABuffer(params[0] + "");
                String data = (String) buffer.get(params[1] + "");
                HashMap temp = new HashMap();
                temp.put("data", data);
                return temp;
            }
            case "armitage.reset": {
                ArmitageBuffer buffer = getABuffer(params[0] + "");
                buffer.reset();
                return new HashMap();
            }
            case "armitage.sleep":
                try {
                    Thread.sleep(Integer.parseInt(params[0] + ""));
                } catch (Exception ignored) {
                }
                return new HashMap();
            default:
                Object[] paramsNew = new Object[params.length + 1];
                paramsNew[0] = rpcToken;
                System.arraycopy(params, 0, paramsNew, 1, params.length);
                Object result = cacheExecute(methodName, paramsNew);
                return result;
        }
	}

	/** Caches certain calls and checks cache for re-executing them.
	 * If not cached or not cacheable, calls exec. */
	private Object cacheExecute(String methodName, Object[] params) throws IOException {
		if (methodName.equals("module.info") || methodName.equals("module.options") || methodName.equals("module.compatible_payloads") || methodName.equals("core.version")) {
			StringBuilder keysb = new StringBuilder(methodName);

			for(int i = 1; i < params.length; i++)
				keysb.append(params[i].toString());

			String key = keysb.toString();
			Object result = callCache.get(key);

			if(result != null)
				return result;

			result = exec(methodName, params);
			callCache.put(key, result);
			return result;
		}
		return exec(methodName, params);
	}

	protected abstract void writeCall(String methodName, Object[] args) throws Exception;
	protected abstract Object readResp() throws Exception;
}
