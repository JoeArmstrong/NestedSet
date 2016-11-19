package org.armstrongonline.nestedset.hierarchy.data;

public class Level {
    String levelName;
    String levelDisplayName;
    int levelNumber;
    String levelGuidType;
    String levelGuid;
    String levelAssetId;
    
    public String getLevelName() {
        return levelName;
    }
    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }
    public String getLevelDisplayName() {
        return levelDisplayName;
    }
    public void setLevelDisplayName(String levelDisplayName) {
        this.levelDisplayName = levelDisplayName;
    }
    public int getLevelNumber() {
        return levelNumber;
    }
    public void setLevelNumber(int levelNumber) {
        this.levelNumber = levelNumber;
    }
    public String getLevelGuidType() {
        return levelGuidType;
    }
    public void setLevelGuidType(String levelGuidType) {
        this.levelGuidType = levelGuidType;
    }
    public String getLevelGuid() {
        return levelGuid;
    }
    public void setLevelGuid(String levelGuid) {
        this.levelGuid = levelGuid;
    }
    public String getLevelAssetId() {
        return levelAssetId;
    }
    public void setLevelAssetId(String levelAssetId) {
        this.levelAssetId = levelAssetId;
    }
}
