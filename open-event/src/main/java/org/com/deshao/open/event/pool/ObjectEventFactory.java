package org.com.deshao.open.event.pool;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.com.deshao.open.event.ObjectEvent;

@SuppressWarnings("rawtypes")
public class ObjectEventFactory extends BasePooledObjectFactory<ObjectEvent>{

	@Override
	public ObjectEvent create() throws Exception {

		return new ObjectEvent<>();
	}

	@Override
	public PooledObject<ObjectEvent> wrap(ObjectEvent obj) {

		return new DefaultPooledObject<ObjectEvent>(obj);
	}

	@Override
	public boolean validateObject(PooledObject<ObjectEvent> p) {

		return super.validateObject(p);
	}
	
	@Override
	public void activateObject(PooledObject<ObjectEvent> p) throws Exception {
		super.activateObject(p);
	}
	
	@Override
	public void destroyObject(PooledObject<ObjectEvent> p) throws Exception {

		super.destroyObject(p);
	}
	
	@Override
	public void passivateObject(PooledObject<ObjectEvent> p) throws Exception {

		super.passivateObject(p);
	}
}
