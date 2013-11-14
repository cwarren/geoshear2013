/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.williams.geoshear2013;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JPanel;

/**
 *
 * @author cwarren
 */
class GSComplexUI extends JPanel {
    public GSComplex gsc;
    private MainWindow mainWindow;

    public Deformation tentativeDeformation;
    public Deformation cumuDeformation;
    public Deformation cumuTentativeDeformation;
    public GSPebble tentativeStrain;
    public GSPebble cumuStrain;
    public GSPebble cumuTentativeStrain;
    
    private AffineTransform displayTransform; // the pan and zoom controlled by the user
    
    private int currentUIMode;
    private int cachedUIMode;
    
    public static int UI_MODE_DEFORMS = 1;
    public static int UI_MODE_EDIT_PEBBLES = 2;
    public static int UI_MODE_STRAIN_NAV = 3;

    public static double ZOOM_MIN = .1;
    public static double ZOOM_MAX = 10;
    
    private static double zoomPerScrollFactor = .025;

//    public static Color INFO_COLOR_TENT = Color.RED;
//    public static Color INFO_COLOR_CUMUTENT = Color.PINK;
//    public static Color INFO_COLOR_CUMU = Color.MAGENTA;
    public static Color INFO_COLOR_TENT = new Color(180,0,0);
    public static Color INFO_COLOR_CUMUTENT = new Color(140,0,0);
    public static Color INFO_COLOR_CUMU = new Color(90,0,0);
    public static Color INFO_COLOR_NAV_DEF = new Color(150,150,150);
    
    private static BasicStroke INFO_STROKE_TENT = new BasicStroke(2,BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0,new float[] { 1, 2 }, 0);
    private static BasicStroke INFO_STROKE_CUMUTENT = new BasicStroke(2,BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0,new float[] { 3, 6 }, 0);
    private static BasicStroke INFO_STROKE_CUMU = new BasicStroke(3,BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0,new float[] { 4, 5 }, 0);
    private static BasicStroke INFO_STROKE_NAV_DEF = new BasicStroke(2,BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0,new float[] { 1, 3 }, 0);
    
    private double lastMouseDownX;
    private double lastMouseDownY;
    private Point2D lastMouseDownPoint;
    private Point2D lastMouseDownPointRelGSCOrigin;
    private double lastMouseDownAngleInGSCSystem;
    private Point2D lastMouseDownPointInGSCSystem;

    private double lastMouseDragX;
    private double lastMouseDragY;
    private Point2D lastMouseDragPoint;
    private Point2D lastMouseDragPointRelGSCOrigin;
    private double lastMouseDragAngleInGSCSystem;
        
    private boolean shiftIsDown = false;
    private boolean ctrlIsDown = false;
    private boolean altIsDown = false;

    private boolean isDragDeform = false;

    public static double NEW_PEBBLE_MIN_FIRST_AXIS_DRAG = 10;
    public int pebbleCreationStage;
    public static int PEBBLE_CREATION_STAGE_BEGIN = 0;
    public static int PEBBLE_CREATION_STAGE_SETTING_FIRST_AXIS = 1;
    public static int PEBBLE_CREATION_STAGE_SETTING_SECOND_AXIS = 2;
    public static BasicStroke PEBBLE_CREATION_STROKE = new BasicStroke(2);
    public Point2D newPebbleAxis1Pt1;
    public Point2D newPebbleAxis1Pt2;
    public double newPebbleAxis1ThetaRad;
    public Point2D newPebbleCenter;
    public Point2D newPebbleAxis2Pt1;
    public Point2D newPebbleAxis2Pt2;
    public double newPebbleAxis2ThetaRad;
//    public 
    
    
    private boolean flagDisplayPebbleAxes = true;
    private boolean flagDisplayPebbleFill = false;
    private boolean flagDisplayBackgroundAxis = true;
    private boolean flagDisplayBackgroundImage = true;
    private boolean flagDisplayStrainEllipses = true;

    private Color colorOfNewPebbles = Color.BLUE;

    private final JFileChooser fileChooser = new JFileChooser ();
    private final FileFilterImage filterImage = new FileFilterImage();
    private final FileFilterTab filterTab = new FileFilterTab();
    private final FileFilterGeoShear filterGeoShear = new FileFilterGeoShear();    
    /*------------------------------------------------------------------------*/
    private Shape newPebShape;

    public GSComplexUI(GSComplex gsc,MainWindow mainWindow) {
        this.gsc = gsc;//.setCenter = new GSPoint(0,0);
        this.gsc.setUsedUI(this);
        this.mainWindow = mainWindow;
        this.displayTransform = new AffineTransform();
        this.tentativeDeformation = new Deformation();
        this.cumuDeformation = this.gsc.getCompositeTransform();
//        Util.todo("uses getCompositeTransform");
        this.cumuTentativeDeformation = this.tentativeDeformation.times(this.cumuDeformation);
        this.setStrains();
        this.setModeDeforms();
//        this.currentUIMode = GSComplexUI.UI_MODE_DEFORMS;
    }
    
    private void setDeformations() {
        this.tentativeDeformation = new Deformation();
        this.cumuDeformation = this.gsc.getCompositeTransform();
//        Util.todo("uses getCompositeTransform");
        this.cumuTentativeDeformation = this.tentativeDeformation.times(this.cumuDeformation);
    }
    
    private void setDeformations(Deformation d) {
        this.tentativeDeformation = d.clone();
        this.cumuDeformation = this.gsc.getCompositeTransform();
//        Util.todo("uses getCompositeTransform");
        this.cumuTentativeDeformation = this.tentativeDeformation.times(this.cumuDeformation);
    }


    
    private void setStrains() {
        this.tentativeStrain = new GSPebble(100,100);
        this.tentativeStrain.deform(this.tentativeDeformation);

        this.cumuStrain = new GSPebble(100,100);
//        this.gsc.deformations.runAllDeformationsOn(cumuStrain);
        this.gsc.deformations.runAllDeformationsOn(cumuStrain,this.gsc.getCurrentDeformationNumber());
//        Util.todo("uses runAllDeformationsOn");
        
//        this.cumuStrain.deform(this.cumuDeformation);
//        this.cumuTentativeStrain = cumuStrain.clone();
//        this.cumuTentativeStrain = new GSPebble(100,100);
//        this.gsc.deformations.runAllDeformationsOn(cumuTentativeStrain);
//        this.cumuTentativeStrain.deform(this.tentativeDeformation);
    }

