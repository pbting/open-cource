package org.com.deshao.open.event.parallel.action;

import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;

import org.com.deshao.open.event.IHelpGC;

public interface IActionQueue<T extends Runnable> extends IHelpGC{

	public IActionQueue<T> getActionQueue();

	public void enqueue(T t);

	public void dequeue(T t);

	public void clear();

	public Queue<T> getQueue();
	
	/**
	 * 得到最后一次活动的时间。会定时的清理一些长时间未活动的队列。
	 */
	public long getLastActiveTime();
	
	/**
	 * 
	 * @param action
	 */
	public void doExecute(Runnable runnable);
	
	/**
	 * 
	 * @return
	 */
	public ReentrantLock getActionQueueLock();
}
