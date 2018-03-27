package org.com.deshao.open.event.loop;

public interface IEventLoopHandler<T> extends Runnable{

	public boolean execute();
	
	public AbstractAsyncEventLoopGroup<T> getAsyncEventLoopGroup();
	
	public void setAsyncEventLoopGroup(AbstractAsyncEventLoopGroup<T> asyncEventLoopGroup);
}
