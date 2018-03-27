package org.com.deshao.open.event.mcache;

import java.util.Set;

public interface IHighCacheMap<K,V> {

	/**
	 * The default initial number of table slots for this table (32). Used when not otherwise specified in constructor.
	 **/
	public static int DEFAULT_INITIAL_CAPACITY = 32;

	/**
	 * The minimum capacity. Used if a lower value is implicitly specified by either of the constructors with arguments.
	 * MUST be a power of two.
	 */
	public static final int MINIMUM_CAPACITY = 4;

	/**
	 * The maximum capacity. Used if a higher value is implicitly specified by either of the constructors with
	 * arguments. MUST be a power of two <= 1<<30.
	 */
	public static final int MAXIMUM_CAPACITY = 1 << 30;

	/**
	 * The default load factor for this table. Used when not otherwise specified in constructor, the default is 0.75f.
	 **/
	public static final float DEFAULT_LOAD_FACTOR = 0.75f;

	// 设置最大的缓存实体
	public void setMaxEntries(int newLimit);

	public int getMaxEntries();

	// 是指是否支持内存缓存
	public void setMemoryCaching(boolean memoryCaching);

	// 设置是否支持二级缓存
	public void setUnlimitedDiskCache(boolean unlimitedDiskCache);

	// 清除缓存
	public void clear();

	// 从缓存中得到一个缓存数据项
	public V get(K key);

	//根据key获取指定的value 时，如果缓存未命中，可以根据指定的 缓存未命中handler 来处，从其他地方（redis,mysql and so on）获取值
	public V get(K key,IMissCacheHandler<K, V> missCacheHandler);

	//如果在一开始设置了缓存未命中时的handler,则调用此api，会在缓存未命中时触发相关的miss cache handler
	public V getWithMissCacheHandler(K key) throws NoMissCacheHandlerException;
	
	// 像缓存容器中放入一个缓存数据项
	public V put(K key, V value);

	// 向缓存容器中移除一个缓存数据项
	public V remove(Object key);

	// 得到当前缓存容器的一个大小
	public int size();

	/**
	 * 
	 * @param key
	 */
	public void itemPut(K key);

	/**
	 * Notify any underlying algorithm that an item has been retrieved from the cache.
	 *
	 * @param key The cache key of the item that was retrieved.
	 */
	public void itemRetrieved(K key);

	/**
	 * Notify the underlying implementation that an item was removed from the cache.
	 *
	 * @param key The cache key of the item that was removed.
	 */
	public void itemRemoved(K key);

	/**
	 * The cache has reached its cacpacity and an item needs to be removed. (typically according to an algorithm such as
	 * LRU or FIFO).
	 *
	 * @return The key of whichever item was removed.
	 */
	public K removeItem(boolean isRemove);

	public Set<java.util.Map.Entry<K, V>> getAllEntrySet();

	public Set<K> getAllKeySet();
}
