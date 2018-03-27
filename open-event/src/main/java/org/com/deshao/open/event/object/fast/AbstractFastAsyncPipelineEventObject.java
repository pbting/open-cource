package org.com.deshao.open.event.object.fast;

import java.util.Deque;

import org.com.deshao.open.event.IAsyncEventObject;
import org.com.deshao.open.event.ObjectEvent;
import org.com.deshao.open.event.object.IEventCallBack;
import org.com.deshao.open.event.object.pipeline.IPipelineObjectListener;
import org.com.deshao.open.event.object.pipeline.PipelineAbstractEventObject;
import org.com.deshao.open.event.parallel.IParallelQueueExecutor;

public abstract class AbstractFastAsyncPipelineEventObject<V> extends PipelineAbstractEventObject<V> implements IAsyncEventObject<V> {

	protected DefaultAsyncEventObjectImpl<V> defaultAsyncEventObject = null ;
	
	public AbstractFastAsyncPipelineEventObject(IParallelQueueExecutor superFastParallelQueueExecutor) {
		this(superFastParallelQueueExecutor,true);
	}

	public AbstractFastAsyncPipelineEventObject(IParallelQueueExecutor superFastParallelQueueExecutor,boolean isOptimism) {
		super(isOptimism);
		this.defaultAsyncEventObject = new DefaultAsyncEventObjectImpl<>(superFastParallelQueueExecutor,this);
	}

	@Override
	public void shutdown() {
		
		defaultAsyncEventObject.shutdown();
	}

	@Override
	public void adjustExecutor(int coreSize, int maxSize) {
		
		defaultAsyncEventObject.adjustExecutor(coreSize, maxSize);
	}

	@Override
	public void enEmergencyQueue(Runnable runnable) {
		
		defaultAsyncEventObject.enEmergencyQueue(runnable);
	}

	@Override
	public void listenerHandler(final Deque<IPipelineObjectListener<V>> objectListeners,final ObjectEvent<V> event) {
		getParallelQueueExecutor().execute(partitioner(event), new Runnable() {
			@Override
			public void run() {
				doListenerHandler(objectListeners, event);
			}
		});
	}
	
	@Override
	public String partitioner(ObjectEvent<V> event) {
		
		return defaultAsyncEventObject.partitioner(event);
	}

	@Override
	public IParallelQueueExecutor getParallelQueueExecutor() {
		
		return defaultAsyncEventObject.getParallelQueueExecutor();
	}

	@Override
	public void publish(V value, int eventType, IEventCallBack iEventCallBack) {
		
		defaultAsyncEventObject.publish(value, eventType, iEventCallBack);
	}
	
	@Override
	public void notifyListeners(ObjectEvent<V> objectEvent, IEventCallBack iEventCallBack) {
		
		defaultAsyncEventObject.notifyListeners(objectEvent, iEventCallBack);
	}
	
	@Override
	public abstract void attachListener() ;

}
