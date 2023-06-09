//
// MyJavaCrasherConsole.java is a Java sample
// that uses the BugSplat uncaught exception
// handling, reporting, and online analysis
//
// Copyright 2014 BugSplat, LLC.
//

package com.bugsplat.app;

import java.io.*;

import com.bugsplat.BugSplat;

public class MyJavaCrasherConsole {
    public static void main(String[] args) {
        try {
            // init the bugsplat library with the required parameters
            BugSplat.init("Fred", "MyJavaCrasherConsole", "1.0");

            // optionally set crash dialog defaults
            BugSplat.setUser("Fred");
            BugSplat.setEmail("fred@bugsplat.com");
            BugSplat.setDescription("Please enter a description");

            // optionally set key to display associated support response page configured in BugSplat web app
            BugSplat.setKey("en-US");

            // optionally set notes field with arbitrary info
            BugSplat.setNotes("bobby testing notes");

            // optionally add file attachments
            File additionalFile = new File("additional.txt");
            if (additionalFile.exists()) {
                BugSplat.addAdditionalFile(additionalFile.getAbsolutePath());
            }

            // suppress all dialogs
            BugSplat.setQuietMode(true);

            // get the command line arguments
            String opt = "1";
            if (null != args && args.length > 0) {
                opt = args[0];
            }

            if (opt.compareTo("1") == 0) {
                NullPointerTest();
            } else if (opt.compareTo("2") == 0) {
                DivideByZeroTest();
            } else if (opt.compareTo("3") == 0) {
                ArrayIndexTest();
            } else if (opt.compareTo("4") == 0) {
                NegativeArraySizeTest();
            } else if (opt.compareTo("5") == 0) {
                ChainedExceptionTest();
            }
        } catch (Exception e) {
            // let the BugSplat library report the exception
            BugSplat.handleException(e);
        } catch (Throwable t) {
            // construct an exception and let the BugSplat library report it
            BugSplat.handleException(new Exception(t));
        }

        // do not terminate the app, otherwise the report will not be posted
        // System.exit(1);

        // tell the BugSplat handler to terminate the app (optional)
        BugSplat.setTerminateApplication(true);
    }

    //
    // null pointer test
    //
    private static void NullPointerTest() {
        Object o = null;
        String temp = o.toString();
    }

    //
    // divide by zero test
    //
    private static void DivideByZeroTest() {
        int x = 1;
        int y = 0;
        int z = x / y;
    }

    //
    // invalid array index test
    //
    private static void ArrayIndexTest() {
        String arr[] = {"One", "Two"};
        String temp = arr[2];
    }

    //
    // negative array size test
    //
    private static void NegativeArraySizeTest() {
        int len = -1;
        String arr[] = new String[len];
    }

    //
    // chained exception test
    //
    private static void ChainedExceptionTest() throws Error {
        ChainedException1();
    }

    private static void ChainedException1() throws Error {
        try {
            ChainedException2();
        } catch (RuntimeException e) {
            throw new Error(e);
        }
    }

    private static void ChainedException2() throws RuntimeException {
        ChainedException3();
    }

    private static void ChainedException3() throws RuntimeException {
        try {
            ChainedException4();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void ChainedException4() throws IOException {
        ChainedException5();
    }

    private static void ChainedException5() throws IOException {
        throw new IOException();
    }
}
