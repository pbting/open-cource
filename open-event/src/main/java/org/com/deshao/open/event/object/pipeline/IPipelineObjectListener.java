package org.com.deshao.open.event.object.pipeline;

import java.util.EventListener;

import org.com.deshao.open.event.ObjectEvent;

/**
 * 前驱事件监听器。即 只有当前一个事件监听器处理完后，返回 true，后面的才可以继续执行
 * @author pengbingting
 *
 * @param <V>
 */
public interface IPipelineObjectListener<V> extends EventListener {

	/**
	 * @param event an object trigger an event
	 * @param eventIndex current object listener index
	 * @return 如果返回 true,则会触发后一个事件监听器执行，否则终止执行
	 */
	public boolean onEvent(ObjectEvent<V> event,int listenerIndex);
}
