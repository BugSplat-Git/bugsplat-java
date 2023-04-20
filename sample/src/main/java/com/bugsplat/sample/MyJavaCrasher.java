//
// MyJavaCrasher.java is a Java sample
// that uses the BugSplat uncaught exception
// handling, reporting, and online analysis
//
// Copyright 2014 BugSplat, LLC.
//

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;

import com.bugsplat.BugSplat;

public class MyJavaCrasher implements ActionListener {

    //
    // button labels
    //
    String labels[] = {"Null Pointer Exception",
        "Divide By Zero",
        "Array Index Out Of Bounds",
        "Negative Array Size",
        "Chained Exception"};

    //
    // populate the panel
    //
    public Component createComponents() {
        JButton button1 = new JButton(labels[0]);
        JButton button2 = new JButton(labels[1]);
        JButton button3 = new JButton(labels[2]);
        JButton button4 = new JButton(labels[3]);
        JButton button5 = new JButton(labels[4]);

        button1.setMargin(new Insets(5, 25, 5, 25));
        button2.setMargin(new Insets(5, 25, 5, 25));
        button3.setMargin(new Insets(5, 25, 5, 25));
        button4.setMargin(new Insets(5, 25, 5, 25));
        button5.setMargin(new Insets(5, 25, 5, 25));

        button1.addActionListener(this);
        button2.addActionListener(this);
        button3.addActionListener(this);
        button4.addActionListener(this);
        button5.addActionListener(this);

        JPanel panel = new JPanel(new GridLayout(5, 0));
        panel.add(button1);
        panel.add(button2);
        panel.add(button3);
        panel.add(button4);
        panel.add(button5);

        panel.setBorder(BorderFactory.createEmptyBorder(
                20, //top
                20, //left
                20, //bottom
                20) //right
        );

        return panel;
    }

    //
    // use the system look and feel
    //
    private static void initLookAndFeel() {
        String lookAndFeel = UIManager.getSystemLookAndFeelClassName();

        try {
            UIManager.setLookAndFeel(lookAndFeel);
        } catch (ClassNotFoundException e) {
            System.out.println("Unable to set look and feel!");
        } catch (InstantiationException e) {
            System.out.println("Unable to set look and feel!");
        } catch (IllegalAccessException e) {
            System.out.println("Unable to set look and feel!");
        } catch (UnsupportedLookAndFeelException e) {
            System.out.println("Unable to set look and feel!");
        }
    }

    //
    // create the GUI and show it.
    // for thread safety, this method should be
    // invoked from the event-dispatching thread.
    //
    private static void createAndShowGUI() {
        // set the look and feel.
        initLookAndFeel();

        // make sure we have nice window decorations.
        JFrame.setDefaultLookAndFeelDecorated(true);

        // create and set up the window.
        JFrame frame = new JFrame("MyJavaCrasher");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        MyJavaCrasher app = new MyJavaCrasher();
        Component contents = app.createComponents();
        frame.getContentPane().add(contents, BorderLayout.CENTER);

        // prevent resize
        frame.setResizable(false);

        // size the window
        frame.pack();

        // set the location
        Dimension sdim = frame.getToolkit().getScreenSize();
        Dimension fdim = frame.getSize();
        frame.setLocation(sdim.width / 2 - fdim.width / 2, sdim.height / 2 - fdim.height / 2);

        // display the window.
        frame.setVisible(true);
    }

    //
    // entry point
    //
    public static void main(String[] args) {
        try {
            // init the bugsplat library with the database, application and version parameters
            BugSplat.Init("Fred", "MyJavaCrasher", "1.0");

            // set optional parameters
            BugSplat.SetDescription("Java application");

            // set optional parameters
            File additionalFile = new File("additional.txt");
            if (additionalFile.exists()) {
                BugSplat.AddAdditionalFile(additionalFile.getAbsolutePath());
            }

            // tell the BugSplat handler to terminate the app (optional)
            BugSplat.SetTerminateApplication(true);

            // schedule a job for the event-dispatching thread:
            // creating and showing this application's GUI.
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    createAndShowGUI();
                }
            });
        } catch (Exception e) {
            // let the BugSplat library report the exception
            BugSplat.HandleException(e);
        }
    }

    //
    // button handlers
    //
    public void actionPerformed(ActionEvent e) {
        try {
            if (labels[0].equals(e.getActionCommand())) {
                NullPointerTest();
            } else if (labels[1].equals(e.getActionCommand())) {
                DivideByZeroTest();
            } else if (labels[2].equals(e.getActionCommand())) {
                ArrayIndexTest();
            } else if (labels[3].equals(e.getActionCommand())) {
                NegativeArraySizeTest();
            } else if (labels[4].equals(e.getActionCommand())) {
                ChainedExceptionTest();
            }
        } catch (Exception ex) {
            // let the BugSplat library report the exception
            BugSplat.HandleException(ex);
        } catch (Throwable t) {
            // construct an exception and let the BugSplat library report it
            BugSplat.HandleException(new Exception(t));
        }
    }

    //
    // null pointer test
    //
    public void NullPointerTest() {
        Object o = null;
        String temp = o.toString();
    }

    //
    // divide by zero test
    //
    public void DivideByZeroTest() {
        int x = 1;
        int y = 0;
        int z = x / y;
    }

    //
    // invalid array index test
    //
    public void ArrayIndexTest() {
        String arr[] = {"One", "Two"};
        String temp = arr[2];
    }

    //
    // negative array size test
    //
    public void NegativeArraySizeTest() {
        int len = -1;
        String arr[] = new String[len];
    }

    //
    // chained exception test
    //
    public void ChainedExceptionTest() throws Error {
        ChainedException1();
    }

    void ChainedException1() throws Error {
        try {
            ChainedException2();
        } catch (RuntimeException e) {
            throw new Error(e);
        }
    }

    void ChainedException2() throws RuntimeException {
        ChainedException3();
    }

    void ChainedException3() throws RuntimeException {
        try {
            ChainedException4();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void ChainedException4() throws IOException {
        ChainedException5();
    }

    void ChainedException5() throws IOException {
        throw new IOException();
    }
}
