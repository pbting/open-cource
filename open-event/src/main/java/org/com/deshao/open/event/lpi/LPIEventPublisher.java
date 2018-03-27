package org.com.deshao.open.event.lpi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Pattern;

import org.com.deshao.open.event.ObjectEvent;
import org.com.deshao.open.event.common.ConcurrentHashSet;
import org.com.deshao.open.event.common.Log;
import org.com.deshao.open.event.exception.NoRelatedEventGroupException;
import org.com.deshao.open.event.exception.NoRelatedEventListenerException;
import org.com.deshao.open.event.object.AbstractAsyncEventObject;
import org.com.deshao.open.event.object.DefaultAsyncEventObject;
import org.com.deshao.open.event.object.DefaultEventObject;
import org.com.deshao.open.event.object.FutureObjectEvent;
import org.com.deshao.open.event.object.IEventObjectListener;
import org.com.deshao.open.event.object.pipeline.AbstractAsyncPipelineEventObject;
import org.com.deshao.open.event.object.pipeline.DefaultAsyncPipelineEventObject;
import org.com.deshao.open.event.object.pipeline.DefaultPipelineEventObject;
import org.com.deshao.open.event.object.pipeline.IPipelineObjectListener;
import org.com.deshao.open.event.parallel.action.ParallelActionExecutor;

/**
 * LPI means listener provider implement 
 * @author pengbingting
 *
 */
public final class LPIEventPublisher {

	private static final Pattern NAME_SEPARATOR = Pattern.compile("\\s*[,]+\\s*");
	private static final Pattern EVENTGROUP_SEPARATOR = Pattern.compile("\\s*[;]+\\s*");
	
	//----------------------------------------------//
	private static final String LISTENER_DIRECTORY = "META-INF/listeners/" ;
	
	private static final String LISTENER_FILE_SUFFIX = ".properties" ;
	//corePoolSize, maxPoolSize
	private static final String ASYNC_EVENT_GROUP = "async.event.topic.group.";
//	private static final String ASYNC_EVENT_GROUP_PIPELINE = "async.event.topic.group.pipeline.";
	
	//定义前置事件监听器和后置事件监听器 的特定配置属性。
	private static final String FRONT_EVENT_LISTENER = "front.event.listener";
	private static final String POST_EVENT_LISTENER = "post.event.listener";
	private static final String FRONT_EVENT_LISTENER_PIPELINE = "front.event.listener.pipeline";
	private static final String POST_EVENT_LISTENER_PIPELINE = "post.event.listener.pipeline";
	
	private static final int ASYNC_EVENT_THREAD_CORE_SIZE = 5;
	private static final int ASYNC_EVENT_THREAD_MAX_SIZE = 10;
	
	//----------------------------------------------//
	private static final ConcurrentHashMap<String, Object> baseParameterRegister = new ConcurrentHashMap<>();
	private final static ConcurrentHashMap<Integer, ConcurrentLinkedQueue<EventListener>> eventObjectListenerRegister = new ConcurrentHashMap<>();
	private final static ConcurrentHashMap<String, Integer> eventTopicToNumRegister = new ConcurrentHashMap<>();
	
	@SuppressWarnings("rawtypes")
	private final static DefaultEventObject eventObject = new DefaultEventObject<>();
	@SuppressWarnings("rawtypes")
	private final static DefaultPipelineEventObject pipelineEventObject = new DefaultPipelineEventObject<>();
	
	private final static ConcurrentHashMap<String, AbstractAsyncEventObject<?>> eventTopicAsyncObjectRegister = new ConcurrentHashMap<>();
	private final static ConcurrentHashMap<String, AbstractAsyncPipelineEventObject<?>> eventTopicAsyncPipelineRegister = new ConcurrentHashMap<>();
	//----------------------------------------------//
	//event topic group
	private final static ConcurrentHashMap<String, ConcurrentHashSet<String>> eventGroupToTopicRegister = new ConcurrentHashMap<>();
	private final static ConcurrentHashMap<String, String> eventTopicToGroupRegister = new ConcurrentHashMap<>();
	private final static ConcurrentHashMap<String, ParallelActionExecutor> eventGroupIsolationExecutor = new ConcurrentHashMap<>();
	//---------------------------------
	
