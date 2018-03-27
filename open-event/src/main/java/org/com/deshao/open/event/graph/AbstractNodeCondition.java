package org.com.deshao.open.event.graph;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.com.deshao.open.event.BaseCondition;

public abstract class AbstractNodeCondition extends BaseCondition<Node<NodeCommand>, Set<Node<NodeCommand>>> {

	protected GraphScheduler scheduler ;
	protected ConcurrentHashMap<Node<NodeCommand>,AtomicInteger> messageCountMap = null;
	protected int messageTotal = 0;
	
	/**
	 * 
	 * @param schedule
	 * @param observiable graph 中 当前的这个 node
	 * @param v
	 */
	public AbstractNodeCondition(GraphScheduler schedule,Node<NodeCommand> observiable,Set<Node<NodeCommand>> v) {
		super(observiable, v);
		this.scheduler = schedule ;
		if(v!=null){
			this.messageCountMap = new ConcurrentHashMap<Node<NodeCommand>, AtomicInteger>(v.size());
			this.messageTotal = v.size();
			Iterator<Node<NodeCommand>> iter = getValue().iterator();
			while(iter.hasNext()){
				Node<NodeCommand> tmp = iter.next();
				messageCountMap.put(tmp, new AtomicInteger(0));
			}
		}
	}

	@Override
	public abstract boolean isFinished() ;

	public abstract void increMessageCount(Node<NodeCommand> targetJobNode);
	
	@Override
	public void handler() {
		if(this.isFinished()){
			//一次消息消费成功，对每一个前驱节点的消息数减一。
			for(AtomicInteger value : messageCountMap.values()){
				if(value.get()>=1){
					value.decrementAndGet();
				}
			}
			Node<NodeCommand> observiable = getObserviable();
			GraphUtils.submit(new GraphRunnable(observiable, scheduler));
		}
	}
}
