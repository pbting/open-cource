package org.com.deshao.open.event.pool;

import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.AbandonedConfig;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.com.deshao.open.event.ObjectEvent;

@SuppressWarnings("rawtypes")
public class ObjectEventPool extends GenericObjectPool<ObjectEvent>{

	public ObjectEventPool(PooledObjectFactory<ObjectEvent> factory, GenericObjectPoolConfig config,
			AbandonedConfig abandonedConfig) {
		super(factory, config, abandonedConfig);
	}

	public ObjectEventPool(PooledObjectFactory<ObjectEvent> factory, GenericObjectPoolConfig config) {
		super(factory, config);
	}

	public ObjectEventPool(PooledObjectFactory<ObjectEvent> factory) {
		super(factory);
	}

	@Override
	public ObjectEvent borrowObject() throws Exception {

		return super.borrowObject();
	}
	
	@Override
	public void returnObject(ObjectEvent obj) {

		super.returnObject(obj);
	}
	
}
