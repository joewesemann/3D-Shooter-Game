import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.PopupMenu;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.ListIterator;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.fixedfunc.GLLightingFunc;
import javax.media.opengl.fixedfunc.GLMatrixFunc;
import javax.media.opengl.glu.GLU;
import javax.swing.JApplet;

import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.gl2.GLUT;

public class Game extends JApplet implements GLEventListener, MouseListener, KeyListener, MouseMotionListener {
	ArrayList balls, bombs;
	Camera camera, camera2;
	FPSAnimator animator;
	Thread updateThread;
	GLU glu; 
	GLUT glut;
	int winWidth=1000, winHeight=800;
	
	//constructor
	public Game(){
		balls = new ArrayList<KDTree.KDNode.Ball>();
		bombs = new ArrayList<KDTree.KDNode.Ball>();
		camera = new Camera(0.0, 25, 0.0, 0.0, 25, 1.0, 0.0, 1.0, 0.0);
		//create 15 balls
		for(int i = 0; i < 15; i++){
			double posx = (Math.random() - 0.5) * 200;
            double posy = Math.random() * 100;
            double posz = (Math.random() - 0.5) * 200;
            double velx = 0.25 * (Math.random() - 0.5);
            double vely = 0.25 * (Math.random() - 0.5);
            double velz = 0.25 * (Math.random() * 0.5);
            double radius = 5;
            balls.add(new KDTree.KDNode.Ball(posx, posy, posz, velx, vely, velz, radius, 0));
        }
	}
	
	//setup the box and draw the balls and bombs
	@Override
	public synchronized void display(GLAutoDrawable gld) {
		GL2 gl = gld.getGL().getGL2();
        //clear the buffer and load the identity
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();
        
        //set the camera to be located at the origin
        camera.setLookAt(glu);
        
        //set the position and diffuse/ambient terms of the light
        float pos[] = {0.0F, 50F, 0.0F, 1.0F};
        gl.glLightfv(16385, 4611, pos, 0);
        float diffuse[] = {0.7F, 0.7F, 0.7F, 0.0F};
        gl.glLightfv(16385, 4609, diffuse, 0);
        float ambient[] = {0.2F, 0.2F, 0.2F, 0.0F};
        gl.glLightfv(16385, 4608, ambient, 0);
        
        
        //set the colors
        float topBottomColor[] = {0.4F, 0.4F, 0.4F, 0.4F};
        float frontBackColor[] = {1.0F, 1.0F, 5.0F, 1.0F};
        float leftRightColor[] = {0.5F, 0.5F, 0.5F, 0.5F}; 
        
        //begin building
        gl.glBegin(7);
        //draw the floor and ceiling
        gl.glMaterialfv(1032, 5634, topBottomColor, 0);
        gl.glNormal3d(0, -1, 0);
        gl.glVertex3d(-150, 150, -150);
        gl.glVertex3d(150, 150, -150);
        gl.glVertex3d(150, 150, 150);
        gl.glVertex3d(-150, 150, 150);
        gl.glMaterialfv(1032, 5634, topBottomColor, 0);
        gl.glNormal3d(0, 1, 0);
        gl.glVertex3d(-150, 0, -150);
        gl.glVertex3d(-150, 0, 150);
        gl.glVertex3d(150, 0, 150);
        gl.glVertex3d(150, 0, -150);
        
        //draw back and front walls
        gl.glMaterialfv(1032, 5634, frontBackColor, 0);
        gl.glNormal3d(0, 0, 1);
        gl.glVertex3d(-150, 0, -150);
        gl.glVertex3d(150, 0, -150);
        gl.glVertex3d(150, 150, -150);
        gl.glVertex3d(-150, 150, -150);
        gl.glMaterialfv(1032, 5634, frontBackColor, 0);
        gl.glNormal3d(0, 0, -1);
        gl.glVertex3d(-150, 0, 150);
        gl.glVertex3d(-150, 150, 150);
        gl.glVertex3d(150, 150, 150);
        gl.glVertex3d(150, 0, 150);
        
        //draw left and right walls 
        gl.glMaterialfv(1032, 5634, leftRightColor, 0);
        gl.glNormal3d(1, 0, 0);
        gl.glVertex3d(-150, 0, -150);
        gl.glVertex3d(-150, 150, -150);
        gl.glVertex3d(-150, 150, 150);
        gl.glVertex3d(-150, 0, 150);
        gl.glMaterialfv(1032, 5634, leftRightColor, 0);
        gl.glNormal3d(-1, 0, 0);
        gl.glVertex3d(150, 0, -150);
        gl.glVertex3d(150, 0, 150);
        gl.glVertex3d(150, 150, 150);
        gl.glVertex3d(150, 150, -150);
        //end
        gl.glEnd();
        
        //loop across list of balls 
        for(ListIterator ballsIterator = balls.listIterator(); ballsIterator.hasNext();)
        {
            //draw balls to the canvas
        	((KDTree.KDNode.Ball)ballsIterator.next()).draw(gld, glut);
        	
        	//loop across list of bombs
        	for(ListIterator bombsIterator = bombs.listIterator(); bombsIterator.hasNext();)
            {
                //draw bomb to canvas if any exsist
        		((KDTree.KDNode.Ball)bombsIterator.next()).draw(gld, glut);
            }
        }
        
        //when there are no more balls (targets)
        if(balls.isEmpty()){
            //display a victory screen (Text is moveable because it is implemented as a matrix)
        	gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
            gl.glMatrixMode(GL2.GL_MODELVIEW);
            gl.glLoadIdentity();
            camera.setLookAt(glu);
            gl.glPushMatrix();
            GLUT glut = new GLUT();
            gl.glTranslatef(0, 0, 0);
            gl.glColor3f(1, 0, 0);
            gl.glRasterPos2d(60, 50);
            glut.glutBitmapString(GLUT.BITMAP_TIMES_ROMAN_24, "You Win!! Thanks for playing.");
            gl.glPopMatrix();
        }
	}
	
