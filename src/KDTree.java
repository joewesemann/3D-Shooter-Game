import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

import com.jogamp.opengl.util.gl2.GLUT;

//class containing the KD Tree structure
public class KDTree {
	static KDNode root;
	
	//Node class
	public static class KDNode<T>{
		//every node has a ball, left and right, and variable to determine axis
		Ball b;
		KDNode left, right;
		int axis; // 0 is x axis, 1 is y axis, 2 is z axis
		
		//Node Constructor
		KDNode(Ball ball, int newAxis){
			//set left and right to null
			left = null;
			right = null;
			//get the ball/axis passed in
			b = ball;
			axis = newAxis;
		}
		
		//Ball class
		public static class Ball {
			//every ball has x/y/z coordinate
			double x;
			double y;
			double z;
			double radius;
			
			//velocity coord
			double vx, vy, vz;
			
			//keep track of time 
			long lastTime;
			
			//store if ball or bomb, bomb = 1, ball = 0
			int type;
			
			//ball constructor
			Ball(double X, double Y, double Z, double VX, double VY, double VZ, double RADIUS, int TYPE){
				//set all the values from values passed in
				x = X;
				y = Y;
				z = Z;
				vx = VX;
				vy = VY; 
				vz = VZ;
				radius = RADIUS;
				//store the time
				lastTime = System.currentTimeMillis();
				//ball or bomb
				type = TYPE;
			}
			//update method
			public void update(){
				//dt is current time, store the previous time each update
				long dt = System.currentTimeMillis() - lastTime;
		        lastTime = System.currentTimeMillis();
		        //calculate the velocity of all of the balls
		        x += vx * dt;
		        y += vy * dt;
		        z += vz * dt;
		        		        
		        //keep the balls inside of the room...
		        //if we hit one of the X coord walls reverse the velocity variables
		        //left wall
		        if(x < -150 + radius){
		            x = -150 + radius;
		            vx = -vx;
		        }
		        //right wall
		        else if(x > 150 - radius){
		            x = 150 - radius;
		            vx = -vx;
		        }
		        //if we hit one of the Y coord walls reverse the velocity variables
		        //floor
		        if(y < 0 + radius){
		            y = 0 + radius;
		            vy = -vy;
		        }
		        //ceiling
		        else if(y > 150 - radius){
		            y = 150 - radius;
		            vy = -vy;
		        }
		        //if we hit one of the Z coord walls reverse the velocity variables
		        //back wall
		        if(z < -150 + radius){
		            z = -150 + radius;
		            vz = -vz;
		        }
		        //front wall
		        else if(z > 150 - radius){
		            z = 150 - radius;
		            vz = -vz;
		        } 
			}
			
			//draw method
			public void draw(GLAutoDrawable gld, GLUT gt){
				final GL2 gl = gld.getGL().getGL2(); 
	            // set color
	            //for balls (targets)
				if(type == 0){
	            	float color[] = {1.5F, 0.5F, 0.6F, 1.0F};
	            	gl.glMaterialfv(1032, 5634, color, 0);
	            }
				//for bombs
				else if(type == 1){
	            	float color[] = {0.0F, 0.0F, 0.0F, 1.0F};
	            	gl.glMaterialfv(1032, 5634, color, 0);
	            }
	            gl.glPushMatrix();
	            gl.glTranslated(x, y, z);
	            gt.glutSolidSphere(radius, 30, 30);
	            gl.glPopMatrix();
			}
		}
	}
	
	//object to compare the X values with
    public class XComparator implements Comparator{
		@Override
		public int compare(Object o1, Object o2) {
			return compare((KDNode.Ball)o1, (KDNode.Ball)o2);
		}
		//method to make a comparison on the x value of 2 Balls
		public int compare(KDNode.Ball ball1, KDNode.Ball ball2)
        {
            if(ball1.x < ball2.x){
                return -1;
            }
            if(ball1.x == ball2.x){
            	return 0;
            }
            else{
            	return 1;
            }
        }
    }
    
