//
// BugSplat integration code for Java applications.
// This class implements a dialog to get information
// from the user to add to the BugSplat report.
//
// Copyright 2005 BugSplat, LLC.
//

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import javax.swing.*;

class BugSplatDialog extends JDialog implements ActionListener
{
	private static final String image1 = "resources/BugSplatHeader440x49.gif";

	boolean Result = true;
	String ResultString = "";
	boolean QuietMode = false;

	String UserDescription = "";
	String UserName = "";
	String UserEmail = "";
	String UserAddress = "";

	ArrayList m_requiredFiles;
	ArrayList m_additionalFiles;
	boolean m_enableAdditionalFiles = true;

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


	BugSplatDialog(ArrayList requiredFiles, ArrayList additionalFiles, boolean enableAdditionalFiles, boolean quietMode)
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

class BugSplatDetails extends JDialog implements ActionListener
{
	private static final String image1 = "resources/BugSplatHeader440x49.gif";

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

	BugSplatDetails(String error)
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

class BugSplatViewDetails extends JDialog implements ActionListener
{
	private static final String image1 = "resources/BugSplatHeader440x49.gif";

	boolean Result = false;
	boolean m_enableAdditionalFiles = true;

	String ServerError = "None.";
	String ServerURL = "http://www.bugsplatsoftware.com/post/post_form.php";

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

class BugSplatProgress extends JDialog implements ActionListener
{
	private static final String image1 = "resources/BugSplatHeader350x49.gif";
	private static final String image2 = "resources/BugSplatUpload64x64.gif";

	public static final int taskCompilingReport = 1;
	public static final int taskContactingServer = 2;
	public static final int taskSendingReport = 3;

	boolean Result = true;
	boolean Cancelled = false;
	boolean QuietMode = false;

	int TaskComplete = 0;
	String ResultString = "";

	String message1a = "Generating error report . . . ";
	String message1b = message1a + "done";
	String message2a = "Contacting server . . . ";
	String message2b = message2a + "done";
	String message3a = "Posting data . . . ";
	String message3b = message3a + "done";
	String message4 = "Thank you for sending this error report. \n It has been received successfully.";

	private BugSplatLabel label1; // message1
	private BugSplatLabel label2; // message2
	private BugSplatLabel label3; // message3
	private BugSplatLabel label4; // message4

	private JProgressBar progressBar;
	private JButton button;

	BugSplatProgress(boolean quietMode)
	{
        super(new JFrame(), "Sending error report", false);

        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true); // get space for the string
        progressBar.setString("");          // but don't paint it
        progressBar.setIndeterminate(true);

		// messages
		label1 = new BugSplatLabel(message1b, BugSplatLabel.LEFT, false);
		label2 = new BugSplatLabel(message2b, BugSplatLabel.LEFT, false);
		label3 = new BugSplatLabel(message3b, BugSplatLabel.LEFT, false);
		label4 = new BugSplatLabel(message4, BugSplatLabel.LEFT, false);

		// create a canvas for the image
	    BugSplatImageCanvas canvas1 = new BugSplatImageCanvas(image2);

		// button
		button = new JButton("Cancel");
		button.addActionListener(this);

        JPanel p1 = new JPanel();
		p1.setLayout(new BorderLayout(8,8));
		p1.add(BorderLayout.CENTER, canvas1);

        JPanel p2 = new JPanel();
		p2.setLayout(new GridLayout(3,1));
		p2.add(label1);
		p2.add(label2);
		p2.add(label3);

        JPanel p3 = new JPanel();
		p3.setLayout(new BorderLayout(8,8));
        p3.add(BorderLayout.EAST, p1);
		p3.add(BorderLayout.WEST, p2);

        JPanel p4 = new JPanel();
		p4.setLayout(new BorderLayout(8,8));
		p4.add(BorderLayout.EAST, button);
        p4.add(BorderLayout.WEST, progressBar);

        JPanel p5 = new JPanel();
		p5.setLayout(new BorderLayout(8,8));
		p5.add(BorderLayout.NORTH, label4);
        p5.add(BorderLayout.SOUTH, p4);

		// set the panels on the layout
		JPanel p6 = new JPanel();
		p6.setLayout(new BorderLayout(8,8));
		p6.add(BorderLayout.NORTH, p3);
		p6.add(BorderLayout.SOUTH, p5);

