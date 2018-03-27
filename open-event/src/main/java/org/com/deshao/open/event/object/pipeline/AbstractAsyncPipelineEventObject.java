package org.com.deshao.open.event.object.pipeline;

import java.util.Deque;

import org.com.deshao.open.event.IAsyncEventObject;
import org.com.deshao.open.event.ObjectEvent;
import org.com.deshao.open.event.object.FutureObjectEvent;
import org.com.deshao.open.event.object.IEventCallBack;
import org.com.deshao.open.event.parallel.IParallelQueueExecutor;
import org.com.deshao.open.event.parallel.action.Action;
import org.com.deshao.open.event.parallel.action.ActionExecuteException;
import org.com.deshao.open.event.parallel.action.FastParallelActionExecutor;
import org.com.deshao.open.event.parallel.action.IParallelActionExecutor;

public abstract class AbstractAsyncPipelineEventObject<V> extends PipelineAbstractEventObject<V> implements IAsyncEventObject<V> {
	protected IParallelActionExecutor executor = null ;
	public AbstractAsyncPipelineEventObject(IParallelActionExecutor executor) {
		super();
		this.executor = executor;
	}

	public AbstractAsyncPipelineEventObject(boolean isOptimism,IParallelActionExecutor executor) {
		super(isOptimism);
		this.executor = executor;
	}

	public AbstractAsyncPipelineEventObject(boolean isOptimism,String executorName) {
		super(isOptimism);
		int coreSize = Runtime.getRuntime().availableProcessors()*4;
		this.executor = new FastParallelActionExecutor(coreSize, executorName);
	}
	
	/**
	 * 提供异步模式的事件 发布
	 * @param value
	 * @param eventTopic
	 * @return
	 */
	public FutureObjectEvent<V> publishWithFuture(V value,int eventTopic){
		FutureObjectEvent<V> futureObjectEvent = new FutureObjectEvent<>();
		futureObjectEvent.setValue(value);
		futureObjectEvent.setEventType(eventTopic);
		notifyListeners(futureObjectEvent);
		return futureObjectEvent;
	}
	
	@Override
	public void listenerHandler(final Deque<IPipelineObjectListener<V>> objectListeners,final ObjectEvent<V> event) {
		executor.enParallelAction(partitioner(event),new Action() {
			@Override
			public void execute() throws ActionExecuteException {
				doListenerHandler(objectListeners, event);
			}
		});
	}

	@Override
	public String partitioner(ObjectEvent<V> event) {

		return event.getEventTopic();
	}
	
	@Override
	public void adjustExecutor(int coreSize, int maxSize) {
		executor.adjustPoolSize(coreSize, maxSize);
	}
	
	@Override
	public void enEmergencyQueue(Runnable runnable) {
		executor.enEmergenceyQueue(runnable);
	}
	
	public void shutdown(){
		this.executor.stop();
	}
	
	@Override
	public IParallelQueueExecutor getParallelQueueExecutor() {

		return this.executor;
	}
	
	@Override
	public void publish(V value, int eventType, IEventCallBack iEventCallBack) {
		
		notifyListeners(new ObjectEvent<V>(value, eventType), iEventCallBack);
	}

	@Override
	public void notifyListeners(ObjectEvent<V> objectEvent, IEventCallBack iEventCallBack) {
		objectEvent.setParameter(ObjectEvent.EVENT_CALLBACK, iEventCallBack);
		notifyListeners(objectEvent);
	}
}
