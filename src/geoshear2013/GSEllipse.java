/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package geoshear2013;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;

/**
 *
 * @author cwarren
 */
public class GSEllipse {
    protected double x,y;
    protected double majorRadius,minorRadius;
    protected double theta;
    
    protected AffineTransform matrix; // the affine tranform matrix that converts the unit circle into this ellipse;
    
    public Shape shape;
    
    /*------------------------------------------------------------------------*/
    public GSEllipse() { // unit circle is the default
        this(0, 0, 1, 1, 0);
    }
    public GSEllipse(double majorRadius, double minorRadius) {
        this(0, 0, majorRadius, minorRadius, 0);
    }
    public GSEllipse(double majorRadius, double minorRadius, double theta) {
        this(0, 0, majorRadius, minorRadius, theta);
    }
    public GSEllipse(double x, double y, double majorRadius, double minorRadius, double theta) {
        this.x = x;
        this.y = y;
        this.majorRadius = majorRadius;
        this.minorRadius = minorRadius;
        this.theta = theta;
        
        this.setMatrixFromKeyData();
    }
    public GSEllipse(AffineTransform t) { // unit circle is the default
        new GSEllipse().deform(t);
    }
    /*------------------------------------------------------------------------*/

    @Override
    public GSEllipse clone() {
        return new GSEllipse(this.x,this.y,this.majorRadius,this.minorRadius,this.theta);
    }
    
    @Override
    public String toString() {
        return this.keyDataAsString()+"\n"+this.getMatrix().toString();
    }

    public String keyDataAsString() {
        return "x: "+this.x+", y: "+this.y+", majorRadius: "+this.majorRadius+", minorRadius: "+this.minorRadius+", theta: "+this.theta;
    }

    public void errDump() {
        System.err.println(this.toString());
    }
    /*------------------------------------------------------------------------*/

    public GSEllipse getDeformedGSEllipse(AffineTransform deformation) {
        GSEllipse deformedEllipse = this.clone();
        deformedEllipse.deform(deformation);
        return deformedEllipse;
    }

    public void deform(AffineTransform deformation) {
        System.err.println("predeform key data: "+this.keyDataAsString());
        System.err.println("predeform matrix: "+this.getMatrix().toString());
        System.err.println("deformation: "+deformation.toString());
        this.getMatrix().concatenate(deformation);
        System.err.println("postdeform key data: "+this.keyDataAsString());
        System.err.println("postdeform matrix: "+this.getMatrix().toString());
        this.setKeyDataFromMatrix();
        this.setMatrixFromKeyData();
        System.err.println("postset key data: "+this.keyDataAsString());
    }
    
    /*------------------------------------------------------------------------*/

    /**
     * set the matrix that defines this ellipse from its center (x and y), axes (majorRadius and minorRadius) and rotation (theta)
     */
    public final void setMatrixFromKeyData() {        
        this.matrix = new AffineTransform();
//        this.matrix = T*R*S
        this.matrix.concatenate(AffineTransform.getTranslateInstance(this.x, this.y*-1));
        this.matrix.concatenate(AffineTransform.getRotateInstance(this.theta*-1));
        this.matrix.concatenate(AffineTransform.getScaleInstance(this.majorRadius, this.minorRadius));

        //System.err.println(this.matrix);
        
        this.setShape();
    }
    
    public void setKeyDataFromMatrix() {
        Matrix2x2[] u_sig_vt = Matrix2x2.svdOf(this.getMatrix());

        this.majorRadius = u_sig_vt[1].m00;
        this.minorRadius = u_sig_vt[1].m11;
        System.err.println("sig: "+u_sig_vt[1].toString());
        System.err.println("vt: "+u_sig_vt[2].toString());
//        this.theta = Math.acos(u_sig_vt[2].m00);
        this.theta = Math.asin(u_sig_vt[2].m01);
    }

    /**
     * set the shape object for this ellipse from its internal data
     */
    public void setShape() {
        this.shape = this.getMatrix().createTransformedShape(new Ellipse2D.Double(-1,-1,2,2)); // transform the unit circle
    }
    
    /**
     * set the center (x and y) of this ellipse relative to a new origin that's given in the coordinate of the original
     */
    public void shiftPosition(double x, double y) {
        this.x = this.x - x;
        this.y = this.y - y;
        this.setMatrixFromKeyData();
    }
    
    public void applyEllipseAsTransform(GSEllipse e) {
        System.err.println("TODO: implement setKeyDataFromMatrix in GSEllipse");
    }
            
    /*------------------------------------------------------------------------*/

    /**
     * @return the x
     */
    public double getX() {
        return x;
    }

    /**
     * @param x the x to set
     */
    public void setX(double x) {
        this.x = x;
        this.setMatrixFromKeyData();
    }

    /**
     * @return the y
     */
    public double getY() {
        return y;
    }

    /**
     * @param y the y to set
     */
    public void setY(double y) {
        this.y = y;
        this.setMatrixFromKeyData();
    }

    /**
     * @return the majorRadius
     */
    public double getMajorRadius() {
        return majorRadius;
    }

    /**
     * @param majorRadius the majorRadius to set
     */
    public void setMajorRadius(double majorRadius) {
        this.majorRadius = majorRadius;
        this.setMatrixFromKeyData();
    }

    /**
     * @return the minorRadius
     */
    public double getMinorRadius() {
        return minorRadius;
    }

    /**
     * @param minorRadius the minorRadius to set
     */
    public void setMinorRadius(double minorRadius) {
        this.minorRadius = minorRadius;
        this.setMatrixFromKeyData();
    }

    /**
     * NOTE: theta is internally inverted to implement counter-clockwise rotation
     * @return the theta
     */
    public double getTheta() {
        return theta;
    }

    /**
     * NOTE: theta is internally inverted to implement counter-clockwise rotation
     * @param theta the theta to set
     */
    public void setTheta(double theta) {
        this.theta = theta;
        this.setMatrixFromKeyData();
    }
    
    /**
     * @return the affine transform that converts the unit circle to this ellipse
     */
    public AffineTransform getMatrix() {
        return this.matrix;
    }

}
