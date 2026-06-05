package com.yh.bigdata.silkworm.api.algorithm.graph;

import java.util.Hashtable;
import java.util.List;

public class Node {
	private long id;
	private List<Node> childs;
	
	public Boolean hasChilds(){
		return false;
	}
	
	public List<Node> getChilds(){
		return childs;
	}
}
