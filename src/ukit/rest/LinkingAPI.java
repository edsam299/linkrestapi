package ukit.rest;

import java.io.File;
import java.io.Serializable;
import java.net.URLDecoder;
import java.sql.Connection;
import java.util.Base64;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import ukit.model.JsonResponse;
import ukit.service.ImportFile;
import ukit.service.LinkingAdmin;
import ukit.service.LinkingAdmin_EPUB;
import ukitsd.editing.connection.ConnectionFactory;

@Path("ImportFile")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON+";charset=UTF-8"})
public class LinkingAPI implements Serializable{
	private static final long serialVersionUID = -8889135514746558365L;

	@GET
	@Path("getSutta/{h2}")
	public Response getSutta(@PathParam("h2") String h2) {
		Connection epConn=null; Connection mpConn=null; JsonArray jsonArray=null;
		try {
			epConn=ConnectionFactory.getConnectionEp();
			mpConn=ConnectionFactory.getConnectionItap();
			jsonArray=new ImportFile().getSuttaTable(mpConn, epConn, h2);
			return Response.ok().entity(new GsonBuilder().serializeNulls().create().toJson(new JsonResponse(true, (jsonArray==null)?"Not Found: "+h2:null, jsonArray))).build();
		}catch(Exception e) {
			return Response.ok().entity(new GsonBuilder().serializeNulls().create().toJson(new JsonResponse(false, String.valueOf(e), null))).build();
		}finally {
			jsonArray=null;
			ConnectionFactory.closeConnection(mpConn);
			ConnectionFactory.closeConnection(epConn);
		}
	}
	@GET
	@Path("linkforadmin/{h3}/{iduser}/{pdffilename}/{idmlfilename}/{startpage}/{endpage}/{pathfile}/{status}")
	public Response managefile(@PathParam("h3") String h3,@PathParam("iduser") String iduser,
			@PathParam("pdffilename") String pdffilename,@PathParam("idmlfilename") String idmlfilename,
			@PathParam("startpage") String startpage,@PathParam("endpage") String endpage,
			@PathParam("pathfile") String pathfile,@PathParam("status") String status) {
		Connection epConn=null; Connection mpConn=null; Connection linkingCon=null;
		JsonObject jsonobject = null;
		long start = System.nanoTime();
		try {
			System.out.println(h3);
			System.out.println(iduser);
			System.out.println(pdffilename);
			linkingCon=ConnectionFactory.getConnectionPostgres();
			epConn=ConnectionFactory.getConnectionEp();
			pathfile = new String(Base64.getDecoder().decode(pathfile),"UTF-8");
			pathfile = URLDecoder.decode(pathfile);
			pathfile = URLDecoder.decode(pathfile);
			System.out.println("pathfileddd : "+pathfile);
			pdffilename = new String(Base64.getDecoder().decode(pdffilename),"UTF-8");
			System.out.println("pdffilename "+pdffilename);
			File pdffile=new File(pathfile+pdffilename);
			idmlfilename=new String(Base64.getDecoder().decode(idmlfilename),"UTF-8");
			System.out.println("idml "+idmlfilename);
			System.out.println("iduser : "+iduser);
			File idmlfile=new File(pathfile+idmlfilename);
			String idlink = new LinkingAdmin_EPUB().linkingAdmin(linkingCon,h3, pdffile, idmlfile, idmlfilename,
					startpage,endpage, iduser,epConn,status);
			return Response.ok().entity(new GsonBuilder().serializeNulls().create().toJson(new JsonResponse(true, h3,idlink))).build();
		}catch(Exception e) {
			e.printStackTrace();
			return Response.ok().entity(new GsonBuilder().serializeNulls().create().toJson(new JsonResponse(false, String.valueOf(e), null))).build();
		}finally {
			ConnectionFactory.closeConnection(mpConn);
			ConnectionFactory.closeConnection(epConn);
			ConnectionFactory.closeConnection(linkingCon);
		}
	}
	
}
