package org.com.deshao.open.event.parallel.action;

import org.com.deshao.open.event.parallel.IParallelQueueExecutor;
import org.com.deshao.open.event.parallel.policy.IRejectedActionPolicy;

public interface IParallelActionExecutor extends IParallelQueueExecutor {

	/**
	 * 
	 * @param queueTopic
	 * @param action
	 */
	public  void enParallelAction(String queueTopic,Action action);
	
	/**
	 * 
	 * @param queueTopic
	 */
	public void removeParallelAction(String queueTopic);
	
	/**
	 * 
	 * @param newCorePoolSize
	 * @param newMaxiPoolSize
	 */
	public void adjustPoolSize(int newCorePoolSize,int newMaxiPoolSize);

	/**
	 * 
	 * @param action
	 */
	public void executeOneTimeAction(Action action);
	
	/**
	 * 
	 * @param gatingActionQueueSize 门控 action 队列的大小。当超过这个大小的时候，会触发  RejectedExecutionHandler
	 * @param rejectedExecutionHandler 当超过这个大小的时候的 rejected execution handler
	 */
	public void registerRejectedActionHandler(int gatingActionQueueSize,IRejectedActionPolicy rejectedExecutionHandler);
	
	/**
	 * 
	 */
	public void trrigerWithRejectActionPolicy(IActionQueue<Action> actionQueue,Action action);
	
}
