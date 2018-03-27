package org.com.deshao.open.event;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.com.deshao.open.event.object.AbstractAsyncEventObject;
import org.com.deshao.open.event.object.IEventObjectListener;
import org.com.deshao.open.event.parallel.IParallelQueueExecutor;
import org.com.deshao.open.event.parallel.SuperFastParallelQueueExecutor;
import org.com.deshao.open.event.parallel.action.FastParallelActionExecutor;
import org.com.deshao.open.event.parallel.action.IParallelActionExecutor;
import org.com.deshao.open.event.parallel.action.ParallelActionExecutor;

public class ParallelQueueExecutorBuilder extends AbstractAsyncEventObject<BuilderStackTrace>{
	
	private IBuilderStackTraceAdaptor iBuilderStackTraceAdaptor; 
	private final static int builderStackTraceEvent = 1 ;
	private ParallelQueueExecutorBuilder(IParallelActionExecutor executor, boolean isOptimism) {
		super(executor, isOptimism);
	}

	private static class  ParallelQueueQueueExecutorBuilderHolder{
		private final static FastParallelActionExecutor FAST_PARALLEL_QUEUE_EXECUTOR = 
				new FastParallelActionExecutor(1, ParallelQueueExecutorBuilder.class.getName());
		public final static ParallelQueueExecutorBuilder builder = 
				new ParallelQueueExecutorBuilder(FAST_PARALLEL_QUEUE_EXECUTOR,true);
	}
	
	public void registerBuilderStackTraceAdaptor(IBuilderStackTraceAdaptor iBuilderStackTraceAdaptor){
		
		final ParallelQueueExecutorBuilder builder = ParallelQueueQueueExecutorBuilderHolder.builder;
		if(builder.iBuilderStackTraceAdaptor != null){
			
			throw new UnsupportedOperationException("this builder instance has register one iBuilderStackTraceAdaptor:"+builder.iBuilderStackTraceAdaptor);
		}
		
		this.iBuilderStackTraceAdaptor = iBuilderStackTraceAdaptor;
	}
	
	public static ParallelQueueExecutorBuilder getInstance(){

		return ParallelQueueQueueExecutorBuilderHolder.builder;
	}
	
	public static ParallelQueueExecutorBuilder builder(){

		return ParallelQueueQueueExecutorBuilderHolder.builder;
	}
	
	public ParallelActionExecutor builderParallelQueueExecutor(int corePoolSize, int maxPoolSize, int keepAliveTime, String prefix){
		if(iBuilderStackTraceAdaptor != null){
			publish(getCurrentStackInfo(corePoolSize,maxPoolSize,prefix), builderStackTraceEvent);
		}
		return new ParallelActionExecutor(corePoolSize, maxPoolSize, keepAliveTime, prefix);
	}
	
	public ParallelActionExecutor builderParallelQueueExecutor(int corePoolSize, int maxPoolSize, int keepAliveTime,TimeUnit unit, String prefix){
		if(iBuilderStackTraceAdaptor != null){
			publish(getCurrentStackInfo(corePoolSize,maxPoolSize,prefix), builderStackTraceEvent);
		}
		return new ParallelActionExecutor(corePoolSize, maxPoolSize, keepAliveTime,unit,prefix);
	}
	
	public ParallelActionExecutor builderParallelQueueExecutor(int corePoolSize, int maxPoolSize, int keepAliveTime, String prefix,RejectedExecutionHandler rejectedExecutionHandler){
		if(iBuilderStackTraceAdaptor != null){
			publish(getCurrentStackInfo(corePoolSize,maxPoolSize,prefix), builderStackTraceEvent);
		}
		return new ParallelActionExecutor(corePoolSize,maxPoolSize,keepAliveTime,prefix,rejectedExecutionHandler);
	}
	
	
	public ParallelActionExecutor builderParallelQueueExecutor(int corePoolSize, int maxPoolSize, int keepAliveTime,TimeUnit unit, String prefix,RejectedExecutionHandler rejectedExecutionHandler){
		if(iBuilderStackTraceAdaptor != null){
			publish(getCurrentStackInfo(corePoolSize,maxPoolSize,prefix), builderStackTraceEvent);
		}
		return new ParallelActionExecutor(corePoolSize,maxPoolSize,keepAliveTime,unit,prefix,rejectedExecutionHandler);
	}
	
	public ParallelActionExecutor builderParallelQueueExecutor(ThreadPoolExecutor pool){
		if(iBuilderStackTraceAdaptor != null){
			publish(getCurrentStackInfo(pool.getCorePoolSize(),pool.getMaximumPoolSize(),"parallel-default"), builderStackTraceEvent);
		}
		return new ParallelActionExecutor(pool);
	}
	
	public FastParallelActionExecutor builderFastParallelQueueExecutor(int threads,String prefix){
		if(iBuilderStackTraceAdaptor != null){
			publish(getCurrentStackInfo(threads,0,prefix), builderStackTraceEvent);
		}
		return new FastParallelActionExecutor(threads, prefix);
	}
	
	public IParallelQueueExecutor builderSuperFastParallelQueueExecutor(int threads, String prefix){
		if(iBuilderStackTraceAdaptor != null){
			publish(getCurrentStackInfo(threads,0,prefix), builderStackTraceEvent);
		}
		return new SuperFastParallelQueueExecutor(threads, prefix);
	}
	
	private BuilderStackTrace getCurrentStackInfo(int corePoolSize, int maxPoolSize,String prefix){
		Thread currentThread = Thread.currentThread();
		StackTraceElement[] stackTraceElements = currentThread.getStackTrace();
		//忽略最顶层的三个堆栈，就是上游代码的堆栈信息
		int startIndex = 0 ;
		BuilderStackTrace builderStackTrace = new BuilderStackTrace();
		builderStackTrace.setCorePoolSize(corePoolSize);
		builderStackTrace.setMaxPoolSize(maxPoolSize);
		builderStackTrace.setThreadPrefix(prefix);
		for(StackTraceElement stackTraceElement:stackTraceElements){
			if(startIndex++ < 2){
				continue;
			}
			builderStackTrace.addStackInfo(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())+"; " +stackTraceElement.toString());
		}
		
		return builderStackTrace;
	}
	
	@Override
	public void attachListener() {
		this.addListener(new IEventObjectListener<BuilderStackTrace>() {
			@Override
			public void onEvent(ObjectEvent<BuilderStackTrace> event) throws Throwable {
				BuilderStackTrace builderStackTrace = event.getValue();
				iBuilderStackTraceAdaptor.builderStack(builderStackTrace);
			}
		}, builderStackTraceEvent);
	}
}
