package cmsc420_f22; // Do not delete this line

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

//---------------------------------------------------------------------
//Author: Zach Teselko
//For: CMSC 420
//Date: Fall 2022
//
//This is an implementation of an extended kd-Tree data structure. This is a tree
//which represents how points are distributed across a 2D box, and has internal nodes
//which represent horizontal/vertical lines, as well as external nodes that store one
//or more points. Supports normal queries, insertion, deletion, nearest neighbor queries,
//and more.
//---------------------------------------------------------------------

public class XkdTree<LPoint extends LabeledPoint2D> {
	
	int bucketSize;
	Rectangle2D bbox;
	int count;
	Node root;
	
	/**
	 * Outline of methods both InternalNodes and ExternalNodes will have. Both of these
	 * classes will extend this one.
	 */
	private abstract class Node {
		abstract LPoint find(Point2D pt);
		abstract Node bulkInsert(ArrayList<LPoint> points) throws Exception;
		abstract ArrayList<String> list(ArrayList<String> inputList);
		abstract LPoint nearestNeighbor(Point2D center);
		abstract Node delete(Point2D pt);
		abstract void kNearestNeighbor(Point2D q, MinK<Double, LPoint> minPoints, Rectangle2D currCell);
	}
	
	/**
	 * Class to represent an InternalNode in our XkdTree.
	 */
	private class InternalNode extends Node {
		int cutDim;
		double cutVal;
		Node left, right;
		
		/**
		 * InternalNode constructor.
		 * @param cutDim -- dimension this InternalNode splits.
		 * @param cutVal -- at what value of this dimension the InternalNode splits.
		 * @param left -- root of left subtree.
		 * @param right -- root of right subtree.
		 */
		InternalNode(int cutDim, double cutVal, Node left, Node right) {
			this.cutDim = cutDim;
			this.cutVal = cutVal;
			this.left = left;
			this.right = right;
		}
		
		/*
		 * Helper method for internal nodes to find if a point at q is present in the tree.
		 * Eventually returns null if a point at q isn't present, otherwise returns the point at q.
		 * @param q, an instance of Point2D.
		 */
		LPoint find(Point2D q) {
			LPoint left_res = null;
			LPoint right_res = null;
			
			if (q.get(cutDim) <= cutVal) {
				left_res = left.find(q);
			}
			if (q.get(cutDim) >= cutVal) {
				right_res = right.find(q);
			}
			
			return (left_res == null) ? (right_res):(left_res);
		}
		
		/**
		 * Helper method to insert more than one point in the tree at once.
		 * @param pts -- each LPoint we want to insert.
		 * @return a reference to this current InternalNode.
		 */
		Node bulkInsert(ArrayList<LPoint> pts) throws Exception {
			
			if (cutDim == 0) {
				Collections.sort(pts, new PointsComparatorX());
			} else {
				Collections.sort(pts, new PointsComparatorY());
			}
			
			int split_index = 0;
			for (LPoint pt : pts) {
				if (pt.get(cutDim) >= cutVal) {
					break;
				}
				split_index++;
			}
			ArrayList<LPoint> left_pts = new ArrayList<LPoint>(pts.subList(0, split_index));
			ArrayList<LPoint> right_pts = new ArrayList<LPoint>(pts.subList(split_index, pts.size()));
			if (left_pts.size() >= 1) { 
				this.left = left.bulkInsert(left_pts);
			}
			if (right_pts.size() >= 1) {
				this.right = right.bulkInsert(right_pts);
			}
			return this;
		}
		
		/**
		 * Helper method to convert this tree into an ArrayList format.
		 */
		ArrayList<String> list(ArrayList<String> inputList) {
			String entry;
			if (cutDim == 0) {
				entry = "(x=" + cutVal + ")";
			} else {
				entry = "(y=" + cutVal + ")";
			}
			inputList.add(entry);
			inputList = right.list(inputList);
			inputList = left.list(inputList);
			return inputList;
		}
		
