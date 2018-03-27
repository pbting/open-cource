package org.com.deshao.open.event.parallel;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import org.com.deshao.open.event.common.AtomicLongMap;

/**
 * 此设计模拟高速公路上车道设计。
 * n-1 条车道为正常处理车道，1条车道为应急车道。
 * 并行队列 执行器
 * 
 * 叫 SuperFastParallelQueueExecutor。是因为响应式优先。没有两层的并行队列，只有一层的并行执行队列
 *
 */
public class SuperFastParallelQueueExecutor extends AbstractParallelQueueExecutor{

	private ThreadPoolExecutor[] pools;
	// 维护一个 可以水平缩放的并行队列。并行队列的大小 n = paralleleQueue.size() + 1
	private ReentrantLock lock = new ReentrantLock(true);
	private ThreadPoolExecutorIndexAllocator threadPoolExecutorIndexChooser ;
	private ConcurrentMap<String, Integer> queueExecutorMapping = new ConcurrentHashMap<>();
	private AtomicLongMap<String> topicLastExecuteTime = AtomicLongMap.create();
	private AtomicBoolean isOperating = new AtomicBoolean(false);
	private AtomicBoolean isCronTrriger = new AtomicBoolean(false);
	/**
	 * @param prefix
	 *            线程池前缀名称
	 */
	public SuperFastParallelQueueExecutor(int threads, String prefix) {
		if(threads <=0){
			threads = DEFAULT_QUEUE_SIZE;
		}
		
		pools = new ThreadPoolExecutor[threads];
		for (int i = 0; i < threads; i++) {
			// 半个小时之后还没有任务过来，则销毁线程
			pools[i] = new ThreadPoolExecutor(0, 1, 30L, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>(),
					new DefaultThreadFactory(prefix));
		}
		
		if(isPowerOfTwo(threads)){
			threadPoolExecutorIndexChooser = new PowerOfTwoExecutorIndexChooser(pools.length);
		}else{
			threadPoolExecutorIndexChooser = new GenericExecutorIndexChooser(pools.length);
		}
	}

	@Override
	public void execute(String queueTopic, Runnable command) {
		Integer index = queueExecutorMapping.get(queueTopic);
		if(index == null){
			try {
				lock.tryLock(3, TimeUnit.SECONDS);
				isOperating.set(true);
				index = queueExecutorMapping.get(queueTopic);
				if (index == null) {
					index = threadPoolExecutorIndexChooser.allocator();
					queueExecutorMapping.put(queueTopic, index);
				} 
			}catch(Exception e){
				if(index == null){
					index = threadPoolExecutorIndexChooser.allocator();
				}
			}finally {
				isOperating.set(false);
				lock.unlock();
			}
		}
		ThreadPoolExecutor executor = pools[index];
		runing(executor, command);
		topicLastExecuteTime.put(queueTopic, System.currentTimeMillis());
	}
	
	public void stop() {
		super.stop();
		for (int i = 0; i < pools.length; i++) {
			pools[i].shutdown();
		}
	}

	private void runing(ThreadPoolExecutor executor, Runnable task) {
		if (executor.getQueue().isEmpty()) {
			executor.execute(task);
		} else {
			executor.getQueue().offer(task);// (action);
		}
	}
	
	public void execute(Runnable command) {
		execute(command.getClass().getName(), command);
	}
	
	@Override
	public void registerTopics(String... topics) {
		if(topics == null || topics.length == 0){
			return ;
		}
		
		lock.lock();
		try {
			for(String queueTopic : topics){
				Integer index = queueExecutorMapping.get(queueTopic);
				if(index != null){
					continue;
				}
				
				index = threadPoolExecutorIndexChooser.allocator();
				queueExecutorMapping.put(queueTopic, index);
			}
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void removeTopic(String topic) {
		
		queueExecutorMapping.remove(topic);
	}
	
	@Override
	public void cronTrriger(boolean isAuto) {
		if(!isCronTrriger.compareAndSet(false, true)){
			
			return ;
		}
		
		final int size = queueExecutorMapping.size();
		if(size  <= 0){
			//后面那个防止持续增长一次性的 队列，可能会导致内存撑爆。所以这里允许持续增长100个，超过100个
			return ;
		}
		
		int millisInterval = -1 ;
		if(isOperating.get()){
			if(size < 2000){
				//持续增长 小于2000 不管
				return ;
			}else{
				millisInterval = 1 ;
			}
		}else{
			//超过100 加快回收
			millisInterval = size > 100 ?  5 : 10 ;
		}
		
		try {
			for(String topic:queueExecutorMapping.keySet()){
				if(topicLastExecuteTime.get(topic) >= TimeUnit.SECONDS.toMillis(millisInterval)){
					queueExecutorMapping.remove(topic);
					topicLastExecuteTime.remove(topic);
				}
			}
			if(queueExecutorMapping.isEmpty()){
				
				super.resetIndexEpocher();
			}
		} catch (Exception e) {
			//nothing do to
		}finally{
			isCronTrriger.set(false);
		}
	}

	@Override
	public void executeOneTime(Runnable command) {
		ThreadPoolExecutor executor = pools[threadPoolExecutorIndexChooser.allocator()];
		runing(executor, command);
	}
}
