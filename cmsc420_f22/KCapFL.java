package cmsc420_f22; // Do not delete this line

import java.util.ArrayList;

//---------------------------------------------------------------------
//Author: Zach Teselko
//For: CMSC 420
//Date: Fall 2022
//
//This is an implementation of a k-capacitated facility locator. It stores
//a kdTree of points in our problem, a LeftistHeap of clusters, and a capacity
//value, and allows us to implement a greedy facility location algorithm to
//find some working combinations of service centers and associated points.
//---------------------------------------------------------------------

public class KCapFL<LPoint extends LabeledPoint2D> {
	
	private int capacity;
	private XkdTree<LPoint> kdTree;
	private LeftistHeap<Double, ArrayList<LPoint>> heap;
	
	/**
	 * KCapFL constructor.
	 * @param capacity -- max. number of demand points per service centers.
	 * @param bucketSize -- max. number of points held in each external node of the xkdTree.
	 * @param bbox -- bounding box of the points in the xkdTree.
	 */
	public KCapFL(int capacity, int bucketSize, Rectangle2D bbox) {
		this.capacity = capacity;
		this.kdTree = new XkdTree<LPoint>(bucketSize, bbox);
		this.heap = new LeftistHeap<Double, ArrayList<LPoint>>();
	}
	
	public void clear() { 
		kdTree.clear();
		heap.clear();
	}
	
	/**
	 * Initializes the two data structures with the list of points. Inserts all points into our xkdTree, then
	 * calculates the k nearest neighbors (where k = capacity) for each of these points, along with this radius
	 * between the initial point and its farthest neighbor, and inserts into the LeftistHeap.
	 * @param pts -- a list of demand points for our problem.
	 * @throws Exception if the number of demand points is invalid.
	 */
	public void build(ArrayList<LPoint> pts) throws Exception {
		int numPts = pts.size();
		if (numPts < 0 || numPts % capacity != 0) {
			throw new Exception("Invalid point set size");
		}
		kdTree.bulkInsert(pts);
		for (LPoint pt : pts) {
			ArrayList<LPoint> kNearestNeighbors = kdTree.kNearestNeighbor(pt.getPoint2D(), capacity);
			Point2D centerPoint = kNearestNeighbors.get(0).getPoint2D();
			Point2D farPoint = kNearestNeighbors.get(capacity-1).getPoint2D();
			Double radius = centerPoint.distanceSq(farPoint);
			heap.insert(radius, kNearestNeighbors);
		}
	}
	
	/**
	 * Find the smallest valid cluster of points, where one represents a service center and the rest are its
	 * associated demand points. Once this cluster is found, it is extracted (meaning its points are removed from 
	 * both the xkdTree and LeftistHeap) and returned in ArrayList format If there are no more valid clusters, 
	 * return null. Clusters in our LeftistHeap might get damaged over time (if a single point is in two clusters 
	 * but gets removed from the tree when one of them is extracted), so as this method searches for valid clusters, 
	 * it detects these damaged clusters and repairs them as well.
	 * @return an ArrayList of LPoints representing one cluster.
	 */
	public ArrayList<LPoint> extractCluster() {
		if (kdTree.size() == 0) {
			return null;
		}
		ArrayList<LPoint> nextCluster = new ArrayList<LPoint>();
		Boolean foundCluster = false;
		
		// keep going until we find one valid cluster
		while (!foundCluster) {
			try {
				nextCluster = heap.extractMin();
			} catch (Exception e) {
				// code shouldn't get here -- just keeps compiler happy
				System.out.println("An error has occurred running extractMin() on the heap with extractCluster().");
				return null;
			}
			
			foundCluster = true;
			for (LPoint point : nextCluster) {
				if (kdTree.find(point.getPoint2D()) == null) {
					foundCluster = false;
				}
			}
			
			if (foundCluster) {
				break;
			} else {
				// check if center is still in xkdTree -- if so, recompute cluster for it
				Point2D centerPoint = nextCluster.get(0).getPoint2D();
				if (kdTree.find(centerPoint) != null) {
					ArrayList<LPoint> kNearestNeighbors = kdTree.kNearestNeighbor(centerPoint, capacity);
					Point2D farPoint = kNearestNeighbors.get(capacity-1).getPoint2D();
					Double radius = centerPoint.distanceSq(farPoint);
					heap.insert(radius, kNearestNeighbors);
				}
			}
		}
		
		for (LPoint point : nextCluster) {
			try {
				kdTree.delete(point.getPoint2D());
			} catch (Exception e) {
				// code shouldn't get here -- just keeps compiler happy
				System.out.println("Point has not been found in xkdTree.");
			}
		}
		return nextCluster;
	}
	
	/**
	 * Convert the xkdTree into a list format.
	 * @return this ArrayList.
	 */
	public ArrayList<String> listKdTree() {
		return kdTree.list();
	}
	
	/**
	 * Convert the LeftistHeap into a list format.
	 * @return this ArrayList.
	 */
	public ArrayList<String> listHeap() {
		return heap.list();
	}
}
