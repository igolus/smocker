package comm;

import java.io.UnsupportedEncodingException;
import java.util.Base64;

import com.jenetics.smocker.util.SmockerContainer;

public class SocketClient {

	private SocketClient() {}
	
	private static SocketClient instance = null;
	
	public synchronized static SocketClient getSocketClient() {
		// TODO Auto-generated method stub
		if (instance == null) {
			instance = new SocketClient();
		}
		return new SocketClient();
	}

	public void sendConnectionOpen(SmockerContainer smockerContainer) {
		System.out.println(	 buildJsonConnection(
	    		smockerContainer.getHost(),
	    		smockerContainer.getPort(),
	    		smockerContainer.isSsl()));
	}

	public void sendConnectionClosed(SmockerContainer smockerContainer) throws UnsupportedEncodingException {
		System.out.println("comm \n" 
		+ "\n=====================================================================================\n"  
	    + buildCommunication(
	    		smockerContainer.getSmockerSocketOutputStream().getSmockerOutputStreamData().getString(),
	    		smockerContainer.getSmockerSocketInputStream().getSmockerOutputStreamData().getString()
	    		));
	}
	
	private String buildJsonConnection (String host, int port, boolean ssl) {
		StringBuffer sb = new StringBuffer();
		sb.append("{ \"connection\" : ")
		.append("{")
		.append(" \"host\": \"").append(host).append("\",")
		.append(" \"port\": \"").append(port).append("\",")
		.append(" \"ssl\": \"").append(ssl).append("\"")
		.append("}")
		.append("}");
		return sb.toString();
	}
	
	private String buildCommunication (String input, String output) {
		StringBuffer sb = new StringBuffer();
		sb.append("{ \"communication\" : ")
		.append("{")
		.append(" \"input\": \"").append(encode(input)).append("\",")
		.append(" \"output\": \"").append(encode(output)).append("\"")
		.append("}")
		.append("}");
		return sb.toString();
	}

	private String encode(String ret) {
		return Base64.getEncoder().encodeToString(ret.getBytes());
	}

}