    //object to compare the Y values with
    public class YComparator implements Comparator{
    	@Override
		public int compare(Object o1, Object o2) {
			return compare((KDNode.Ball)o1, (KDNode.Ball)o2);
		}
		//method to make a comparison on the y value of two Balls
		public int compare(KDNode.Ball ball1, KDNode.Ball ball2){
            if(ball1.y < ball2.y){
                return -1;
            }
            if(ball1.y == ball2.y){
            	return 0;
            }
            else{
            	return 1;
            }
        }
    }
    
    //object to compare the Z values with
    public class ZComparator implements Comparator{
		@Override
		public int compare(Object o1, Object o2) {
			return compare((KDNode.Ball)o1, (KDNode.Ball)o2);
		}
		//method to make a comparison on the z value of 2 Balls
		public int compare(KDNode.Ball ball1, KDNode.Ball ball2)
        {
            if(ball1.z < ball2.z){
                return -1;
            }
            if(ball1.z == ball2.z){
            	return 0;
            }
            else{
            	return 1;
            }
        }
    }
	
  	//KDTree Constructor
	KDTree(ArrayList<KDNode.Ball> list){
        //make an array to be sorted by X's, Y's, and Z's
		KDNode.Ball xPts[] = new KDNode.Ball[list.size()];
        KDNode.Ball yPts[] = new KDNode.Ball[list.size()];
        KDNode.Ball zPts[] = new KDNode.Ball[list.size()];
        //put all the points in the x, y, and z lists
        for(int i = 0; i < list.size(); i++){
        	xPts[i] = list.get(i);
        	yPts[i] = list.get(i);
        	zPts[i] = list.get(i);
        }
        //sort the arrays based on either the x, y, or z coordinate
        Arrays.sort(xPts, new XComparator());
        Arrays.sort(yPts, new YComparator());
        Arrays.sort(zPts, new ZComparator());
        //start from the root, pass in both lists and the start and end
        root = buildTreeX(xPts, yPts, zPts, 0, list.size());
	} 
	
	//buildtree on an X split
	public KDNode buildTreeX(KDNode.Ball xPts[], KDNode.Ball yPts[], KDNode.Ball zPts[], int start, int end){
		//base case 
		if(start >= end)
			return null;
		
		//tmp array and counter
		KDNode.Ball temp[] = new KDNode.Ball[end - start];
		int j = 0;
		
		//get the median
		int median = start + (end - start) / 2;
		//get the pivot
		KDNode.Ball pivot = xPts[median];
		
		//organize the yPts into the temp array that belong on the left of the pivot
		for(int k = start; k < end; k++){
            //loop until we get to the pivot boid
			if(yPts[k].x <= pivot.x && yPts[k] != pivot)
                temp[j++] = yPts[k]; //put the lesser boid into the temp array
        }
		//organize the yPts into the temp array that belong on the right side of the pivot
        for(int k = start; k < end; k++){
            //loop until we reach the end of the array
        	if(yPts[k].x > pivot.x)
                temp[j++] = yPts[k]; // put the greater boid into the temp array
        }
        //finish organizing the yPts array by moving everything back in
        for(int k = start; k < end; k++){
            //until we get to the original pivot
        	if(k < median)
                yPts[k] = temp[k - start];
            //when we get to the original pivot
        	else if(k == median)
                yPts[k] = pivot; //set the pivot at the median
            //while we are greater than the original pivot
        	else if(k > median)
                yPts[k] = temp[k - start - 1];
        }
		        
		//create the node and start moving down the tree, breaking the list apart
		KDNode n = new KDNode(pivot, 0); //set node to 0 for x axis
		n.left = buildTreeY(xPts, yPts, zPts, start, median);
		n.right = buildTreeY(xPts, yPts, zPts, median + 1, end);
		
		return n;
	}
	
