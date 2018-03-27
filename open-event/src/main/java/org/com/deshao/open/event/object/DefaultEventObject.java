package org.com.deshao.open.event.object;

public class DefaultEventObject<V> extends AbstractEventObject<V> {
	
	public DefaultEventObject() {
		super();
	}

	public DefaultEventObject(boolean isOptimism) {
		super(isOptimism);
	}

	@Override
	public void attachListener() {
		//nothing to do
	}

	public void subscriber(IEventObjectListener<V> eventObjectListener,int eventType){
		
		this.addListener(eventObjectListener, eventType);
	}
}
