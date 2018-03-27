package org.com.deshao.open.event.object.pipeline;

public interface IPostPipelineEventObjectListener<V> extends IPipelineObjectListener<V> {

	/**
	 * 实现这个类 返回 true ，表示无论如何都要触发后置 listener,否则 返回false 不触发
	 * @return
	 */
	public boolean isPostEventListener();
}
