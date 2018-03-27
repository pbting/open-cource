package org.com.deshao.open.event.graph;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class Graph {

	private Node<NodeCommand> startNode ;
	 // 图中节点的集合  
	private Set<Node<NodeCommand>> vertexSet = new HashSet<Node<NodeCommand>>();  
    // 相邻的节点，纪录边   start -> end
    private Map<Node<NodeCommand>, Set<Node<NodeCommand>>> adjaNode = new ConcurrentHashMap<Node<NodeCommand>, Set<Node<NodeCommand>>>();
    // 记录： end -> start
    private Map<Node<NodeCommand>, Set<Node<NodeCommand>>> reverseAdjaNode = new ConcurrentHashMap<Node<NodeCommand>, Set<Node<NodeCommand>>>();
    
    private ReentrantLock lock = new ReentrantLock();
    
    public Graph(Node<NodeCommand> startNode) {
		super();
		this.startNode = startNode;
	}

	// 将节点加入图中  
    public boolean addNode(Node<NodeCommand> start, Node<NodeCommand> end) {  
    	//1、save all of vertex
    	lock.lock();
    	try{
	    	if (!vertexSet.contains(start)) {  
	            vertexSet.add(start);  
	        }  
	        if (!vertexSet.contains(end)) {  
	            vertexSet.add(end);  
	        } 
    	}finally{
    		lock.unlock();
    	}
        
    	//2、save the relation of  start -> end relation
        if (adjaNode.containsKey(start)  
                && adjaNode.get(start).contains(end)) {  
            return false;  
        } 
        if (adjaNode.containsKey(start)) {  
            adjaNode.get(start).add(end);  
        } else {  
            Set<Node<NodeCommand>> temp = new HashSet<Node<NodeCommand>>();  
            temp.add(end);  
            adjaNode.put(start, temp);  
        } 
        
        //3、save the relation of  end -> start relation
        if(reverseAdjaNode.containsKey(end)&&reverseAdjaNode.get(end).contains(start)){
        	return false;
        }
        if(reverseAdjaNode.containsKey(end)){
        	reverseAdjaNode.get(end).add(start);
        }else{
        	Set<Node<NodeCommand>> temp = new HashSet<Node<NodeCommand>>();
        	temp.add(start);
        	reverseAdjaNode.put(end, temp);
        }
        
        end.setPathIn(end.getPathIn()+1);
        return true;  
    }  
    
    /**
     * 得到所有的节点
     * @return
     */
    public Set<Node<NodeCommand>> getVertexSet() {
		return Collections.unmodifiableSet(vertexSet);
	}
    /**
     * 得到每一个节点的下一个节点 集合：forexample
     * 
     *   /B 一 D\  
     *  A      	F
     *   \C 一 E/
     * 
     * @return
     */
	public Map<Node<NodeCommand>, Set<Node<NodeCommand>>> getAdjaNode() {
		return Collections.unmodifiableMap(adjaNode);
	}

	/**
	 * 得到每一个节点的前驱节点 集合。
	 * 当一个节点收到一个消息时，判断这个节点是否能够触发具体的业务逻辑，需要判断是否全部收到他的前驱节点发过来的消息。这个时候是非常有用的。
	 * @return
	 */
	public Map<Node<NodeCommand>, Set<Node<NodeCommand>>> getReverseAdjaNode() {
		return Collections.unmodifiableMap(reverseAdjaNode);
	}

	public Node<NodeCommand> getStartNode() {
		return startNode;
	}

	public void setStartNode(Node<NodeCommand> startNode) {
		this.startNode = startNode;
	}

}
