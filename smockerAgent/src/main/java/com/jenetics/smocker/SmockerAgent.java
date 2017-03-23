package com.jenetics.smocker;

import java.io.IOException;
import java.lang.instrument.Instrumentation;

import com.jenetics.smocker.util.RessourceLoader;

/**
 * Javassist agent
 * @author igolus
 *
 */
public class SmockerAgent {
	
	private static final String SMOCKER_ASCII_TXT = "smockerAscii.txt";
	
	public static void premain(String agentArgs, Instrumentation inst) {
		try {
			String smockerAscii = RessourceLoader.readFile(SMOCKER_ASCII_TXT);
			System.out.println(smockerAscii);
			inst.addTransformer(new MainTransformer());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
