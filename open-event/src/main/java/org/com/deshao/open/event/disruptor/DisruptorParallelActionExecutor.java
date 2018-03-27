package org.com.deshao.open.event.disruptor;

import java.io.Serializable;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import org.com.deshao.open.event.disruptor.DisruptorParallelQueueExecutor.RunnableEvent;
import org.com.deshao.open.event.parallel.action.AbstractParallelActionExecutor;
import org.com.deshao.open.event.parallel.action.Action;
import org.com.deshao.open.event.parallel.action.ActionBridge;
import org.com.deshao.open.event.parallel.action.IActionQueue;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.EventTranslatorOneArg;
import com.lmax.disruptor.LiteBlockingWaitStrategy;
import com.lmax.disruptor.Sequence;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;

/**
 * 
 * @author pengbingting
 *
 */
public class DisruptorParallelActionExecutor extends AbstractParallelActionExecutor{
	
	private ActionEventTranslator translator = new ActionEventTranslator();
	@SuppressWarnings("rawtypes")
	private Disruptor[] disruptors = null ;
	private ThreadPoolExecutorIndexAllocator ringBufferExecutorIndexChooser ;
	private ReentrantLock lock = new ReentrantLock(true);
	private ConcurrentMap<String, IActionQueue<Action>> paralleleQueue = new ConcurrentHashMap<String, IActionQueue<Action>>();
	private AtomicBoolean isOperating = new AtomicBoolean(false);
	private AtomicBoolean isCronTrriger = new AtomicBoolean(false);
	private Sequence actionQueueSize = new Sequence(0);
	
	public DisruptorParallelActionExecutor(int queueCount, int ringBufferSize) {
		if(queueCount <=0){
			queueCount = DEFAULT_QUEUE_SIZE;
		}
		
		disruptors = new Disruptor[queueCount];
		for (int i = 0; i < queueCount; i++) {
			// 鍗婁釜灏忔椂涔嬪悗杩樻病鏈変换鍔¤繃鏉ワ紝鍒欓攢姣佺嚎绋�
			disruptors[i] = builderDisruptor(ringBufferSize);
		}
		
		if(isPowerOfTwo(queueCount)){
			ringBufferExecutorIndexChooser = new PowerOfTwoExecutorIndexChooser(queueCount);
		}else{
			ringBufferExecutorIndexChooser = new GenericExecutorIndexChooser(queueCount);
		}

	}
	@Override
	public void execute(Runnable command) {
		execute(command.getClass().getName(), command);
	}
	
	/**
	 * 濡傛灉杩欓噷涓嶅寘瑁呮垚涓�涓狝ction锛屽垯璺烻uperFastParallelQueueExecutor 娌′粈涔堝澶х殑鍖哄埆
	 */
	@Override
	public void execute(String topic, Runnable command) {
		if(!(command instanceof Action)){
			ActionBridge actionBridge = new ActionBridge(command);
			enParallelAction(topic, actionBridge);
		}else{
			Action action = (Action) command;
			enParallelAction(topic, action);
		}
	}

	@SuppressWarnings("unchecked")
	private Disruptor<ActionEvent> builderDisruptor(int ringBufferSize){
		Disruptor<ActionEvent> disruptor = 
				new Disruptor<ActionEvent>(new ActionEventFactory(),ringBufferSize, DaemonThreadFactory.INSTANCE,ProducerType.SINGLE,new LiteBlockingWaitStrategy());
		disruptor.handleEventsWith(new ActionEventHandler());
		disruptor.start();
		return disruptor;
	}
	
	public static class ActionEventTranslator implements EventTranslatorOneArg<ActionEvent, Action>{

		public void translateTo(ActionEvent event, long sequence, Action value) {
			event.setValue(value);
		}
	}
	
	public static class ActionEventFactory implements EventFactory<ActionEvent>{

		public ActionEvent newInstance() {

			return new ActionEvent();
		}
	}
	
	public static class ActionEvent implements Serializable{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		private Action value ;
		public ActionEvent() {
		}
		public Action getValue() {
			return value;
		}
		public void setValue(Action value) {
			this.value = value;
		}
	}
	
	public static class ActionEventHandler implements EventHandler<ActionEvent>{

