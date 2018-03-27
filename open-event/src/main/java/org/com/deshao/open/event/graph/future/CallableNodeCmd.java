package org.com.deshao.open.event.graph.future;

import org.com.deshao.open.event.graph.NodeCommand;

/**
 * 提供异步可回调的 graph 节点 command.我们建议最好是在构造函数中就把 FutureNodeCmd 的 instance 给传递过来
 * @author pengbingting
 *
 */
public interface CallableNodeCmd<T> extends NodeCommand{

	public T call() throws Exception;
	
	public FutureResult<T> getFutureResult();
	
	public T get();
}
