package ukit.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;


public class Excecute implements Serializable {
    private static void print(String str){
    	//System.out.println(str);
    }
	private static final long serialVersionUID = 4945404767832113057L;
		public static ResultSet query(String sql, Statement stmt, Connection oConn) throws Exception{
		try{
             stmt = oConn.createStatement();
             print(sql);
             return stmt.executeQuery(sql);
		}catch(SQLException e){
              throw e;
		}finally{
		}
	}
		public static int updateinsert(String sql, Statement stmt, Connection oConn) throws Exception{
			try{
	             stmt = oConn.createStatement();
	             print(sql);
	             return stmt.executeUpdate(sql);
			}catch(SQLException e){
	              throw e;
			}finally{
			}
		}
        public static final JsonArray ResultSetToJsonArray(ResultSet rs) {
        JsonObject element = null;
        JsonArray ja = new JsonArray();
        ResultSetMetaData rsmd = null;
        String columnName, columnValue = null;
        try {
            rsmd = rs.getMetaData();
            while (rs.next()) {
                element = new JsonObject();
                for (int i = 0; i < rsmd.getColumnCount(); i++) {
                    columnName = rsmd.getColumnName(i + 1);
                    columnValue = rs.getString(columnName);
                    element.addProperty(columnName, columnValue);
                }
                ja.add(element);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ja;
    }
}