		// create a border
		JPanel p7 = new JPanel();
		p7.setLayout(new BorderLayout(8,8));
        p7.setBorder(BorderFactory.createEmptyBorder(0,20,10,20));
		p7.add(BorderLayout.CENTER, p6);

		// create a canvas for the image
	    BugSplatImageCanvas canvas = new BugSplatImageCanvas(image1);

		getContentPane().setLayout(new BorderLayout(8,8));
		getContentPane().add(BorderLayout.NORTH, canvas);
        getContentPane().add(BorderLayout.SOUTH, p7);

		// prevent resize
		setResizable(false);

        // size the window
		pack();

		// set the location
		Dimension sdim = getToolkit().getScreenSize();
		Dimension fdim = getSize();
		setLocation(sdim.width/2-fdim.width/2, sdim.height/2-fdim.height/2);

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
		label2.setVisible(false);
		label3.setVisible(false);
		label4.setVisible(false);

        // quite mode - do nothing
        QuietMode = quietMode;
        if (QuietMode == true)
        	return;

		// display the window
		setVisible(true);
	}

	public void actionPerformed(ActionEvent ae)
	{
		if (ae.getSource() == button)
		{
			if (button.getText() == "Close")
			{
				Result = true;
				Cancelled = false;
				ResultString = "Report sent successfully.";
			}
			else
			{
				Result = false;
				Cancelled = true;
				ResultString = "User cancelled.";
			}
	    }

        // hide the window.
		setVisible(false);
	}

	public void setTaskComplete(int task)
	{
		TaskComplete = task;

		if  (task == taskCompilingReport)
		{
			label1.setText(message1b);
			label1.setVisible(true);
			label2.setText(message2a);
			label2.setVisible(true);
		}
		if (task == taskContactingServer)
		{
			label2.setText(message2b);
			label2.setVisible(true);
			label3.setText(message3a);
			label3.setVisible(true);
		}
		if  (task == taskSendingReport)
		{
			label3.setText(message3b);
			label3.setVisible(true);
			label4.setVisible(true);

	        progressBar.setValue(100);
	        progressBar.setIndeterminate(false);
			progressBar.setEnabled(false);
			button.setText("Close");
		}

        // quite mode - do nothing
        if (QuietMode == true)
        	return;

		// display the window
		setVisible(true);
	}
}

class BugSplatImageCanvas extends Canvas
{
	Image image = null;
	int width = 0;
	int height = 0;

	BugSplatImageCanvas(String filename)
	{
		// use getResourceAsStream so that this works in a JAR

		// TODO: media tracker looks risky - try this
		// Image i = new javax.swing.ImageIcon("tarsier.png").getImage();

		// wait for the image to load
		InputStream is = getClass().getResourceAsStream(filename);
  		BufferedInputStream bis = new BufferedInputStream(is);
  		byte[] byBuf = new byte[4096];  // a buffer large enough for our image
		try
		{
			int byteRead = bis.read(byBuf, 0, 4096);
		}
		catch (IOException ioe) { }

  		image = Toolkit.getDefaultToolkit().createImage(byBuf);

		MediaTracker mediaTracker = new MediaTracker(this);
		mediaTracker.addImage(image, 0);

		try
		{
			mediaTracker.waitForID(0);
		}
		catch (InterruptedException ie)
		{
			System.out.println("InterruptedException in BugSplatImageCanvas: " + ie.toString());
			// System.exit(1);
		}

		width = image.getWidth(this);
	    height = image.getHeight(this);

		setBounds(0, 32, width, height);
	}

	public int getWidth()
	{
		return width;
	}

	public int getHeight()
	{
		return height;
	}

	public void paint(Graphics graphics)
	{
		if (image != null)
			graphics.drawImage(image, 0, 0, this);
	}
}

//
// Have Label with many lines
// http://www.rgagnon.com/javadetails/java-0269.html
//
class BugSplatLabel extends Canvas
{
  public static final int LEFT = 0;
  public static final int CENTER = 1;
  public static final int RIGHT = 2;
  private String text;
  private String lines[];
  private int num_lines;
  private int line_height;
  private int line_ascent;
  private int line_widths[];
  private int max_width;
  private int alignment;
  private boolean border;
  private int topBottomMargin;
  private int leftRightMargin;
  private int x = 0;
  private int y = 0;
  Dimension offDimension;
  Image offImage;
  Graphics offGraphics;
  Color borderColor = new Color(0).black;

