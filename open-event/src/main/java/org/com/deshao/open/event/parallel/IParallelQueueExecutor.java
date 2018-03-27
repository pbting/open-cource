package org.com.deshao.open.event.parallel;

import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;

public interface IParallelQueueExecutor extends Executor{

	/**
	 * 
	 * @param command
	 */
	public void enEmergenceyQueue(Runnable command);
	
	/**
	 * 
	 * @param topic
	 * @param command
	 */
	public void execute(String topic,Runnable command);

	/**
	 * 执行一次性的 runnable
	 */
	public void executeOneTime(Runnable command);
	
	/**
	 * 
	 */
	public void stop();

	/**
	 * 
	 * @return
	 */
	public ScheduledExecutorService getScheduledExecutorService();
	
	/**
	 * 
	 * @param topics
	 */
	public void registerTopics(String ...topics );
	
	/**
	 * 
	 * @param topic
	 */
	public void removeTopic(String topic);
	
	/**
	 * 
	 * @param isAuto 是否系统触发
	 */
	public void cronTrriger(boolean isAuto);
	
}
