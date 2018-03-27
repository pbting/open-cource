package org.com.deshao.open.event.mcache;

public interface IMissCacheHandler<K,V> {

	public V missCacheHandler(K key);
}
