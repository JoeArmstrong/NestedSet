package org.armstrongonline.nestedset.hierarchy.model;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.DataAccessException;

public class NodeJDBCTemplate implements NodeDAO {
    private DataSource dataSource;
    private JdbcTemplate jdbcTemplateObject;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplateObject = new JdbcTemplate(dataSource);
    }

    // corresponds to POST /hierarchy/<level_guid> - substituting name here for now...
    @Override
    public Node createNode(String name, String displayName, String assetId, String levelGuid) {       
        if (levelGuid == null)
            return createFirstNode(name, displayName, assetId);
        else
            return createChildNode(name, displayName, assetId, levelGuid);
    }
    
    // The first node that gets created is always at level=1, with a lft=1, rgt=2
    private Node createFirstNode( String name, String displayName, String assetId ) {
        String SQL = " insert into nested_extended_hierarchy (name, display_name, assetId, level, lft, rgt) values (?, ?, ?, ?, ?, ?)";
        jdbcTemplateObject.update( SQL, name, displayName, assetId, 1, 1, 2);
       
        return getNodeByName(name);
    }
    
    //
    // The nested set insertion makes more sense if you look at the visualization in the Joe Celko book
    //
    private Node createChildNode( String name, String displayName, String assetId, String levelGuid ) {
        
        Node parentNode = getNodeByLevelGuid(levelGuid);
        
        int parentRightVal = parentNode.getRgt();
        
        //
        // The new node is inserted at the rightmost boundary of the parent node, the new node
        // will push the right side of the parent further to the right - the end result is 
        // that the new node's left boundary will be the parent's current right boundary.
        int newNodeLeftVal = parentRightVal;
        int newNodeRightVal = newNodeLeftVal + 1;
        
        int level = parentNode.getLevelInfo().getLevelNumber() + 1;
        
        //
        // push the set boundaries further right to make room for the new node for all boundaries 
        // to the right of the insertion point (the parents right boundary and everything right of the parent's right boundary)
        //
        String SQL = " UPDATE nested_extended_hierarchy SET rgt = rgt + 2 WHERE rgt >= " + parentRightVal;
        jdbcTemplateObject.execute(SQL);
        String SQL2 =" UPDATE nested_extended_hierarchy SET lft = lft + 2 WHERE lft >= " + parentRightVal;
        jdbcTemplateObject.execute(SQL2);
        
        String SQL3 = " insert into nested_extended_hierarchy (name, display_name, assetId, level, lft, rgt) values (?, ?, ?, ?, ?, ?)";
        jdbcTemplateObject.update( SQL3, name, displayName, assetId, level, newNodeLeftVal, newNodeRightVal);
        return getNodeByName(name);
    }
    
    @Override
    public Node getNodeByAssetId(String assetId) {
        String SQL = "select * from nested_extended_hierarchy where assetId = ?";
        Node node = jdbcTemplateObject.queryForObject(SQL, 
                          new Object[]{assetId}, new NodeMapper());
        return node;
    }
    
    @Override
    public Node getNodeByName(String name) {
        String SQL = "select * from nested_extended_hierarchy where name = ?";
        Node node = jdbcTemplateObject.queryForObject(SQL, 
                          new Object[]{name}, new NodeMapper());
        return node;
    }
    
    @Override
    public Node getNodeByLevelGuid(String levelGuid) {
        String SQL = "select * from nested_extended_hierarchy where id = ?";
        Node node = jdbcTemplateObject.queryForObject(SQL, 
                          new Object[]{levelGuid}, new NodeMapper());
        return node;
    }

    // Used as part of GET /resources?modifiedafter="<date time>"
    //
    @Override
    public List<Node> getModifiedAfter(String ts) {
        String SQL = "select * from nested_extended_hierarchy where modified_date > ?";
        List <Node> nodes = jdbcTemplateObject.query(SQL, new Object[]{ts}, new NodeMapper());
        return nodes;
    }

    /**
     * Gets a given node and all of its parents
     * Intended to be called only on leaf-nodes - they are the only nodes that must have assetIds
     * will be used by the GET /resource/{assetId} interface to gather the hierarchy information.
     * Uses the AssetId as the key since this call will always target the leaf node of a hierarchy
     * (which is always a resource) and not arbitrary levels of a hierarchy.
     * 
     * Ordering the return List by level to make it easier to create the hierarchy JSON in the
     * application layer above this data management layer.
     */
    @Override
    public List<NodeHierarchy> getHierarchy(String assetId) {
        String SQL =  "SELECT parent.* FROM nested_extended_hierarchy AS node, " +
                "nested_extended_hierarchy AS parent " +
               "WHERE node.lft BETWEEN parent.lft AND parent.rgt " +
                "AND node.assetId = ? " +
               "ORDER BY parent.lft DESC";
        List <Node> nodes = jdbcTemplateObject.query(SQL, new Object[]{assetId},
                new NodeMapper());
        return convertToHierarchy(nodes);
        // return convertToReverseHierarchy(nodes); Can't re-use this as the reverse hierarchy assumes the parent level exists before the child.
    }
    
    private List<NodeHierarchy> convertToHierarchy( List<Node> nodes ) {
        List<NodeHierarchy> hierarchy = new ArrayList<NodeHierarchy>();
        NodeHierarchy current = new NodeHierarchy();
        hierarchy.add(current);
        
        Iterator<Node> itr = nodes.iterator();
        while (itr.hasNext()) {
            Node node = itr.next();
            current.setLevelData(node);
            current.setNextLevel(new ArrayList<NodeHierarchy>());
            NodeHierarchy next = new NodeHierarchy();
            current.getNextLevel().add(next);
            current = next;
        }
        
        return hierarchy;
    }
    
    //
    // Note that this assumes the parent level exists before a child is added under it
    // therefore we can't use the same convert method for both hierarchy and reverseHierarchy
    //
    private List<NodeHierarchy> convertToReverseHierarchy( List<Node> nodes ) {
        List<NodeHierarchy> hierarchy = new ArrayList<NodeHierarchy>();
        NodeHierarchy current = new NodeHierarchy();
        hierarchy.add(current);
        
        Iterator<Node> itr = nodes.iterator();
        boolean firsttime = true;
        while (itr.hasNext()) {
            Node node = itr.next();
            
            if (firsttime) {
                firsttime = false;
                current.setLevelData(node);
            } else  {
                current = findCorrespondingHierarchy(hierarchy, node);
                if (current.getNextLevel() == null)
                    current.setNextLevel(new ArrayList<NodeHierarchy>());
                NodeHierarchy next = new NodeHierarchy();
                next.setLevelData(node);
                current.getNextLevel().add(next);
            }
        }
        
        return hierarchy;
    }
    
    private NodeHierarchy findCorrespondingHierarchy( List<NodeHierarchy> theList, Node target ) {
        int level = target.getLevelInfo().getLevelNumber();
        int lft = target.getLft();
        int rgt = target.getRgt();
        
        Iterator<NodeHierarchy> itr = theList.iterator();
        while (itr.hasNext()) {
            NodeHierarchy hierarchy = itr.next();
            int thisLevel = hierarchy.getLevelData().getLevelInfo().getLevelNumber();
            int thisLft = hierarchy.getLevelData().getLft();
            int thisRgt = hierarchy.getLevelData().getRgt();
            if ( thisLevel == level - 1 && thisLft < lft && thisRgt > rgt) {
                return hierarchy;
            }
            if (hierarchy.getNextLevel() != null) {
                NodeHierarchy result = findCorrespondingHierarchy(hierarchy.getNextLevel(), target);
                if ( result != null)
                    return result;
            }
        }
        return null;
    }
    
    /**
     * Gets a given node and all of its children in a child-first order.
     * Uses the levelGuid as the key since the target of this call may not be a resource
     * but any level within the hierarchy.
     * 
     * Ordering the return List to make it easier to create the hierarchy JSON in the
     * application layer above this data management layer.
     */
    @Override
    public List<NodeHierarchy> getReverseHierarchy(String name) {
        String SQL =  "SELECT parent.* FROM nested_extended_hierarchy AS node, " +
                "nested_extended_hierarchy AS parent " +
               "WHERE parent.lft BETWEEN node.lft AND node.rgt " +
                "AND node.name = ? " +
               "ORDER BY parent.lft ASC";
        List <Node> nodes = jdbcTemplateObject.query(SQL, new Object[]{name},
                new NodeMapper());
        return convertToReverseHierarchy(nodes);
    }
    
    @Override
    public List<Node> getReverseHierarchyNodesOnly(String name) {
        String SQL =  "SELECT parent.* FROM nested_extended_hierarchy AS node, " +
                "nested_extended_hierarchy AS parent " +
               "WHERE parent.lft BETWEEN node.lft AND node.rgt " +
                "AND node.name = ? " +
               "ORDER BY parent.lft ASC";
        List <Node> nodes = jdbcTemplateObject.query(SQL, new Object[]{name},
                new NodeMapper());
        return nodes;
    }
     
    /**
     * When a node is deleted out of a set, the set boundaries to the target node's right
     * must be shifted left to cover the hole in the set boundary created when the node
     * is deleted.
     * 
     * Since we only allow a leaf-node to be deleted the boundary shift is always 2
     */
    @Override
    public Node deleteNode(String levelGuid) {
        
        Node node = getNodeByLevelGuid(levelGuid);
        
        if (node.getLft() + 1 != node.getRgt()) {
            System.out.println( "Cannot delete node " + node.getLevelInfo().getLevelName() + " - it is not a leaf node");
            return null;
        }
        
        int rightVal = node.getRgt();
        
        String SQL1 = "DELETE from nested_extended_hierarchy WHERE id = '" + levelGuid +"'";
        jdbcTemplateObject.execute(SQL1);
        
        String SQL2 = " UPDATE nested_extended_hierarchy SET rgt = rgt - 2 WHERE rgt > " + rightVal;
        jdbcTemplateObject.execute(SQL2);
        
        String SQL3 =" UPDATE nested_extended_hierarchy SET lft = lft - 2 WHERE lft > " + rightVal;
        jdbcTemplateObject.execute(SQL3);
        
        return node;
    }

    // HTTP PUT
    //
    @Override
    public Node updateNode(String levelGuid, String assetId, String name, String displayName) {
        String SQL = "UPDATE nested_extended_hierarchy SET " + 
                " name = '" + name + 
                "', display_name = '" + displayName + 
                "', assetId = '" + assetId + 
                "' WHERE id = '" + levelGuid + "'";
        jdbcTemplateObject.execute(SQL);
        return null;
    }

    /**
     * HTTP PATCH
     * 
     * This is not the most efficient way of moving a set from one parent to another, a better implementation is likely to be
     * implemented by using an SQL Stored Procedure, but my Stored Procedure knowledge is a little lacking so this method
     * outlines the steps needed to be performed within the Stored Procedure.  An example of the Stored Procedure is shown in 
     * the Joe Celko book referenced on the design wiki.
     */
    @Override
    public void moveBranch(String levelGuid, String newParentLevelGuid) {
        
        Node target = getNodeByLevelGuid(levelGuid);
        Node newParent = getNodeByLevelGuid(newParentLevelGuid);
        
        //
        // Ensure the newParent is not a child of the tree being moved
        //
        List<Node> tree = getReverseHierarchyNodesOnly(target.getLevelInfo().getLevelName());
        Iterator<Node>itr = tree.iterator();
        while (itr.hasNext()) {
            Node node = itr.next();
            if (node.getLevelInfo().getLevelGuid().equalsIgnoreCase(newParent.getLevelInfo().getLevelGuid())) {
                System.out.println("ERROR: Cannot move a node below itself.");
                return;
            }    
        }
        
        //
        // The 'span' indicates how much to shift the boundaries to the left in the old parent, and how
        // much to shift the boundaries to the right in the new parent.
        int span = target.getRgt() - target.getLft();
        int targetLft = target.getLft();
        int targetRgt = target.getRgt();
        
        //
        // Squeeze the lft/rgt of the old placement of the tree, since the target set is moving
        // then the target set's lft thru rgt boundaries need to be subtracted from each node to the set's 
        // right.
        //
        int span1 = span + 1;
        String SQL2 = " UPDATE nested_extended_hierarchy SET lft = lft - " + span1 + " WHERE lft > " + targetRgt;
        jdbcTemplateObject.execute(SQL2);
        
        String SQL1 = " UPDATE nested_extended_hierarchy SET rgt = rgt - " + span1 + " WHERE rgt > " + targetRgt;
        jdbcTemplateObject.execute(SQL1);
              
        //
        // Set new values for the lft/rgt/level values
        // Have to re-get the parent node since it's lft/rgt may have changed
        // due to the collapse of the set boundaries performed above
        //
        newParent = getNodeByLevelGuid(newParentLevelGuid);
        int newTargetLft = newParent.getRgt(); // Yes, newLft is the newParent's Rgt...
        int oldParentLevel = target.getLevelInfo().getLevelNumber() - 1;
        int newParentLevel = newParent.getLevelInfo().getLevelNumber();
        Iterator<Node>itr2 = tree.iterator();
        while (itr2.hasNext()) {
            Node node = itr2.next();
            
            int newLft = newTargetLft + node.getLft() - targetLft;
            int newRgt = newTargetLft + node.getRgt() - targetRgt + span;
            int newLevel = node.getLevelInfo().getLevelNumber() - oldParentLevel + newParentLevel;
            
            //
            // record the new values in our in-memory list of nodes - we'll update the DB rows down below.
            // Can't update the rows here because we are about to re-calculate the lft & rgt values to make
            // room for the set someplace else.
            node.setLft(newLft);
            node.setRgt(newRgt);    
            node.getLevelInfo().setLevelNumber(newLevel);
        }
        
        //
        // Make room for the branch in the new parent
        //
        String SQL4 =" UPDATE nested_extended_hierarchy SET lft = lft + " + span1 + " WHERE lft >= " + newParent.getRgt();
        jdbcTemplateObject.execute(SQL4);
        
        String SQL3 = " UPDATE nested_extended_hierarchy SET rgt = rgt + " + span1 + " WHERE rgt >= " + newParent.getRgt();
        jdbcTemplateObject.execute(SQL3);
        
        
        //
        // Update the records of the target tree to be the new Lft/Rgt/level
        //
        Iterator<Node>itr3 = tree.iterator();
        while (itr3.hasNext()) {
            Node node = itr3.next(); 
            
            String SQL6 = " UPDATE nested_extended_hierarchy SET" +
                    "  lft = " + node.getLft() +
                    ", rgt = " + node.getRgt() +
                    ", level = " + node.getLevelInfo().getLevelNumber() +
                    " WHERE id = '" + node.getLevelInfo().getLevelGuid() + "'";
            jdbcTemplateObject.execute(SQL6);
        }
    }
}