  public BugSplatLabel(String s, int i, boolean b) {
    // s the label
    // i alignement BugSplatLabel.CENTER, BugSplatLabel.RIGHT,
    //   BugSplatLabel.LEFT
    //   default BugSplatLabel.LEFT
    // b border present or not
    setAlignment(i);
    setText(s);
    setBorder(b);
    }

  public BugSplatLabel(String string, int i) {
    this(string, i, false);
    }

  public BugSplatLabel(String string) {
    this(string, 0);
    }

  public BugSplatLabel() {
    this("", 0);
    }

  public void addNotify() {
    super.addNotify();
    calc();
    }

  public void setX(int i) { x = i;  }
  public void setY(int i) { y = i;  }


  public int getLeftRightMargin() {
    return leftRightMargin;
    }

  public void setLeftRightMargin(int i) {
    // make sense only if alignment is BugSplatLabel.LEFT!
    if (i >= 0)  leftRightMargin = i  ;
    }

  public int getAlignment() {
    return alignment;
    }

  public void setAlignment(int i) {
    switch (alignment) {
      case 0:
      case 1:
      case 2:
        alignment = i;
        break;
      default:
        throw new IllegalArgumentException();
      }
    repaint();
    }

  public int getTopBottomMargin() {
    return topBottomMargin;
    }

  public void setTopBottomMargin(int i) {
    if (i >= 0) topBottomMargin = i;
    }

  public void setFont(Font font) {
    super.setFont(font);
    calc();
    repaint();
    }

  public Dimension getMinimumSize() {
    Dimension d = new Dimension
       (max_width + leftRightMargin * 2,
        num_lines * line_height + topBottomMargin * 2);
    if (d.width == 0) d.width = 10;
    if (d.height == 0)  d.height = 10;
    return d;
    }

  public Dimension getPreferredSize() {
    return getMinimumSize();
    }

  public boolean getBorder() {
    return border;
    }

  public void setBorder(boolean flag) {
    border = flag;
    }

  public void setText(String s) {
    // parse the string , "\n" is a the line separator
    StringTokenizer st =
        new StringTokenizer(s,"\n");
    num_lines = st.countTokens();
    lines = new String[num_lines];
    line_widths = new int[num_lines];
    for (int i = 0; i < num_lines; i++)
        lines[i] = st.nextToken();
    calc();
    repaint();
    text = new String(s);
    }

  public String getText() {
    return text;
    }

  public Color getBorderColor() {
   return borderColor;
   }

  public void setBorderColor(Color c) {
   borderColor = c;
   }

  private void calc() {
    // calc dimension and extract maximum width
    Font f = getFont();
    if (f != null) {
      FontMetrics fm = getFontMetrics(f);
      if (fm != null) {
        line_height = fm.getHeight();
        line_ascent = fm.getAscent();
        max_width = 0;
        for (int i = 0; i < num_lines; i++) {
          line_widths[i] =
            fm.stringWidth(lines[i]);
          if (line_widths[i] > max_width)
             max_width = line_widths[i];
          }
        }
      }
    }

  public void update(Graphics g) {
    super.paint(g);
    Dimension d = getSize();
    if ( (offGraphics == null) ||
         (d.width != offDimension.width) ||
         (d.height != offDimension.height)
       ) {
      offDimension = d;
      offImage = createImage(d.width, d.height);
      offGraphics = offImage.getGraphics();
      }
    offGraphics.setColor(getBackground());
    offGraphics.fillRect
         (x, y, getSize().width - 1,
          getSize().height - 1);
    if (border) {
      offGraphics.setColor(borderColor);
      offGraphics.drawRect
         (x, y, getSize().width - 1, getSize().height - 1);
      }
    int j = line_ascent +
      (d.height - num_lines * line_height) / 2;
    for (int k = 0; k < num_lines; ) {
      int i;
      switch (alignment) {
        case 0:
          i = 0;
          break;
        case 2:
          i = d.width - line_widths[k];
          break;
        default:
          i = (d.width - line_widths[k]) / 2;
          break;
        }
      i += leftRightMargin;
      offGraphics.setColor(getForeground());
      offGraphics.drawString(lines[k], i + x, j + y);
      k++;
      j += line_height;
      }
    g.drawImage(offImage,0,0,this);
    }

  public void paint(Graphics g) {
    update(g);
    }
}