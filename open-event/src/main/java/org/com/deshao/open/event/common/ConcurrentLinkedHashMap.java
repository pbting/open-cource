package org.com.deshao.open.event.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ConcurrentLinkedHashMap<K,V> extends LinkedHashMap<K, V>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static int SEGMENT_MASK = 16 ;
	private final HashMap<Integer, ReentrantReadWriteLock> conditionMapping = new HashMap<Integer, ReentrantReadWriteLock>();

	@SuppressWarnings("unchecked")
	private LinkedHashMap<K, V>[] linkedHashMapArray = new LinkedHashMap[SEGMENT_MASK+1];
	
	private AtomicInteger size = new AtomicInteger(0);
	public ConcurrentLinkedHashMap() {
		this(SEGMENT_MASK);
	}
	
	public ConcurrentLinkedHashMap(int segment) {
		ConcurrentLinkedHashMap.SEGMENT_MASK = segment;
	}
	//初始化 16 个 
	{
		for(int i=0;i< SEGMENT_MASK;i++){
			linkedHashMapArray[i] = new LinkedHashMap<K, V>();
			conditionMapping.put(i, new ReentrantReadWriteLock());
		}
	}
	
	@Override
	public V put(K key, V value) {
		size.incrementAndGet();
		int index = getIndex(key);

		LinkedHashMap<K, V> linkedHashMap = linkedHashMapArray[index];
		ReentrantReadWriteLock lock = conditionMapping.get(index);
		V v = null ;
		try {
			lock.writeLock().tryLock(3, TimeUnit.SECONDS);
			v = linkedHashMap.put(key, value);
			lock.writeLock().unlock();
		} catch (InterruptedException e) {
		}
		
		return v;
	}

	@Override
	public V get(Object key) {
		int index = getIndex(key);
		LinkedHashMap<K, V> linkedHashMap = linkedHashMapArray[index];
		ReentrantReadWriteLock lock = conditionMapping.get(index);
		V v = null ;
		lock.readLock().lock();
		v = linkedHashMap.get(key);
		lock.readLock().unlock();
		return v;
	}
	
	@Override
	public int size() {
		
		return size.get();
	}

	@Override
	public boolean isEmpty() {
		return size.get() == 0;
	}

	@Override
	public V remove(Object key) {
		int index = getIndex(key);
		LinkedHashMap<K, V> linkedHashMap = linkedHashMapArray[index];
		ReentrantReadWriteLock lock = conditionMapping.get(index);
		V v = null ;
		lock.writeLock().lock();
		v = linkedHashMap.remove(key);
		lock.writeLock().unlock();
		return v;
	}
	
	@Override
	public boolean containsKey(Object key) {
		int index = getIndex(key);
		LinkedHashMap<K, V> linkedHashMap = linkedHashMapArray[index];
		ReentrantReadWriteLock lock = conditionMapping.get(index);
		boolean v = false ;
		lock.readLock().lock();
		v = linkedHashMap.containsKey(key);
		lock.readLock().unlock();
		return v;
	}

	@Override
	public boolean containsValue(Object value) {
		boolean result = false ;
		for(int i=0;i<=SEGMENT_MASK;i++){
			LinkedHashMap<K, V> linkedHashMap = linkedHashMapArray[i];
			ReentrantReadWriteLock lock = conditionMapping.get(i);
			try{
				lock.readLock().lock();
				if(linkedHashMap.containsValue(value)){
					result = true ;
					break;
				}
			}finally{
				lock.readLock().unlock();
			}
		}
		return result;
	}

	@Override
	protected void finalize() throws Throwable {
		for(int i=0;i<=SEGMENT_MASK;i++){
			LinkedHashMap<K, V> linkedHashMap = linkedHashMapArray[i];
			ReentrantReadWriteLock lock = conditionMapping.get(i);
			try{
				lock.readLock().lock();
				linkedHashMap.clear();
				linkedHashMap = null ;
				linkedHashMapArray[i] = null;
			}finally{
				lock.readLock().unlock();
			}
		}
	}

	@Override
	public Collection<V> values() {
		Collection<V> values = new ArrayList<V>();
		for(int i=0;i<=SEGMENT_MASK;i++){
			LinkedHashMap<K, V> linkedHashMap = linkedHashMapArray[i];
			ReentrantReadWriteLock lock = conditionMapping.get(i);
			try{
				lock.readLock().lock();
				values.addAll(linkedHashMap.values());
			}finally{
				lock.readLock().unlock();
			}
		}
		return values;
	}

	@Override
	public Set<K> keySet() {
		Set<K> keySet = new HashSet<K>();
		for(int i=0;i<=SEGMENT_MASK;i++){
			LinkedHashMap<K, V> linkedHashMap = linkedHashMapArray[i];
			ReentrantReadWriteLock lock = conditionMapping.get(i);
			try{
				lock.readLock().lock();
				keySet.addAll(linkedHashMap.keySet());
			}finally{
				lock.readLock().unlock();
			}
		}
		
		return keySet;
	}

	@Override
	public void clear() {
		for(int i=0;i<=SEGMENT_MASK;i++){
			LinkedHashMap<K, V> linkedHashMap = linkedHashMapArray[i];
			ReentrantReadWriteLock lock = conditionMapping.get(i);
			try{
				lock.readLock().lock();
				linkedHashMap.clear();
			}finally{
				lock.readLock().unlock();
			}
		}
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		Set<java.util.Map.Entry<K, V>> entry = new HashSet<java.util.Map.Entry<K, V>>();
		for(int i=0;i<=SEGMENT_MASK;i++){
			LinkedHashMap<K, V> linkedHashMap = linkedHashMapArray[i];
			ReentrantReadWriteLock lock = conditionMapping.get(i);
			try{
				lock.readLock().lock();
				entry.addAll(linkedHashMap.entrySet());
			}finally{
				lock.readLock().unlock();
			}
		}
		
		return entry;
	}
	
	private int getIndex(Object key){
		
		if(isPowerOfTwo(SEGMENT_MASK )){
			return key.hashCode() & SEGMENT_MASK - 1 ;
		}else{
			return Math.abs(key.hashCode() % (SEGMENT_MASK ));
		}
	}
	
	private boolean isPowerOfTwo(int val) {
		return (val & -val) == val;
	}
	
	public static void main(String[] args) {
		
		ConcurrentLinkedHashMap<String, String> h = new ConcurrentLinkedHashMap<String, String>(4);
		System.err.println(h.getIndex("A"));
		System.err.println(h.getIndex("b"));
		System.err.println(h.getIndex("b"));
		System.err.println(h.getIndex("c"));
		System.err.println(h.getIndex("d"));
	}
}