	//main init method that runs update
	@Override
	public void init() {
		setLayout(new FlowLayout());
        // create a gl drawing canvas
        GLProfile glp = GLProfile.getDefault();
        GLCapabilities caps = new GLCapabilities(glp);
        GLCanvas canvas = new GLCanvas(caps);
        canvas.setPreferredSize(new Dimension(winWidth, winHeight));
        
        // add gl event listener
        canvas.addGLEventListener(this);
        canvas.addMouseListener(this);
        canvas.addKeyListener(this);
        add(canvas);
        setSize(winWidth, winHeight);
        // add the canvas to the frame
        animator = new FPSAnimator(canvas,30);
        updateThread = new Thread(new Runnable() {
        	public void run() {
        		while(true) {
        			update();
        		}
        	}
	    });
	}
	
	//init method that initializes the lighting for 3D graphics
	@Override
	public void init(GLAutoDrawable gld) {
		glu = new GLU();
        glut = new GLUT();
        
        GL2 gl = gld.getGL().getGL2();
        //setup the projection matrix
        gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluPerspective(50, 1.3, 0.5, 10000);
        //turn on lighting and smooth, 3D Graphics
        gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
        gl.glLoadIdentity();
        gl.glEnable(GLLightingFunc.GL_NORMALIZE);
        gl.glEnable(GL.GL_CULL_FACE);
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glEnable(2896); //lighting
        gl.glEnable(16385); //lighting
        gl.glShadeModel(GLLightingFunc.GL_SMOOTH);
	}
	
