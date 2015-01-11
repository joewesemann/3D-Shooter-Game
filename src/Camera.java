//Camera class from Adam

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.glu.GLU;

public class Camera {
    double ex, ey, ez, cx, cy, cz, ux, uy, uz;
    double theta = 0.087266463;
        
    Camera(double ex, double ey, double ez, double cx, double cy, double cz, double ux, double uy, double uz) {
	this.ex = ex;
	this.ey = ey;
	this.ez = ez;
	this.cx = cx;
	this.cy = cy;
	this.cz = cz;
	this.ux = ux;
	this.uy = uy; 
	this.uz = uz;
    }
        
    void setLookAt(GLU glu) {
	glu.gluLookAt(ex, ey, ez, cx, cy, cz, ux, uy, uz);
    }
        
    void forward() {
	double dx = cx - ex, dy = cy - ey, dz = cz - ez;
	double m = Math.sqrt(sqr(dx)+sqr(dy)+sqr(dz));
	dx /= m/2;
	dy /= m/2;
	dz /= m/2;
            
	cx += dx;
	cy += dy;
	cz += dz;
	ex += dx;
	ey += dy;
	ez += dz;
    }
        
    void back() {
	double dx = cx - ex, dy = cy - ey, dz = cz - ez;
	double m = Math.sqrt(sqr(dx)+sqr(dy)+sqr(dz));
	dx /= m/2;
	dy /= m/2;
	dz /= m/2;
            
	cx -= dx;
	cy -= dy;
	cz -= dz;
	ex -= dx;
	ey -= dy;
	ez -= dz;
    }
        
    void left() {
	double dx = cx - ex, dy = cy - ey, dz = cz - ez;
	double m = Math.sqrt(sqr(dx)+sqr(dy)+sqr(dz));
	dx /= m;
	dy /= m;
	dz /= m;
	double nx = Math.cos(theta)*dx + Math.sin(theta)*dz;
	double ny = dy;
	double nz = -Math.sin(theta)*dx + Math.cos(theta)*dz;
	cx = ex+nx;
	cy = ey+ny;
	cz = ez+nz;
    }

    void right() {
	double dx = cx - ex, dy = cy - ey, dz = cz - ez;
	double m = Math.sqrt(sqr(dx)+sqr(dy)+sqr(dz));
	dx /= m;
	dy /= m;
	dz /= m;
	double nx = Math.cos(theta)*dx - Math.sin(theta)*dz;
	double ny = dy;
	double nz = Math.sin(theta)*dx + Math.cos(theta)*dz;
	cx = ex+nx;
	cy = ey+ny;
	cz = ez+nz;
    }
    
    //look up, implemented by me
    void up() {
    	double dx = cx - ex, dy = cy - ey, dz = cz - ez;
    	double m = Math.sqrt(sqr(dx)+sqr(dy)+sqr(dz));
    	dx /= m/2;
    	dy /= m/2;
    	dz /= m/2;
                
    	cx += dx;
    	//cy += dy;
    	cz += dz;
    	ex += dx;
    	//ey += dy;
    	ez += dz;
    }
    
    double sqr(double x) { 
		return x * x; 
	}
}