package org.com.deshao.open.event.graph;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.com.deshao.open.event.ObjectEvent;
import org.com.deshao.open.event.object.AbstractEventObject;
import org.com.deshao.open.event.object.IEventObjectListener;

public class GraphScheduler extends AbstractEventObject<Node<NodeCommand>> {
	
	private Graph graph ;
	private Map<Node<NodeCommand>, NodeCondition> nodeConditionsMap = null ;
	GraphScheduler(Graph graph){
		this.graph = graph ;
		this.initNodeCondition();
	}
	@Override
	public void attachListener() {
		this.addListener(new IEventObjectListener<Node<NodeCommand>>() {
			public void onEvent(ObjectEvent<Node<NodeCommand>> event) {
				Node<NodeCommand> nodeCmd = event.getValue();
				//得到当前这个节点的前驱节点[可能有多个]
				Set<Node<NodeCommand>> dependsNodes = (Set<Node<NodeCommand>>) graph.getAdjaNode().get(nodeCmd);
				if(dependsNodes!=null){
					//如果不为null,则向他的后继节点广播一个消息。后继节点都知道自己将收到多少个消息后便可以开始执行
					Iterator<Node<NodeCommand>> iter = dependsNodes.iterator();
					for(;iter.hasNext();){
						Node<NodeCommand> temp = (Node<NodeCommand>) iter.next();
						NodeCondition nodeCondition = nodeConditionsMap.get(temp) ;
						nodeCondition.increMessageCount(nodeCmd);//收到一个消息。进行加一操作。
						nodeCondition.handler();
					}
				}else{
					if(nodeConditionsMap.get(nodeCmd) == null){//处理只有一个节点的情况
						return ;
					}
					nodeConditionsMap.get(nodeCmd).handler();//如果是最后一个节点每次执行完就直接看消息有没有收足够，收足够了，就执行，没有收足够，就直接丢弃
				}
			}
		},4);
	}
	
	private void initNodeCondition(){
		if(graph == null){
			return ;
		}
		nodeConditionsMap = new ConcurrentHashMap<Node<NodeCommand>, NodeCondition>(graph.getVertexSet().size());
		Iterator<Node<NodeCommand>> nodes = graph.getVertexSet().iterator();
		Map<Node<NodeCommand>, Set<Node<NodeCommand>>> reverseAdjaNode = graph.getReverseAdjaNode();
		while(nodes.hasNext()){
			Node<NodeCommand> node = (Node<NodeCommand>) nodes.next();
			Set<Node<NodeCommand>> reverseNode = (Set<Node<NodeCommand>>) reverseAdjaNode.get(node);
			nodeConditionsMap.put(node,new NodeCondition(this,node,reverseNode));
		}
	}
	
	public void notify(Node<NodeCommand> node){
		ObjectEvent<Node<NodeCommand>> objectEvent = new ObjectEvent<Node<NodeCommand>>(node, 4);
		this.notifyListeners(objectEvent);
	}
}