    public void handleKeyPressed(java.awt.event.KeyEvent evt) {
        this.altIsDown = evt.isAltDown();
        this.ctrlIsDown = evt.isControlDown();
        this.shiftIsDown = evt.isShiftDown();
    }

    public void handleKeyReleased(java.awt.event.KeyEvent evt) {
//        System.err.println("gscui handleKeyReleased");
//        System.err.println(" evt: "+evt.toString());
        this.altIsDown = evt.isAltDown();
        this.ctrlIsDown = evt.isControlDown();
        this.shiftIsDown = evt.isShiftDown();
        if (this.currentUIMode == GSComplexUI.UI_MODE_DEFORMS) {
            if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER && ! this.isTentativeDeformationCleared()) {
                this.mainWindow.handleDeformationApplyRemove();
            } else 
            if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE  && ! this.isTentativeDeformationCleared()) {
                this.mainWindow.handleDeformReset();
            }
        } else if (this.currentUIMode == GSComplexUI.UI_MODE_EDIT_PEBBLES) {
            if (this.pebbleCreationStage == GSComplexUI.PEBBLE_CREATION_STAGE_SETTING_FIRST_AXIS) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_SPACE) {
                    this.pebbleCreationStage = GSComplexUI.PEBBLE_CREATION_STAGE_SETTING_SECOND_AXIS;
//                    this.newPebbleAxis1Pt2 =  (Point2D) this.lastMouseDragPoint.clone();
                    this.newPebbleAxis2ThetaRad = this.newPebbleAxis1ThetaRad + Math.PI/2;
                    if (this.newPebbleAxis2ThetaRad > Math.PI/2) {
                        this.newPebbleAxis2ThetaRad -= Math.PI;
                    }
                }
            } else
            if (this.pebbleCreationStage == GSComplexUI.PEBBLE_CREATION_STAGE_SETTING_SECOND_AXIS) {
                // to actually create the new pebble we:
                // 1. set up initial minor, major, and theta values from the new peb data
                // 2. alter those values based on the display transform
                // 3. locate the true center using the existing function inGSCSystem
                // 4. create a 0-centered pebble using the major, minor, and theta values
                // 5. step backwards through the deformations, applying the inversion at each step
                // 6. this gives the shape/data as it exists in the un-deformed state
                // 7. set the center of the back-deformed pebble
                // 8. add that new pebble to the base pebble set then rebuild the pebble set series
                double majorRadius = this.newPebbleAxis1Pt1.distance(this.newPebbleAxis1Pt2) / 2;
                double minorRadius = this.newPebbleAxis2Pt1.distance(this.newPebbleAxis2Pt2) / 2;
                double thetaRad = this.newPebbleAxis1ThetaRad;
                if (minorRadius > majorRadius) {
                    majorRadius = this.newPebbleAxis2Pt1.distance(this.newPebbleAxis2Pt2) / 2;
                    minorRadius = this.newPebbleAxis1Pt1.distance(this.newPebbleAxis1Pt2) / 2;
                    thetaRad = this.newPebbleAxis2ThetaRad;
                }
                majorRadius = majorRadius / this.displayTransform.getScaleX();
                minorRadius = minorRadius / this.displayTransform.getScaleX();
                Point2D centerInGSC = this.inGSCSystem(this.newPebbleCenter);
                double centerX = centerInGSC.getX();
                double centerY = centerInGSC.getY();
                GSPebble newPebble = new GSPebble(0, 0, majorRadius, minorRadius, thetaRad, this.colorOfNewPebbles);
                for (int i=this.gsc.getCurrentDeformationNumber()-1; i > 0; i--) {
                    Deformation d = this.gsc.deformations.get(i-1).inverted();
                    newPebble.deform(d);
                }
                newPebble.setX(centerX);
                newPebble.setY(centerY);
                this.gsc.pebbleSets.get(0).add(newPebble);
                this.gsc.rebuildPebbleSetsFromDeformationSeries();
            } else
            if ((evt.getKeyCode() == java.awt.event.KeyEvent.VK_DELETE) || (evt.getKeyCode() == java.awt.event.KeyEvent.VK_BACK_SPACE)) {
                this.gsc.deleteSelectedPebbles();
                this.repaint();
            }
        }
    }

    /**
     * @param panelClickP
     * @return a new Point2D that is the initial point relative to the origin of the GS system
     */
    public Point2D fromGSCOrigin(Point2D panelClickP) {
        Point2D pointOfGSCOrigin = this.gsc.getCenter().asPoint2D();
        this.displayTransform.transform(pointOfGSCOrigin, pointOfGSCOrigin);
//        System.out.println("fromGSCOrigin: pointOfGSCOrigin point is : "+pointOfGSCOrigin.toString());
            
        return new Point2D.Double((panelClickP.getX()-pointOfGSCOrigin.getX())/this.displayTransform.getScaleX(),
                                  (pointOfGSCOrigin.getY()-panelClickP.getY())/this.displayTransform.getScaleX());
    }
    
    /**
     * @param panelClickP
     * @return a new Point2D that is the initial point in the GS coordinate system (i.e. w/ all relevant deforms in place)
     */
    public Point2D inGSCSystem(Point2D panelClickP) {
        Point2D gscPoint = (Point2D) panelClickP.clone();
        try {
//            System.out.println("---------------");
            Point2D pointOfGSCOrigin = this.gsc.getCenter().asPoint2D();
//            this.displayTransform.transform(pointOfGSCOrigin, pointOfGSCOrigin);
//            System.out.println("inGSCSystem: pointOfGSCOrigin point is : "+pointOfGSCOrigin.toString());
//            System.out.println("inGSCSystem: base gscPoint point is : "+gscPoint.toString());
            this.displayTransform.inverseTransform(gscPoint, gscPoint);
//            System.out.println("inGSCSystem: invert display xfm gscPoint point is : "+gscPoint.toString());
            gscPoint.setLocation(gscPoint.getX()-pointOfGSCOrigin.getX(), pointOfGSCOrigin.getY()-gscPoint.getY());
//            System.out.println("inGSCSystem: rel gsc center gscPoint point is : "+gscPoint.toString());
            Point2D altGscPoint = (Point2D) gscPoint.clone();
//            for (int i=1; i < this.gsc.getCurrentDeformationNumber(); i++) {
            if (! this.isTentativeDeformationCleared()) {
                if (this.tentativeDeformation.isScaling()) {
                    this.tentativeDeformation.asAffineTransform().inverseTransform(gscPoint, gscPoint);
                } else {
                    this.tentativeDeformation.transposed().asAffineTransform().transform(gscPoint, gscPoint);
                }
            }
            for (int i=this.gsc.getCurrentDeformationNumber()-1; i> 0; i--) {
                Deformation d = this.gsc.deformations.get(i-1).clone();
                if (d.isScaling()) {
                    d.asAffineTransform().inverseTransform(gscPoint, gscPoint);
                } else {
                    d.transposed().asAffineTransform().transform(gscPoint, gscPoint);
                }
            }
//            System.out.println("inGSCSystem: composite xfm gscPoint point is : "+gscPoint.toString());
            
            //        Point2D pointOfGSCOrigin = this.gsc.getCenter().asPoint2D();
    //        this.displayTransform.transform(pointOfGSCOrigin, pointOfGSCOrigin);
    //        System.out.println("inGSCSystem: pointOfGSCOrigin point is : "+pointOfGSCOrigin.toString());
    //            
    //        Point2D.Double clickP = new Point2D.Double((panelClickP.getX()-pointOfGSCOrigin.getX())/this.displayTransform.getScaleX(),
    //                                      (pointOfGSCOrigin.getY()-panelClickP.getY())/this.displayTransform.getScaleX());
    //        
    //        System.out.println("inGSCSystem: clickP pre-undeform point is : "+clickP.toString());
    //        try {
    ////            this.gsc.getCompositeTransform().transposed().asAffineTransform().createInverse().transform(clickP, clickP);
    //            this.gsc.getCompositeTransform().asAffineTransform().createInverse().transform(clickP, clickP);
    //        } catch (NoninvertibleTransformException ex) {
    //            Logger.getLogger(GSComplexUI.class.getName()).log(Level.SEVERE, null, ex);
    //        }
    //        System.out.println("inGSCSystem: clickP post-undeform point is : "+clickP.toString());
    //        return clickP;
    //        return clickP;
        } catch (NoninvertibleTransformException ex) {
            Logger.getLogger(GSComplexUI.class.getName()).log(Level.SEVERE, null, ex);
        }
        return gscPoint;
    }
    
    
    public void handleMousePressed(java.awt.event.MouseEvent evt) {
//        System.out.println("handleMousePressed is : "+evt.toString());
        this.lastMouseDownX = evt.getPoint().x;
        this.lastMouseDownY = evt.getPoint().y;
        this.lastMouseDownPoint = (Point2D) evt.getPoint().clone();
        this.lastMouseDownPointRelGSCOrigin = this.fromGSCOrigin(evt.getPoint());
        this.lastMouseDownPointInGSCSystem = this.inGSCSystem(evt.getPoint());
        this.lastMouseDownAngleInGSCSystem = Math.atan(this.lastMouseDownPointRelGSCOrigin.getY()/this.lastMouseDownPointRelGSCOrigin.getX());
        if (this.lastMouseDownPointRelGSCOrigin.getX() < 0) {
            if (this.lastMouseDownPointRelGSCOrigin.getY() > 0) {
                this.lastMouseDownAngleInGSCSystem += Math.PI;
            } else {
                this.lastMouseDownAngleInGSCSystem -= Math.PI;
            }
        }

        this.lastMouseDragX = evt.getPoint().x;
        this.lastMouseDragY = evt.getPoint().y;
        this.lastMouseDragPoint = (Point2D) evt.getPoint().clone();
        this.lastMouseDragAngleInGSCSystem = this.lastMouseDownAngleInGSCSystem;
        this.lastMouseDragPointRelGSCOrigin = (Point2D) this.lastMouseDownPointRelGSCOrigin.clone();
    
        this.altIsDown = evt.isAltDown();
        this.ctrlIsDown = evt.isControlDown();
        this.shiftIsDown = evt.isShiftDown();

        if (this.altIsDown || this.ctrlIsDown || this.shiftIsDown) {
            this.setDeformations();
//            this.tentativeDeformation = new Deformation();
//            this.cumuTentativeDeformation = this.tentativeDeformation.times(this.cumuDeformation);
            this.setStrains();

            this.pebbleCreationStage = GSComplexUI.PEBBLE_CREATION_STAGE_BEGIN;
            this.newPebbleAxis1Pt1 = (Point2D) evt.getPoint().clone();
        }
    }  

    public void handleMouseReleased(java.awt.event.MouseEvent evt) {
//        System.out.println("handleMouseReleased is : "+evt.toString());
        this.altIsDown = evt.isAltDown();
        this.ctrlIsDown = evt.isControlDown();
        this.shiftIsDown = evt.isShiftDown();
        this.isDragDeform = false;
        this.pebbleCreationStage = GSComplexUI.PEBBLE_CREATION_STAGE_BEGIN;
        this.repaint();
    } 
    
    public void tentativeDeformationSetFromRfPhi(double rf, double phiDeg) {
        this.setDeformations(Deformation.createFromRfPhi(rf,phiDeg));

//        this.tentativeDeformation = ;
//        this.cumuTentativeDeformation = this.tentativeDeformation.times(this.cumuDeformation);
//        System.err.println("td on set from rf phi: "+this.tentativeDeformation.toString());
        this.setStrains();
    }
    
    public void tentativeDeformationSetToRotate(double angleRad) {
//        this.tentativeDeformation = new Deformation(Math.cos(angleRad), -1 * Math.sin(angleRad), Math.sin(angleRad), Math.cos(angleRad));
        this.setDeformations(Deformation.createFromAngle(angleRad));
//        this.tentativeDeformation = Deformation.createFromAngle(angleRad);
//        this.cumuTentativeDeformation = this.tentativeDeformation.times(this.cumuDeformation);
        this.setStrains();
    }
    
    public void tentativeDeformationSetToCompression(double compressionFactorX,double compressionFactorY,boolean isInXDirection) {
        if (compressionFactorX < .01) { compressionFactorX = .01; }
        if (compressionFactorY < .01) { compressionFactorY = .01; }
        if (isInXDirection) {
            this.setDeformations(new Deformation(compressionFactorX, 0, 0, 1/compressionFactorX));
//            this.tentativeDeformation = new Deformation(compressionFactorX, 0, 0, 1/compressionFactorX);
        } else {
            this.setDeformations(new Deformation(1/compressionFactorY, 0, 0, compressionFactorY));
//            this.tentativeDeformation = new Deformation(1/compressionFactorY, 0, 0, compressionFactorY);
        }
//        this.cumuTentativeDeformation = this.tentativeDeformation.times(this.cumuDeformation);
        this.setStrains();
    }
    
    public void tentativeDeformationSetToShear(double shearFactorX,double shearFactorY,boolean isInXDirection) {
        if (shearFactorX > 100) { shearFactorX = 100; }
        if (shearFactorX < -100) { shearFactorX = -100; }
        if (shearFactorY > 100) { shearFactorY = 100; }
        if (shearFactorY < -100) { shearFactorY = -100; }
        if (isInXDirection) {
            this.setDeformations(new Deformation(1, 0, shearFactorX*-1, 1));
//            this.tentativeDeformation = new Deformation(1, 0, shearFactorX*-1, 1);;
        } else {
            this.setDeformations(new Deformation(1, shearFactorY*-1, 0, 1));
//            this.tentativeDeformation = new Deformation(1, shearFactorY*-1, 0, 1);
        }
//        this.cumuTentativeDeformation = this.tentativeDeformation.times(this.cumuDeformation);
        this.setStrains();
    }
    
    public void tentativeDeformationClear() {
        this.resetDeformations();
//        this.setDeformations();
////        this.tentativeDeformation = new Deformation();
////        this.cumuTentativeDeformation = this.tentativeDeformation.times(this.cumuDeformation);
//        this.setStrains();
    }
    
    public void resetDeformations() {
        this.setDeformations();
        this.setStrains();
    }
    
    public boolean isTentativeDeformationCleared() {
        return this.tentativeDeformation.isIdentity();
    }
    
    public Deformation getTentativeDeformationCopy() {
        return this.tentativeDeformation.clone();
    }
    
    public GSPebble getTentativeStrainCopy() {
        return this.tentativeStrain.clone();
    }
            
    public void handleMouseDrag(java.awt.event.MouseEvent evt) {
        Point2D evtPtRelativeToGSCOrigin = this.fromGSCOrigin(evt.getPoint());
        Point2D evtPtInGSCSystem = this.inGSCSystem(evt.getPoint());
        double deltaX = evt.getX() - this.lastMouseDownX;
        double deltaY = this.lastMouseDownY - evt.getY();
//        if (this.currentUIMode == GSComplexUI.UI_MODE_DEFORMS) {
        if (this.currentUIMode == GSComplexUI.UI_MODE_DEFORMS) {
            if (evt.isAltDown()) {
                if (this.currentUIMode == GSComplexUI.UI_MODE_DEFORMS) {
                    this.tentativeDeformationSetToRotate(this.lastMouseDragAngleInGSCSystem - this.lastMouseDownAngleInGSCSystem);
                    this.isDragDeform = true;
                }
            }  else if (evt.isControlDown()) {
                if (this.currentUIMode == GSComplexUI.UI_MODE_DEFORMS) {
                    this.tentativeDeformationSetToCompression(evtPtRelativeToGSCOrigin.getX()/this.lastMouseDownPointRelGSCOrigin.getX(),
                                                          evtPtRelativeToGSCOrigin.getY()/this.lastMouseDownPointRelGSCOrigin.getY(),
                                                          Math.abs(deltaX) > Math.abs(deltaY));
                    this.isDragDeform = true;
                }
            } else if (evt.isShiftDown()) {
                if (this.currentUIMode == GSComplexUI.UI_MODE_DEFORMS) {
                    this.tentativeDeformationSetToShear(deltaX/this.lastMouseDragPointRelGSCOrigin.getY(),
                                                    deltaY/this.lastMouseDragPointRelGSCOrigin.getX(), 
                                                    Math.abs(deltaX) > Math.abs(deltaY));
                
                    this.mainWindow.updateDeformAndStrainControlsFromDeformation(this.tentativeDeformation);
                    this.isDragDeform = true;
                }
            } else {
                this.displayTransform.translate((evt.getPoint().x - this.lastMouseDragX) * 1/this.displayTransform.getScaleX(),
                                                (evt.getPoint().y - this.lastMouseDragY) * 1/this.displayTransform.getScaleX());
            }
//            this.cumuTentativeDeformation = this.tentativeDeformation.times(this.cumuDeformation);

            this.mainWindow.updateDeformAndStrainControlsFromDeformation(this.tentativeDeformation);
            this.repaint();
        } else if (this.currentUIMode == GSComplexUI.UI_MODE_EDIT_PEBBLES) {
            if (evt.isShiftDown()) {
                double dragDist = this.lastMouseDownPointInGSCSystem.distance(evtPtInGSCSystem);
                if (this.pebbleCreationStage == GSComplexUI.PEBBLE_CREATION_STAGE_BEGIN) {
                    if (dragDist > GSComplexUI.NEW_PEBBLE_MIN_FIRST_AXIS_DRAG) {
                        this.pebbleCreationStage = GSComplexUI.PEBBLE_CREATION_STAGE_SETTING_FIRST_AXIS;
                    }
                }
                if (this.pebbleCreationStage == GSComplexUI.PEBBLE_CREATION_STAGE_SETTING_FIRST_AXIS) {
                      this.newPebbleAxis1Pt2 = (Point2D) evt.getPoint().clone();
                      this.newPebbleCenter = new Point2D.Double(
                              (this.newPebbleAxis1Pt1.getX()+this.newPebbleAxis1Pt2.getX()) / 2, 
                              (this.newPebbleAxis1Pt1.getY()+this.newPebbleAxis1Pt2.getY()) / 2);
                      this.newPebbleAxis1ThetaRad = Math.PI/2;
                      if (this.newPebbleAxis1Pt1.getX() != this.newPebbleAxis1Pt2.getX()) {
                        this.newPebbleAxis1ThetaRad = Math.atan(
                                (this.newPebbleAxis1Pt2.getY()-this.newPebbleAxis1Pt1.getY()) /
                                (this.newPebbleAxis1Pt1.getX()-this.newPebbleAxis1Pt2.getX())
                                );
                      }
//                      System.err.println("this.newPebbleThetaRad: "+this.newPebbleThetaRad);
//                    if (dragDist > GSPebble.MIN_LONG_AXIS) {
//                        double midX = (this.lastMouseDownPointInGSCSystem.getX()+evtPtInGSCSystem.getX()) / 2;
//                        double midY = (this.lastMouseDownPointInGSCSystem.getY()+evtPtInGSCSystem.getY()) / 2;
//                        double theta = 0;
//                        double deltaXGSC = evtPtInGSCSystem.getX() - this.lastMouseDownPointInGSCSystem.getX();
//                        double deltaYGSC = evtPtInGSCSystem.getY() - this.lastMouseDownPointInGSCSystem.getY();
//                        if (deltaXGSC != 0) {
//                            theta = Math.atan(deltaYGSC/deltaXGSC);
//                        }
//                        
////                        this.newPebble = new GSPebble(midX, midY, dragDist, 1, Util.toDegrees(theta), GSComplexUI.PEBBLE_CREATION_COLOR);
//                        this.newPebble = new GSPebble(dragDist/2, 1);
//                        this.newPebble.setX(midX);
//                        this.newPebble.setY(midY);
//                        this.newPebble.setTheta(theta);
//                    }
                } else if (this.pebbleCreationStage == GSComplexUI.PEBBLE_CREATION_STAGE_SETTING_SECOND_AXIS) {
                    double d = Line2D.ptLineDist(
                                      this.newPebbleAxis1Pt1.getX(), this.newPebbleAxis1Pt1.getY(), 
                                      this.newPebbleAxis1Pt2.getX(), this.newPebbleAxis1Pt2.getY(), 
                                      evt.getPoint().getX(), evt.getPoint().getY());
                    double axis2DeltaX = d * Math.cos(this.newPebbleAxis2ThetaRad);
                    double axis2DeltaY = d * Math.sin(this.newPebbleAxis2ThetaRad)*-1;
                    this.newPebbleAxis2Pt1 = new Point2D.Double(this.newPebbleCenter.getX()+axis2DeltaX,this.newPebbleCenter.getY()+axis2DeltaY);
                    this.newPebbleAxis2Pt2 = new Point2D.Double(this.newPebbleCenter.getX()-axis2DeltaX,this.newPebbleCenter.getY()-axis2DeltaY);
                    
                    AffineTransform pebAff = new AffineTransform();
                    pebAff.translate(this.newPebbleCenter.getX(), this.newPebbleCenter.getY());
                    pebAff.rotate(this.newPebbleAxis1ThetaRad*-1);
                    pebAff.scale(this.newPebbleAxis1Pt1.distance(this.newPebbleAxis1Pt2)/2, d);
//                    System.err.println("pebAdd: "+pebAff);
                    this.newPebShape = pebAff.createTransformedShape(new Ellipse2D.Double(-1,-1,2,2));
                }
            } else {
                this.displayTransform.translate((evt.getPoint().x - this.lastMouseDragX) * 1/this.displayTransform.getScaleX(),
                                                (evt.getPoint().y - this.lastMouseDragY) * 1/this.displayTransform.getScaleX());
            }
            this.repaint();
        }  else if (this.currentUIMode == GSComplexUI.UI_MODE_STRAIN_NAV) {
            this.displayTransform.translate((evt.getPoint().x - this.lastMouseDragX) * 1/this.displayTransform.getScaleX(),
                                            (evt.getPoint().y - this.lastMouseDragY) * 1/this.displayTransform.getScaleX());
            this.repaint();
        }
        this.lastMouseDragX = evt.getPoint().x;
        this.lastMouseDragY = evt.getPoint().y;
        this.lastMouseDragPoint = (Point2D) evt.getPoint().clone();
        this.lastMouseDragPointRelGSCOrigin = (Point2D) evtPtRelativeToGSCOrigin.clone();
        this.lastMouseDragAngleInGSCSystem = Math.atan(evtPtRelativeToGSCOrigin.getY()/evtPtRelativeToGSCOrigin.getX());
        if (evtPtRelativeToGSCOrigin.getX() < 0) {
            if (evtPtRelativeToGSCOrigin.getY() > 0) {
                this.lastMouseDragAngleInGSCSystem += Math.PI;
            } else {
                this.lastMouseDragAngleInGSCSystem -= Math.PI;
            }
        }
    }
    
    public void handleMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
        double zoom_factor = 1- ((evt.getPreciseWheelRotation() * evt.getScrollAmount()) * GSComplexUI.zoomPerScrollFactor);

        this.setDisplayZoom(zoom_factor, false,evt.getPoint());
        
        this.mainWindow.updateZoomSlider(this.displayTransform.getScaleX());
    }

    public void setDisplayZoom(double amt, boolean isExact, Point2D toPoint) {
        Point2D toPointInGCSprescale = this.fromGSCOrigin(toPoint);

        double initAmt = amt;
        if (! isExact) {
            amt = this.displayTransform.getScaleX() * amt;
        }
        if (amt < GSComplexUI.ZOOM_MIN) { amt = GSComplexUI.ZOOM_MIN; }
        if (amt > GSComplexUI.ZOOM_MAX) { amt = GSComplexUI.ZOOM_MAX; }
        double zoomFactor = amt/this.displayTransform.getScaleX();
        this.displayTransform.scale(zoomFactor,zoomFactor);

        Point2D toPointInGCSpostscale = this.fromGSCOrigin(toPoint);
        this.shiftPan((toPointInGCSprescale.getX()-toPointInGCSpostscale.getX())*this.displayTransform.getScaleX(),
                      (toPointInGCSpostscale.getY()-toPointInGCSprescale.getY())*this.displayTransform.getScaleX());

        this.repaint();
    }
    
    /*------------------------------------------------------------------------*/

    public void setCenter(double x, double y) {
        this.gsc.setCenter(x,y);
        this.repaint();
    }
    public void centerDisplay() {
        this.setPan(this.getWidth()/2 - this.gsc.getCenter().x*this.displayTransform.getScaleX(),
                    this.getHeight()/2 - this.gsc.getCenter().y*this.displayTransform.getScaleX());    
    }   
    public void setPan(double x, double y) {
        // REFACTOR? a way to just update the translate vals of the display transform
        //System.err.println("setting pan to: "+x+","+y);
        double[] matrixVals = new double[6];
        this.displayTransform.getMatrix(matrixVals);
        matrixVals[4] = x;
        matrixVals[5] = y;
        this.displayTransform = new AffineTransform(matrixVals);
        this.repaint();
    }

    public void shiftPan(double deltaX, double deltaY) {
        // REFACTOR? a way to just update the translate vals of the display transform
        //System.err.println("setting pan to: "+x+","+y);
        double[] matrixVals = new double[6];
        this.displayTransform.getMatrix(matrixVals);
        matrixVals[4] -= deltaX;
        matrixVals[5] -= deltaY;
        this.displayTransform = new AffineTransform(matrixVals);
        this.repaint();
    }    
    //    public void setZoom(double scale) {