		/**
		 * Helper method to query the tree searching for a single point's nearest neighbor present in the tree.
		 */
		LPoint nearestNeighbor(Point2D center) {
			boolean leftChosen;
			LPoint initialPoint;
			if (center.get(cutDim) < cutVal) {
				initialPoint = left.nearestNeighbor(center);
				leftChosen = true;
			} else {
				initialPoint = right.nearestNeighbor(center);
				leftChosen = false;
			}
			
			Point2D boundaryPoint;
			if (cutDim == 0) {
				boundaryPoint = new Point2D(cutVal, center.get(1));
			} else {
				boundaryPoint = new Point2D(center.get(0), cutVal);
			}
			
			double boundaryDistance = center.distance(boundaryPoint);
			double initialDistance = center.distance(initialPoint.getPoint2D());
			
			LPoint nextPoint;
			LPoint returnPoint = initialPoint;
			if (boundaryDistance < initialDistance) {
				if (leftChosen) {
					nextPoint = right.nearestNeighbor(center);
				} else {
					nextPoint = left.nearestNeighbor(center);
				}
				double nextDistance = center.distance(nextPoint.getPoint2D());
				if (nextDistance < initialDistance) {
					returnPoint = nextPoint;
				}
			}
			
			return returnPoint;
			
		}
		
		/**
		 * Helper method to delete a single point from the tree.
		 */
		Node delete(Point2D pt) {
			if (pt.get(cutDim) <= cutVal) {
				this.left = left.delete(pt);
				if (this.left == null) { // internal node has non-empty and empty child --> return non-empty to make grandparent directly have this child (delete current internal node)
					return this.right;
				}
			}
			if (pt.get(cutDim) >= cutVal) {
				this.right = right.delete(pt);
				if (this.right == null) {
					return this.left;
				}
			}
			return this;
		}
		
		/**
		 * Helper method to query the tree for the k nearest neighbors present in the tree around q. The k nearest
		 * neighbors are kept track of in minPoints.
		 */
		void kNearestNeighbor(Point2D q, MinK<Double, LPoint> minPoints, Rectangle2D currCell) {
			/**
			 * Outline:
			 * -Get current internal node's cell
			 * -Compute left part of cell, see if a node in that left part could get in MinK
			 * 	based on current max of MinK. If so, recursive call on left node
			 * -Do the same thing for right part of cell
			 */
			
			Rectangle2D leftCell = currCell.leftPart(cutDim, cutVal);
			Rectangle2D rightCell = currCell.rightPart(cutDim, cutVal);
			
			if (leftCell.distanceSq(q) <= minPoints.getKth()) {
				left.kNearestNeighbor(q, minPoints, leftCell);
			}
			if (rightCell.distanceSq(q) <= minPoints.getKth()) {
				right.kNearestNeighbor(q, minPoints, rightCell);
			}
		}
	}
	
	/**
	 * Class to represent an ExternalNode in our XkdTree.
	 */
	private class ExternalNode extends Node {
		ArrayList<LPoint> points;
		
		/**
		 * ExternalNode constructor -- just initializes points as an empty ArrayList, which will act as
		 * a bucket that collects various points.
		 */
		ExternalNode(){
			points = new ArrayList<LPoint>();
		}
		
		/**
		 * Helper method to find if a particular point is present in this ExternalNode. If so, the point is
		 * returned, and if not, null is returned.
		 */
		LPoint find(Point2D pt) {
			for (LPoint point : points) {
				if (point.getPoint2D().getX() == pt.getX() && point.getPoint2D().getY() == pt.getY()) {
					return point;
				}
			}
			return null;
		}
		
		/**
		 * Helper method to insert more than one point at a time into our ExternalNode. Inserts each point in pts,
		 * and splits the current external node into new internal and external nodes if the insertion overflows
		 * the current bucket.
		 */
		Node bulkInsert(ArrayList<LPoint> pts) throws Exception {
			points.addAll(pts);
			int size = points.size();
			if (size > bucketSize) {
				Rectangle2D currentBox = new Rectangle2D(points.get(0).getPoint2D(), points.get(1).getPoint2D());
				if (size >= 3) {
					for (LPoint point : points.subList(2, size)) {
						currentBox.expand(point.getPoint2D());
					}
				}
				int newCutDim = (currentBox.getWidth(0) >= currentBox.getWidth(1)) ? (0):(1);
				if (newCutDim == 0) {
					Collections.sort(points, new PointsComparatorX());
				} else {
					Collections.sort(points, new PointsComparatorY());
				}
				int median;
				double newCutVal;
				if (size % 2 == 1) {
					median = size/2;
					newCutVal = points.get(median).get(newCutDim);
				} else {
					int l_median = (size/2)-1;
					median = size/2;
					newCutVal = (points.get(l_median).get(newCutDim) + points.get(median).get(newCutDim))/2;
				}
				ArrayList<LPoint> leftList = new ArrayList<LPoint>(points.subList(0, median));
				ArrayList<LPoint> rightList = new ArrayList<LPoint>(points.subList(median, size));
				Node newLeftNode = new ExternalNode();
				if (leftList.size() >= 1) {
					newLeftNode = newLeftNode.bulkInsert(leftList);
				}
				Node newRightNode = new ExternalNode();
				if (rightList.size() >= 1) {
					newRightNode = newRightNode.bulkInsert(rightList);
				}
				InternalNode newNode = new InternalNode(newCutDim, newCutVal, newLeftNode, newRightNode);
				return newNode;
			} else {
				return this;
			}
		}
		
