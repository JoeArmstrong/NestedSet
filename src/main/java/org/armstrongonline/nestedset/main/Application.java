package org.armstrongonline.nestedset.main;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.armstrongonline.nestedset.hierarchy.model.Node;
import org.armstrongonline.nestedset.hierarchy.model.NodeJDBCTemplate;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.support.ClassPathXmlApplicationContext;

@ComponentScan(basePackages={"org.armstrongonline.nestedset.hierarchy.controller"})

@SpringBootApplication
@ImportResource("classpath:Beans.xml")
public class Application {

    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(Application.class, args);
      
        doDBStuff(ctx);
    }
    
    public static void doDBStuff(ApplicationContext context) {
        
        setupNodeTable(context);
    
     }
     
     public static void setupNodeTable(ApplicationContext context) {
         
         NodeJDBCTemplate nodeJDBCTemplate = 
                 (NodeJDBCTemplate)context.getBean("nodeJDBCTemplate");
         System.out.println("Creating Nested_extended_hierarchy Table");
         Node level1 = nodeJDBCTemplate.createNode("Level1", "Level: Level1", null, null);
         Node level1_1 = nodeJDBCTemplate.createNode("Level1_1", "Level: Level1_1", null, level1.getLevelInfo().getLevelGuid());
         Node level1_1_1 = nodeJDBCTemplate.createNode("Level1_1_1", "Level: Level1_1_1", "100001", level1_1.getLevelInfo().getLevelGuid());
           Node level1_1_1_1 = nodeJDBCTemplate.createNode("Level1_1_1_1", "Level: Level1_1_1_1", null, level1_1_1.getLevelInfo().getLevelGuid());
             nodeJDBCTemplate.createNode("level1_1_1_1_1", "Level: level1_1_1_1_1", "100002", level1_1_1_1.getLevelInfo().getLevelGuid());
             nodeJDBCTemplate.createNode("level1_1_1_1_2", "Level: level1_1_1_1_2", "100003", level1_1_1_1.getLevelInfo().getLevelGuid());
         
           Node level1_1_1_2 = nodeJDBCTemplate.createNode("level1_1_1_2", "Level: level1_1_1_2", null, level1_1_1.getLevelInfo().getLevelGuid());
             nodeJDBCTemplate.createNode("level1_1_1_2_1", "Level: level1_1_1_2_1", "100004", level1_1_1_2.getLevelInfo().getLevelGuid());
             nodeJDBCTemplate.createNode("level1_1_1_2_2", "Level: level1_1_1_2_2", "100005", level1_1_1_2.getLevelInfo().getLevelGuid());
             Node level1_1_1_2_3 = nodeJDBCTemplate.createNode("level1_1_1_2_3", "Level: level1_1_1_2_3", "100006", level1_1_1_2.getLevelInfo().getLevelGuid());
               Node level1_1_1_2_3_1 = nodeJDBCTemplate.createNode("level1_1_1_2_3_1", "Level: level1_1_1_2_3_1", "100007", level1_1_1_2_3.getLevelInfo().getLevelGuid());
                 nodeJDBCTemplate.createNode("level1_1_1_2_3_1_1", "Level: level1_1_1_2_3_1_1", "100008", level1_1_1_2_3_1.getLevelInfo().getLevelGuid());
                 nodeJDBCTemplate.createNode("level1_1_1_2_3_1_2", "Level: level1_1_1_2_3_1_2", "100009", level1_1_1_2_3_1.getLevelInfo().getLevelGuid());
               nodeJDBCTemplate.createNode("level1_1_1_2_3_2", "Level: level1_1_1_2_3_2", "1000010", level1_1_1_2_3.getLevelInfo().getLevelGuid());
           System.out.println("Done creating Nested_extended_hierarchy Table");
     }
     
     public static void printHierarchy( List<Node> nodes ) {
         Iterator<Node> itr = nodes.iterator();
         while (itr.hasNext()) {
             Node node = itr.next();
             String leader = getLeader(node);
             System.out.println( leader + node.getLevelInfo().getLevelDisplayName());
         }
     }
     
     public static String getLeader( Node node ) {
         String leader = "";
         for (int cnt=0; cnt < node.getLevelInfo().getLevelNumber(); cnt++)
             leader = leader + "+";
         leader = leader + " ";
         return leader;
     }
}