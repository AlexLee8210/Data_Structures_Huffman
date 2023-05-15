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

import java.io.IOException;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.LinkedList;

public class HuffmanTree implements Iterable<TreeNode> {

	private TreeNode root;
	private HashMap<Integer, String> huffmanCodes;
	private TreeMap<Integer, Integer> frequencies;
	private int totalDataSizeBits;
	
	private final int EMPTY_NODE_VALUE = -1;
	
	/** 
	 * count characters from input and create Huffman tree
	 * <br> pre: map != null && map.size() >= 1
	 * @param in is the stream which could be subsequently compressed
	 */
	public HuffmanTree(TreeMap<Integer, Integer> map) {
		if (map == null || map.size() == 0) {
			throw new IllegalArgumentException("map cannot be null and"
					+ " must have a size greater than 0");
		}
		PriorityQueue314<TreeNode> pq = new PriorityQueue314<>();
		frequencies = map;
    	for (int num : map.keySet()) {
    		TreeNode node = new TreeNode(num, map.get(num));
    		pq.enqueue(node);
    	}
    	    	
    	while (pq.size() > 1) {
    		TreeNode newNode = new TreeNode(pq.dequeue(), EMPTY_NODE_VALUE, pq.dequeue());
    		pq.enqueue(newNode);
    	}
    	
    	root = pq.dequeue();
    	createCodeMappings();
	}
	
	/**
	 * Create a Huffman Tree from the input stream based on the tree
	 * header.
	 * @param bitIn the input stream to read the tree header from
	 * @throws IOException 
	 */
	public HuffmanTree(BitInputStream bitIn) throws IOException {
		int size = bitIn.readBits(IHuffConstants.BITS_PER_INT);
		root = createTree(bitIn, size, new int[1]);
	}
	
	/**
	 * Creates a Huffman tree recursively from bitIn with a total bit size of
	 * size.
	 * @param bitIn the input stream to read the tree header from
	 * @param size the total number of bits to be read from in
	 * @param currentSize the current number of bits that have been written in 
	 * an array at index 0.
	 * @return the node created
	 * @throws IOException if there is an error reading from bitIn
	 */
	private TreeNode createTree(BitInputStream bitIn, int size, int[] currentSize)
			throws IOException {
		int input = bitIn.readBits(1);
		if (input == 0) {
			currentSize[0]++;
			TreeNode node = new TreeNode(createTree(bitIn, size, currentSize),
					EMPTY_NODE_VALUE, createTree(bitIn, size, currentSize));
			return node;
		} else if (input == 1) {
			int numBits = IHuffConstants.BITS_PER_WORD + 1;
			currentSize[0] += numBits;
			int value = bitIn.readBits(numBits);
			TreeNode node = new TreeNode(value, 1);
			return node;
		} else {
			throw new IllegalStateException("format error, something "
					+ "is incorrect about format of the input file");
		}
	}
	
	/**
	 * Gets the new code using the Huffman encoding scheme
	 * pre: num must be contained within the Huffman tree
	 * @param num the sequence of bits to get the code for
	 * @return the Huffman code for num
	 */
	public String getCode(int num) {
		if (!huffmanCodes.containsKey(num)) {
			throw new IllegalArgumentException("num must be within the Huffman tree");
		}
		return huffmanCodes.get(num);
	}
	

	/**
	 * Creates the new Huffman codes for each character (int)
	 */
	private void createCodeMappings() {
    	huffmanCodes = new HashMap<>();
		createCodeMappingsHelper(root, "");
	}
	
	/**
	 * Creates the new Huffman codes for each character (int)
	 * starting from node
	 * @param node the node to start at
	 * @param code the code of the character
	 */
	private void createCodeMappingsHelper(TreeNode node, String code) {
		int value = node.getValue();
		if (0 <= value && value <= IHuffConstants.ALPH_SIZE) {
			huffmanCodes.put(value, code);
			totalDataSizeBits += node.getFrequency() * code.length();
		}
		if (node.getLeft() != null) {
			createCodeMappingsHelper(node.getLeft(), code + "0");
		}
		if (node.getRight() != null) {
			createCodeMappingsHelper(node.getRight(), code + "1");
		}
	}
	
	/**
	 * Returns the total number of bits that the new file would contain
	 * from the data portion of the file.
	 * @return the total number of bits that the new file would contain
	 * from the data portion of the file.
	 */
	public int getTotalDataSizeInBits() {
		return totalDataSizeBits;
	}
	
	/**
	 * Returns the number of bits that the tree representation takes up
	 * in the header.
	 * @return the number of bits that the tree representation takes up
	 * in the header.
	 */
	public int getTreeSizeInBits() {
		return huffmanCodes.size() * (IHuffConstants.BITS_PER_WORD + 1) + getNumNodes();
	}
	
	/**
	 * Returns the number of nodes in the tree
	 * @return the number of nodes in the tree
	 */
	private int getNumNodes() {
		return getNumNodesHelper(root);
	}
	
	/**
	 * Helper method that returns the number of nodes in the tree
	 * starting from node
	 * @param node the node to begin at
	 * @return the number of nodes in the tree
	 */
	private int getNumNodesHelper(TreeNode node) {
		if (node == null) {
			return 0;
		}
		return getNumNodesHelper(node.getLeft()) + getNumNodesHelper(node.getRight()) + 1;
	}
	
	/**
	 * Fills the counts array with the frequencies of
	 * each value in the tree
	 * @param counts the array to fill with the frequencies
	 */
	public void fillCounts(int[] counts) {
		for (Integer n : huffmanCodes.keySet()) {
			if (n != IHuffConstants.ALPH_SIZE) {
				counts[n] = getFrequency(n);
			}
		}
	}
	
	/**
	 * Returns the frequency for a given value
	 * @param num the value to find the frequency of
	 * @return the frequency of num, -1 if not in Huffman tree
	 */
	private int getFrequency(int num) {
		return frequencies.get(num);
	}
	
	/**
	 * Returns an iterator over the TreeNodes within the
	 * Huffman Tree in pre-order traversal order.
	 */
	@Override
	public Iterator<TreeNode> iterator() {
		return new HTIterator();
	}
	
	/*
	 * Iterator for doing a pre-order traversal of the tree
	 */
	private class HTIterator implements Iterator<TreeNode> {

		private Deque<TreeNode> nodeStack;
		
		public HTIterator() {
			nodeStack = new LinkedList<>();
			nodeStack.push(root);
		}
		
		@Override
		public boolean hasNext() {
			return !nodeStack.isEmpty();
		}

		@Override
		public TreeNode next() {
			TreeNode nextNode = nodeStack.pop();
			// if not leaf
			if (!(nextNode.isLeaf())) {
				if (nextNode.getRight() == null) { // 1 left child
					nodeStack.push(nextNode.getLeft());
				} else if (nextNode.getLeft() == null) { // 1 right child
					nodeStack.push(nextNode.getRight());
				} else { // 2 children
					nodeStack.push(nextNode.getRight());
					nodeStack.push(nextNode.getLeft());
				}
			}
			return nextNode;
		}
		
	}
	
	/**
	 * Returns the root of the tree
	 * @return the tree root
	 */
	public TreeNode getRoot() {
		return root;
	}
	
}