	//method that is called constantly while running
	public synchronized void update() {
		
		//loop across list of balls updating position of the balls
        for(ListIterator listIteratorBall = balls.listIterator(); listIteratorBall.hasNext();){
        	//call update on every ball
        	((KDTree.KDNode.Ball)listIteratorBall.next()).update();
        	//loop across list of bombs
            for(ListIterator listIteratorBomb = bombs.listIterator(); listIteratorBomb.hasNext();){
            	KDTree.KDNode.Ball ball = (KDTree.KDNode.Ball)listIteratorBomb.next();
            	ball.update(); //call update on every bomb
            	//check if bomb is too small to be on screen 
            	if(ball.radius < 0){
            		listIteratorBomb.remove(); //remove the bomb from the list
                }
            }
        }
        
        
        //COLLISION DETECTION USING KD TREE (COMMENT THIS OUT IF YOU ARE USING THE SIMPLE LIST DETECTION BELOW)
        /*
        //loop across the list of bombs to see if any bombs have hit any targets
        for(ListIterator listIteratorBomb = bombs.listIterator(); listIteratorBomb.hasNext();){
        	//build KD tree of balls to detect collisions
        	KDTree tree = new KDTree(balls);
        	//arraylist to store the nearest neighbors
        	ArrayList neighbors;
            //get the next bomb
        	KDTree.KDNode.Ball bomb = (KDTree.KDNode.Ball)listIteratorBomb.next();
        	//make a list of nearest neighbors using the KD Tree, radius of 30 so it is easy to hit targets
        	neighbors = tree.neighbors(30, bomb);
        	
        	//guard against null pointer
        	if(neighbors != null){
        		//if there is something in the list, we have a collision 
        		if(!neighbors.isEmpty()){
            		//remove the current bomb from the list
            		if(listIteratorBomb != null)
            			listIteratorBomb.remove();
            		//remove any neighbors that have collided with it
            		for(int i = 0; i < neighbors.size(); i++){
            			balls.remove(neighbors.get(i));
            		}
            	}
        	}
        }
        */
        
        
        //COLLISION DETECTION SIMPLE LIST (UNCOMMENT FOR SLIGHTLY FASTER IMPLEMENTATION)
        //loop across list of balls checking for collisions
        for(ListIterator listIteratorBall = balls.listIterator(); listIteratorBall.hasNext();){
        	//get next ball
        	KDTree.KDNode.Ball currentBall = (KDTree.KDNode.Ball)listIteratorBall.next();
        
        	//loop across list of bombs
        	for(ListIterator listIteratorBomb = bombs.listIterator(); listIteratorBomb.hasNext();){
        		//get next bomb
        		KDTree.KDNode.Ball currentBomb = (KDTree.KDNode.Ball)listIteratorBomb.next();
        		
        		//check if a bomb and a ball have collided
                if((currentBall.x - currentBomb.x) + camera.sqr(currentBall.y - currentBomb.y) + camera.sqr(currentBall.z - currentBomb.z) < camera.sqr(currentBall.radius + currentBomb.radius)){
                    //remove the ball and the bomb
                	listIteratorBall.remove();
                    listIteratorBomb.remove();
                }
            }
        }
	}

	//mehod to move camera, and shoot bomb on key press
	@Override
	public synchronized void keyPressed(KeyEvent e) {
		//left key pressed
		if(e.getKeyCode() == 37){
			camera.left();
		}
		//right key pressed
		if(e.getKeyCode() == 39){
			camera.right();
		}
		//up key pressed
		if(e.getKeyCode() == 38){
			camera.up();
		}
		//down key pressed
		if(e.getKeyCode() == 40){
			camera.back();
		}
		//space bar pressed
		if(e.getKeyCode() == 32){
			bombs.add(new KDTree.KDNode.Ball(camera.ex, camera.ey, camera.ez, 0.1 * (camera.cx - camera.ex), 0.05, 0.2 * (camera.cz - camera.ez), 3, 1));
		}
		//0 key pressed
		if(e.getKeyCode() == 96){
		}
	}
		
	@Override
    public void start(){
        animator.start();
        updateThread.start();
    }

    @Override
    public void stop(){
        animator.stop();
        updateThread.interrupt();
    }
    
    //unused methods
	@Override
	public void mouseClicked(MouseEvent e) {}
	@Override
	public void mousePressed(MouseEvent e) {}
	@Override
	public void mouseReleased(MouseEvent e) {}
	@Override
	public void mouseEntered(MouseEvent e) {}
	@Override
	public void mouseExited(MouseEvent e) {}
	@Override
	public void keyTyped(KeyEvent e) {}
	@Override
	public void keyReleased(KeyEvent e) {}
	@Override
	public void reshape(GLAutoDrawable arg0, int arg1, int arg2, int arg3, int arg4) {}
	@Override
	public void dispose(GLAutoDrawable arg0) {}
	@Override
	public void mouseDragged(MouseEvent e) {}
	@Override
	public void mouseMoved(MouseEvent e) {}
}
