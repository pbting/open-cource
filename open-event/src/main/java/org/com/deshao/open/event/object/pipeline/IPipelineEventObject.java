package org.com.deshao.open.event.object.pipeline;

import java.util.Deque;

import org.com.deshao.open.event.ObjectEvent;
import org.com.deshao.open.event.object.IEventObject;

public interface IPipelineEventObject<V> extends IEventObject<V>{

	/**
	 * 添加某个事件类型的监听器。一个 eventType 可对应多个 object listener
	 * @param objectListener
	 * @param eventType
	 */
	public void addListener(IPipelineObjectListener<V> objectListener, int eventType);
	
	/**
	 * 移除指定 event type 中的一个object listener
	 * @param objectListener
	 * @param eventType
	 */
	public void removeListener(IPipelineObjectListener<V> objectListener, int eventType);

	/**
	 * 在某个事件类型后面添加一个新的 对象监听器
	 * @param objectListener
	 * @param eventType
	 */
	public void addLast(IPipelineObjectListener<V> objectListener, int eventType);
	
	/**
	 * listener handler the event
	 */
	public void listenerHandler(Deque<IPipelineObjectListener<V>> objectListeners,ObjectEvent<V> event);
	
}
