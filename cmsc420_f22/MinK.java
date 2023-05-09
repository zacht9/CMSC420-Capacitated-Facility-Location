package cmsc420_f22; // Do not delete this line

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

//---------------------------------------------------------------------
//Author: Zach Teselko
//For: CMSC 420
//Date: Fall 2022
//
//This is an implementation of a MinK data structure. It is a binary max heap,
//represented in an ArrayList. Supports operations like getKth() which gets the maximum
//key and add(), along with others. In the case that the user has manually inputed less
//than k key-value pairs, the rest of the keys will be set to maxKey by default.
//---------------------------------------------------------------------

public class MinK<Key extends Comparable<Key>, Value> {
	
	// class to represent a key-value pair which will be stored in MinK.
	class KeyValuePair {
		Key key;
		Value value;
		
		KeyValuePair(Key key, Value value) {
			this.key = key;
			this.value = value;
		}
		
		public String toString() {
			return "(" + this.key.toString() + ", " + this.value.toString() + ")";
		}
	}
	
	ArrayList<KeyValuePair> heap;
	int capacity;
	int size;
	Key maxKey;
	
	/**
	 * Constructor for MinK class.
	 * @param k -- the maximum number of KeyValuePair instances the heap can hold.
	 * @param maxKey -- the key to return as max if there are less pairs than capacity.
	 */
	public MinK(int k, Key maxKey) {
		this.heap = new ArrayList<KeyValuePair>(Collections.nCopies(k+1, null)); // k+1 because the 0th element is always null (so that indices go from 1 to k)
		heap.set(0, null); // for convenience, we'll be starting to store nodes at index 1 so 0 can be skipped
		this.maxKey = maxKey;
		this.capacity = k;
		this.size = 0;
	}
	
	public int size() {
		return size;
	}
	
	public void clear() {
		this.heap = new ArrayList<KeyValuePair>(Collections.nCopies(capacity+1, null));
		heap.set(0, null);
		this.size = 0;
	}
	
	/**
	 * Find the maximum key in the heap (at its root) and return but don't remove.
	 * @return the maximum key in the heap.
	 */
	public Key getKth() {
		if (size == capacity) {
			return heap.get(1).key;
		} else {
			return maxKey;
		}
	}
	
	/**
	 * Add a key-value pair to the heap.
	 * @param x -- the key.
	 * @param v -- the value.
	 */
	public void add(Key x, Value v) {
		//System.out.println("Adding key " + x.toString() + " and value " + v.toString());
		if (size < capacity) {
			size++;
			int i = siftUp(size, x);
			heap.set(i, new KeyValuePair(x, v));
		} else if (size == capacity) {
			if (x.compareTo(heap.get(1).key) < 0) {
				this.removeMax();
				size++;
				int i = siftUp(size, x);
				heap.set(i, new KeyValuePair(x, v));
			}
		}
		//System.out.println(this.toString());
	}
	
	/**
	 * Helper method for add(), moves key-value pairs recently inserted at the bottom of the heap up towards the
	 * root according to its key.
	 * @param i -- current index.
	 * @param key -- the new key we want to insert.
	 * @return the index the new key belongs at.
	 */
	private int siftUp(int i, Key key) {
		while (i > 1 && key.compareTo(heap.get(i/2).key) > 0) {
			heap.set(i, heap.get(i/2));
			i /= 2;
			//System.out.println("After sifting up: " + this.toString());
		}
		return i;
	}
	
	/**
	 * Helper method for add(), removes the key-value pair at the heap's root from the tree, makes the new root
	 * the very last element of the heap, and calls siftDown() to repair the heap's format.
	 */
	private void removeMax() {
		if (size > 0) {
			KeyValuePair newMax = heap.get(size);
			heap.set(size, null);
			size--;
			int i = siftDown(1, newMax.key);
			heap.set(i, newMax);
		}
	}
	
	/**
	 * Helper method for removeMax() above, sifts the current key down until it is in its right place.
	 * @param i -- current index.
	 * @param key -- key of the key-value pair we want to sift down.
	 * @return updated index
	 */
	private int siftDown(int i, Key key) {
		while (2*i <= size) {
			int left = 2*i;
			int right = (2*i)+1;
			if (right <= size && heap.get(right).key.compareTo(heap.get(left).key) > 0) {
				left = right;
			}
			if (heap.get(left).key.compareTo(key) > 0) {
				heap.set(i, heap.get(left));
				i = left;
			} else {
				break;
			}
		}
		return i;
	}
	
	/**
	 * Displays the heap as a list of values, sorted in ascending order by their keys.
	 * @return an ArrayList of values from the heap.
	 */
	public ArrayList<Value> list() {
		ArrayList<KeyValuePair> sortedKVList = new ArrayList<KeyValuePair>(heap.subList(1, size+1));
		Collections.sort(sortedKVList, new keyComparator());
		ArrayList<Value> valueList = new ArrayList<Value>();
		for (KeyValuePair pair : sortedKVList) {
			valueList.add(pair.value);
		}
		return valueList;
	}
	
	/**
	 * Converts the heap's data into a single string for debugging purposes.
	 */
	public String toString() {
		String resString = "";
		for (KeyValuePair pair : heap.subList(1, size+1)) {
			resString += (pair.toString() + "; ");
		}
		return resString;
	}
	
	/**
	 * Custom comparator to sort KeyValuePair instances by their keys, in ascending order.
	 */
	public class keyComparator implements Comparator<KeyValuePair> {
		public int compare(KeyValuePair p1, KeyValuePair p2) {
			return p1.key.compareTo(p2.key);
		}
	}
}
