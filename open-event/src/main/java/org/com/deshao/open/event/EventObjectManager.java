package org.com.deshao.open.event;

import java.util.concurrent.ConcurrentHashMap;

import org.com.deshao.open.event.object.DefaultAsyncEventObject;
import org.com.deshao.open.event.object.DefaultEventObject;
import org.com.deshao.open.event.object.IEventObject;
import org.com.deshao.open.event.object.IEventObjectListener;
import org.com.deshao.open.event.object.pipeline.DefaultAsyncPipelineEventObject;
import org.com.deshao.open.event.object.pipeline.DefaultPipelineEventObject;
import org.com.deshao.open.event.object.pipeline.IPipelineObjectListener;

public final class EventObjectManager {

	private EventObjectManager(){}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private final static ConcurrentHashMap<Class<?>,IEventObject> EVENT_OBJECT_MAPPING =   new ConcurrentHashMap();
	
	private static class EventObjectHolder{
		
		@SuppressWarnings("rawtypes")
		public final static DefaultEventObject EVENTOBJECT =new DefaultEventObject(true);
	}
	
	private static class AsyncEventObjectHolder{
		
		@SuppressWarnings("rawtypes")
		public final static DefaultAsyncEventObject ASYNCEVENT_OBJECT = new DefaultAsyncEventObject("async-event", true);
	}
	
	private static class PipelineEventObjectHolder{
		
		@SuppressWarnings("rawtypes")
		public final static DefaultPipelineEventObject PIPELINE_EVENT_OBJECT = new DefaultPipelineEventObject(true);
	}
	
	private static class AsyncPipelineEventObjectHolder{
		
		@SuppressWarnings("rawtypes")
		public final static DefaultAsyncPipelineEventObject ASYNCPIPELINE_EVENTOBJECT = new DefaultAsyncPipelineEventObject(true,"async-pipeline-event");
	}
	
	@SuppressWarnings("unchecked")
	public static <V> void registerEventObjectListener(IEventObjectListener<V> objectListener,Integer eventTopic){
		
		EventObjectHolder.EVENTOBJECT.addListener(objectListener, eventTopic);
	}
	
	@SuppressWarnings("unchecked")
	public static <V> void registerAsyncEventObjectListener(IEventObjectListener<V> objectListener,Integer eventTopic){
		
		AsyncEventObjectHolder.ASYNCEVENT_OBJECT.addListener(objectListener, eventTopic);
	}
	
	@SuppressWarnings("unchecked")
	public static <V> void registerPipelineEventObjectListener(IPipelineObjectListener<V> objectListener, int eventTopic){
		
		PipelineEventObjectHolder.PIPELINE_EVENT_OBJECT.addLast(objectListener,eventTopic);
	}
	
	@SuppressWarnings("unchecked")
	public static <V> void registerAsyncPipelineEventObjectListener(IPipelineObjectListener<V> objectListener, int eventTopic){
		
		AsyncPipelineEventObjectHolder.ASYNCPIPELINE_EVENTOBJECT.addLast(objectListener, eventTopic);
	}
	
	@SuppressWarnings("unchecked")
	public static <V> void publishEventObject(V dto,int eventTopic){
	
		EventObjectHolder.EVENTOBJECT.publish(dto, eventTopic);
	}
	
	@SuppressWarnings("unchecked")
	public static <V> void publishAsyncEventObject(V dto,int eventTopic){
		
		AsyncEventObjectHolder.ASYNCEVENT_OBJECT.publish(dto, eventTopic);
	}
	
	@SuppressWarnings("unchecked")
	public static <V> void publishAsyncEventObject(ObjectEvent<V> objectEvent){
		
		AsyncEventObjectHolder.ASYNCEVENT_OBJECT.notifyListeners(objectEvent);
	}
	
	@SuppressWarnings("unchecked")
	public static <V> void publishPipelineEventObject(V dto,int eventTopic){
	
		PipelineEventObjectHolder.PIPELINE_EVENT_OBJECT.publish(dto, eventTopic);
	}
	
	@SuppressWarnings("unchecked")
	public static <V> void publishAsyncPipelineEventObject(V dto,int eventTopic){
	
		AsyncPipelineEventObjectHolder.ASYNCPIPELINE_EVENTOBJECT.publish(dto, eventTopic);
	}
	
	public static void shutdown(){
		AsyncEventObjectHolder.ASYNCEVENT_OBJECT.shutdown();
		AsyncPipelineEventObjectHolder.ASYNCPIPELINE_EVENTOBJECT.shutdown();
	}
	
	public static <V> void registerEventObject(IEventObject<V> eventObject){
		EVENT_OBJECT_MAPPING.put(eventObject.getClass(), eventObject);
	}
	
	@SuppressWarnings("unchecked")
	public static <V> void publish(Class<?> eventObjectClazz,V dto,int eventTopic){
		IEventObject<V> eventObject = EVENT_OBJECT_MAPPING.get(eventObjectClazz);
		if(eventObject == null){
			return ;
		}
		
		eventObject.publish(dto, eventTopic);
	}
}
