package org.com.deshao.open.event;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

public class ObjectEvent<V> implements Serializable,IHelpGC{
	public final static String EVENT_TOPIC = "event.topic";
	public final static String EVENT_CALLBACK = "event.call.back";
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private V value;
	private int eventType;
	private boolean isBroken ;
	private ConcurrentHashMap<String, Object> eventContext = new ConcurrentHashMap<String, Object>();
	public ObjectEvent() {
		this(false);
	}
	public ObjectEvent(int eventType) {
		this(false);
		this.eventType = eventType;
	}
	public ObjectEvent(boolean isBroken) {
		this.isBroken = isBroken;
	}
	/**
	 * @param source 系统默认参数
	 * @param objData 自定义参数
	 * @param eventType 事件健值
	 */
	public ObjectEvent( V value, int eventType) {
		this.value = value;
		this.eventType = eventType;
	}

	public int getEventType() {
		return eventType;
	}

	public V getValue() {
		return value;
	}

	public void setValue(V value) {
		this.value = value;
	}
	public void setEventType(int eventType) {
		this.eventType = eventType;
	}
	public boolean isBroken() {
		return isBroken;
	}
	public void setBroken(boolean isBroken) {
		this.isBroken = isBroken;
	}
	
	public void setParameter(String key,Object value){
		eventContext.put(key, value);
	} 
	
	@SuppressWarnings("unchecked")
	public <T> T getParameter(String key){
		
		return (T) eventContext.get(key);
	}
	
	public String getEventTopic(){
		String eventTopic = (String) eventContext.get(EVENT_TOPIC);
		if(eventTopic != null){
			return eventTopic;
		}
		
		return String.valueOf(eventType);
	}
	
	@Override
	public void helpGC() {
		eventContext.clear();
		value = null ;
		eventContext = null ;
	}
}
