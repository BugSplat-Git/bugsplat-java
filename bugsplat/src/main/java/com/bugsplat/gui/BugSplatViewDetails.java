//
// BugSplat integration code for Java applications.
// This class implements a dialog to get information
// from the user to add to the BugSplat report.
//
// Copyright 2005 BugSplat, LLC.
//
package com.bugsplat.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.io.File;
import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.BorderFactory;

public class BugSplatViewDetails extends JDialog implements ActionListener
{
	private static final String image1 = "images/BugSplatHeader440x49.gif";

	boolean Result = false;
	boolean m_enableAdditionalFiles = true;

	String ServerError = "None.";
	String ServerURL = "http://www.bugsplat.com/post/post_form.php";

	JTable table1;
	JButton ok, cancel;
	JCheckBox addFilesBox;
	Object[][] data = null;  // displayed files
	ArrayList m_requiredFiles;
	ArrayList m_additionalFiles;

	String message1 = "A crash report has been generated for you, containing the files listed below.\n" +
	                  "The report contains detailed information about the state of the application at \n" +
	                  "the time that it crashed, as well as the information that you provided us.";

	String message2 = "In addition, we may have requested that additional files be sent in order to\n" +
	                  "get more detailed information about the crash.";

	String message3 = "If you wish to disable sending the additional files, please uncheck the \n" +
	                  "following box. Files appended with * will be sent with any report. \n \n";

	String message4 = "I wish to allow sending of the additional files";

	BugSplatViewDetails(ArrayList requiredFiles, ArrayList additionalFiles, boolean addFiles)
	{
		super(new JFrame(), "Report Details", true);

		// save the file lists
		m_requiredFiles = requiredFiles;
		m_additionalFiles = additionalFiles;

		// display all files
		ShowFiles();

		// add first Panel
		JPanel p1 = new JPanel();
		p1.setLayout(new BorderLayout(8,8));
		p1.add(BorderLayout.NORTH, new BugSplatLabel(message1, BugSplatLabel.LEFT, false));
		p1.add(BorderLayout.CENTER, new BugSplatLabel(message2, BugSplatLabel.LEFT, false));
		p1.add(BorderLayout.SOUTH, new BugSplatLabel(message3, BugSplatLabel.LEFT, false));

		// list Panel
		JPanel p2 = new JPanel();
		p2.setLayout(new BorderLayout(8,8));
		addFilesBox = new JCheckBox(message4, addFiles);
		p2.add(BorderLayout.NORTH, addFilesBox);
		addFilesBox.addActionListener(this);

		String[] columns = {"Additional file", "Path"};
		table1 = new JTable(data, columns);
		table1.setShowGrid(false);
		JScrollPane scroller = new JScrollPane(table1);
		table1.setPreferredScrollableViewportSize(new Dimension(300, 80));
		table1.getColumnModel().getColumn(0).setPreferredWidth(100);
		table1.getColumnModel().getColumn(1).setPreferredWidth(200);
		p2.add(BorderLayout.CENTER, scroller);

		// add buttons
		JPanel p3 = new JPanel();
		p3.setLayout(new FlowLayout());
		ok = new JButton("OK");
		p3.add(ok);
		ok.addActionListener(this);
		cancel = new JButton("Cancel");
		p3.add(cancel);
		cancel.addActionListener(this);

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

		// show/hide additional files
		DisplayAdditionalFiles(addFiles);

        // display the window.
        setVisible(true);
	}

	public void actionPerformed(ActionEvent ae)
	{
		if(ae.getSource() == ok)
		{
			Result = true;

	        // hide the window.
			setVisible(false);
	    }
		if(ae.getSource() == cancel)
		{
			Result = false;

	        // hide the window.
			setVisible(false);
	    }

		if(ae.getSource() == addFilesBox)
		{
			if (addFilesBox.isSelected() == true) {
				m_enableAdditionalFiles = true;
				DisplayAdditionalFiles(true);
			}
			else {
				m_enableAdditionalFiles = false;
				DisplayAdditionalFiles(false);
			}
	    }
	}

	public void ShowFiles()
	{
		int count1 = m_requiredFiles.size();
		int count2 = m_additionalFiles.size();

		// allocate for the JTable
		data = new Object[count1 + count2][2];

		// required files
		for (int i=0; i<count1; i++)
		{
			File file = new File((String)m_requiredFiles.get(i));
			data[i][0] = file.getName();
			data[i][1] = file.getPath();
		}

		// additional files
		for (int i=0; i<count2; i++)
		{
			File file = new File((String)m_additionalFiles.get(i));
			data[i+count1][0] = file.getName();
			data[i+count1][1] = file.getPath();
		}
	}

	public void DisplayAdditionalFiles(boolean show)
	{
		int count1 = m_requiredFiles.size();
		int count2 = m_additionalFiles.size();

		if (show)
		{
			for (int i=0; i<count2; i++)
			{
				File file = new File((String)m_additionalFiles.get(i));
				table1.setValueAt(file.getName(), i+count1, 0);
				table1.setValueAt(file.getPath(), i+count1, 1);
			}
		}
		else
		{
			for (int i=0; i<count2; i++)
			{
				// use an empty string
				// its simpler than reallocating the table data
				table1.setValueAt("", i+count1, 0);
				table1.setValueAt("", i+count1, 1);
			}
		}
	}
}