	static{
		
		loadEventListener(IEventObjectListener.class,LISTENER_DIRECTORY);
		loadEventListener(IPipelineObjectListener.class, LISTENER_DIRECTORY);
		postLoadListenerProcess();
		addShutdownHook();
	}
	
	private LPIEventPublisher() {}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static <T> void loadEventListener(Class<T> listenerType, String dir){
		String fileName = dir + listenerType.getSimpleName() + LISTENER_FILE_SUFFIX;
		Enumeration<URL> urls ;
		ClassLoader classLoader = findClassLoader();
		try {
			if (classLoader != null) {
			    urls = classLoader.getResources(fileName);
			} else {
			    urls = ClassLoader.getSystemResources(fileName);
			}
			
			if(urls!=null){
				while(urls.hasMoreElements()){
					URL url = urls.nextElement();
					 try {
						BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "utf-8"));
						String line ;
						while((line = reader.readLine()) != null){
//							System.err.println(line);
							 final int ci = line.indexOf('#');
                             if (ci >= 0) {//跳过注释行
                            	 continue;
                             }
                             line = line.trim();
                             
                             if (line.length() <= 0) {
                            	 continue;
                             }

                        	 String name = null;
                             int i = line.indexOf('=');
                             if (i > 0) {
                                 name = line.substring(0, i).trim();
                                 line = line.substring(i + 1).trim();
                             }
                             
                             if (line.length() <= 0) {
                            	 continue;
                             }
                             

                        	 if(name.startsWith(ASYNC_EVENT_GROUP)){
                        		 processEventTopicGroup(name, line);
                        		 continue;
                        	 }
                        	 
                        	 //处理前置事件处理器
                        	 if(name.startsWith(FRONT_EVENT_LISTENER)){
                        		 String[] frontEventListener = NAME_SEPARATOR.split(line);
                        		 if(frontEventListener.length>1){
                        			 //前置 事件监听器只允许配置一个，
                        			 throw new IllegalStateException("the front event listener only configure one");
                        		 }
                        		 EventListener eventListener = constructEventObjectListener(frontEventListener[0]);
                        		 if(eventListener!=null){
                        			 if(FRONT_EVENT_LISTENER_PIPELINE.equals(name)){
                        				 if(!(eventListener instanceof IPipelineObjectListener)){
                        					 throw new IllegalStateException("the front event listener configure error.expect "+IPipelineObjectListener.class.getName()+" instance but is ["+eventListener.getClass().getName()+"]");
                        				 }
                        				 baseParameterRegister.put(FRONT_EVENT_LISTENER_PIPELINE, eventListener);
                        			 }else{
                        				 if(!(eventListener instanceof IEventObjectListener)){
                        					 throw new IllegalStateException("the front event listener configure error.expect "+IEventObjectListener.class.getName()+" instance but is ["+eventListener.getClass().getName()+"]");
                        				 }
                        				 baseParameterRegister.put(FRONT_EVENT_LISTENER,eventListener);
                        			 }
                        		 }
                        		 continue;
                        	 }
                        	 
                        	 //处理 后置 事件监听器
                        	 if(name.startsWith(POST_EVENT_LISTENER)){
                        		 String[] postEventListener = NAME_SEPARATOR.split(line);
                        		 if(postEventListener.length>1){
                        			 //前置 事件监听器只允许配置一个，
                        			 throw new IllegalStateException("the post event listener only configure one");
                        		 }
                        		 EventListener eventListener = constructEventObjectListener(postEventListener[0]);
                        		 if(eventListener != null){
                        			 if(POST_EVENT_LISTENER_PIPELINE.equals(name)){
                        				 baseParameterRegister.put(POST_EVENT_LISTENER_PIPELINE, eventListener);
                        			 }else{
                        				 baseParameterRegister.put(POST_EVENT_LISTENER,eventListener);
                        			 }
                        		 }
                        		 continue;
                        	 }
                        	 
                        	 String[] eventTopics = NAME_SEPARATOR.split(name);//可以多个 event topic 对应多个 event listener
                        	 String[] listeners = NAME_SEPARATOR.split(line);//允许 一个  event topic 对应多个 event listener
                             if (eventTopics != null && eventTopics.length > 0) {
                                 for (String n : eventTopics) {
                                	 int eventTopicWithCheckSum = getEventTopicCheckSum(n);
                                	 eventTopicToNumRegister.putIfAbsent(n,eventTopicWithCheckSum);
                                	 //store in memory
                                	 ConcurrentLinkedQueue eventListenerQueue = eventObjectListenerRegister.put(eventTopicWithCheckSum, new ConcurrentLinkedQueue());
                                	 if(eventListenerQueue == null){
                                		 eventListenerQueue = eventObjectListenerRegister.get(eventTopicWithCheckSum);
                                	 }
                                	 for(String listener:listeners){
                                		 EventListener eventObjectListener = constructEventObjectListener(listener);
                                		 if(eventObjectListener !=null){
                                			 eventListenerQueue.add(eventObjectListener);
                                		 }
                                	 }
                                 }
                             }
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 构造出一个有效（必须是IEventObjectListener or IPipelineObjectListener 的实现）的事件监听器
	 * @param listener
	 * @return
	 */
	private static EventListener constructEventObjectListener(String listener) {
		Class<?> clazz;
		try {
			clazz = Class.forName(listener, true, findClassLoader());
			if (!EventListener.class.isAssignableFrom(clazz)) {
				throw new IllegalStateException(
						"Error when load extension class(interface: " + IEventObjectListener.class + ", class line: " + clazz.getName()
						+ "), class " + clazz.getName() + "is not subtype of interface.");
			}
			
			if(!IEventObjectListener.class.isAssignableFrom(clazz) && !IPipelineObjectListener.class.isAssignableFrom(clazz)){
				throw new IllegalStateException(
						"Error when load extension class(interface: " + IEventObjectListener.class + ", class line: " + clazz.getName()
						+ "), class " + clazz.getName() + "is not subtype of interface.");
			}
			
			return (EventListener) clazz.newInstance();
		} catch (Exception e) {
			Log.error("construct an event object listener cause an exception",e);
		}
		return null ;
	}
	
	/**
	 * 不同的 event topic 共用一个 event group thread pool.也就是共用一个 AsyncEventObject instance.
	 * 这里是以 event group 中的 event topic 为主的。所有的event topic 都要关联一个event group.否则程序中将抛出异常
	 * @param eventGroup
	 * @param eventTopics
	 */
	private static void processEventTopicGroup(String eventGroup,String eventTopics){
		String[] eventTopicArray = EVENTGROUP_SEPARATOR.split(eventTopics);
		 if (eventTopicArray != null && eventTopicArray.length > 0) {
			 ConcurrentHashSet<String> eventTopicSet = eventGroupToTopicRegister.putIfAbsent(eventGroup, new ConcurrentHashSet<String>());
			 if(eventTopicSet == null){
				 eventTopicSet = eventGroupToTopicRegister.get(eventGroup);
			 }
			
			 eventGroupIsolationExecutor.put(eventGroup, new ParallelActionExecutor(ASYNC_EVENT_THREAD_CORE_SIZE, ASYNC_EVENT_THREAD_MAX_SIZE, 1, eventGroup));
			 
			 AbstractAsyncEventObject<?> asyncEventObject = new DefaultAsyncEventObject<>(eventGroupIsolationExecutor.get(eventGroup), true);
			 AbstractAsyncPipelineEventObject<?> asyncPipelineEventObject = new DefaultAsyncPipelineEventObject<>(true,eventGroupIsolationExecutor.get(eventGroup));
			 for(String eventTopic:eventTopicArray){
				 eventTopicSet.add(eventTopic);
				 //处理 event topic 和 event group 的映射关系
				 if(eventTopicToGroupRegister.containsKey(eventTopic)){
					 throw new IllegalStateException("the event topic["+eventTopic+"] has related two event group["+eventTopicToGroupRegister.get(eventTopic)+","+eventGroup+"]");
				 }else{
					 eventTopicToGroupRegister.put(eventTopic, eventGroup);
				 }
				 
				 /**
				  * 因为异步触发是分组了，所以多个不同的event topic 可能会对应一个 async event object。
				  * 但是异步的有两种不同的方式。走 pipeline 和非 pipeline。因此根据 定义的 event topic 来进行区分。
				  * 约定：pipeline 的 event topic 在配置文件中都已 pipeline开头。
				  */
				 if(eventTopic.startsWith("pipeline")){
					 eventTopicAsyncPipelineRegister.put(eventTopic, (DefaultAsyncPipelineEventObject<?>) asyncPipelineEventObject);
				 }else{
					 eventTopicAsyncObjectRegister.put(eventTopic, asyncEventObject);
				 }
			 }
		 }
	}
	
	/**
	 * 后置加载 处理器.装配所有的事件监听器
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void postLoadListenerProcess(){
		for(Entry<String, ConcurrentHashSet<String>> entry:eventGroupToTopicRegister.entrySet()){
			ConcurrentHashSet<String> eventTopicSet = entry.getValue();
			for(String eventTopic:eventTopicSet){
				Integer eventType = eventTopicToNumRegister.get(eventTopic);
				if(eventType == null){
					throw new NoRelatedEventListenerException("this event topic [ "+eventTopic+" ] does related any event listener.please configure in specified file.");
				}
				AbstractAsyncEventObject asyncEventObject = eventTopicAsyncObjectRegister.get(eventTopic);
				AbstractAsyncPipelineEventObject<?> asyncPipelineEventObject = eventTopicAsyncPipelineRegister.get(eventTopic);
				
				ConcurrentLinkedQueue<EventListener> objectListenerQueue = eventObjectListenerRegister.get(eventType);
				
				//1、添加前置事件监听器
				if(eventTopic.startsWith("pipeline")){
					IPipelineObjectListener iPipelineObjectListener = (IPipelineObjectListener) baseParameterRegister.get(FRONT_EVENT_LISTENER_PIPELINE);
					if(iPipelineObjectListener !=null){
						asyncPipelineEventObject.addLast(iPipelineObjectListener, eventType);
						pipelineEventObject.addLast(iPipelineObjectListener, eventType);
					}
				}else{
					IEventObjectListener frontEventListener = (IEventObjectListener)baseParameterRegister.get(FRONT_EVENT_LISTENER);
					if(frontEventListener !=null){
						asyncEventObject.addListener(frontEventListener, eventType);
						eventObject.addListener(frontEventListener,eventType);
					}
				}
				//2、添加业务 事件监听器
				for(EventListener eventListener:objectListenerQueue){
					if(eventListener instanceof IEventObjectListener){
						IEventObjectListener eventObjectListener = (IEventObjectListener) eventListener;
						asyncEventObject.addListener(eventObjectListener, eventType);
						eventObject.addListener(eventObjectListener,eventType);
					}else if(eventListener instanceof IPipelineObjectListener){
						IPipelineObjectListener eventObjectPipelineListener = (IPipelineObjectListener) eventListener;
						asyncPipelineEventObject.addLast(eventObjectPipelineListener, eventType);
						pipelineEventObject.addLast(eventObjectPipelineListener, eventType);
					}
				}
				
				//3、添加后置 事件监听器
				if(eventTopic.startsWith("pipeline")){
					IPipelineObjectListener iPipelineObjectListener = (IPipelineObjectListener) baseParameterRegister.get(POST_EVENT_LISTENER_PIPELINE);
					if(iPipelineObjectListener !=null){
						asyncPipelineEventObject.addLast(iPipelineObjectListener, eventType);
						pipelineEventObject.addLast(iPipelineObjectListener, eventType);
					}
				}else{
					IEventObjectListener postEventListener = (IEventObjectListener) baseParameterRegister.get(POST_EVENT_LISTENER);
					if(postEventListener != null){
						asyncEventObject.addListener(postEventListener, eventType);
						eventObject.addListener(postEventListener,eventType);
					}
				}
			}
		}
	}
	
	private static ClassLoader findClassLoader() {
        return LPIEventPublisher.class.getClassLoader();
    }
	
	private static int getEventTopicCheckSum(String eventTopic){
		char[] charArray = eventTopic.toCharArray();
		int checkSum = 0 ;
		for(char ch:charArray){
			checkSum += ch;
		}
		
		return checkSum;
	}
	
	private static void addShutdownHook(){
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			
			@Override
			public void run() {
				shutdown();
			}
		}));
	}
	
	/**
	 * 
	 */
	public final static void shutdown(){
		Collection<ParallelActionExecutor> executors = eventGroupIsolationExecutor.values();
		for(ParallelActionExecutor executor:executors){
			executor.stop();
		}
	}
	/**
	 * 调整指定event topic group thread pool size
	 * @param eventGroup
	 * @param newCorePoolSize
	 * @param newMaxiPoolSize
	 */
	public final static void adjustExecutor(String eventGroup,int newCorePoolSize,int newMaxiPoolSize){
		ParallelActionExecutor executor = eventGroupIsolationExecutor.get(eventGroup);
		if(executor == null){
			return ;
		}
		
		executor.adjustPoolSize(newCorePoolSize, newMaxiPoolSize);
	}
	
	/**
	 * 同步发布一个事件
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public final static <V> void publishWithSync(String eventTopic,V value){
		Integer eventType = eventTopicToNumRegister.get(eventTopic);
		if(eventType == null){
			System.err.println(eventTopic +" for listener is null");
		}
		ObjectEvent<V> objectEvent = new ObjectEvent(value, eventType);
		objectEvent.setParameter("event.topic", eventTopic);
		eventObject.notifyListeners(objectEvent);
	}
	
	/**
	 * 同步发布一个事件
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public final static <V> void publishPipelineWithSync(String eventTopic,V value){
		Integer eventType = eventTopicToNumRegister.get(eventTopic);
		if(eventType == null){
			System.err.println(eventTopic +" for listener is null");
		}
		ObjectEvent<V> objectEvent = new ObjectEvent(value, eventType);
		objectEvent.setParameter("event.topic", eventTopic);
		pipelineEventObject.notifyListeners(objectEvent);
	}
	
	/**
	 * @param eventTopic 没有给当前这个event topic 关联到一个event group,则程序给出异常
	 * @param value
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public final static <V> FutureObjectEvent<V> publishWithAsync(String eventTopic,V value){
		Integer eventType = eventTopicToNumRegister.get(eventTopic);
		if(eventType == null){//一个无效的 event topic。就直接 pass
			Log.warn(eventTopic +" for listener is null.");
			return null;
		}
		
		//在有效的情况下，没有给当前这个event topic 关联到一个event group,则程序给出异常
		AbstractAsyncEventObject<V> asyncEventObject = (AbstractAsyncEventObject<V>) eventTopicAsyncObjectRegister.get(eventTopic);
		if(asyncEventObject == null){
			throw new NoRelatedEventGroupException("this event topic ["+eventTopic+"] does related any event group.please configure in specified file.");
		}
		FutureObjectEvent<V> futureObjectEvent = new FutureObjectEvent<>();
		futureObjectEvent.setValue(value);
		futureObjectEvent.setEventType(eventType);
		futureObjectEvent.setParameter("event.topic", eventTopic);
		asyncEventObject.notifyListeners(futureObjectEvent);
		return futureObjectEvent;
	}
	
	/**
	 * @param eventTopic 没有给当前这个event topic 关联到一个event group,则程序给出异常
	 * @param value
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public final static <V> FutureObjectEvent<V> publishPipelineWithAsync(String eventTopic,V value){
		Integer eventType = eventTopicToNumRegister.get(eventTopic);
		if(eventType == null){//一个无效的 event topic。就直接 pass
			Log.warn(eventTopic +" for listener is null.");
			return null;
		}
		
		//在有效的情况下，没有给当前这个event topic 关联到一个event group,则程序给出异常
		AbstractAsyncPipelineEventObject<V> asyncEventObject = (AbstractAsyncPipelineEventObject<V>) eventTopicAsyncPipelineRegister.get(eventTopic);
		if(asyncEventObject == null){
			throw new NoRelatedEventGroupException("this event topic ["+eventTopic+"] does related any event group.please configure in specified file.");
		}
		FutureObjectEvent<V> futureObjectEvent = new FutureObjectEvent<>();
		futureObjectEvent.setValue(value);
		futureObjectEvent.setEventType(eventType);
		futureObjectEvent.setParameter("event.topic", eventTopic);
		asyncEventObject.notifyListeners(futureObjectEvent);
		return futureObjectEvent;
	}
}
