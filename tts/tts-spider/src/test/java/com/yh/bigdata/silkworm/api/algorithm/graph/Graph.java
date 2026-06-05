package com.yh.bigdata.silkworm.api.algorithm.graph;

import java.util.List;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;

public class Graph {
	
	private List<Node> roots;
	
	
	private boolean isDag() {
		List<Node> visitedsList = Lists.newArrayList();
		for (Node node : roots) {
			if(!dfs(node, visitedsList)) {return false;}
		}
		return true;
	}
	
	private Boolean dfs(Node node, List<Node> visiteds){
		if (visiteds.contains(node)) {
			System.out.println(JSON.toJSONString(node));
			return false;
		}else{
			
			visiteds.add(node);
			
			if (!node.hasChilds()) {
				return true;
			}
			
			for (Node node2 : node.getChilds()) {
				if(!dfs(node2, visiteds)){
					return false;
				}
			}
		}
		return true;
	}
	
}
