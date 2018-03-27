package org.com.deshao.open.event.object.pipeline;

import org.com.deshao.open.event.IDefaultAsyncEvent;
import org.com.deshao.open.event.IEventPartitioner;
import org.com.deshao.open.event.ObjectEvent;
import org.com.deshao.open.event.parallel.action.IParallelActionExecutor;

public class DefaultAsyncPipelineEventObject<V> extends AbstractAsyncPipelineEventObject<V> implements IDefaultAsyncEvent{

	protected IEventPartitioner iEventPartitioner;
	public DefaultAsyncPipelineEventObject(boolean isOptimism, IParallelActionExecutor executor) {
		super(isOptimism, executor);
	}

	public DefaultAsyncPipelineEventObject(boolean isOptimism, String executorName) {
		super(isOptimism, executorName);
	}

	public DefaultAsyncPipelineEventObject(IParallelActionExecutor executor) {
		super(executor);
	}

	@Override
	public void attachListener() {
		//nothing to do
	}

	public void subscriber(IPipelineObjectListener<V> pipelineObjectListener,int eventType){
		
		this.addLast(pipelineObjectListener, eventType);
	}
	
	@Override
	public String partitioner(ObjectEvent<V> event) {
		if(this.iEventPartitioner != null){
			
			return this.iEventPartitioner.partitioner(event);
		}
		
		return super.partitioner(event);
	}
	
	@Override
	public void registerEventPartitioner(IEventPartitioner eventPartitioner) {
		
		this.iEventPartitioner = eventPartitioner;
	}
	
}
