package org.armstrongonline.nestedset.hierarchy.model;


import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

import org.armstrongonline.nestedset.hierarchy.data.Level;

public class NodeMapper implements RowMapper<Node> {
   public Node mapRow(ResultSet rs, int rowNum) throws SQLException {
      Level level = new Level();
      level.setLevelAssetId(rs.getString("assetId"));
      level.setLevelDisplayName(rs.getString("display_name"));
      level.setLevelGuid(rs.getString("id"));
      level.setLevelNumber(rs.getInt("level"));
      if (level.getLevelNumber() <= 3 )
          level.setLevelGuidType("snow");
      else
          level.setLevelGuidType("hierarchy");
      level.setLevelName(rs.getString("name"));
      
      Node node = new Node();
      node.setLevelInfo(level);
      node.setLft(rs.getInt("lft"));
      node.setRgt(rs.getInt("rgt"));
      node.setCreate_date(rs.getTimestamp("create_date"));
      node.setModified_date(rs.getTimestamp("modified_date"));
      return node;
   }
}