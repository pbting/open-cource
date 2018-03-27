package org.com.deshao.open.event.loop;

import org.com.deshao.open.event.IDefaultAsyncEvent;
import org.com.deshao.open.event.IEventPartitioner;
import org.com.deshao.open.event.ObjectEvent;
import org.com.deshao.open.event.object.pipeline.IPipelineObjectListener;
import org.com.deshao.open.event.parallel.IParallelQueueExecutor;

public class DefaultEventLoopGroup<V> extends AbstractAsyncEventLoopGroup<V> implements IDefaultAsyncEvent{
	
	private IEventPartitioner iEventPartitioner;
	
	public DefaultEventLoopGroup(IParallelQueueExecutor executor, boolean isOptimism) {
		super(executor, isOptimism);
	}

	public DefaultEventLoopGroup(IParallelQueueExecutor executor, long schedulerInterval) {
		super(executor, schedulerInterval);
	}

	public DefaultEventLoopGroup(String executorName, boolean isOptimism) {
		super(executorName, isOptimism);
	}

	public DefaultEventLoopGroup(String executorName, long schedulerInterval) {
		super(executorName, schedulerInterval);
	}

	@Override
	public void attachListener() {
		//nothing to do
	}
	
	/**
	 * 订阅某一个事件类型，同时添加这个事件处理的 listener
	 * @param pipelineObjectListener
	 * @param eventType
	 */
	public void subscriber(IPipelineObjectListener<V> pipelineObjectListener,int eventType){
		
		this.addLast(pipelineObjectListener, eventType);
	}

	@Override
	public String partitioner(ObjectEvent<V> objectEvent) {
		
		if(this.iEventPartitioner != null){
			
			return this.iEventPartitioner.partitioner(objectEvent);
		}
		
		return super.partitioner(objectEvent);
	}

	@Override
	public void registerEventPartitioner(IEventPartitioner eventPartitioner) {
		
		this.iEventPartitioner = eventPartitioner;
	}
}