	//buildtree on a Y split
	public KDNode buildTreeY(KDNode.Ball xPts[], KDNode.Ball yPts[], KDNode.Ball zPts[], int start, int end){
		//base case 
		if(start >= end)
			return null;
		
		//tmp array and counter
		KDNode.Ball temp[] = new KDNode.Ball[end - start];
		int j = 0;
		
		//get the median
		int median = start + (end - start) / 2;
		//get the pivot
		KDNode.Ball pivot = yPts[median];
		
		//organize the zPts into the temp array that belong on the left of the pivot
		for(int k = start; k < end; k++){
            //loop until we get to the pivot boid
			if(zPts[k].y <= pivot.y && zPts[k] != pivot)
                temp[j++] = zPts[k]; //put the lesser boid into the temp array
        }
		//organize the xPts into the temp array that belong on the right side of the pivot
        for(int k = start; k < end; k++){
            //loop until we reach the end of the array
        	if(zPts[k].y > pivot.y)
                temp[j++] = zPts[k]; // put the greater boid into the temp array
        }
        //finish organizing the zPts array by moving everything back in
        for(int k = start; k < end; k++){
            //until we get to the original pivot
        	if(k < median)
                zPts[k] = temp[k-start];
            //when we get to the original pivot
        	else if(k == median)
                zPts[k] = pivot; //set the pivot at the median
            //while we are greater than the original pivot
        	else if(k > median)
                zPts[k] = temp[k-start-1];
        }
        
		//create the node and start moving down the tree, breaking the list apart
		KDNode n = new KDNode(pivot, 1); //set node to 1 for y axis
		n.left = buildTreeZ(xPts, yPts, zPts, start, median);
		n.right = buildTreeZ(xPts, yPts, zPts, median + 1, end);
		
		return n;
	}
	
	//buildtree on a Z split
	public KDNode buildTreeZ(KDNode.Ball xPts[], KDNode.Ball yPts[], KDNode.Ball zPts[], int start, int end){
		//base case 
		if(start >= end)
			return null;
		
		//tmp array and counter
		KDNode.Ball temp[] = new KDNode.Ball[end - start];
		int j = 0;
		
		//get the median
		int median = start + (end - start) / 2;
		//get the pivot
		KDNode.Ball pivot = zPts[median];
		
		//organize the xPts into the temp array that belong on the left of the pivot
		for(int k = start; k < end; k++){
            //loop until we get to the pivot boid
			if(xPts[k].z <= pivot.z && xPts[k] != pivot)
                temp[j++] = xPts[k]; //put the lesser boid into the temp array
        }
		//organize the xPts into the temp array that belong on the right side of the pivot
        for(int k = start; k < end; k++){
            //loop until we reach the end of the array
        	if(xPts[k].z > pivot.z)
                temp[j++] = xPts[k]; // put the greater boid into the temp array
        }
        //finish organizing the xPts array by moving everything back in
        for(int k = start; k < end; k++){
            //until we get to the original pivot
        	if(k < median)
                xPts[k] = temp[k-start];
            //when we get to the original pivot
        	else if(k == median)
                xPts[k] = pivot; //set the pivot at the median
            //while we are greater than the original pivot
        	else if(k > median)
                xPts[k] = temp[k-start-1];
        }
        
		//create the node and start moving down the tree, breaking the list apart
		KDNode n = new KDNode(pivot, 2); //set node to 2 for z axis
		n.left = buildTreeX(xPts, yPts, zPts, start, median);
		n.right = buildTreeX(xPts, yPts, zPts, median + 1, end);
		
		return n;
	}
	
