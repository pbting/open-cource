package org.com.deshao.open.event.pool;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.com.deshao.open.event.ObjectEvent;

public class ObjectEventPoolBuilder {

	public static ObjectEventPool builderObjectEventPool() {

		ObjectEventPool objectEventPool = new ObjectEventPool(new ObjectEventFactory());
		return objectEventPool;
	}

	public static GenericObjectPoolConfig getGenericPoolConfig(int maxIdle, int minIdle, int maxTotal) {

		GenericObjectPoolConfig genericObjectPoolConfig = new GenericObjectPoolConfig();
		genericObjectPoolConfig.setTestOnBorrow(false);
		genericObjectPoolConfig.setTestOnBorrow(false);
		genericObjectPoolConfig.setBlockWhenExhausted(true);
		genericObjectPoolConfig.setMaxIdle(maxIdle);
		genericObjectPoolConfig.setMinIdle(minIdle);
		genericObjectPoolConfig.setMaxTotal(maxTotal);

		return genericObjectPoolConfig;
	}

	public static ObjectEventPool builderObjectEventPool(GenericObjectPoolConfig genericObjectPoolConfig) {

		ObjectEventPool objectEventPool = new ObjectEventPool(new ObjectEventFactory(), genericObjectPoolConfig);
		return objectEventPool;
	}
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		
		ObjectEventPool objectEventPool = ObjectEventPoolBuilder.builderObjectEventPool(getGenericPoolConfig(2<<8, 2<<10, 2<<12));
		try {
			ObjectEvent<String> objectEvent = objectEventPool.borrowObject();
			objectEvent.setEventType(1);
			objectEvent.setValue("event");
			System.err.println(objectEvent.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
}
