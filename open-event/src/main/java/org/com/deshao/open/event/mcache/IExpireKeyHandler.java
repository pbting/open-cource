package org.com.deshao.open.event.mcache;

public interface IExpireKeyHandler<K,V> {

	public void expire(K key,V value,AbstractConcurrentCache<K,V> cache);
}
