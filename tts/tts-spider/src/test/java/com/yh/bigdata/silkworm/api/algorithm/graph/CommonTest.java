package com.yh.bigdata.silkworm.api.algorithm.graph;

import java.util.Arrays;
import java.util.Stack;

import org.junit.Test;
import org.springframework.aop.framework.autoproxy.target.QuickTargetSourceCreator;

public class CommonTest {

	
	/**
	 * 质数/合数
	 */
	@Test
	public void isPrimeNumber() {
		int num = 6;
		for (int i = 2; i <= num / 2; i++) {
			if (num % i == 0) {
				System.out.println("合数");
				return;
			}
		}
		System.out.println("质数");
	}
	
	@Test
	public void jiechent() throws Exception {
		System.out.println(jiecheng(6));
	}
	
	public int jiecheng(int num) throws Exception {
		try{
			if(num <= 1){
				return 1;
			}else{
				return num * jiecheng(num - 1);	
			}
		}catch(Exception e) {
			throw new Exception("illegal input param");	
		}
	}
	
	@Test
	public void printJuzhen() {
		int juzhen[][] = {{1,2,3,4}, {5,6,7,8}, {9,10,11,12}, {13,14,15,16}};
		int w = 4;	//当前边长
		int i=0, j=0;
		while(true){
			System.out.println(juzhen[i][j]);
			if(j == w - 1) {	//撞边
				
			}
		}
	}
	
	@Test
	public void printCount() {
		String string = "*12aZ30fdsf fdfj12_7*72123)";
		int chCnt = 0;
		int numCnt = 0;
		int blankCnt = 0;
		int otherCnt = 0;

		for (int i = 0; i < string.length(); i++) {
			char ch = string.charAt(i);
			if ((ch >= 'a' && ch <= 'z')
					|| (ch >= 'A' && ch <= 'Z')
					) {
				chCnt++;
			}else if (ch >= '0' && ch <= '9') {
				numCnt++;
			}else if (ch == ' ') {
				blankCnt++;
			}else {
				otherCnt++;

			}
		}
		System.out.println("chCnt\t" + chCnt);
		System.out.println("numCnt\t" + numCnt);
		System.out.println("blankCnt\t" + blankCnt);
		System.out.println("otherCnt\t" + otherCnt);

	}
	
	
	
	@Test
	public void quickSort() {
		
		int[] arr = new int[]{0,1,6,4, 9,2,5,3,7,4,8};
		
        //调用快速排序算法
        quick(arr,0,arr.length-1);
        
        //打印排序的后结果，查看是否正确
        System.out.println(Arrays.toString(arr));
	}
	
    /**
     * 快速排序算法
     * @param arr 被排序的算法
     * @param start 快速排序的起始位置
     * @param end 快速排序的结束位置
     */
    public void quick(int[] arr,int leftIndex,int rightIndex){
    	if (leftIndex >= rightIndex) {
            return;
        }

        int left = leftIndex;
        int right = rightIndex;
        //待排序的第一个元素作为基准值
        int key = arr[left];

        //从左右两边交替扫描，直到left = right
        while (left < right) {
        	
            while (right > left && arr[right] >= key) {
                //从右往左扫描，找到第一个比基准值小的元素
                right--;
            }
            
            //找到这种元素将arr[right]放入arr[left]中
            arr[left] = arr[right];
            
            while (left < right && arr[left] <= key) {
                //从左往右扫描，找到第一个比基准值大的元素
                left++;
            }
            
            //找到这种元素将arr[left]放入arr[right]中
            arr[right] = arr[left];
        }
        //基准值归位
        arr[left] = key;
        //对基准值左边的元素进行递归排序
        quick(arr, leftIndex, left - 1);
        //对基准值右边的元素进行递归排序。
        quick(arr, right + 1, rightIndex);
    }
	
    
    public void quick(int[] arrays) {
    	Stack<Integer> stack = new Stack<Integer>();
    }
    
    @Test
    public void Digital2BinaryStr() {
    	int number = 10;
    	int digits = 8;
    	int val = 1 << digits | number;
    	System.out.println(Integer.toBinaryString(val));
    }

    
    @Test
    public void binarySearch() {
    	int[] nums = {1,2,3,4,5,6,7,8,9};
    	int target = 4;
    	int left = 0;
    	int right = nums.length;
    	while (left <= right) {
			int mid = (left + right) / 2;	
			if(nums[mid] == target) {
				System.out.println(mid);
				break;
			}else if (nums[mid] < target) {
				left = mid + 1;
			}else if (nums[mid] > target) {
				right = mid - 1;
			}
		}
    	System.out.println("-1");
    }

}
