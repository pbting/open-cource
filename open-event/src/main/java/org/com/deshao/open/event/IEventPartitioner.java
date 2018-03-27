package org.com.deshao.open.event;

/**
 * 
 * @author pbting
 *
 */
public interface IEventPartitioner {

	public <V> String partitioner(ObjectEvent<V> objectEvent);
}
