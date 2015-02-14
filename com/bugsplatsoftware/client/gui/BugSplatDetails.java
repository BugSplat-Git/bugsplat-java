//
// BugSplat integration code for Java applications.
// This class implements a dialog to get information
// from the user to add to the BugSplat report.
//
// Copyright 2005 BugSplat, LLC.
//
package com.bugsplatsoftware.client.gui;

import java.awt.TextArea;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.BorderFactory;

public class BugSplatDetails extends JDialog implements ActionListener
{
	private static final String image1 = "images/BugSplatHeader440x49.gif";

	boolean Result = false;

	String ServerError = "None.";
	String ServerURL = "http://www.bugsplatsoftware.com/post/post_form.php";

	TextArea input1;

	JButton ok;

	String message1 = "An error report has been generated for you, and currently resides on \n" +
	                  "your computer. \n" +
	                  " \n" +
	                  "This report contains debugging information that we can inspect to give us \n" +
	                  "details about the application at the exact time that the crash occurred. \n" +
	                  "In addition, it contains the information you entered in to the crash \n" +
	                  "report dialog.";

	String message2 = "The following error was returned when we attempted to upload the \n" +
	                  "the report to our server:";


	String message3 = "If you would still like to report the problem, you can upload the file to the \n" +
	                  "following location:";

	public BugSplatDetails(String error)
	{
		super(new JFrame(), "Error Report Details", true);

		ServerError = error;

		// add first Panel
		JPanel p1 = new JPanel();
		p1.setLayout(new BorderLayout(8,8));
		p1.add(BorderLayout.NORTH, new BugSplatLabel(message1, BugSplatLabel.LEFT, false));
		p1.add(BorderLayout.CENTER, new BugSplatLabel(message2, BugSplatLabel.LEFT, false));
		input1 = new TextArea(ServerError, 5, 50, TextArea.SCROLLBARS_VERTICAL_ONLY);
		p1.add(BorderLayout.SOUTH, input1);

		// name Panel
		JPanel p2 = new JPanel();
		p2.setLayout(new BorderLayout(8,8));
		p2.add(BorderLayout.NORTH, new BugSplatLabel(message3, BugSplatLabel.LEFT, false));
		p2.add(BorderLayout.CENTER, new BugSplatLabel(ServerURL, BugSplatLabel.LEFT, false));

		// add OK BUtton
		JPanel p3 = new JPanel();
		p3.setLayout(new FlowLayout());
		ok = new JButton("OK");
		p3.add(ok);
		ok.addActionListener(this);

		// another Panel
		JPanel p4 = new JPanel();
		p4.setLayout(new BorderLayout(8,8));
		p4.add(BorderLayout.NORTH, p1);
		p4.add(BorderLayout.CENTER, p2);
		p4.add(BorderLayout.SOUTH, p3);

		// create a border
		JPanel p5 = new JPanel();
		p5.setLayout(new BorderLayout(8,8));
        p5.setBorder(BorderFactory.createEmptyBorder(0,20,10,20));
		p5.add(BorderLayout.CENTER, p4);

		// create a canvas for the image
	    BugSplatImageCanvas canvas = new BugSplatImageCanvas(image1);

		getContentPane().setLayout(new BorderLayout(8,8));
		getContentPane().add(BorderLayout.NORTH, canvas);
		getContentPane().add(BorderLayout.SOUTH, p5);

		// prevent resize
		setResizable(false);

        // size the window
        pack();

		// set the location
		Dimension sdim = getToolkit().getScreenSize();
		Dimension fdim = getSize();
		setLocation(sdim.width/2-fdim.width/2, sdim.height/2-fdim.height/2);

        // display the window.
        setVisible(true);
	}

	public void actionPerformed(ActionEvent ae)
	{
		if(ae.getSource() == ok)
		{
			Result = true;
	    }

        // hide the window.
		setVisible(false);
	}
}
