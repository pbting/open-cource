package org.com.deshao.open.event;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
public class ThreadPoolExecutorTests {

	public static void main(String[] args) throws Exception {
		
		ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(6, 16, 1, TimeUnit.MINUTES,new LinkedBlockingQueue<Runnable>());
		int loop = 10000000;
		final CountDownLatch countDownLatch = new CountDownLatch(loop);
		final ReentrantLock lock = new ReentrantLock();
		long s = System.currentTimeMillis();
		for(int i=0;i<loop;i++){
			threadPoolExecutor.execute(new Runnable() {
				@Override
				public void run() {
					lock.lock();
					ArrayList<Integer> values = new ArrayList<>(100);
					for(int x=0;x<100;x++){
						values.add(x);
					}
					countDownLatch.countDown();
					lock.unlock();
				}
			});
		}
		countDownLatch.await();
		System.err.println(countDownLatch.getCount()+"cost time for "+loop+" is "+(System.currentTimeMillis()-s));
		TimeUnit.SECONDS.sleep(5);
		threadPoolExecutor.shutdown();
	}
}
