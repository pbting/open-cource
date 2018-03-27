package org.com.deshao.open.event.parallel;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.com.deshao.open.event.parallel.action.Action;

/**
 * 
 * @author pbting
 *
 */
public abstract class AbstractParallelQueueExecutor implements IParallelQueueExecutor {
	public final static int DEFAULT_QUEUE_SIZE = 8 ;
	
	private ThreadPoolExecutor emergencyQueueExecutor = new ThreadPoolExecutor(1, 1,0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());
	
	private static ScheduledExecutorService scheduledExecutorService = null;
	
	static{
		scheduledExecutorService = 
				Executors.newScheduledThreadPool(DEFAULT_QUEUE_SIZE,new DefaultThreadFactory("event-loop-scheduler-task"));
	}
	
	public AbstractParallelQueueExecutor() {
		scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				try{
					cronTrriger(true);
				}catch(Exception e){
					//nothing to do
				}
			}
		}, 30, 10,TimeUnit.SECONDS);
	}
	
	/**
	 * 
	 */
	@Override
	public void enEmergenceyQueue(Runnable command) {
		if(command instanceof Action){
			
			throw new IllegalArgumentException("the parameter of this method does not support Action instance.");
		}
		
		if(emergencyQueueExecutor.getQueue().isEmpty()){
			emergencyQueueExecutor.execute(command);
		}else{
			emergencyQueueExecutor.getQueue().offer(command);
		}
	}
	
	@Override
	public void stop() {
		emergencyQueueExecutor.shutdown();
		scheduledExecutorService.shutdown();
	}
	
	@Override
	public ScheduledExecutorService getScheduledExecutorService() {
		final ScheduledExecutorService tmp = scheduledExecutorService;
		return tmp;
	}
	
	public boolean isPowerOfTwo(int val) {
		return (val & -val) == val;
	}
	
	public interface ThreadPoolExecutorIndexAllocator {
		public final AtomicInteger indexEpocher = new AtomicInteger(1);
		
		public Integer allocator();
		
		public Integer getCurrentIndex();
	}

	public final class PowerOfTwoExecutorIndexChooser implements ThreadPoolExecutorIndexAllocator {
		private int mask ;
		
		public PowerOfTwoExecutorIndexChooser(int mask) {
			super();
			this.mask = mask;
		}

		@Override
		public Integer allocator() {
			
			return indexEpocher.getAndIncrement() & mask - 1;
		}

		@Override
		public Integer getCurrentIndex() {
			
			return indexEpocher.get();
		}
	}

	public final class GenericExecutorIndexChooser implements ThreadPoolExecutorIndexAllocator {
		private int mask ;
		public GenericExecutorIndexChooser(int mask) {
			super();
			this.mask = mask;
		}

		@Override
		public Integer allocator() {

			return Math.abs(indexEpocher.getAndIncrement() % mask);
		}

		@Override
		public Integer getCurrentIndex() {

			return indexEpocher.get();
		}
	}
	
	/**
	 * 
	 */
	@Override
	public void cronTrriger(boolean isAuto) {
		//nothing to do
	}
	
	public void resetIndexEpocher(){
		
		ThreadPoolExecutorIndexAllocator.indexEpocher.set(1);
	}
}
