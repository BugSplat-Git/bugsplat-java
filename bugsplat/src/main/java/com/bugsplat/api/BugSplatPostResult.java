package com.bugsplat.api;

public class BugSplatPostResult {

    public boolean success;
    public String infoUrl;

    public BugSplatPostResult(boolean success, String infoUrl) {
        this.success = success;
        this.infoUrl = infoUrl;
    }
}