		/**
		 * Helper method to convert this tree into an ArrayList of string representations of nodes.
		 */
		ArrayList<String> list(ArrayList<String> inputList) {
			Collections.sort(points, new LabelComparator());
			String entry = "[";
			for (LPoint point : points) {
				entry += (" {" + point.getLabel() + ": (" + point.getX() + "," + point.getY() + ")}");
			}
			entry += " ]";
			inputList.add(entry);
			return inputList;
		}
		
		/**
		 * Helper method to find the one point in the tree closest to the center parameter.
		 */
		LPoint nearestNeighbor(Point2D center) {
			LPoint min_point = null;
			Double min_dist = Double.POSITIVE_INFINITY;
			for (LPoint point : points) {
				Double curr_dist = center.distance(point.getPoint2D());
				if (curr_dist < min_dist) {
					min_point = point;
					min_dist = curr_dist;
				}
			}
			return min_point;
		}
		
		/**
		 * Helper method to delete a single point from the tree. Destroys the current ExternalNode if the
		 * deletion results in an empty bucket.
		 */
		Node delete(Point2D pt) {
			for (LPoint point : points) {
				if (point.getPoint2D().equals(pt)) {
					points.remove(point);
					break; // points are unique so we don't need to finish the loop
				}
			}
			
			
			if (points.size() == 0) {
				return null; // if there are no more points in this bucket, no need for this external node
			} else {
				return this;
			}
		}
		
		/**
		 * Helper method to find the k nearest neighbors of q present in the tree. These k nearest neighbors are kept
		 * track of in minPoints.
		 */
		void kNearestNeighbor(Point2D q, MinK<Double, LPoint> minPoints, Rectangle2D currCell) {
			/**
			 * Outline:
			 * -For each point of points at this stage, see if it can go into minPoints.
			 * -We can just add all points because minK won't add something if it doesn't fit -- it'll check logic for us
			 */
			for (LPoint point : points) {
				Double distance = q.distanceSq(point.getPoint2D());
				minPoints.add(distance, point);
			}
		}
	}
	
	/**
	 * Comparator to compare points based on their x-value first, y-value second.
	 */
	public class PointsComparatorX implements Comparator<LPoint> {
		@Override
		public int compare(LPoint p1, LPoint p2) {
			if (p1.getX() > p2.getX()) {
				return 1;
			} else if (p1.getX() < p2.getX()) {
				return -1;
			} else {
				if (p1.getY() > p2.getY()) {
					return 1;
				} else {
					return -1;
				}
			}
		}
	}
	
	/**
	 * Comparator to compare points based on their y-value first, x-value second.
	 */
	public class PointsComparatorY implements Comparator<LPoint> {
		@Override
		public int compare(LPoint p1, LPoint p2) {
			if (p1.getY() > p2.getY()) {
				return 1;
			} else if (p1.getY() < p2.getY()) {
				return -1;
			} else {
				if (p1.getX() > p2.getX()) {
					return 1;
				} else {
					return -1;
				}
			}
		}
	}
	
	/**
	 * Comparator to compare points alphabetically based on their labels.
	 */
	public class LabelComparator implements Comparator<LPoint> {
		@Override
		public int compare(LPoint p1, LPoint p2) {
			return p1.getLabel().compareTo(p2.getLabel());
		}
	}
	
