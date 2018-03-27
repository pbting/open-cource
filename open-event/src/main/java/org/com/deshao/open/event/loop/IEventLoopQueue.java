package org.com.deshao.open.event.loop;

import org.com.deshao.open.event.parallel.action.IActionQueue;

public interface IEventLoopQueue<T extends Runnable> extends IActionQueue<T> {

	public void appendEvent(T event);
	
	public long getEventLoopInterval(T eventHandler);
}
