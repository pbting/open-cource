package org.com.deshao.open.event.object;

import java.util.concurrent.TimeUnit;

import org.com.deshao.open.event.ObjectEvent;
import org.com.deshao.open.event.graph.future.FutureResult;

/**
 * 
 * @author pengbingting
 *
 * @param <V>
 */
public class FutureObjectEvent<V> extends ObjectEvent<V> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private FutureResult<Object> futureResult ;
	
	public FutureObjectEvent() {
		this.futureResult = new FutureResult<>();
	}
	
	public void setResult(Object result){
		this.futureResult.setResult(result);
	}
	
	public <T> Object getResult(){
		
		return this.futureResult.getResult();
	}
	
	public <T> Object getResult(int timeOut){
		
		return this.futureResult.getResult(timeOut);
	}
	
	public <T> Object getResult(int timeOut,TimeUnit timeUnit){
		
		return this.futureResult.getResult(timeUnit.toMillis(timeOut));
	}
	
	public static void main(String[] args) {
		System.err.println(TimeUnit.SECONDS.toMillis(1));
	}
}
