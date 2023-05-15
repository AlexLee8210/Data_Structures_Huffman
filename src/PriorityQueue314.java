/*  Student information for assignment:
 *
 *  On OUR honor, Alexander Lee and Luis Pabon, this programming assignment is OUR own work
 *  and WE have not provided this code to any other student.
 *
 *  Number of slip days used: 0
 *
 *  Alexander Lee
 *  UTEID: al55332
 *  email address: alexlee8210@gmail.com
 *  Grader name: Sai
 *
 *  Luis Pabon
 *  UTEID: lap3865
 *  email address: luisalepabon@utexas.edu
 *
 */

public class PriorityQueue314 <E extends Comparable<? super E>> {
    private ListNode root;
    private int size;
    
    /**
     * Adds an item to the priority queue in a sorted order
     * @param item - item that will be added
     */
    public void enqueue(E item) {
    	if (root == null) {
    		root = new ListNode(item);
    	} else if (root.value.compareTo(item) > 0) {
    		ListNode node = new ListNode(item);
    		node.next = root;
    		root = node;
    	} else {
    		ListNode node = root;
    		while (node.next != null && node.next.value.compareTo(item) <= 0) {
    			node = node.next;
    		}
    		ListNode temp = node.next;
    		node.next = new ListNode(item);
    		node.next.next = temp;
    	}
    	size++;
    }
    
    /**
     * Otherwise known as peek. Returns first element of the queue.
     * @return the front element of the queue
     */
    public E front() {
    	return root.value;
    }
    
    /**
     * Removes the first element of the queues
     * @return the element that has been removed from the queue
     */
    public E dequeue() {
    	if (root == null) {
    		throw new IllegalArgumentException("Queue is empty");
    	}
    	size--;
    	ListNode temp = root;
    	root = root.next;
    	return temp.value;
    }
    
    /**
     * Checks if PriorityQueue314 is empty
     * @return true if the queue is empty, false otherwise
     */
    public boolean isEmpty() {
        return size == 0;
    }
    
    /**
     * Returns the size of the queue.
     * @return the size of the queue
     */
    public int size() {
    	return size;
    }
    
    /*
     * The nodes used to represent the queue as
     * a LinkedList.
     */
    private class ListNode {
    	private ListNode next;
    	private E value;
    	
    	public ListNode(E val) {
    		value = val;
    	}
    }
}