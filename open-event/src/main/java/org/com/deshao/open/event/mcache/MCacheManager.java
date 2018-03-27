package org.com.deshao.open.event.mcache;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.com.deshao.open.event.ObjectEvent;
import org.com.deshao.open.event.loop.AbstractAsyncEventLoopGroup;
import org.com.deshao.open.event.loop.EventLoopConstants;
import org.com.deshao.open.event.mcache.AbstractConcurrentCache.UpdateCacheRateEvent;
import org.com.deshao.open.event.object.pipeline.DefaultPipelineEventObject;
import org.com.deshao.open.event.object.pipeline.IPipelineObjectListener;
import org.com.deshao.open.event.parallel.IParallelQueueExecutor;

class MCacheManager<K, V> extends AbstractAsyncEventLoopGroup<Object>{
	public final static int checkCapacityEvent = 1;//单个事件循环处理的过程
	public final static int cacheRateUpdateEvent =  2;//多个事件异步串行处理，不需要多次执行
	public final static int clearCacheEvent = 3 ;
	
	private AbstractConcurrentCache<K, V> abstractConcurrentCache;
	private DefaultPipelineEventObject<Object> defaultPipelineEventObject = new DefaultPipelineEventObject<Object>(true);
	private ReentrantReadWriteLock reentrantLock = new ReentrantReadWriteLock(true);// new ReentrantLock(true);
	
	public MCacheManager(AbstractConcurrentCache<K, V> abstractConcurrentCache,IParallelQueueExecutor executor, boolean isOptimism) {
		super(executor, isOptimism);
		this.abstractConcurrentCache = abstractConcurrentCache;
	}

	{
		// 异步实时计算每个key 的响应因子。这里会有一个问题：在高并发的情况下，会产生很多这种访问日志，如果 cpu
		// 处理能力不够，那么队列里面会积压未处理的
		defaultPipelineEventObject.addLast(new ItemPutCacheRateUpdateListener(), cacheRateUpdateEvent);
		defaultPipelineEventObject.addLast(new ItemRetriveCacheRateUpdateListener(), cacheRateUpdateEvent);
		defaultPipelineEventObject.addLast(new ItemRemoveCacheRateUpdateListener(), cacheRateUpdateEvent);
	}
	@Override
	public void attachListener() {
		this.addLast(new IPipelineObjectListener<Object>() {
			@Override
			public boolean onEvent(ObjectEvent<Object> event, int listenerIndex) {
				int currentSize = abstractConcurrentCache.size(); 
				int maxEntries = abstractConcurrentCache.getMaxEntries();
				
				if((abstractConcurrentCache.getEpollCache() > 0 && (currentSize - maxEntries < 50 )) || abstractConcurrentCache.getIsClearCache()){
					event.setParameter(EventLoopConstants.EVENT_LOOP_INTERVAL_PARAM, 1);
					return false ;
				}
				
				while(removeCheck()) {
					final K key = abstractConcurrentCache.removeItem(true);
					if(key == null){
						break;
					}
					final V value = abstractConcurrentCache.remove(key,false);
					//异步触发
					abstractConcurrentCache.getiParallelQueueExecutor().execute(new Runnable() {
						@Override
						public void run() {
							abstractConcurrentCache.getiExpireKeyAdaptor().expire(key, value,abstractConcurrentCache);
						}
					});
				}
				event.setParameter(EventLoopConstants.EVENT_LOOP_INTERVAL_PARAM,TimeUnit.SECONDS.toMillis(1));
				//返回false 表示还没有结束，进行下一次 循环处理。单个事件循环处理的过程
				return false;
			}
			
		},  checkCapacityEvent);
		
		this.addLast(new IPipelineObjectListener<Object>() {
			@Override
			public boolean onEvent(ObjectEvent<Object> event, int listenerIndex) {
				for (Iterator<K> iter = abstractConcurrentCache.keySet().iterator(); iter.hasNext();) {
					K key = (K) iter.next();
					abstractConcurrentCache.itemRemoved(key);
				}
				abstractConcurrentCache.clear();
				abstractConcurrentCache.setClearFinish();
				return false;
			}
		}, clearCacheEvent);
	}

