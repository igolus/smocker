package com.jenetics.smocker;

import java.io.IOException;
import java.lang.instrument.Instrumentation;

import com.jenetics.smocker.util.MessageLogger;
import com.jenetics.smocker.util.RessourceLoader;
import com.jenetics.smocker.util.network.RemoteServerChecker;

/**
 * Javassist agent
 * @author igolus
 *
 */
public class SmockerAgent {
	private static final String SMOCKER_ASCII_TXT = "smockerAscii.txt";
	
	
	private SmockerAgent() {
		super();
	}


	public static void premain(String agentArgs, Instrumentation inst) {
		
		try {
			RemoteServerChecker.getInstance();
			String smockerAscii = RessourceLoader.readFile(SMOCKER_ASCII_TXT);
			System.out.println(smockerAscii);
			inst.addTransformer(new MainTransformer());
		} catch (IOException e) {
			MessageLogger.logThrowable(e, SmockerAgent.class);
		}
	}
}
