package ukit.service;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import ukitsd.editing.connection.ConnectionFactory;

public class ImportFile implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = -8798390343000685130L;

	private String getStatus(int status){
        switch(status){
            case 1:
                return "Editing";
            case 2 :
                return "Edited";
            case 3 :
            	return "Linking";
            case 4:
            	return "Linked";
            case 5 :
                return "Published";
            default:
                return "Editing";  
        }
    }
    
    private String getStatus(ResultSet rs1) throws Exception{
        int s=0;
        try {
            if(rs1==null||rs1.next()==false){
                s=0;
            }else{
               
               do{
                   if(s==0){
                       s = rs1.getInt("status");
                   }else{
                       if(s!=rs1.getInt("status")){
                           s=0; 
                           break;
                       }
                   }
               }while(rs1.next());         
            }  	
        }catch(Exception e) {
        	throw e;
        }   
        return this.getStatus(s);
    }
    
    public JsonArray getSuttaTable(Connection mp, Connection ep, String h2) throws Exception{
        ResultSet rs = null; Statement stmt=null;
      ResultSet rs1 = null; JsonObject jsonObject=null; JsonArray jsonArray=null;
        try{  	
            rs = ConnectionFactory.query("SELECT sutta.fcrid,sutta.fcedition, sutta.fcname, sutta.fnseq, sutta.idseries, sutta.idbasetext," +
                " sutta.id, sutta.lastupd, sutta.fcbegcsnum, sutta.fcendcsnum, sutta.fcbcsline, sutta.fcecsline," +
                " basetext.fcrid AS fcridbasetext FROM sutta " +
                "INNER JOIN  basetext ON  basetext.id =  sutta.idbasetext " +
                "WHERE basetext.fcrid = '"+h2+"' ORDER BY CAST(sutta.fcbegcsnum AS INT) ", stmt, ep);
            jsonArray = new JsonArray();
            while(rs.next()) {            	
                rs1 = ConnectionFactory.query("SELECT  header.idseries, header.idbasetext, header.idsutta, chkunit.idh, chkunit.unitno, chkunit.[all]," +
                        " chkunit.[count], chkunit.status FROM header INNER JOIN  chkunit ON  header.idh =  chkunit.idh " +
                        "WHERE header.idseries = "+rs.getInt("idseries")+" AND header.idbasetext ="+rs.getInt("idbasetext")+" AND " +
                        " header.idsutta ="+rs.getInt("id")+" ORDER BY chkunit.unitno ASC", stmt, ep);
                
            	jsonObject=new JsonObject();
            	jsonObject.addProperty("fcrid", rs.getString("fcrid").trim());
            	jsonObject.addProperty("fcname", rs.getString("fcname"));            	
            	jsonObject.addProperty("status", this.getStatus(rs1));
            	jsonArray.add(jsonObject);
            }
            return (jsonArray.size()>0?jsonArray:null);
        }catch(Exception e){
            throw e;
        }finally{
            ConnectionFactory.closeResultSet(rs);
            ConnectionFactory.closeResultSet(rs1);
            ConnectionFactory.closeStatement(stmt);
            jsonArray=null; jsonObject=null;
        }
     }   
    
//	public static void main(String[] args) {
//		// TODO Auto-generated method stub
//		Connection conMP=null; Connection conEP=null;
//		try {
//			conMP=ConnectionFactory.getConnectionItap(); conEP=ConnectionFactory.getConnectionEp();
//			System.out.println(new ImportFile().getSuttaTable(conMP, conEP, "1"));
//		}catch(Exception e) {
//			e.printStackTrace();
//		}
//	}

}
