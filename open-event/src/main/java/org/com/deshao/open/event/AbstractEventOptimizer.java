package org.com.deshao.open.event;

import java.util.Deque;
import java.util.EventListener;
import java.util.LinkedHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.com.deshao.open.event.common.AtomicLongMap;

/**
 * 事件对象优化器： 关于优化的一些参数
 * @author pengbingting
 *
 */
public abstract class AbstractEventOptimizer<V> {

	/**
	 * 标识获取listener 时的一个状态，如果发现这个状态值和listenersModifyStatus 一致，那说明期间没有被其他线程修改过这个listener 列表，那么这个时候直接
	 * 拿取出来触发即可
	 */
	protected AtomicLongMap<Integer> listenersModifyStatus = AtomicLongMap.create();
	protected AtomicLongMap<Integer> getListenerStatus = AtomicLongMap.create();
	protected LinkedHashMap<Integer,Deque<EventListener>> trrigerObjectListener = new LinkedHashMap<Integer, Deque<EventListener>>();
	
	protected static boolean isDebug = false;
	protected boolean isOptimism ;
	protected ReentrantLock lock = new ReentrantLock();
	
	protected int gatingYeild = 1000 ;
	
	/**
	 * 和 disruptor 相关的优化器
	 */
	
	/**
	 * 支持乐观触发和悲观触发两种模式.
	 * @param isOptimism true 表示乐观触发，false 表示悲观触发
	 */
	public AbstractEventOptimizer(boolean isOptimism){
		this.isOptimism = isOptimism;
	}
	
	public AbstractEventOptimizer(boolean isOptimism,int gatingYeild){
		this.isOptimism = isOptimism;
		this.gatingYeild = gatingYeild;
	}
	
}
