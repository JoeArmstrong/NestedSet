package org.armstrongonline.nestedset.hierarchy.model;

import java.sql.Timestamp;

import org.armstrongonline.nestedset.hierarchy.data.Level;

public class Node {
    private Level levelInfo;
    private int lft;
    private int rgt;
    private Timestamp create_date;
    private Timestamp modified_date;
    
    public Level getLevelInfo() {
        return levelInfo;
    }
    public void setLevelInfo(Level levelInfo) {
        this.levelInfo = levelInfo;
    }
    public int getLft() {
        return lft;
    }
    public void setLft(int lft) {
        this.lft = lft;
    }
    public int getRgt() {
        return rgt;
    }
    public void setRgt(int rgt) {
        this.rgt = rgt;
    }
    public Timestamp getCreate_date() {
        return create_date;
    }
    public void setCreate_date(Timestamp create_date) {
        this.create_date = create_date;
    }
    public Timestamp getModified_date() {
        return modified_date;
    }
    public void setModified_date(Timestamp modified_date) {
        this.modified_date = modified_date;
    }
    
 }