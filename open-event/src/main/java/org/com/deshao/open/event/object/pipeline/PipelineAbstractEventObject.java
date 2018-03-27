package org.com.deshao.open.event.object.pipeline;

import java.util.Collection;
import java.util.Deque;
import java.util.EventListener;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.locks.ReentrantLock;

import org.com.deshao.open.event.AbstractEventOptimizer;
import org.com.deshao.open.event.ObjectEvent;
import org.com.deshao.open.event.common.Log;
import org.com.deshao.open.event.object.IEventCallBack;

/**
 * 
 * 基于业务流水线的编程模式：
 * 如果你的一些业务前后有所依赖(A->B->C)，即 C 的 执行必须依赖于 B 的正确执行，而 B 的执行有依赖与 A 的正确执行。那么继承这个
 * 类是非常有意义的。实现类似于业务流水线的编程模式。将各道工序业务脱离出来。实现业务内高内聚低耦合的设计模式。
 * 高内聚的实现：即将各个相关的业务统一由一个 事件类型 给 穿线 穿起来。从而将他们一次串起来，达到高内聚的效果
 * 低耦合的实现：前后业务通过各个 pipeline object listener 进行隔离。前后耦合仅仅是发一个 true or false 来表示后一个业务(逻辑)是否需要执行
 * 【注意】 基于业务流水线的 不提供降级处理策略
 * @author pengbingting
 *
 * @param <V>
 */
public abstract class PipelineAbstractEventObject<V> extends AbstractEventOptimizer<V> implements IPipelineEventObject<V> {

	protected ConcurrentHashMap<Integer, Collection<IPipelineObjectListener<V>>> listeners;
	protected static boolean isDebug = false;
	protected ReentrantLock lock = new ReentrantLock();
	
	public PipelineAbstractEventObject(){
		this(false);
	}
	
	/**
	 * 支持乐观触发和悲观触发两种模式.
	 * @param isOptimism true 表示乐观触发，false 表示悲观触发
	 */
	public PipelineAbstractEventObject(boolean isOptimism){
		super(isOptimism);
		this.attachListener();
	}
	
	public abstract void attachListener();
	
	public void addLast(IPipelineObjectListener<V> objectListener, int eventType) {
		addListener(objectListener, eventType);
	}
	
	/**
	 * this method has deprecated,please use the notifyListeners
	 */
	public void publish(V v, int eventType) {
		ObjectEvent<V> objectEvent = new ObjectEvent<V>(v, eventType); 
		notifyListeners(objectEvent);
	}
	
	public void addListener(IPipelineObjectListener<V> objectListener, int eventType) {
		if (listeners == null) {
			lock.lock();
			try{
				if (listeners == null) {
					listeners = new ConcurrentHashMap<Integer, Collection<IPipelineObjectListener<V>>>();
				}
			}finally{
				lock.unlock();
			}
		}
		
		if (listeners.get(eventType) == null) {
			Collection<IPipelineObjectListener<V>> tempInfo = new ConcurrentLinkedDeque<IPipelineObjectListener<V>>();
			tempInfo.add(objectListener);
			listeners.put(eventType, tempInfo);
		} else {
			listeners.get(eventType).add(objectListener);
		}
		
		listenersModifyStatus.incrementAndGet(eventType);
		debugEventMsg("注册一个事件,类型为" + eventType);
	}

	public void removeListener(IPipelineObjectListener<V> objectListener, int eventType) {
		if (listeners == null)
			return;
		lock.lock();
		try{
			Collection<IPipelineObjectListener<V>> tempInfo = listeners.get(eventType);
			if(tempInfo == null){
				return ;
			}
			if(tempInfo.size()==1){
				tempInfo.clear();
				return ;
			}
			tempInfo.remove(objectListener);
		}finally{
			lock.unlock();
		}
		listenersModifyStatus.incrementAndGet(eventType);
		debugEventMsg("移除一个事件,类型为" + eventType);
	}
	
	public void removeListener(int eventType){
		Collection<IPipelineObjectListener<V>> listener = listeners.remove(eventType);
		listener.clear();
		listener = null ;
		
		listenersModifyStatus.remove(eventType);
		getListenerStatus.remove(eventType);
		Deque<EventListener> dequeue = trrigerObjectListener.remove(eventType);
		if(dequeue != null && dequeue.size() > 0){
			dequeue.clear();
		}
		dequeue = null ;
		debugEventMsg("移除一个事件,类型为" + eventType);
	}
	
	@SuppressWarnings("unchecked")
	private final void doNotify(ObjectEvent<V> event){
		@SuppressWarnings("rawtypes")
		Deque tempList = null;
		if (listeners == null){
			return;
		}
		
		int eventType = event.getEventType();
		//2、乐观触发和悲观触发控制
		if(!isOptimism){
			long modifyStatus = listenersModifyStatus.get(eventType);
			if(getListenerStatus.get(eventType) != modifyStatus){
				getListenerStatus.addAndGet(eventType, modifyStatus);
				Collection<IPipelineObjectListener<V>> tempInfo = listeners.get(eventType);
				lock.lock();
				try{
					if (tempInfo != null) {
						tempList = new ConcurrentLinkedDeque<IPipelineObjectListener<V>>(tempInfo);
						//更换新的值
						trrigerObjectListener.put(eventType, tempList);
					}
				}finally{
					lock.unlock();
				}
			}else{
				tempList = trrigerObjectListener.get(eventType);
			}
		}else{
			tempList = (Deque<IPipelineObjectListener<V>>) listeners.get(eventType);
		}
		
		if(tempList == null || tempList.isEmpty()){
			return ;
		}
		
		// 3、触发,
		listenerHandler(tempList, event);
	}
	
	public void notifyListeners(ObjectEvent<V> event) {
		doNotify(event);
	}
	
	/**
	 * 处理 单个的事件
	 */
	public void listenerHandler(Deque<IPipelineObjectListener<V>> objectListeners, ObjectEvent<V> event) {
		
		doListenerHandler(objectListeners, event);
	}
	
	protected final void doListenerHandler(Deque<IPipelineObjectListener<V>> objectListeners, ObjectEvent<V> event){
		int index = 1;
		boolean isBreak = false ;
		try {
			for (IPipelineObjectListener<V> listener : objectListeners) {
				boolean isSuccessor = listener.onEvent(event, index);
				if (!isSuccessor) {
					isBreak = true;
					break;
				}
				index++;
			}
			//1、检测是否需要触发后置事件监听器
			if(isBreak){
				IPipelineObjectListener<V> lastPipeline = objectListeners.getLast();
				if(IPostPipelineEventObjectListener.class.isAssignableFrom(lastPipeline.getClass())){
					IPostPipelineEventObjectListener<V> postListener = (IPostPipelineEventObjectListener<V>) lastPipeline;
					if(postListener.isPostEventListener()){
						lastPipeline.onEvent(event, index+1);
					}
				}
			}
			//2、触发事件回调
			Object value = event.getParameter(ObjectEvent.EVENT_CALLBACK);
			if(value != null && value instanceof IEventCallBack){
				IEventCallBack eventCallBack = (IEventCallBack) value;
				eventCallBack.eventCallBack(event);
			}
			
			//3、
			event.helpGC();
			event = null ;
		} catch (Exception e) {
			Log.error("handler the event cause an exception."+event.getValue().toString(), e);
		}
	}
	
	public void clearListener() {
		lock.lock();
		try{
			if (listeners != null) {
				listeners = null;
			}
		}finally{
			lock.unlock();
		}
	}

	protected void debugEventMsg(String msg) {
		if (isDebug) {
			System.out.println(msg);;
		}
	}

}
