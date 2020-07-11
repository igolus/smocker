/*
 * Copyright 2004-2011 H2 Group. Multiple-Licensed under the H2 License,
 * Version 1.0, and under the Eclipse Public License, Version 1.0
 * (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package com.jenetics.smocker.bootstrap;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.h2.tools.Server;
import org.h2.util.StringUtils;

/**
 * This class can be used to start the H2 TCP server (or other H2 servers, for
 * example the PG server) inside a web application container such as Tomcat or
 * Jetty. It can also open a database connection.
 */
public class DbStarter implements ServletContextListener {

	private Connection conn;
	private Server server;

	private Logger logger = Logger.getLogger(DbStarter.class.getName());

	public void contextInitialized(ServletContextEvent servletContextEvent) {
		try {
			org.h2.Driver.load();

			// This will get the setting from a context-param in web.xml if defined:
			ServletContext servletContext = servletContextEvent.getServletContext();

			// Start the server if configured to do so
			String serverParams = getParameter(servletContext, "db.tcpServer", null);

			serverParams += " ";
			serverParams += "-webAllowOthers";
			serverParams += " ";
			serverParams += "-webPort";
			serverParams += " ";
			serverParams += "8888";
			serverParams += " ";

			serverParams += "-baseDir";
			serverParams += " ";
			serverParams += "C:/temp";
			serverParams += " ";

			String dbDir = System.getProperty("smocker.dbdir");
			if (dbDir != null) {
				serverParams += "-baseDir";
				serverParams += " ";
				serverParams += dbDir;
				serverParams += " ";
			}

			serverParams += "-key";
			serverParams += " ";

			serverParams += "./smockerPersistant";
			serverParams += " ";
			serverParams += "./smockerPersistant";


			if (serverParams != null) {
				String[] params = StringUtils.arraySplit(serverParams, ' ', true);
				server = Server.createTcpServer(params);
				server.start();
			}


			String url = "jdbc:h2:tcp://localhost/./smockerPersistant";
			String user = getParameter(servletContext, "db.user", "sa");
			String password = getParameter(servletContext, "db.password", "sa");
			conn = DriverManager.getConnection(url, user, password);

			servletContext.setAttribute("connection", conn);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Unable to start H2 db", e);
		}
	}

	private static String getParameter(ServletContext servletContext, String key, String defaultValue) {
		String value = servletContext.getInitParameter(key);
		return value == null ? defaultValue : value;
	}

	/**
	 * Get the connection.
	 *
	 * @return the connection
	 */
	public Connection getConnection() {
		return conn;
	}

	public void contextDestroyed(ServletContextEvent servletContextEvent) {
		//        try {
		//            Statement stat = conn.createStatement();
		//            stat.execute("SHUTDOWN");
		//            //stat.close();
		//        } catch (Exception e) {
		//        	logger.log(Level.SEVERE, "Unable to close the statement", e);
		//        }
		//        try {
		//            if (conn != null) {
		//            	conn.close();
		//            }
		//        } catch (Exception e) {
		//        	logger.log(Level.SEVERE, "Unable to close the connection", e);
		//        }
		//        if (server != null) {
		//            server.stop();
		//            server = null;
		//        }
	}

}
