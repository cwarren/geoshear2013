/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * HelpWindow.java
 *
 * Created on Aug 24, 2010, 11:40:26 AM
 */

package edu.williams.geoshear2013;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

/**
 *
 * @author cwarren
 */
public class HelpWindow extends javax.swing.JFrame {

    /** Creates new form HelpWindow */
    public HelpWindow() {
        initComponents();
        this.jTextAreaHelpText.setMargin(new Insets(10,10,10,10));

        // all this is used to bind the escape key to hide this window
        JPanel theContentPanel = (JPanel) this.getContentPane();
        theContentPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "hideit");
        theContentPanel.getActionMap().put("hideit", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
               setVisible(false);
            }
        });        
        
        // load the help text from the file
        BufferedInputStream bis = new BufferedInputStream(getClass().getResourceAsStream("/edu/williams/geoshear2013/help.txt"));
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        try {
            int result = bis.read();
            while(result != -1) {
                byte b = (byte)result;
                buf.write(b);
                result = bis.read();
            }
        } catch (IOException exc) {
            System.out.println("Could not read from help.txt: "+ exc.toString());
        }
        this.jTextAreaHelpText.setText(buf.toString());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabelTitleHelp = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPaneHelpText = new javax.swing.JScrollPane();
        jTextAreaHelpText = new javax.swing.JTextArea();
        jLabel2 = new javax.swing.JLabel();
        jButtonClose = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();

        setMinimumSize(new java.awt.Dimension(400, 300));
        addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                formKeyPressed(evt);
            }
        });
        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.Y_AXIS));

        jLabelTitleHelp.setFont(new java.awt.Font("Tahoma", 1, 36)); // NOI18N
        jLabelTitleHelp.setText("GeoShear Help");
        jLabelTitleHelp.setAlignmentX(0.5F);
        getContentPane().add(jLabelTitleHelp);

        jLabel1.setText(" ");
        getContentPane().add(jLabel1);

        jTextAreaHelpText.setColumns(20);
        jTextAreaHelpText.setEditable(false);
        jTextAreaHelpText.setFont(new java.awt.Font("Lucida Sans", 0, 13)); // NOI18N
        jTextAreaHelpText.setLineWrap(true);
        jTextAreaHelpText.setRows(5);
        jTextAreaHelpText.setWrapStyleWord(true);
        jTextAreaHelpText.setAlignmentX(0.0F);
        jScrollPaneHelpText.setViewportView(jTextAreaHelpText);

        getContentPane().add(jScrollPaneHelpText);

        jLabel2.setText(" ");
        getContentPane().add(jLabel2);

        jButtonClose.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        jButtonClose.setText("Close");
        jButtonClose.setToolTipText("Close the help window");
        jButtonClose.setAlignmentX(1.0F);
        jButtonClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCloseActionPerformed(evt);
            }
        });
        getContentPane().add(jButtonClose);

        jLabel3.setText(" ");
        getContentPane().add(jLabel3);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCloseActionPerformed
        this.setVisible(false);
    }//GEN-LAST:event_jButtonCloseActionPerformed

    private void formKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyPressed
        System.out.println("help window key pressed: "+evt.toString());
    }//GEN-LAST:event_formKeyPressed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new HelpWindow().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonClose;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabelTitleHelp;
    private javax.swing.JScrollPane jScrollPaneHelpText;
    private javax.swing.JTextArea jTextAreaHelpText;
    // End of variables declaration//GEN-END:variables

}

