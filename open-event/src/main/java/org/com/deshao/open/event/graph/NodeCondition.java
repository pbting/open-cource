package org.com.deshao.open.event.graph;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 
 * @author pengbingting
 *
 */
public class NodeCondition extends AbstractNodeCondition {
	
	public NodeCondition(GraphScheduler schedule,Node<NodeCommand> observiable, Set<Node<NodeCommand>> v) {
		super(schedule,observiable, v);
	}

	@Override
	public boolean isFinished() {
		//统计 收到 大于  1 的 node 的数量
		int tmpNum = 0 ;
		for(AtomicInteger value : messageCountMap.values()){
			if(value.get()>=1){
				tmpNum++ ;
			}
		}
		return tmpNum >= messageTotal;
	}

	/**
	 * 收到 node 完成的消息数
	 * @param targetJobNode
	 */
	public void increMessageCount(Node<NodeCommand> targetJobNode){
		
		messageCountMap.get(targetJobNode).getAndIncrement();
	}
}
