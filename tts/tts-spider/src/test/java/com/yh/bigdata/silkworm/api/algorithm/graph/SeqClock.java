package com.yh.bigdata.silkworm.api.algorithm.graph;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * 顺时针遍历
 * @author junifer
 *
 */
public class SeqClock {
	
	static List<Pos> travels = Lists.newArrayList();
	static int front = 0;  	//0=右、1=下、2=左、3=上
	
	public static class Pos {
		int x;
		int y;
		
		public Pos() {
			super();
		}
		public Pos(int x, int y) {
			super();
			this.x = x;
			this.y = y;
		}
		public int getX() {
			return x;
		}
		public void setX(int x) {
			this.x = x;
		}
		public int getY() {
			return y;
		}
		public void setY(int y) {
			this.y = y;
		}
		@Override
		public boolean equals(Object obj) {
			Pos o = (Pos)obj;
			return o.getX() == this.x && o.getY() == this.y;
		}
	}
	
	public static void main(String[] args) {
		int[][] arr = {{1,5,8,9},
				{11,55,88,99},
				{111,555,888,999},
				{1111,5555,8888,9999},
		};
		
		Pos next = new Pos();
		next.setX(0);
		next.setY(0);
		
		while(next != null) {
			System.out.println(arr[next.getX()][next.getY()]);
			travels.add(next);
			
			next = getNextPos(next);
		}
	}

	
	public static Pos getNextStep(Pos cur) {
		Pos pos = null;
		switch (front) {
		case 0:
			pos = new Pos(cur.getX(), cur.getY() + 1);	//向右走
			break;
		case 1:
			pos = new Pos(cur.getX() + 1, cur.getY());	//向下走

			break;
		case 2:
			pos = new Pos(cur.getX(), cur.getY() - 1);	//向左走

			break;
		case 3:
			pos = new Pos(cur.getX() - 1, cur.getY());	//向上走
			break;

		default:
			break;
		}
		return pos;
	}
	
	public static Pos getNextPos(Pos cur) {
		Pos pos = getNextStep(cur);
		int swithCnt = 0;
		while (swithCnt < 4) {
			if(isStepAvailable(pos)) {
				return pos;
			}
			
			front = (front + 1) % 4;
			pos = getNextStep(cur);
			swithCnt ++;
		}
		return null;
	}
	
	public static boolean isStepAvailable(Pos pos) {
		//碰到边界、已走过
		if (pos.getX() >= 4 || pos.getY() >= 4
				|| pos.getX() < 0|| pos.getY() < 0
				||  travels.contains(pos)
				) {
			return false;
		}
		return true;
	}
	
}
