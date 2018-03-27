package org.com.deshao.open.event.object;

import org.com.deshao.open.event.ObjectEvent;

/**
 * 提供降级的接口实现
 * @author pengbingting
 *
 */
public interface IFallBackEventObjectListener<V> extends IEventObjectListener<V>{

	public void fallBack(ObjectEvent<V> event);
}
