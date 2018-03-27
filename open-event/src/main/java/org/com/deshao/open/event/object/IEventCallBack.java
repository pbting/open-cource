package org.com.deshao.open.event.object;

import org.com.deshao.open.event.ObjectEvent;

public interface IEventCallBack {

	public <V> void eventCallBack(ObjectEvent<V> objectEvent);
}