	/**
	 * XkdTree constructor.
	 * @param bucketSize -- max. number of points that can be present in one ExternalNode.
	 * @param bbox -- the rectangular area this XkdTree will cover.
	 */
	public XkdTree(int bucketSize, Rectangle2D bbox) {
		this.bucketSize = bucketSize;
		this.bbox = bbox;
		count = 0;
		this.root = new ExternalNode();
	}
	
	public void clear() {
		count = 0;
		this.root = new ExternalNode();
	}
	
	public int size() {
		return count;
	}
	
	/**
	 * Finds and returns a labeled point present in the tree with q's position.
	 * @param q: the point we want to find.
	 * @return the labeled point at q's position if present, null if not.
	 */
	public LPoint find(Point2D q) {
		return root.find(q);
	}
	
	/**
	 * Inserts a single new labeled point into our XkdTree.
	 * @param pt: the point we want to add.
	 * @throws Exception if this point is outside of the bounding box.
	 */
	public void insert(LPoint pt) throws Exception {
		ArrayList<LPoint> pts = new ArrayList<LPoint>();
		pts.add(pt);
		bulkInsert(pts);
	}
	
	/**
	 * Inserts multiple new labeled points at a time into our XkdTree.
	 * @param pts: the list of labeled points we want to add.
	 * @throws Exception if one of the points is outside of the bounding box.
	 */
	public void bulkInsert(ArrayList<LPoint> pts) throws Exception {
		
		Collections.sort(pts, new PointsComparatorX());
		LPoint min_pt = pts.get(0);
		LPoint max_pt = pts.get(pts.size()-1);
		if (min_pt.get(0) < bbox.getLow().get(0) || max_pt.get(0) > bbox.getHigh().get(0)) {
			throw new Exception("Attempt to insert a point outside bounding box");
		}
		
		Collections.sort(pts, new PointsComparatorY());
		LPoint min_pt2 = pts.get(0);
		LPoint max_pt2 = pts.get(pts.size()-1);
		if (min_pt2.get(1) < bbox.getLow().get(1) || max_pt2.get(1) > bbox.getHigh().get(1)) {
			throw new Exception("Attempt to insert a point outside bounding box");
		}
		
		count += pts.size();
		this.root = root.bulkInsert(pts);
	}
	
	/**
	 * Converts this XkdTree into an ArrayList of string representations of its nodes.
	 * @return an ArrayList in this format.
	 */
	public ArrayList<String> list() {
		ArrayList<String> outputList = new ArrayList<String>();
		return root.list(outputList);
	}
	
	/**
	 * Find the point in the tree which is closest to center. 
	 * @param center: the point whose nearest neighbor we want to search for.
	 * @return the point which is the nearest neighbor, null if no nearest neighbor is found (if the tree is empty).
	 */
	public LPoint nearestNeighbor(Point2D center) { 
		if (count == 0) {
			return null;
		}
		return root.nearestNeighbor(center);
	}
	
	/**
	 * Delete a single point from the tree.
	 * @param pt: the point we want to delete.
	 * @throws Exception if the point is not present in the tree in the first place.
	 */
	public void delete(Point2D pt) throws Exception {
		if (root.find(pt) == null) {
			throw new Exception("Deletion of nonexistent point");
		}
		root = root.delete(pt);
		count--;
		
		/**
		 *  when we've deleted the last element from an ExternalNode, our helper methods make the root null.
		 *  the one case where we don't want this is when the root is this ExternalNode -- this code detects that
		 *  case and fixes it
		 */
		if (root == null) {
			root = new ExternalNode(); 
		}
		
	}
	
	/**
	 * Find and return the k points in the tree which are closest to q. 
	 * @param q: our center point.
	 * @param k: the number of nearest neighbors we want to find.
	 * @return an ArrayList with the k labeled points which are the nearest neighbors.
	 */
	public ArrayList<LPoint> kNearestNeighbor(Point2D q, int k) {
		MinK<Double, LPoint> kNearestNeighbors = new MinK<Double, LPoint>(k, Double.POSITIVE_INFINITY);
		root.kNearestNeighbor(q, kNearestNeighbors, this.bbox);
		// Don't need to return the call to root.kNearestNeighbor(...) because return object is passed in, modified across calls
		return kNearestNeighbors.list();
	}
}