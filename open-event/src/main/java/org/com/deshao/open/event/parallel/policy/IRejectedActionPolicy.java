package org.com.deshao.open.event.parallel.policy;

import java.util.concurrent.locks.ReentrantLock;

import org.com.deshao.open.event.parallel.action.Action;
import org.com.deshao.open.event.parallel.action.IActionQueue;

public interface IRejectedActionPolicy {

	/**
	 * 
	 * @param actionQueue
	 * @param action
	 * @param lock
	 */
	@SuppressWarnings("rawtypes")
	public void rejectedAction(IActionQueue actionQueue,Action action,ReentrantLock lock);
}
