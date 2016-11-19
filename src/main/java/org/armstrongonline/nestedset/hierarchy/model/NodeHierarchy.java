package org.armstrongonline.nestedset.hierarchy.model;

import java.util.List;

public class NodeHierarchy {
    Node levelData;
    List<NodeHierarchy> nextLevel;
    
    public Node getLevelData() {
        return levelData;
    }
    public void setLevelData(Node levelData) {
        this.levelData = levelData;
    }
    public List<NodeHierarchy> getNextLevel() {
        return nextLevel;
    }
    public void setNextLevel(List<NodeHierarchy> nextLevel) {
        this.nextLevel = nextLevel;
    }
}