//        // REFACTOR? a way to just update the translate vals of the display transform
//        double[] matrixVals = new double[6];
//        this.displayTransform.getMatrix(matrixVals);
//        matrixVals[0] = scale;
//        matrixVals[3] = scale;
//        this.displayTransform = new AffineTransform(matrixVals);
//        this.repaint();
//    }
    public void resetDisplayTransform() {
        this.displayTransform = new AffineTransform();
        this.repaint ();
    }
    
    @Override
    public void paintComponent (Graphics g)
    {
        super.paintComponent (g);
        Graphics2D g2d = (Graphics2D) g;
        
        g2d.setColor (Color.WHITE);
        g2d.fillRect (0,0, this.getWidth (), this.getHeight ());

        
        g2d.transform(this.displayTransform);
        g2d.setColor (Color.BLACK);

        // position and deform the bg axis and image appropriately
        Matrix2x2 bgDeform = this.gsc.getCompositeTransform().clone();
        if (! this.tentativeDeformation.isIdentity()) {
            bgDeform.timesInPlace(this.tentativeDeformation);
        }
        AffineTransform bgTransform = bgDeform.transposed().asAffineTransform();
        
        Point2D p = new Point2D.Double();
        bgTransform.transform(this.gsc.getCenter().asPoint2D(), p);        
        int offsetX = (int) (this.gsc.getCenter().x-p.getX());
        int offsetY = (int) (this.gsc.getCenter().y-p.getY());
        g2d.translate(offsetX, offsetY);
        g2d.transform(bgTransform);
        
        if (this.flagDisplayBackgroundImage) {
//            g2d.drawString("show background image is TRUE", 0,0);
            if (this.gsc.getBgImage() != null)
            {
                int imgPosX = (int) this.gsc.getCenter().x;       
                int imgPosY = (int) this.gsc.getCenter().y;
                imgPosX -= this.gsc.getBgImage().getWidth()/2;
                imgPosY -= this.gsc.getBgImage().getHeight()/2;
                g2d.drawImage (this.gsc.getBgImage(),null,imgPosX,imgPosY);
            }
        }
         
        if (this.flagDisplayBackgroundAxis) {
            //        g2d.drawLine(0,(int)this.gsc.getCenter().y,this.getWidth(),(int)this.gsc.getCenter().y); // horizontal axis
            //        g2d.drawLine((int)this.gsc.getCenter().x,0,(int)this.gsc.getCenter().x,this.getHeight()); // vertical axis
            g2d.drawLine(-10000,(int)this.gsc.getCenter().y,10000,(int)this.gsc.getCenter().y); // horizontal axis
            g2d.drawLine((int)this.gsc.getCenter().x,-10000,(int)this.gsc.getCenter().x,10000); // vertical axis
//            System.err.println("drew axis at "+this.gsc.getCenter().x+","+this.gsc.getCenter().y);

//            g2d.drawLine(-10000,0,10000,0); // horizontal axis
//            g2d.drawLine(0,-10000,0,10000); // vertical axis
            // TODO: figure out how to get 1px wide axes (e.g. apply translation and scaling transforms separately, manually calc the additional scaling offset needed for the axes?
            // TODO: figure out how to draw the axes to the edge of the widow regarless of other factors (quick and dirty would be to set limits to extreme values - e.g. +/- 32000
        }

        // undo the BG stuff
        try {
            g2d.transform(bgTransform.createInverse());
            g2d.translate(offsetX*-1, offsetY*-1);
        } catch (NoninvertibleTransformException ex) {
            Logger.getLogger(GSComplexUI.class.getName()).log(Level.SEVERE, null, ex);
        }

        // shift/center for drawing all the pebbles and the strain ellipses
        g2d.translate(this.gsc.getCenter().x, this.gsc.getCenter().y);
        
        this.gsc.drawOnto(g2d, this.flagDisplayPebbleFill, this.flagDisplayPebbleAxes, this.tentativeDeformation);

        if (this.flagDisplayStrainEllipses) {
            if (! this.cumuDeformation.isIdentity()) {
                g2d.setStroke(GSComplexUI.INFO_STROKE_CUMU);
                this.cumuDeformation.drawOnto(g2d,GSComplexUI.INFO_COLOR_CUMU);
                if (! this.tentativeDeformation.isIdentity()) {
                    Deformation ct = this.cumuDeformation.clone();
                    ct.timesInPlace(this.tentativeDeformation);
                    g2d.setStroke(GSComplexUI.INFO_STROKE_CUMUTENT);
                    ct.drawOnto(g2d,GSComplexUI.INFO_COLOR_CUMUTENT);
                }   
            }

            if (((this.currentUIMode == GSComplexUI.UI_MODE_STRAIN_NAV) || this.tentativeDeformation.isIdentity()) && this.gsc.deformations.size() > 0) {
                g2d.setStroke(GSComplexUI.INFO_STROKE_NAV_DEF);
                this.gsc.deformations.get(this.gsc.getCurrentDeformationNumber()-2).drawOnto(g2d, GSComplexUI.INFO_COLOR_NAV_DEF);
            }

            if (this.currentUIMode == GSComplexUI.UI_MODE_DEFORMS && ! this.tentativeDeformation.isIdentity()) {
                g2d.setStroke(GSComplexUI.INFO_STROKE_TENT);
                this.tentativeDeformation.drawOnto(g2d,GSComplexUI.INFO_COLOR_TENT);

                if (this.isDragDeform) {
                    g2d.translate(this.gsc.getCenter().x*-1, this.gsc.getCenter().y*-1);
                    g2d.setColor (Color.BLUE);
                    if (this.altIsDown) {
                        double rad = this.lastMouseDownPointRelGSCOrigin.distance(0,0);
                        double angleDiff = this.lastMouseDragAngleInGSCSystem - this.lastMouseDownAngleInGSCSystem;
                        double angleDiffDeg = (angleDiff)*180/Math.PI;
                        if (angleDiffDeg > 180) {
                            angleDiffDeg -= 360;
                        } else if (angleDiffDeg < -180) {
                            angleDiffDeg += 360;
                        }
                        g2d.drawArc((int)(this.gsc.getCenter().x - rad), (int)(this.gsc.getCenter().y - rad), (int) (2*rad), (int) (2*rad), 
                                    (int) (this.lastMouseDownAngleInGSCSystem*180/Math.PI), (int)angleDiffDeg);
                    }  else if (this.ctrlIsDown || this.shiftIsDown) {
                        Point2D invertedLastDown = (Point2D) this.lastMouseDownPoint.clone();
                        Point2D invertedLastDrag = (Point2D) this.lastMouseDragPoint.clone();
                        try {
                            this.displayTransform.inverseTransform(invertedLastDown, invertedLastDown);
                            this.displayTransform.inverseTransform(invertedLastDrag, invertedLastDrag);
                        } catch (NoninvertibleTransformException noninvertibleTransformException) {
                        }
                        if (Math.abs(this.lastMouseDragX-this.lastMouseDownX) >= Math.abs(this.lastMouseDragY-this.lastMouseDownY)) {
                            g2d.drawLine((int) invertedLastDown.getX(), (int) invertedLastDown.getY(), (int) invertedLastDrag.getX(), (int) invertedLastDown.getY());
                        } else {
                            g2d.drawLine((int) invertedLastDown.getX(), (int) invertedLastDown.getY(), (int) invertedLastDown.getX(), (int) invertedLastDrag.getY());
                        }
                    }
                }
            }
        }    // end if display strain ellipses

        if (this.pebbleCreationStage != GSComplexUI.PEBBLE_CREATION_STAGE_BEGIN) {
            g2d.setTransform(new AffineTransform());
            g2d.setStroke(GSComplexUI.PEBBLE_CREATION_STROKE);
            g2d.setColor(this.colorOfNewPebbles);
            g2d.drawLine((int) this.newPebbleAxis1Pt1.getX(), (int) this.newPebbleAxis1Pt1.getY(),
                         (int) this.newPebbleAxis1Pt2.getX(), (int) this.newPebbleAxis1Pt2.getY()); // first axis

            g2d.drawOval((int) this.newPebbleCenter.getX()-2, (int) this.newPebbleCenter.getY()-2,4,4);
//            this.newPebbleAxis1Pt1.getX();
//            this.newPebbleAxis1Pt1.getY();
//            this.newPebbleAxis1Pt2.getX();
//            this.newPebbleAxis1Pt2.getY();
            
            if (this.pebbleCreationStage == GSComplexUI.PEBBLE_CREATION_STAGE_SETTING_SECOND_AXIS) {
//                g2d.drawLine(x1, y1, x2, y2); // second axis
                g2d.drawLine((int) this.newPebbleAxis2Pt1.getX(), (int) this.newPebbleAxis2Pt1.getY(),
                             (int) this.newPebbleAxis2Pt2.getX(), (int) this.newPebbleAxis2Pt2.getY());
        
                //                create ellipse and draw it
                g2d.draw(this.newPebShape);
            }
        }
        
//        if (this.newPebble != null) {
//            GSPebble defNewPebble = this.newPebble.clone();
//            this.gsc.deformations.runAllDeformationsOn(defNewPebble,this.gsc.getCurrentDeformationNumber());
//            g2d.setStroke(GSComplexUI.PEBBLE_CREATION_STROKE);
//            defNewPebble.drawOnto(g2d, false, true, true);
//        }
    }

    void handleMouseClicked(MouseEvent evt) {
//        System.out.println("handleMouseClicked is : "+evt.toString());
        if (evt.getButton() == 3) { // right-click to center on the selected point
//            System.out.println("right-click");
            Point2D displayCenterInGCS = this.fromGSCOrigin(new Point2D.Double(this.getWidth()/2,this.getHeight()/2));
            Point2D clickPtInGCS = this.fromGSCOrigin(evt.getPoint());
//            System.out.println("clickPtInGCS: "+clickPtInGCS.toString());
            double deltaX = clickPtInGCS.getX()-displayCenterInGCS.getX();
            double deltaY = displayCenterInGCS.getY()-clickPtInGCS.getY();
            deltaX = deltaX * this.displayTransform.getScaleX();
            deltaY = deltaY * this.displayTransform.getScaleX();
            this.shiftPan(deltaX, deltaY);
        } else if (evt.getButton() == 1) {
            if (this.currentUIMode==GSComplexUI.UI_MODE_EDIT_PEBBLES) {
                Point2D clickPtInGCS = this.inGSCSystem(evt.getPoint());
//                System.out.println("click Pt In GC System: "+clickPtInGCS.toString());
                this.gsc.pebbleSets.selectPebblesByUndeformedPoint(clickPtInGCS, evt.isShiftDown());
            }            
            
        }
    }

    void handleApplyTentativeTransform() {
        this.gsc.applyDeformation(this.tentativeDeformation.clone());
        this.tentativeDeformationClear();
//        this.gsc.deformations.add(this.tentativeDeformation.clone());
//        this.tentativeDeformation = new Deformation();
//        this.tentativeDeformation = new Deformation();
    }

    public void setModeDeforms() {
        this.currentUIMode = GSComplexUI.UI_MODE_DEFORMS;
    }
    public void setModeStrainNav() {
        this.currentUIMode = GSComplexUI.UI_MODE_STRAIN_NAV;
    }
    public void setModeEditPebbles() {
        this.currentUIMode = GSComplexUI.UI_MODE_EDIT_PEBBLES;
    }    

    /**
     * @return the flagDisplayPebbleAxes
     */
    public boolean isFlagDisplayPebbleAxes() {
        return flagDisplayPebbleAxes;
    }

    /**
     * @param flagDisplayPebbleAxes the flagDisplayPebbleAxes to set
     */
    public void setFlagDisplayPebbleAxes(boolean flagDisplayPebbleAxes) {
        this.flagDisplayPebbleAxes = flagDisplayPebbleAxes;
    }

    /**
     * @return the flagDisplayPebbleFill
     */
    public boolean isFlagDisplayPebbleFill() {
        return flagDisplayPebbleFill;
    }

    /**
     * @param flagDisplayPebbleFill the flagDisplayPebbleFill to set
     */
    public void setFlagDisplayPebbleFill(boolean flagDisplayPebbleFill) {
        this.flagDisplayPebbleFill = flagDisplayPebbleFill;
    }

    /**
     * @return the flagDisplayBackgroundAxis
     */
    public boolean isFlagDisplayBackgroundAxis() {
        return flagDisplayBackgroundAxis;
    }

    /**
     * @param flagDisplayBackgroundAxis the flagDisplayBackgroundAxis to set
     */
    public void setFlagDisplayBackgroundAxis(boolean flagDisplayBackgroundAxis) {
        this.flagDisplayBackgroundAxis = flagDisplayBackgroundAxis;
    }

    /**
     * @return the flagDisplayBackgroundImage
     */
    public boolean isFlagDisplayBackgroundImage() {
        return flagDisplayBackgroundImage;
    }

    /**
     * @param flagDisplayBackgroundImage the flagDisplayBackgroundImage to set
     */
    public void setFlagDisplayBackgroundImage(boolean flagDisplayBackgroundImage) {
        this.flagDisplayBackgroundImage = flagDisplayBackgroundImage;
    }

    /**
     * @return the flagDisplayStrainEllipses
     */
    public boolean isFlagDisplayStrainEllipses() {
        return flagDisplayStrainEllipses;
    }

    /**
     * @param flagDisplayStrainEllipses the flagDisplayStrainEllipses to set
     */
    public void setFlagDisplayStrainEllipses(boolean flagDisplayStrainEllipses) {
        this.flagDisplayStrainEllipses = flagDisplayStrainEllipses;
    }

    /**
     * @return the currentUIMode
     */
    public int getCurrentUIMode() {
        return currentUIMode;
    }

    /**
     * @param currentUIMode the currentUIMode to set
     */
    public void setCurrentUIMode(int currentUIMode) {
        this.currentUIMode = currentUIMode;
    }

    void toggleEditUIMode(boolean inEditMode) {
        if (inEditMode) {
            this.cachedUIMode = this.currentUIMode;
            this.currentUIMode = GSComplexUI.UI_MODE_EDIT_PEBBLES;
        } else {
            this.currentUIMode = this.cachedUIMode;
        }
    }

    /**
     * @return the colorOfNewPebbles
     */
    public Color getColorOfNewPebbles() {
        return this.colorOfNewPebbles;
    }

    /**
     * @param colorOfNewPebbles the colorOfNewPebbles to set
     */
    public void setColorOfNewPebbles(Color colorOfNewPebbles) {
        this.colorOfNewPebbles = colorOfNewPebbles;
    }

    void handleChooseBackgroundImage(ActionEvent evt) {
        fileChooser.setFileFilter (this.filterImage);
        int returnVal = fileChooser.showOpenDialog (this);
        if (returnVal == JFileChooser.APPROVE_OPTION)
        {
            // load the cross section from the file
            this.gsc.setBgImageFileName(fileChooser.getSelectedFile ().getAbsolutePath ());
            this.gsc.loadBgImage();
           this.setFlagDisplayBackgroundImage(true);
        }
    }

    void handleApplyColor(Color newColor) {
        this.gsc.pebbleSets.colorSelectedPebbles(newColor);
    }
}
