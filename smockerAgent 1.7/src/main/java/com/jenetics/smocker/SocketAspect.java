package com.jenetics.smocker;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public abstract class SocketAspect {

    // abstract pointcut: no expression is defined
    @Pointcut
    abstract void scope();

    @Before("execution (* java.net.Socket.getOutputStream(..))")
    public void before(JoinPoint jp) {
    	System.out.printf("Before saying Hello ‘%s’%n", jp);
    	//TeeOutputStream test = new TeeOutputStream();
    }
    
    @Before("execution (* java.net.Socket.getInputStream(..))")
    public void beforeInputStream(JoinPoint jp) {
    	System.out.printf("Before saying Hello ‘%s’%n", jp);
    	System.out.println("logBefore() is running!");
    	//TeeOutputStream test = new TeeOutputStream();
    }
}