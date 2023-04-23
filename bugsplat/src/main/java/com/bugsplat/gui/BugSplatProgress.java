//
// BugSplat integration code for Java applications.
// This class implements the progress dialog.
//
// Copyright 2005 BugSplat, LLC.
//
package com.bugsplat.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JDialog;
import javax.swing.JProgressBar;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.BorderFactory;

public class BugSplatProgress extends JDialog implements ActionListener {

    private static final String image1 = "resources/BugSplatHeader350x49.gif";
    private static final String image2 = "resources/BugSplatUpload64x64.gif";

    public static final int taskCompilingReport = 1;
    public static final int taskSendingReport = 2;

    boolean Result = true;
    public boolean Cancelled = false;
    public boolean QuietMode = false;

    public int TaskComplete = 0;
    public String ResultString = "";

    String message1a = "Generating error report . . . ";
    String message1b = message1a + "done";
    String message3a = "Posting data . . . ";
    String message3b = message3a + "done";
    String message4 = "Thank you for sending this error report. \n It has been received successfully.";

    private BugSplatLabel label1; // message1
    private BugSplatLabel label3; // message3
    private BugSplatLabel label4; // message4

    private JProgressBar progressBar;
    private JButton button;

    public BugSplatProgress(boolean quietMode) {
        super(new JFrame(), "Sending error report", false);

        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true); // get space for the string
        progressBar.setString("");          // but don't paint it
        progressBar.setIndeterminate(true);

        // messages
        label1 = new BugSplatLabel(message1b, BugSplatLabel.LEFT, false);
        label3 = new BugSplatLabel(message3b, BugSplatLabel.LEFT, false);
        label4 = new BugSplatLabel(message4, BugSplatLabel.LEFT, false);

        // create a canvas for the image
        BugSplatImageCanvas canvas1 = new BugSplatImageCanvas(image2);

        // button
        button = new JButton("Cancel");
        button.addActionListener(this);

        JPanel p1 = new JPanel();
        p1.setLayout(new BorderLayout(8, 8));
        p1.add(BorderLayout.CENTER, canvas1);

        JPanel p2 = new JPanel();
        p2.setLayout(new GridLayout(3, 1));
        p2.add(label1);
        p2.add(label3);

        JPanel p3 = new JPanel();
        p3.setLayout(new BorderLayout(8, 8));
        p3.add(BorderLayout.EAST, p1);
        p3.add(BorderLayout.WEST, p2);

        JPanel p4 = new JPanel();
        p4.setLayout(new BorderLayout(8, 8));
        p4.add(BorderLayout.EAST, button);
        p4.add(BorderLayout.WEST, progressBar);

        JPanel p5 = new JPanel();
        p5.setLayout(new BorderLayout(8, 8));
        p5.add(BorderLayout.NORTH, label4);
        p5.add(BorderLayout.SOUTH, p4);

        // set the panels on the layout
        JPanel p6 = new JPanel();
        p6.setLayout(new BorderLayout(8, 8));
        p6.add(BorderLayout.NORTH, p3);
        p6.add(BorderLayout.SOUTH, p5);

        // create a border
        JPanel p7 = new JPanel();
        p7.setLayout(new BorderLayout(8, 8));
        p7.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 20));
        p7.add(BorderLayout.CENTER, p6);

        // create a canvas for the image
        BugSplatImageCanvas canvas = new BugSplatImageCanvas(image1);

        getContentPane().setLayout(new BorderLayout(8, 8));
        getContentPane().add(BorderLayout.NORTH, canvas);
        getContentPane().add(BorderLayout.SOUTH, p7);

        // prevent resize
        setResizable(false);

        // size the window
        pack();

        // set the location
        Dimension sdim = getToolkit().getScreenSize();
        Dimension fdim = getSize();
        setLocation(sdim.width / 2 - fdim.width / 2, sdim.height / 2 - fdim.height / 2);

		// set the preferred size, now that the dialog has been calculated
        // this prevents the layout from changing when the label visibility is changed
        p1.setPreferredSize(p1.getSize());
        p2.setPreferredSize(p2.getSize());
        p3.setPreferredSize(p3.getSize());
        p4.setPreferredSize(p4.getSize());
        p5.setPreferredSize(p5.getSize());
        p6.setPreferredSize(p6.getSize());
        p7.setPreferredSize(p7.getSize());
        progressBar.setPreferredSize(progressBar.getSize());
        button.setPreferredSize(button.getSize());

        // hide
        label1.setText(message1a);
        label1.setVisible(true);
        label3.setVisible(false);
        label4.setVisible(false);

        // quite mode - do nothing
        QuietMode = quietMode;
        if (QuietMode == true) {
            return;
        }

        // display the window
        setVisible(true);
    }

    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == button) {
            if (button.getText() == "Close") {
                Result = true;
                Cancelled = false;
                ResultString = "Report sent successfully.";
            } else {
                Result = false;
                Cancelled = true;
                ResultString = "User cancelled.";
            }
        }

        // hide the window.
        setVisible(false);
    }

    public void setTaskComplete(int task) {
        TaskComplete = task;

        if (task == taskCompilingReport) {
            label1.setText(message1b);
            label1.setVisible(true);
        }
        if (task == taskSendingReport) {
            label3.setText(message3b);
            label3.setVisible(true);
            label4.setVisible(true);

            progressBar.setValue(100);
            progressBar.setIndeterminate(false);
            progressBar.setEnabled(false);
            button.setText("Close");
        }

        // quite mode - do nothing
        if (QuietMode == true) {
            return;
        }

        // display the window
        setVisible(true);
    }
}
