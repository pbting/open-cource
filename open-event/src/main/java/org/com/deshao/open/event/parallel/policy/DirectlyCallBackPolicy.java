package org.com.deshao.open.event.parallel.policy;

import java.util.concurrent.locks.ReentrantLock;

import org.com.deshao.open.event.parallel.action.Action;
import org.com.deshao.open.event.parallel.action.ActionExecuteException;
import org.com.deshao.open.event.parallel.action.IActionQueue;

/**
 * 提交 直接回调的策略
 * @author pengbingting
 *
 */
public class DirectlyCallBackPolicy implements IRejectedActionPolicy {

	@SuppressWarnings("rawtypes")
	@Override
	public void rejectedAction(IActionQueue actionQueue, Action action,ReentrantLock lock) {
		try {
			action.execute();
		} catch (ActionExecuteException e) {
			e.printStackTrace();
		}
	}
}
