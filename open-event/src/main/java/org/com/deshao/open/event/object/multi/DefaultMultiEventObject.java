package org.com.deshao.open.event.object.multi;

import java.util.concurrent.TimeUnit;

import org.com.deshao.open.event.ObjectEvent;
import org.com.deshao.open.event.disruptor.DisruptorParallelQueueExecutor;
import org.com.deshao.open.event.object.IEventCallBack;
import org.com.deshao.open.event.object.IEventObjectListener;
import org.com.deshao.open.event.parallel.IParallelQueueExecutor;

public class DefaultMultiEventObject<V> extends AbstractMultiEventObject<V>{

	public DefaultMultiEventObject(IParallelQueueExecutor superFastParallelQueueExecutor, boolean isOptimism) {
		super(superFastParallelQueueExecutor, isOptimism);
	}

	public DefaultMultiEventObject(IParallelQueueExecutor superFastParallelQueueExecutor) {
		super(superFastParallelQueueExecutor);
	}

	@Override
	public void attachListener() {
		//nothing to do
	}

	public static void main(String[] args) throws InterruptedException {
		DefaultMultiEventObject<String> defaultMultiEventObject =
				new DefaultMultiEventObject<>(new DisruptorParallelQueueExecutor(10, 2 << 12),true);
		
		defaultMultiEventObject.addListener(new ParallelOneEventListener(), 1);
		defaultMultiEventObject.addListener(new ParallelTwoEventListener(), 1);
		
		for(int i=0;i<1;i++){
			defaultMultiEventObject.publish("value_"+i, 1,new IEventCallBack() {
				@Override
				public <V> void eventCallBack(ObjectEvent<V> objectEvent) {
					System.err.println("------>"+this.getClass().getName()+"; "+objectEvent.getEventTopic());
				}
			});
		}
		
		TimeUnit.SECONDS.sleep(10);
		defaultMultiEventObject.shutdown();
	}
	
	public static class ParallelOneEventListener implements IEventObjectListener<String>{

		@Override
		public void onEvent(ObjectEvent<String> event) throws Throwable {
			System.err.println(this.getClass().getName());
		}
	}
	
	public static class ParallelTwoEventListener implements IEventObjectListener<String>{

		@Override
		public void onEvent(ObjectEvent<String> event) throws Throwable {
			System.out.println(this.getClass().getName());
		}
	}
}
