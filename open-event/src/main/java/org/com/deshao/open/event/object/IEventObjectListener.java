package org.com.deshao.open.event.object;

import java.util.EventListener;

import org.com.deshao.open.event.ObjectEvent;

/**
 * 定义事件处理接口。由用户真正的实现
 * @author pengbingting
 *
 * @param <V>
 */
public interface IEventObjectListener<V> extends EventListener {
	
	public void onEvent(ObjectEvent<V> event) throws Throwable;

}
