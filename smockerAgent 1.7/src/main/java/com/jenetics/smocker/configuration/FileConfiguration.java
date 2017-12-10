package com.jenetics.smocker.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.jenetics.smocker.util.MessageLogger;

public class FileConfiguration {
	
	private static final String SMOCKER_CONF_PROPERTY_FILE = "config.properties";
	private static final String SMOCKERS_HOME_DIR = ".smockers";
	static Properties configProperties = null; 
	
	static {
		try {
			String userHome = System.getProperty("user.home");
			String smockerHomeDir = userHome + "/" + SMOCKERS_HOME_DIR;
			boolean homeFolderExists = new File(smockerHomeDir).exists();
			if (!homeFolderExists) {
				homeFolderExists = (new File(smockerHomeDir)).mkdirs();
			}
			String propertieFilePath = smockerHomeDir + SMOCKERS_HOME_DIR + "/" + SMOCKER_CONF_PROPERTY_FILE;
			File confpropertyFile = new File(propertieFilePath);
			if (homeFolderExists && !confpropertyFile.exists()) {
				confpropertyFile.createNewFile();
			}
			InputStream input = new FileInputStream(SMOCKER_CONF_PROPERTY_FILE);
			configProperties.load(input);
		} catch (Exception e) {
			MessageLogger.logThrowable(e);
		}
	}
	
	
	
}
