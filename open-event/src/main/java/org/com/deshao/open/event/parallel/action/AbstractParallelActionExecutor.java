package org.com.deshao.open.event.parallel.action;

import org.com.deshao.open.event.parallel.AbstractParallelQueueExecutor;
import org.com.deshao.open.event.parallel.policy.DirectlyCallBackPolicy;
import org.com.deshao.open.event.parallel.policy.IRejectedActionPolicy;

public abstract class AbstractParallelActionExecutor extends AbstractParallelQueueExecutor implements IParallelActionExecutor{

	private IRejectedActionPolicy rejectedExecutionHandler = new DirectlyCallBackPolicy();
	private int gatingActionQueueSize = Integer.MAX_VALUE;
	
	@Override
	public void registerRejectedActionHandler(int gatingActionQueueSize,IRejectedActionPolicy rejectedExecutionHandler) {
		// 1、
		if (gatingActionQueueSize < 0) {
			gatingActionQueueSize = Integer.MAX_VALUE / 4;
		}
		this.gatingActionQueueSize = gatingActionQueueSize;

		// 2、
		if (rejectedExecutionHandler != null) {
			this.rejectedExecutionHandler = rejectedExecutionHandler;
		}
	}
	
	@Override
	public void trrigerWithRejectActionPolicy(IActionQueue<Action> actionQueue,Action action) {
		if(actionQueue.getQueue().size() > gatingActionQueueSize){
			
			this.rejectedExecutionHandler.rejectedAction(actionQueue, action, actionQueue.getActionQueueLock());
		}else{
			actionQueue.enqueue(action);
		}
	}
}
