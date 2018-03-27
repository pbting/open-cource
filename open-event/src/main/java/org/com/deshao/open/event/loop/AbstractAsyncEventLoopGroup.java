package org.com.deshao.open.event.loop;

import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.com.deshao.open.event.IAsyncEventObject;
import org.com.deshao.open.event.ObjectEvent;
import org.com.deshao.open.event.ParallelQueueExecutorBuilder;
import org.com.deshao.open.event.object.IEventCallBack;
import org.com.deshao.open.event.object.pipeline.IPipelineObjectListener;
import org.com.deshao.open.event.object.pipeline.PipelineAbstractEventObject;
import org.com.deshao.open.event.parallel.IParallelQueueExecutor;
import org.com.deshao.open.event.parallel.action.IParallelActionExecutor;

/**
 * 
 * support for event loop groups
 * the default event loop interval time is one seconds.
 * @author pengbingting
 * 注意：不支持后置事件处理器
 * @param <E>
 */
public abstract class AbstractAsyncEventLoopGroup<E> extends PipelineAbstractEventObject<E> implements IAsyncEventObject<E>{
	protected ConcurrentHashMap<Integer, EventLoopQueue<E>> eventLoopQueueGroup = new ConcurrentHashMap<Integer, EventLoopQueue<E>>();
	protected IParallelQueueExecutor executor = null ;
	protected long schedulerInterval;//统一以毫秒为单位
	
	public AbstractAsyncEventLoopGroup(String executorName,boolean isOptimism) {
		super(isOptimism);
		int coreSize = Runtime.getRuntime().availableProcessors()*4;
		this.executor = ParallelQueueExecutorBuilder.getInstance().builderSuperFastParallelQueueExecutor(coreSize,executorName);
		this.schedulerInterval = TimeUnit.SECONDS.toMillis(1);
	}
	
	public AbstractAsyncEventLoopGroup(IParallelQueueExecutor executor,boolean isOptimism) {
		super(isOptimism);
		this.executor = executor;
		this.schedulerInterval = TimeUnit.SECONDS.toMillis(1);
	}
	
	public AbstractAsyncEventLoopGroup(String executorName,long schedulerInterval) {
		super(true);
		this.schedulerInterval = schedulerInterval;
		int coreSize = Runtime.getRuntime().availableProcessors() * 4;
		this.executor = ParallelQueueExecutorBuilder.getInstance().builderSuperFastParallelQueueExecutor(coreSize,executorName);
	}
	
	public AbstractAsyncEventLoopGroup(IParallelQueueExecutor executor,long schedulerInterval) {
		super(true);
		this.schedulerInterval = schedulerInterval;
		this.executor = executor;
	}
	
	/**
	 * then can call must add event loop queue。
	 * 更改触发的方式
	 */
	@Override
	public void listenerHandler(Deque<IPipelineObjectListener<E>> objectListeners, ObjectEvent<E> event) {
		int eventType = event.getEventType();
		EventLoopQueue<E> eventLoopQueue = eventLoopQueueGroup.get(eventType);
		if(eventLoopQueue == null){
			lock.lock();
			try {
				eventLoopQueue = eventLoopQueueGroup.get(eventType);
				if(eventLoopQueue == null){
					eventLoopQueue = new EventLoopQueue<E>(executor, this);
					eventLoopQueueGroup.put(eventType,eventLoopQueue);
				}
			} finally {
				lock.unlock();
			}
		}
		eventLoopQueue.enqueue(new EventLoopHandler<E>(eventLoopQueue,this,objectListeners,event));
	}
	
	/**
	 * 确定不需要用的事件类型，需要手动 remove 是一个很好的习惯
	 */
	@Override
	public void removeListener(int eventType) {
		super.removeListener(eventType);
		EventLoopQueue<E> eventLoopQueue = eventLoopQueueGroup.remove(eventType);
		eventLoopQueue.helpGC();
		eventLoopQueue = null ;
	}
	
	public void setSchedulerInterval(int value,TimeUnit timeUnit){
		
		this.schedulerInterval = timeUnit.toMillis(value);
	}
	
	public long getSchedulerInterval(){
		return this.schedulerInterval;
	}
	
	public void shutdown(){
		this.executor.stop();
	}
	
	public String partitioner(ObjectEvent<E> objectEvent){
		
		return objectEvent.getEventTopic();
	}
	
	/**
	 * 可以进应急队列
	 */
	@Override
	public void enEmergencyQueue(Runnable runnable) {
		
		executor.enEmergenceyQueue(runnable);
	}
	
	@Override
	public void adjustExecutor(int coreSize, int maxSize) {
		if(this.executor instanceof IParallelActionExecutor){
			IParallelActionExecutor parallelActionExecutor = (IParallelActionExecutor) this.executor;
			parallelActionExecutor.adjustPoolSize(coreSize, maxSize);
		}
	}
	
	@Override
	public void publish(E value, int eventType, IEventCallBack iEventCallBack) {
		
		notifyListeners(new ObjectEvent<E>(value, eventType), iEventCallBack);
	}

	@Override
	public void notifyListeners(ObjectEvent<E> objectEvent, IEventCallBack iEventCallBack) {
		objectEvent.setParameter(ObjectEvent.EVENT_CALLBACK, iEventCallBack);
		notifyListeners(objectEvent);
	}
	
	@Override
	public IParallelQueueExecutor getParallelQueueExecutor() {

		return this.executor;
	}
}
