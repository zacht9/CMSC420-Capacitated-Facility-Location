# CMSC420-Capacitated-Facility-Location

- This is a project I completed for CMSC420 when I took it in the Fall 2022 semester at UMD. The goal of the project is to find an optimal solution to a facility location problem, belonging to an important type of problems in the field of operations research.
- The files I wrote are **KCapFL.java**, **LeftistHeap.java**, **MinK.java**, and **XkdTree.java**. 
- Essentially, this problem gives a set of service centers and demand points within a 2D boundary, as well as a capacity for the number of demand points each service center can serve. We don't want the demand points to ever be too far from their service centers, but we don't want the service centers to exceed their capacity. The goal of this project is to find the most optimal way to pair each demand point to a service center, minimizing the total distances between them across each pair, while making sure that no service center exceeds its capacity.
- This project involved many data structures that I learned about in CMSC420, including Leftist Heaps and Extended kd-Trees.