	//find the nearest neighbors within a given radius
	public ArrayList<KDNode.Ball> neighbors(double radius, KDNode.Ball centerBoid){
		ArrayList<KDNode.Ball> neighborList = new ArrayList<>();
		return neighborList = neighbors(root, radius, centerBoid, neighborList);
	}
	public ArrayList<KDNode.Ball> neighbors(KDNode n, double radius, KDNode.Ball centerBoid, ArrayList<KDNode.Ball> neighborList){
		//base case 
		if(n == null)
			return null;
		
		//compute circular distance, add the node to the list if it is within radius
		if(Math.pow(centerBoid.x - n.b.x, 2) + Math.pow(centerBoid.y - n.b.y, 2) + Math.pow(centerBoid.z - n.b.z, 2) < Math.pow(radius, 2)){
			neighborList.add(n.b);
		}
		
		//if the node is an X node
		if(n.axis == 0 && n.left != null && n.right != null){
			//if we have to descend left subtree
			if((n.b.x - centerBoid.x) > radius)
				neighbors(n.left, radius, centerBoid, neighborList);
			//if we have to descend right subtree
			else if((n.b.x - centerBoid.x) < -radius){
				neighbors(n.right, radius, centerBoid, neighborList);
			}
			//if we have to descend both subtrees
			else{
				neighbors(n.left, radius, centerBoid, neighborList);
				neighbors(n.right, radius, centerBoid, neighborList);
			}
		}
		//if the node is an Y node
		if(n.axis == 1 && n.left != null && n.right != null){
			//if we have to descend left subtree
			if((n.b.y - centerBoid.y) > radius)
				neighbors(n.left, radius, centerBoid, neighborList);
			//if we have to descend right subtree
			else if((n.b.y - centerBoid.y) < -radius){
				neighbors(n.right, radius, centerBoid, neighborList);
			}
			//if we have to descend both subtrees
			else{
				neighbors(n.left, radius, centerBoid, neighborList);
				neighbors(n.right, radius, centerBoid, neighborList);
			}
		}
		//if the node is an Z node
		if(n.axis == 2 && n.left != null && n.right != null){
			//if we have to descend left subtree
			if((n.b.z - centerBoid.z) > radius)
				neighbors(n.left, radius, centerBoid, neighborList);
			//if we have to descend right subtree
			else if((n.b.z - centerBoid.z) < -radius){
				neighbors(n.right, radius, centerBoid, neighborList);
			}
			//if we have to descend both subtrees
			else{
				neighbors(n.left, radius, centerBoid, neighborList);
				neighbors(n.right, radius, centerBoid, neighborList);
			}
		}
		
		return neighborList;
	}
	
	
	//preform preorder traversal recursivley
    public static void preOrderPrint() {
        System.out.println("PreOrder Traversal of KDTree: ");
        preOrderPrint(root);
        System.out.println(" ");
    }
    private static void preOrderPrint(KDNode n) {
        if (n == null) return;
        String axis = null;
        if(n.axis == 0)
        	axis = "X";
        else if(n.axis == 1)
        	axis = "Y";
        else if(n.axis == 2)
        	axis = "Z";
        System.out.print("Coordinates: " + n.b.x + ", " + n.b.y + ", " + n.b.z + " Axis: " + axis + "\n");
        preOrderPrint(n.left);
        preOrderPrint(n.right);
    }
	
    
    //MAIN METHOD FOR TESTING PURPOSES 
    /*
	public static void main(String args[]){
		ArrayList<KDNode.Ball> list = new ArrayList();
		//create some boids for list
		KDNode.Ball ball1 = new KDNode.Ball(1,1,3, 0, 0, 0, 0, 0);
		KDNode.Ball ball2 = new KDNode.Ball(2,2,2, 0, 0, 0, 0, 0);
		KDNode.Ball ball3 = new KDNode.Ball(3,3,3, 0, 0, 0, 0, 0);
		KDNode.Ball ball4 = new KDNode.Ball(4,4,6, 0, 0, 0, 0, 0);
		KDNode.Ball ball5 = new KDNode.Ball(5,5,5, 0, 0, 0, 0, 0);
		KDNode.Ball ball6 = new KDNode.Ball(6,6,6, 0, 0, 0, 0, 0);
		KDNode.Ball ball7 = new KDNode.Ball(7,7,9, 0, 0, 0, 0, 0);
		KDNode.Ball ball8 = new KDNode.Ball(8,8,8, 0, 0, 0, 0, 0);
		KDNode.Ball ball9 = new KDNode.Ball(9,9,9, 0, 0, 0, 0, 0);
		KDNode.Ball boid10 = new KDNode.Ball(10,10,10, 0, 0, 0, 0, 0);
		
		//query point
		KDNode.Ball queryPoint = new KDNode.Ball(6,6,4, 0, 0, 0, 0, 0);
		
		//add the boids to the list
		list.add(ball1);
		list.add(ball2);
		list.add(ball3);
		list.add(ball4);
		list.add(ball5);
		list.add(ball6);
		list.add(ball7);
		list.add(ball8);
		list.add(ball9);
		list.add(boid10);
		
		//create and build the tree of the list
		KDTree tree = new KDTree(list);
		
		//run a test query and return into list range of 20
		list = tree.neighbors(20, queryPoint);
		
		//print preorder tree
		preOrderPrint();
    }
    */
}

