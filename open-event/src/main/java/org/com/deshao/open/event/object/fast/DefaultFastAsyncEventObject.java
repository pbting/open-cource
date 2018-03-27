package org.com.deshao.open.event.object.fast;

import org.com.deshao.open.event.IDefaultAsyncEvent;
import org.com.deshao.open.event.IEventPartitioner;
import org.com.deshao.open.event.object.IEventObjectListener;
import org.com.deshao.open.event.parallel.IParallelQueueExecutor;

public class DefaultFastAsyncEventObject<V> extends AbstractFastAsyncEventObject<V> implements IDefaultAsyncEvent{
	
	public DefaultFastAsyncEventObject(IParallelQueueExecutor superFastParallelQueueExecutor, boolean isOptimism) {
		super(superFastParallelQueueExecutor, isOptimism);
	}

	public DefaultFastAsyncEventObject(IParallelQueueExecutor superFastParallelQueueExecutor) {
		super(superFastParallelQueueExecutor);
	}

	@Override
	public void attachListener() {
		//nothing to do
	}
	
	public void subscriber(IEventObjectListener<V> eventObjectListener,int eventType){
		
		this.addListener(eventObjectListener, eventType);
	}

	@Override
	public void registerEventPartitioner(IEventPartitioner eventPartitioner) {
		
		defaultAsyncEventObject.registerEventPartitioner(eventPartitioner);
	}
}
