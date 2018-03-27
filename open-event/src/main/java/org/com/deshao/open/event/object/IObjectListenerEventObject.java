package org.com.deshao.open.event.object;

import java.util.Deque;

import org.com.deshao.open.event.ObjectEvent;

public interface IObjectListenerEventObject<V> extends IEventObject<V>{

	/**
	 * 添加某个事件类型的监听器。一个 eventType 可对应多个 object listener
	 * @param objectListener
	 * @param eventType
	 */
	public void addListener(IEventObjectListener<V> objectListener, int eventType);
	
	/**
	 * 移除指定 event type 中的一个object listener
	 * @param objectListener
	 * @param eventType
	 */
	public void removeListener(IEventObjectListener<V> objectListener, int eventType);
	
	/**
	 * listener handler the event
	 */
	public void listenerHandler(Deque<IEventObjectListener<V>> objectListeners,ObjectEvent<V> event);
	
}
