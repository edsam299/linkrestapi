package ukitsd.editing.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import ukitsd.editing.connection.FileProperties;


public class ConnectionFactory {
    private static ConnectionFactory instance = new ConnectionFactory();
    static Properties properties=FileProperties.getInstance().getConfigProperties();
    private static Connection itapConnection=null;
    private static Connection epConnection;
    private static Connection ephConnection=null;
    private static Connection postgresConnection=null;
    
    private ConnectionFactory() {
        try {
             Class.forName("net.sourceforge.jtds.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

	public static ResultSet query(String sql, Statement stmt, Connection oConn) throws Exception{
		try{
	                stmt = oConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
	                System.out.println(sql);
	                return stmt.executeQuery(sql);
		}catch(SQLException e){
	//		throw new UKITSDException(ErrorCode.CANNOTQUERYDB,e,"query","Excecute");
	                throw e;
		}finally{
	
	            }
	}
    
    private Connection createConnectionSc() throws Exception {
        Connection connection = null;
        try {
        	connection = DriverManager.getConnection(properties.getProperty("SCDataSource"),properties.getProperty("SC_user"),properties.getProperty("SC_password"));
        } catch (Exception e) {
                throw e;
        }
        return connection;
    }   
     private Connection createConnectionItap() throws Exception {
        try {
//        	if(itapConnection==null){
        		itapConnection = DriverManager.getConnection(properties.getProperty("ItapDataSource"),properties.getProperty("Itap_user"),properties.getProperty("Itap_password"));
//        	}
        	return itapConnection;
        } catch (Exception e) {
                throw e;
        }
        
    }    
     private Connection createConnectionEp() throws Exception {
        try {
//        	if(epConnection==null){
        		epConnection = DriverManager.getConnection(properties.getProperty("ePDataSource"),properties.getProperty("eP_user"),properties.getProperty("eP_password"));	
//        	}        	
        	return epConnection;
        } catch (Exception e) {
                throw e;
        }
        
    }    
      private Connection createConnectionEph() throws Exception {
          try {
//        	  if(ephConnection==null){
        		  ephConnection = DriverManager.getConnection(properties.getProperty("EphDataSource"),properties.getProperty("Eph_user"),properties.getProperty("Eph_password"));
//        	  }
          } catch (Exception e) {
                  throw e;
          }
          return ephConnection;
      }  
      
      private Connection createConnectionPostgres() throws Exception {
          try {
//        	 if(postgresConnection==null){
        		 postgresConnection = DriverManager.getConnection(properties.getProperty("postgres_ds"),properties.getProperty("postgres_user"),properties.getProperty("postgres_password"));
//        	 }
          } catch (Exception e) {
                  throw e;
          }
          return postgresConnection ;
      }  
    public static Connection getConnectionSc() throws Exception {
        return instance.createConnectionSc();
    }
    public static Connection getConnectionEp() throws Exception {
        return instance.createConnectionEp();
    }
    public static Connection getConnectionEph() throws Exception {
        return instance.createConnectionEph();
    }
    public static Connection getConnectionItap() throws Exception {
        return instance.createConnectionItap();
    }
    public static Connection getConnectionPostgres() throws Exception {
        return instance.createConnectionPostgres();
    }
    public static void closeConnection(Connection oConn){
		if (oConn != null) {
			try { oConn.close(); oConn = null;} catch (SQLException e) {				
			}oConn = null;
		}
    }
    
    public static void closeResultSet(ResultSet rs){
		if (rs!=null) {
			try { rs.close(); rs = null;} catch (SQLException e) {
			}rs = null;
		}	
    }
    
    public static void closeStatement(Statement stmt){
		if (stmt!=null) {
			try { stmt.close(); stmt=null;} catch (SQLException e) {
			}stmt = null;
		}	
    }
    
    public static void closePreparedStatement(PreparedStatement pstmt){
		if (pstmt!=null) {
			try { pstmt.close(); pstmt=null;} catch (SQLException e) {
			}pstmt = null;
		}	
    }
    
    public static void rollBack(Connection oConn){
		if (oConn!=null) {
			try { oConn.rollback();} catch (SQLException e) {
			}oConn = null;
		}	
    }
}