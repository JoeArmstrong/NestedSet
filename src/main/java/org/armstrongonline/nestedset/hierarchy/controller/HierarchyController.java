package org.armstrongonline.nestedset.hierarchy.controller;

import org.springframework.web.bind.annotation.RestController;

import org.armstrongonline.nestedset.hierarchy.data.LevelHierarchy;
import org.armstrongonline.nestedset.hierarchy.data.Level;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.armstrongonline.nestedset.hierarchy.model.Node;
import org.armstrongonline.nestedset.hierarchy.model.NodeHierarchy;
import org.armstrongonline.nestedset.hierarchy.model.NodeJDBCTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@RestController
public class HierarchyController {
    
    @Autowired
    private ApplicationContext appContext;
    
    @Autowired
    private NodeJDBCTemplate jdbct;

    @RequestMapping(path="/hierarchy/{parentGuid}", method=RequestMethod.POST)
    public @ResponseBody Level post(@PathVariable("parentGuid") String parentGuid, @RequestBody Level payload, HttpServletResponse response) {
        Node node = jdbct.createNode(payload.getLevelName(), payload.getLevelDisplayName(), payload.getLevelAssetId(), parentGuid);
        response.setStatus(HttpServletResponse.SC_OK);
        return node.getLevelInfo();
    }
    
    @RequestMapping(path="/hierarchy", method=RequestMethod.GET)
    public @ResponseBody List<LevelHierarchy> get(@RequestParam("levelid") String levelId, @RequestParam("viewedFrom") String viewedFrom, HttpServletResponse response) {
        List<LevelHierarchy> hierarchyList  = new ArrayList<LevelHierarchy>();
        List<NodeHierarchy> nodes = null;
        
        if (viewedFrom.equalsIgnoreCase("child")) {
        
            // In this case levelName is actually an assetId
            //
            nodes = jdbct.getHierarchy(levelId);
        } else if (viewedFrom.equalsIgnoreCase("parent")) {
            
            // In this case levelName is actually an levelName
            //
            nodes = jdbct.getReverseHierarchy(levelId);    
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }
        convertNodeHiearchyToLevelHierarchy(nodes, hierarchyList);
        response.setStatus(HttpServletResponse.SC_OK);
        return hierarchyList; 
    }
    
    //
    // modified after in the form of 2016-10-17 14:33:00 yyyy-mm-dd hh:mm:ss - URL encoded:
    // /resources?modifiedafter=2016-10-17%2014%3A33%3A00
    @RequestMapping(path="/resources", method=RequestMethod.GET)
    public @ResponseBody List<Level> getDateBased(@RequestParam("modifiedafter") String date, HttpServletResponse response) {
        List<Node> nodes = jdbct.getModifiedAfter(date);
        return convertNodeListToLevelList(nodes);
    }
    
    @RequestMapping(path="/hierarchy/{levelGuid}", method=RequestMethod.PUT)
    public @ResponseBody Level put(@RequestBody Level payload, @PathVariable("levelGuid") String levelGuid, HttpServletResponse response) {
        Node node = jdbct.getNodeByLevelGuid(levelGuid);

        String assetId = payload.getLevelAssetId();
        String name = payload.getLevelName();
        String displayName = payload.getLevelDisplayName();

        if (StringUtils.isEmpty(assetId))
            assetId = node.getLevelInfo().getLevelAssetId();

        if (StringUtils.isEmpty(name))
            name = node.getLevelInfo().getLevelName();

        if (StringUtils.isEmpty(displayName))
            displayName = node.getLevelInfo().getLevelDisplayName();

        jdbct.updateNode(node.getLevelInfo().getLevelGuid(), assetId, name, displayName);


        if (node.getLevelInfo().getLevelNumber() <= 3) {
            // Source of truth for this level information is in ServiceNow - we need to issue REST calls
            // to ServiceNow to update any information at this point.  For now we'll assume it just works.
            System.out.println("Updating ServiceNow for info about " + node.getLevelInfo().getLevelName());
        }

        Node newNode = jdbct.getNodeByLevelGuid(node.getLevelInfo().getLevelGuid());
        response.setStatus(HttpServletResponse.SC_OK);
        return newNode.getLevelInfo();
    }
    
    @RequestMapping(path="/hierarchy/{targetGuid}", method=RequestMethod.PATCH)
    public @ResponseBody List<LevelHierarchy> patch(@PathVariable("targetGuid") String targetGuid, @RequestBody Level payload, HttpServletResponse response) {
        Node target = jdbct.getNodeByLevelGuid(targetGuid);
        Node newParent = jdbct.getNodeByLevelGuid(payload.getLevelGuid());
        jdbct.moveBranch(target.getLevelInfo().getLevelGuid(), newParent.getLevelInfo().getLevelGuid());
        return get(newParent.getLevelInfo().getLevelName(), "parent", response);
    }
    
    @RequestMapping(path="hierarchy/{targetGuid}", method=RequestMethod.DELETE)
    public @ResponseBody Level delete(@PathVariable("targetGuid") String targetGuid,HttpServletResponse response) {
        Node node = jdbct.deleteNode(targetGuid);
        response.setStatus(HttpServletResponse.SC_OK);
        return node.getLevelInfo();
    }
   
    private void convertNodeHiearchyToLevelHierarchy(List<NodeHierarchy> nodes, List<LevelHierarchy> levels) {
        Iterator<NodeHierarchy> nodeHierarchyItr = nodes.iterator();
        while (nodeHierarchyItr.hasNext()) {
            NodeHierarchy nodeHierachy = nodeHierarchyItr.next();
            if (nodeHierachy.getLevelData() == null)
                return;
            LevelHierarchy nextLevelHierarchy = new LevelHierarchy();
            levels.add(nextLevelHierarchy);
            nextLevelHierarchy.setLevelData(nodeHierachy.getLevelData().getLevelInfo());
            if (nodeHierachy.getNextLevel() != null) {
                nextLevelHierarchy.setNextLevel(new ArrayList<LevelHierarchy>());
                convertNodeHiearchyToLevelHierarchy(nodeHierachy.getNextLevel(), nextLevelHierarchy.getNextLevel());
            }
        }
        return;
    }
    
    private List<Level> convertNodeListToLevelList(List<Node> nodes) {
        ArrayList<Level> levels = new ArrayList<Level>();
        Iterator<Node> itr = nodes.iterator();
        while (itr.hasNext()) {
            Node node = itr.next();
            levels.add(node.getLevelInfo());
        }
        return levels;
    }
}
