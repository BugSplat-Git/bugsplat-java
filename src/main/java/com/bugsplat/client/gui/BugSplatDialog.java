//
// BugSplat integration code for Java applications.
// This class implements a dialog to get information
// from the user to add to the BugSplat report.
//
// Copyright 2005 BugSplat, LLC.
//
package com.bugsplat.client.gui;

import java.awt.TextArea;
import java.awt.TextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.BorderFactory;

import com.bugsplat.client.gui.BugSplatViewDetails;

public class BugSplatDialog extends JDialog implements ActionListener
{
	private static final String image1 = "images/BugSplatHeader440x49.gif";

	public boolean Result = true;
	String ResultString = "";
	boolean QuietMode = false;

	public String UserDescription = "";
	public String UserName = "";
	public String UserEmail = "";
	public String UserAddress = "";

	ArrayList m_requiredFiles;
	ArrayList m_additionalFiles;
	public boolean m_enableAdditionalFiles = true;

	TextArea input1;
    TextField input2,
 	          input3,
	          input4;
	JButton ok, cancel, details;

	String message1 = "A problem has been encountered and the program needs to close.\n \n" +
	                  "Reporting this error helps us to make our product better. Every error \n" +
	                  "report is processed by automated debugging tools that help us find and \n" +
	                  "fix problems our users encounter.\n \n" +
	                  "Please send this error report to us using the \"Send Error Report\" button \n" +
	                  "below. All information will be treated confidentially and used only to \n" +
	                  "improve future versions of this program.\n \n" +
	                  "Please describe the events just before this dialog appeared:";

	String message2 = "The contact information below is optional. If provided, we may contact \n" +
	                  "you with additional information about the error.\n \n" +
	                  "By providing a valid email address, you may also be automatically notified \n" +
	                  "of available fixes for your crash.\n \n" +
	                  "Your email address will never be used for marketing or any other purposes.\n";


	public BugSplatDialog(ArrayList requiredFiles, ArrayList additionalFiles, boolean enableAdditionalFiles, boolean quietMode)
	{
		super(new JFrame(), "Error Report", true);

		// save the file lists
		m_requiredFiles = requiredFiles;
		m_additionalFiles = additionalFiles;
		m_enableAdditionalFiles = enableAdditionalFiles;

		// add first Panel
		JPanel p1 = new JPanel();
		p1.setLayout(new BorderLayout(8,8));
		p1.add(BorderLayout.NORTH, new BugSplatLabel(message1, BugSplatLabel.LEFT, false));
		input1 = new TextArea("", 5, 50, TextArea.SCROLLBARS_VERTICAL_ONLY);
		p1.add(BorderLayout.CENTER, input1);
		p1.add(BorderLayout.SOUTH, new BugSplatLabel(message2, BugSplatLabel.LEFT, false));

		// name Panel
		JPanel p2 = new JPanel();
		p2.setLayout(new BorderLayout(8,8));
		input2 = new TextField("", 24);
		p2.add(BorderLayout.NORTH, new BugSplatLabel("Name: (optional)", BugSplatLabel.LEFT, false));
		p2.add(BorderLayout.CENTER, input2);

		// email Panel
		JPanel p3 = new JPanel();
		p3.setLayout(new BorderLayout(8,8));
		input3 = new TextField("", 24);
		p3.add(BorderLayout.NORTH, new BugSplatLabel("Email Address: (optional)", BugSplatLabel.LEFT, false));
		p3.add(BorderLayout.CENTER, input3);

		// address Panel
		JPanel p4 = new JPanel();
		p4.setLayout(new BorderLayout(8,8));
		input4 = new TextField("", 50);
		p4.add(BorderLayout.NORTH, new BugSplatLabel("Postal Address: (optional)", BugSplatLabel.LEFT, false));
		p4.add(BorderLayout.CENTER, input4);

		// another Panel
		JPanel p5 = new JPanel();
		p5.setLayout(new BorderLayout(8,8));
		p5.add(BorderLayout.WEST, p2);
		p5.add(BorderLayout.CENTER, p3);
		p5.add(BorderLayout.SOUTH, p4);

		// add OK Cancel Panel
		JPanel p6 = new JPanel();
		p6.setLayout(new FlowLayout());
		ok = new JButton("Send Error Report");
		p6.add(ok);
		ok.addActionListener(this);
		cancel = new JButton("Don't Send");
		p6.add(cancel);
		cancel.addActionListener(this);
		details = new JButton("View Report Details");
		p6.add(details);
		details.addActionListener(this);

		// set the panels on the layout
		JPanel p7 = new JPanel();
		p7.setLayout(new BorderLayout(8,8));
		p7.add(BorderLayout.NORTH, p1);
		p7.add(BorderLayout.CENTER, p5);
		p7.add(BorderLayout.SOUTH, p6);

		// create a border
		JPanel p8 = new JPanel();
		p8.setLayout(new BorderLayout(8,8));
        p8.setBorder(BorderFactory.createEmptyBorder(0,20,10,20));
		p8.add(BorderLayout.CENTER, p7);

		// create a canvas for the image
	    BugSplatImageCanvas canvas = new BugSplatImageCanvas(image1);

		getContentPane().setLayout(new BorderLayout(8,8));
		getContentPane().add(BorderLayout.NORTH, canvas);
		getContentPane().add(BorderLayout.SOUTH, p8);

		// prevent resize
		setResizable(false);

        // size the window
        pack();

		// set the location
		Dimension sdim = getToolkit().getScreenSize();
		Dimension fdim = getSize();
		setLocation(sdim.width/2-fdim.width/2, sdim.height/2-fdim.height/2);

        // quite mode - do nothing
		QuietMode = quietMode;
		if (QuietMode == true)
			return;

        // display the window.
        setVisible(true);
	}

	public void actionPerformed(ActionEvent ae)
	{
		if(ae.getSource() == ok)
		{
			// get the dialog data
			UserDescription = input1.getText();
			UserName = input2.getText();
			UserEmail = input3.getText();
			UserAddress = input4.getText();

			Result = true;
			ResultString = "Report sent.";

	        // hide the window.
			setVisible(false);
	    }
		else if(ae.getSource() == cancel)
		{
			Result = false;
			ResultString = "Report not sent.";

	        // hide the window.
			setVisible(false);
		}
		else if(ae.getSource() == details)
		{
			BugSplatViewDetails details = new BugSplatViewDetails(m_requiredFiles, m_additionalFiles, m_enableAdditionalFiles);
			if (details.Result == true)
				m_enableAdditionalFiles = details.m_enableAdditionalFiles;
		}
	}
}
