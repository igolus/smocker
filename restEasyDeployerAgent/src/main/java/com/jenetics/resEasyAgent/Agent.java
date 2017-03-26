package com.jenetics.resEasyAgent;

import java.io.IOException;
import java.lang.instrument.Instrumentation;

import com.jenetics.resEasyAgent.transformer.MainTransformer;


/**
 * Javassist agent
 * @author igolus
 *
 */
public class Agent {

	public static void premain(String agentArgs, Instrumentation inst) {
		inst.addTransformer(new MainTransformer());
	}
}