	public class ItemPutCacheRateUpdateListener implements IPipelineObjectListener<Object>{

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public boolean onEvent(ObjectEvent<Object> event, int listenerIndex) {
			UpdateCacheRateEvent rateEvent = (UpdateCacheRateEvent) event;
			if((rateEvent.operatorType & abstractConcurrentCache.ITEM_PUT_OP) > 0){
				WriteLock writerock =  reentrantLock.writeLock();
				try {
					writerock.lock();
					abstractConcurrentCache.itemPut((K) rateEvent.key);
				} finally {
					writerock.unlock();
				}
				
				abstractConcurrentCache.decrementAndGetEpollCache();;
			}
			return true;
		}
	}
	
	public class ItemRetriveCacheRateUpdateListener implements IPipelineObjectListener<Object>{

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public boolean onEvent(ObjectEvent<Object> event, int listenerIndex) {
			UpdateCacheRateEvent rateEvent = (UpdateCacheRateEvent) event;
			if((rateEvent.operatorType & abstractConcurrentCache.ITEM_RETRIVE_OP) > 0){
				WriteLock writerock =  reentrantLock.writeLock();
				try {
					writerock.lock();
					abstractConcurrentCache.itemRetrieved((K) rateEvent.key);// 使用缓存的缓存替换策略，改变其顺序
				} finally {
					writerock.unlock();
				}

				abstractConcurrentCache.decrementAndGetEpollCache();
			}
			
			return true;
		}
	}
	
	public class ItemRemoveCacheRateUpdateListener implements IPipelineObjectListener<Object>{
		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public boolean onEvent(ObjectEvent<Object> event, int listenerIndex) {
			UpdateCacheRateEvent rateEvent = (UpdateCacheRateEvent) event;
			if((rateEvent.operatorType & abstractConcurrentCache.ITEM_REMOVE_OP) > 0){
				WriteLock writerock =  reentrantLock.writeLock();
				try {
					writerock.lock();
					abstractConcurrentCache.itemRemoved((K) rateEvent.key);
				} finally {
					writerock.unlock();
				}

				abstractConcurrentCache.decrementAndGetEpollCache();
			}
			
			return true;
		}
	}
	
	@Override
	public String partitioner(ObjectEvent<Object> objectEvent) {
		String cacheTopic = abstractConcurrentCache.getCacheTopic();
		return cacheTopic;
	}
	
	/**
	 * 是否需要检测 remove 的条件触发
	 * 1、current cache entry > max entries
	 * 2、还没有其他操作cache 的线程
	 * 3、没有触发 clear cache 的操作
	 * @return
	 */
	public boolean removeCheck(){
		int currentSize = abstractConcurrentCache.size(); 
		int maxEntries = abstractConcurrentCache.getMaxEntries();
		
		return currentSize > maxEntries && (abstractConcurrentCache.getEpollCache() <=0 || (currentSize - maxEntries > 50))&& !abstractConcurrentCache.getIsClearCache();
//		return abstractConcurrentCache.size() > abstractConcurrentCache.getMaxEntries() && !abstractConcurrentCache.getIsClearCache();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void updateCacheRate(UpdateCacheRateEvent rateEvent){
		
		defaultPipelineEventObject.notifyListeners(rateEvent);
	}
	
	public void handlerExpireCacheEntry(final AbstractConcurrentCache<K, V> abstractConcurrentCache){
		ReadLock readLock = reentrantLock.readLock();
		try {
			readLock.lock();
			final K key = abstractConcurrentCache.removeItem(true);
			if(key == null){
				return ;
			}
			
			final V value = abstractConcurrentCache.remove(key,false);
			//异步触发
			abstractConcurrentCache.getiParallelQueueExecutor().execute(new Runnable() {
				@Override
				public void run() {
					abstractConcurrentCache.getiExpireKeyAdaptor().expire(key, value,abstractConcurrentCache);
				}
			});
		} finally {
			readLock.unlock();
		}
	}
}
