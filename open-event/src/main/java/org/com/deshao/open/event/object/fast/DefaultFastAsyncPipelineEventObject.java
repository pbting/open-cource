package org.com.deshao.open.event.object.fast;

import org.com.deshao.open.event.IDefaultAsyncEvent;
import org.com.deshao.open.event.IEventPartitioner;
import org.com.deshao.open.event.object.pipeline.IPipelineObjectListener;
import org.com.deshao.open.event.parallel.IParallelQueueExecutor;

public class DefaultFastAsyncPipelineEventObject<V> extends AbstractFastAsyncPipelineEventObject<V> implements IDefaultAsyncEvent{

	public DefaultFastAsyncPipelineEventObject(IParallelQueueExecutor superFastParallelQueueExecutor,
			boolean isOptimism) {
		super(superFastParallelQueueExecutor, isOptimism);
	}

	public DefaultFastAsyncPipelineEventObject(IParallelQueueExecutor superFastParallelQueueExecutor) {
		super(superFastParallelQueueExecutor);
	}

	@Override
	public void attachListener() {
		//nothing to do
	}

	public void subscriber(IPipelineObjectListener<V> pipelineObjectListener,int eventType){
		
		this.addLast(pipelineObjectListener, eventType);
	}
	
	@Override
	public void registerEventPartitioner(IEventPartitioner eventPartitioner) {

		defaultAsyncEventObject.registerEventPartitioner(eventPartitioner);
	}
}
