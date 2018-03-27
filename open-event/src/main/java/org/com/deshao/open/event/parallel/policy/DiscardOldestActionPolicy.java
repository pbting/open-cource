package org.com.deshao.open.event.parallel.policy;

import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;

import org.com.deshao.open.event.parallel.action.Action;
import org.com.deshao.open.event.parallel.action.IActionQueue;

public class DiscardOldestActionPolicy implements IRejectedActionPolicy{

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void rejectedAction(IActionQueue actionQueue, Action action,ReentrantLock lock) {
		final Queue<Action> queue = actionQueue.getQueue();
		try {
			lock.lock();
			queue.poll();
		} finally {
			lock.unlock();
		}
		actionQueue.enqueue(action);
	}

}
