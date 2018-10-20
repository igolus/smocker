package com.jenetics.smocker;

import java.io.IOException;
import java.lang.instrument.Instrumentation;

import com.jenetics.smocker.util.RessourceLoader;
import com.jenetics.smocker.util.network.RemoteServerChecker;
import com.jenetics.smocker.util.network.SmockerServer;

/**
 * Javassist agent
 * @author igolus
 *
 */
public class SmockerAgent {
	
	private static final String SMOCKER_ASCII_TXT = "smockerAscii.txt";
	
	private static SmockerServer smockerServer = null;
	
	public static SmockerServer getSmockerServer() {
		return smockerServer;
	}
	
	public static void premain(String agentArgs, Instrumentation inst) {
		
		try {
			if (smockerServer == null) {
				smockerServer = new SmockerServer();
				smockerServer.startServer();
			}
			
			RemoteServerChecker.getInstance();
			
			Runtime.getRuntime().addShutdownHook(new Thread(){public void run(){
			    smockerServer.release();
			}});
			
			
			String smockerAscii = RessourceLoader.readFile(SMOCKER_ASCII_TXT);
			System.out.println(smockerAscii);
			inst.addTransformer(new MainTransformer());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
