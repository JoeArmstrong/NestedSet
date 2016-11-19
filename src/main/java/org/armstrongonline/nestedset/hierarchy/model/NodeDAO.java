package org.armstrongonline.nestedset.hierarchy.model;

import java.util.List;
import javax.sql.DataSource;

public interface NodeDAO {
   /** 
    * This is the method to be used to initialize
    * database resources ie. connection.
    */
   public void setDataSource(DataSource ds);
   /** 
    * This is the method to be used to create
    * a record in the Node table.
    */
   public Node createNode(String name, String displayName, String assetId, String parentAssetId);
   /** 
    * This is the method to be used to list down
    * a record from the Node table corresponding
    * to a passed student id.
    */
   public Node getNodeByAssetId(String assetId);
   public Node getNodeByName(String name);
   public Node getNodeByLevelGuid(String levelGuid);
   /** 
    * This is the method to be used to list down
    * all the records modified after a given timestamp.
    */
   public List<Node> getModifiedAfter(String ts);
   /**
    * This method returns a set of object representing 
    * the ownership hierarchy of the given assetId
    * This would be called by the Developer Service when it
    * is creating the JSON return structure.  This will always
    * be called using a leaf-node in the hierarchy and leaf-nodes
    * will always have assetId's - hence it is OK to have the
    * search key be an assetId.
    */
   public List<NodeHierarchy> getHierarchy(String assetId);
   
   /**
    * This method returns a set of objects representing
    * the set of nodes "below" the given level.  This 
    * would be called by a UI process so that the UI can
    * show the set of insertion points for a new level.
    * The level may be any point in the hierarchy, and all
    * levels are not guaranteed to have assetIds - so we cannot
    * use the assetId as the search key and must use the
    * level name instead.
    */
   public List<NodeHierarchy> getReverseHierarchy(String name);
   public List<Node> getReverseHierarchyNodesOnly(String name);
   
   public Node deleteNode(String assetId);
   // public void deleteBranch(String assetId);  Don't support this - to dangerous
   
   public void moveBranch(String levelGuid, String newParentLevelGuid);
   
   /** 
    * This is the method to be used to update
    * a record into the Node table.
    */
   public Node updateNode(String levelGuid, String assetId, String name, String displayName);
}