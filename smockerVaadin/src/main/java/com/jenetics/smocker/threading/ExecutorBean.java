package com.jenetics.smocker.threading;

import java.util.function.Consumer;

import javax.ejb.Stateless;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.logging.Logger;

@Stateless
public class ExecutorBean {
	
	private static ManagedExecutorService managedExecutorService = null;
	private static Logger logger = Logger.getLogger(ExecutorBean.class);
	
    private ExecutorBean() {
		super();
	}
    
	private static void initExecutorService() {
		if (managedExecutorService == null) {
			try {
				managedExecutorService = InitialContext.doLookup("java:comp/DefaultManagedExecutorService");
			} catch (NamingException e) {
				logger.error("Unable to get ManagedExecutorService", e);
			}
		}
	}

    public static<T> void executeAsync(T source, Consumer<T> consumer) {
    	initExecutorService();
     	Runnable runnable = new Runnable() {
			@Override
			public void run() {
				consumer.accept(source);
			}
		};
    	managedExecutorService.submit(runnable);
    }
    
    public static void executeRunnable(Runnable runnable) {
    	initExecutorService();
    	managedExecutorService.submit(runnable);
    }
}