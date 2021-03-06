package org.com.deshao.open.event.object;

import java.util.Deque;

import org.com.deshao.open.event.IAsyncEventObject;
import org.com.deshao.open.event.ObjectEvent;
import org.com.deshao.open.event.parallel.IParallelQueueExecutor;
import org.com.deshao.open.event.parallel.action.Action;
import org.com.deshao.open.event.parallel.action.ActionExecuteException;
import org.com.deshao.open.event.parallel.action.FastParallelActionExecutor;
import org.com.deshao.open.event.parallel.action.IParallelActionExecutor;

/**
 * please note:if you extends this class then all of the object listener must handler the problem in concurrent environment
 * This asynchronous event handler is not recommended for high concurrency(10,000,000), 
 * and asynchronous queues are recommended to achieve the same effect.
 * this class is deprecated,please use the AbstractDisruptorEventObject class
 * @author pengbingting
 * @param <V>
 */
public abstract class AbstractAsyncEventObject<V> extends AbstractEventObject<V> implements IAsyncEventObject<V>{
	protected IParallelActionExecutor executor = null ;
	public AbstractAsyncEventObject(String executorName,boolean isOptimism) {
		super(isOptimism);
		int coreSize = Runtime.getRuntime().availableProcessors()*4;
		this.executor = new FastParallelActionExecutor(coreSize, executorName);
	}
	
	public AbstractAsyncEventObject(IParallelActionExecutor executor,boolean isOptimism) {
		super(isOptimism);
		this.executor = executor;
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
	public void listenerHandler(final Deque<IEventObjectListener<V>> eventObjectListener,final ObjectEvent<V> event) {
		
		executor.enParallelAction(partitioner(event),new Action() {
			@Override
			public void execute() throws ActionExecuteException {
				doListenerHandler(eventObjectListener, event);
			}
		});//();
	}
	
	/**
	 * 根据事件 对并行队列进行分区
	 * @param event
	 * @return
	 */
	public String partitioner(ObjectEvent<V> event){
		
		return event.getEventTopic();
	}
	
	@Override
	public void adjustExecutor(int coreSize, int maxSize) {
		this.executor.adjustPoolSize(coreSize, maxSize);
	}
	
	public void shutdown(){
		this.executor.stop();
	}
	@Override
	public void enEmergencyQueue(Runnable runnable) {
		executor.enEmergenceyQueue(runnable);
	}
	
	@Override
	public IParallelQueueExecutor getParallelQueueExecutor() {

		return this.executor;
	}
	
	@Override
	public void publish(V value, int eventType, IEventCallBack iEventCallBack) {
		
		notifyListeners(new ObjectEvent<V>(value,eventType), iEventCallBack);
	}

	@Override
	public void notifyListeners(ObjectEvent<V> objectEvent, IEventCallBack iEventCallBack) {
		
		objectEvent.setParameter(ObjectEvent.EVENT_CALLBACK, iEventCallBack);
		notifyListeners(objectEvent);
	}

}
