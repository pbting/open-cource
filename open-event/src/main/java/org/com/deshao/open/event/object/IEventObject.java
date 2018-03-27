package org.com.deshao.open.event.object;

import org.com.deshao.open.event.ObjectEvent;

public interface IEventObject<V> {
	/**
	 * 在事件对象上面添加事件监听器
	 */
	public void attachListener();
	
	/**
	 * 移除一组 object listeners
	 * @param eventType
	 */
	public void removeListener(int eventType);
	
	/**
	 * 唤醒一组事件监听器。这组事件监听器按序执行
	 * @param event
	 */
	public void notifyListeners(ObjectEvent<V> event);
	
	/**
	 * 清除事件监听器
	 */
	public void clearListener();
	
	/**
	 * 给某个事件类型发布一个消息。这个消息会触发一组事件监听器执行
	 * @param v
	 * @param eventType
	 */
	public void publish(V v,int eventType);
	
}
