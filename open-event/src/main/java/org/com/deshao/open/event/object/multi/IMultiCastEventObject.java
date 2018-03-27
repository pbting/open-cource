package org.com.deshao.open.event.object.multi;

import java.util.Deque;

import org.com.deshao.open.event.ObjectEvent;
import org.com.deshao.open.event.object.IEventObjectListener;

public interface IMultiCastEventObject<V> {

	public void multiCast(Deque<IEventObjectListener<V>> eventObjectListeners, ObjectEvent<V> event);
}
