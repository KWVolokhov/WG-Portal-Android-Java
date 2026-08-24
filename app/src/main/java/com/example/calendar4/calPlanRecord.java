package com.example.calendar4;

import java.io.Serializable;
import java.util.Date;

public class calPlanRecord implements Serializable {
    // Auto-filled fields (not input by user)
    public Integer id;
    public String UNID;
    public Date Okdate;
    public String AuthorID; // Always "BUSINESS"
    public String LastUpdatedByID;
    public String LastUpdatedBy;
    public Date LastUpdatedDate;
    public Date HoldDate;
    public String Revisions;

    // User input fields
    public String Form; // 'Project', 'Note', 'Remember', 'Task'
    public String Name; // Название
    public Integer Priority;
    public String AuthorName;
    public String RequestName;
    public String RequestUNID;
    public String Status;
    public String StatusID;
    public String MainSystem;
    public String AnalitikID;
    public String AnalitikName;
    public String ExectorID;
    public String ExectorName;
    public String BodyText;
    public String Comment;
    public Date StartDate;
    public Date EndDate;
    public String InstallOrder;
    public String KeyWords;

    public calPlanRecord() {
        // Initialize with defaults
        this.AuthorID = "BUSINESS";
        this.Form = "Project";
    }

    public calPlanRecord(String form, String name) {
        this();
        this.Form = form;
        this.Name = name;
    }
}