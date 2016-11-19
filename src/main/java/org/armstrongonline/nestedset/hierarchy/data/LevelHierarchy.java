package org.armstrongonline.nestedset.hierarchy.data;

import java.util.List;

public class LevelHierarchy {
    Level levelData;
    List<LevelHierarchy> nextLevel;
    
    public Level getLevelData() {
        return levelData;
    }
    public void setLevelData(Level levelData) {
        this.levelData = levelData;
    }
    public List<LevelHierarchy> getNextLevel() {
        return nextLevel;
    }
    public void setNextLevel(List<LevelHierarchy> nextLevel) {
        this.nextLevel = nextLevel;
    }
}
