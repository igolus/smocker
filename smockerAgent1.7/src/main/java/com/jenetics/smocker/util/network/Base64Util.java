package com.jenetics.smocker.util.network;

import javax.xml.bind.DatatypeConverter;

/**
 * Encode and decode in base 64
 * @author igolus
 *
 */
public class Base64Util {
	/**
	 * encode
	 * @param source
	 * @return
	 */
	public static String encode(String source) {
		return DatatypeConverter.printBase64Binary(source.getBytes());
	}
	
	public static String decode(String source) {
		return new String(DatatypeConverter.parseBase64Binary(source));
	}
}
