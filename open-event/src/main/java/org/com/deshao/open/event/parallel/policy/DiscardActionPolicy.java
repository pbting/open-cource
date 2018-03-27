package org.com.deshao.open.event.parallel.policy;

import java.util.concurrent.locks.ReentrantLock;

import org.com.deshao.open.event.common.Log;
import org.com.deshao.open.event.parallel.action.Action;
import org.com.deshao.open.event.parallel.action.IActionQueue;

public class DiscardActionPolicy implements IRejectedActionPolicy{

	@SuppressWarnings("rawtypes")
	@Override
	public void rejectedAction(IActionQueue actionQueue, Action action,ReentrantLock lock) {
		
		Log.debug(String.format("the action for name [%s] is discard", action.getTopicName()));
	}

}
