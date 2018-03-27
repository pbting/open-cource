package org.com.deshao.open.event.mcache;

public class DefaultExpireKeyAdaptor<K,V> implements IExpireKeyHandler<K,V>{

	@Override
	public void expire(K key, V value, AbstractConcurrentCache<K, V> cache) {
		
	}

}
