package org.com.deshao.open.event.object.pipeline;

public class DefaultPipelineEventObject<V> extends PipelineAbstractEventObject<V> {

	public DefaultPipelineEventObject() {
		super();
	}

	public DefaultPipelineEventObject(boolean isOptimism) {
		super(isOptimism);
	}

	@Override
	public void attachListener() {
		//nothing to do
	}

	public void subscriber(IPipelineObjectListener<V> pipelineObjectListener,int eventType){
		
		this.addLast(pipelineObjectListener, eventType);
	}
}