		public void onEvent(ActionEvent arg0, long arg1, boolean arg2) throws Exception {
			Action action = arg0.getValue();
			action.run();
			arg0.setValue(action);
			action=null ;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void enParallelAction(String queueTopic, Action action) {
		//1、
		IActionQueue<Action> actionQueue = paralleleQueue.get(queueTopic);
		//2、
		if(actionQueue == null){
			lock.lock();
			try {
				isOperating.set(true);
				actionQueue = paralleleQueue.get(queueTopic);
				if (actionQueue == null) {
					int index = ringBufferExecutorIndexChooser.allocator();
					actionQueue = new DisruptorParallelActionQueue(disruptors[index],this.translator);
					paralleleQueue.put(queueTopic, actionQueue);
					actionQueueSize.addAndGet(1);
				}
			} finally {
				isOperating.set(false);
				lock.unlock();
			}
		}
		
		//3、
		action.setTopicName(queueTopic);
		
		//4、
		trrigerWithRejectActionPolicy(actionQueue, action);
	}
	
	@Override
	public void removeParallelAction(String queueTopic) {
		removeTopic(queueTopic);
	}
	
	@Override
	public void adjustPoolSize(int newCorePoolSize, int newMaxiPoolSize) {
		
		throw new UnsupportedOperationException();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void stop() {
		if(disruptors == null){
			return ;
		}
		
		for(Disruptor<RunnableEvent> disruptor :disruptors){
			disruptor.shutdown();
		}
		
		disruptors = null ;
		super.stop();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void registerTopics(String... topics) {
		if(topics == null || topics.length == 0){
			return ;
		}
		
		try {
			lock.lock();
			for(String queueTopic:topics){
				IActionQueue<Action> actionQueue = paralleleQueue.get(queueTopic);
				if(actionQueue != null){
					continue;
				}

				int index = ringBufferExecutorIndexChooser.allocator();
				actionQueue = new DisruptorParallelActionQueue(disruptors[index],this.translator);
				paralleleQueue.put(queueTopic, actionQueue);
			}
		} finally {
			lock.unlock();
		}
	}
	
	@Override
	public void removeTopic(String topic) {

		if(paralleleQueue.remove(topic) != null){
			actionQueueSize.addAndGet(-1);
		}
	}
	
	/**
	 * 定时的 remove 那些不常用的队列，防止内存持续增长 导致 OOM ERROR
	 */
	@Override
	public void cronTrriger(boolean isAuto) {
		if(!isCronTrriger.compareAndSet(false, true)){
			
			return ;
		}
		
		//1、为空或者有其他线程更改 parallel queue，则下一次检测
		if(isAuto && (actionQueueSize.get() <= 0)){
			
			return ;
		}
		//2、
		int secondsInterval = -1 ;
		if(isOperating.get()){
			if(actionQueueSize.get() < 2000){
				return ;
			}else{
				secondsInterval = 1 ;
			}
		}else{
			secondsInterval = actionQueueSize.get() > 100 ? 5 : 10 ;//根据队列大小,动态remove last interval action queue
		}
		
		try {
			//这里需要 注意：如果外层队列大小很大又是热点队列。那么这里将会花费很多的时间。联系到gc 回收。
			for(Entry<String, IActionQueue<Action>> entry : paralleleQueue.entrySet()){
				String key = entry.getKey();
				IActionQueue<Action> actionQueue = entry.getValue();
				//队列中的 action 都执行完了
				if(actionQueue.getQueue().size() <=0 &&(System.currentTimeMillis() - actionQueue.getLastActiveTime() >= TimeUnit.SECONDS.toMillis(secondsInterval))){
					paralleleQueue.remove(key);
					actionQueueSize.addAndGet(-1);
					actionQueue.helpGC();
					actionQueue = null ;
				}
			}
			//3、
			if(actionQueueSize.get() <= 0){
				
				super.resetIndexEpocher();
			}
		} catch (Exception e) {
			//nothing to do
		}finally{
			isCronTrriger.set(false);
		}
	}
	@Override
	public void executeOneTime(Runnable command) {
		
		if(command instanceof Action){
			Action action = (Action) command;
			executeOneTimeAction(action);
		}else{
			ActionBridge actionBridge = new ActionBridge(command);
			executeOneTimeAction(actionBridge);
		}
	}
	@Override
	public void executeOneTimeAction(Action action) {
		@SuppressWarnings("unchecked")
		IActionQueue<Action> actionQueue =
				new DisruptorParallelActionQueue(disruptors[ringBufferExecutorIndexChooser.allocator()],this.translator);
		actionQueue.enqueue(action);
	}

}