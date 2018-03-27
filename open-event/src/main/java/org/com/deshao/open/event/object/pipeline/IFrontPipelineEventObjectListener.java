package org.com.deshao.open.event.object.pipeline;

/**
 * 基于pipeline 的 前置事件监听器；
 * 前置事件监听器，在使用 LPI 的方式来发布一个事件时，可以指定一个前置事件 监听器来做一些初始化的操作。
 * 注意：前置事件监听器 只能有一个
 * @author pengbingting
 */
public interface IFrontPipelineEventObjectListener<V> extends IPipelineObjectListener<V> {

}
