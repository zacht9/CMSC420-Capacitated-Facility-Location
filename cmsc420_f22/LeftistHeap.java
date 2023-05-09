package cmsc420_f22; // Do not delete this line

import java.util.ArrayList;

//---------------------------------------------------------------------
//Author: Zach Teselko
//For: CMSC 420
//Date: Fall 2022
//
//This is an implementation of a LeftistHeap data structure. It supports
//standard operations like insert(), mergeWith(), split(), extractMin(),
//and others.
//---------------------------------------------------------------------

public class LeftistHeap<Key extends Comparable<Key>, Value> {
	
	class LHNode {
		Key key;
		Value value;
		LHNode left, right;
		int npl;
		
		public LHNode(Key key, Value value) {
			this.key = key;
			this.value = value;
			left = null;
			right = null;
			npl = 0;
		}
	}
	
	private LHNode root;
	
	public LeftistHeap() {
		root = null;
	}
	
	public boolean isEmpty() { 
		return (root == null) ? true : false;
	}
	
	public void clear() {
		root = null;
	}
	
	public void insert(Key x, Value v) {
		LHNode newNode = new LHNode(x, v);
		root = merge(this.root, newNode);
	}
	
	/**
	 * Merges the current LeftistHeap with another one, forming one new larger LeftistHeap instance.
	 * @param h2 -- the other LeftistHeap instance we want to merge this one with.
	 */
	public void mergeWith(LeftistHeap<Key, Value> h2) {
		if (!(h2 == null || this == h2)) {
			root = merge(this.root, h2.root);
			h2.root = null;
		}
	}
	
	/**
	 * Recursive helper method for merging two nodes in separate LeftistHeap instances.
	 * @param u -- the first node.
	 * @param v -- the second node.
	 * @return a reference to the first node with things merged.
	 */
	private LHNode merge(LHNode u, LHNode v) {
		if (u == null) return v;
		if (v == null) return u;
		if (u.key.compareTo(v.key) > 0) {
			LHNode temp = u;
			u = v;
			v = temp;
		} 
		if (u.left == null) {
			u.left = v;
		} else {
			u.right = merge(u.right, v);
			if (u.left.npl < u.right.npl) {
				LHNode temp = u.right;
				u.right = u.left;
				u.left = temp;
			}
			u.npl = u.right.npl + 1;
		}
		return u;
	}
	
	/**
	 * Split the current heap into two separate heaps, where elements with keys less than or equal to x
	 * are in one heap, and elements with keys strictly greater than x are in the other. Elements with keys
	 * strictly greater than x will be added to a new heap, while the other ones will remain in this one.
	 * @param x -- the key we want to split by.
	 * @return the newly-created LeftistHeap.
	 */
	public LeftistHeap<Key, Value> split(Key x) {
		if (root == null) {
			return this;
		}
		LeftistHeap<Key, Value> h2 = new LeftistHeap<Key, Value>();
		if (root.key.compareTo(x) > 0) {
			h2.root = root;
			root = null;
			return h2;
		}
		ArrayList<LHNode> nodeList = new ArrayList<LHNode>();
		nodeList = splitTraverse(x, root, nodeList);
		if (nodeList.size() >= 1) {
			h2.root = nodeList.get(0);
		}
		for (int i = 1; i < nodeList.size(); i++) {
			LeftistHeap<Key, Value> h3 = new LeftistHeap<Key, Value>();
			h3.root = nodeList.get(i);
			h2.mergeWith(h3);
		}
		repairTraverse(root);
		return h2;
	}
	
	/**
	 * Helper method for split() above.
	 * @param x -- splitting key.
	 * @param u -- current node.
	 * @param nodeList -- list of nodes that are in line to be added to the new subtree.
	 * @return nodeList.
	 */
	public ArrayList<LHNode> splitTraverse(Key x, LHNode u, ArrayList<LHNode> nodeList) {
		if (u == null) {
			return nodeList;
		}
		if (u.left != null) {
			if (u.left.key.compareTo(x) > 0) {
				nodeList.add(u.left);
				u.left = null;
				u.npl = 0;
			} else {
				nodeList = splitTraverse(x, u.left, nodeList);
			}
		}
		if (u.right != null) {
			if (u.right.key.compareTo(x) > 0) {
				nodeList.add(u.right);
				u.right = null;
				u.npl = 0;
			} else {
				nodeList = splitTraverse(x, u.right, nodeList);
			}
		}
		if (u.left != null && u.right != null) {
			u.npl = Math.min(u.left.npl, u.right.npl) + 1;
		}
		return nodeList;
	}
	
	/**
	 * Another helper method for split() above. Repairs the original tree once the splitting process
	 * is done.
	 * @param u -- current node.
	 */
	private void repairTraverse(LHNode u) {
		if (u != null) {
			if (u.left == null) {
				if (u.right != null) {
					u.left = u.right;
					u.right = null;
					repairTraverse(u.left);
				}
			} else {
				if (u.right != null && u.right.npl > u.left.npl) {
					LHNode temp = u.left;
					u.left = u.right;
					u.right = temp;
				}
				repairTraverse(u.left);
				repairTraverse(u.right);
			}
		}
	}
	
	/**
	 * Take a look at the key at the root of the LeftistHeap.
	 * @return this key.
	 */
	public Key getMinKey() {
		if (root == null) {
			return null;
		}
		return root.key;
	}
	
	/**
	 * Remove and return the minimum element of the heap.
	 * @return
	 * @throws Exception if there is no minimum element to extract.
	 */
	public Value extractMin() throws Exception {
		if (root == null) {
			throw new Exception("Empty heap");
		}
		LHNode minNode = root;
		root = merge(root.left, root.right);
		return minNode.value;
		
	}
	
	/**
	 * Convert the current LeftistHeap to an ArrayList of String representations of nodes in 
	 * right-to-left pre-order traversal (including null pointers).
	 * @return an ArrayList of this format.
	 */
	public ArrayList<String> list() {
		ArrayList<String> outputList = new ArrayList<String>();
		return listTraverse(root, outputList);
	}
	
	/**
	 * Helper method for list() above.
	 * @param u -- the current node.
	 * @param list -- the current list of nodes.
	 * @return
	 */
	private ArrayList<String> listTraverse(LHNode u, ArrayList<String> list) {
		if (u == null) {
			list.add("[]");
			return list;
		}
		list.add("(" + u.key + ", " + u.value + ") [" + u.npl + "]");
		list = listTraverse(u.right, list);
		list = listTraverse(u.left, list);
		return list;
	}
}
