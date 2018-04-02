package org.com.deshao.open.event;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.com.deshao.open.event.disruptor.DisruptorParallelActionExecutor;
import org.com.deshao.open.event.parallel.action.Action;
import org.com.deshao.open.event.parallel.action.ActionExecuteException;

public class DisruptorParallelActionTests {

	public static void main(String[] args) throws Exception{
		DisruptorParallelActionExecutor disruptorParallelActionExecutor = new DisruptorParallelActionExecutor(10, 2 << 12);
		
		int loop = 10000000;
		final CountDownLatch countDownLatch = new CountDownLatch(loop);
		long s = System.currentTimeMillis();
		for(int i=0;i<loop;i++){
			disruptorParallelActionExecutor.execute("test", new Action() {
				@Override
				public void execute() throws ActionExecuteException {
					ArrayList<Integer> values = new ArrayList<>(100);
					for(int x=0;x<100;x++){
						values.add(x);
					}
					countDownLatch.countDown();
				}
			});
		}
		countDownLatch.await();
		System.err.println(countDownLatch.getCount()+"cost time for "+loop+" is "+(System.currentTimeMillis()-s));
		TimeUnit.SECONDS.sleep(5);
		disruptorParallelActionExecutor.stop();
	}
}
