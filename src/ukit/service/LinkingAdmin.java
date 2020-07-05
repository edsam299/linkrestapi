package ukit.service;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ukitsd.editing.connection.ConnectionFactory;



public class LinkingAdmin {
	String hostrestful="http://odem:8082";
	public static void main(String[] args) throws Exception {
		
		
		String h3="DN02000002";
		File pdffile=new File("D:\\test\\originalfile\\a.pdf");
		String idmlfilename="a.idml";
		idmlfilename="a.epub";
		File idmlfile=new File("D:\\test\\originalfile\\"+idmlfilename);
		String startpage="1";
		String endpage="52";
		String iduser="15";
		
		LinkingAdmin o = new LinkingAdmin();
		JsonObject jj = o.linkingAdmin(ConnectionFactory.getConnectionPostgres(),h3, pdffile, idmlfile, idmlfilename, startpage, endpage, iduser);
		System.out.println(new Gson().toJson(jj));
	}
	
	Connection linkingCon=null;
	Statement stmt=null; ResultSet rs =null;
	
	Map<String,String> mapspace = null;
	Map<String, Integer> mapheader = null;
	Map<Integer, JsonElement> mapinddparagraph = null;
	Map<Integer, JsonElement> jsmapinddparagraph = null;
	
	int fix=0; int header=0; int contentlslide=0; int contentrslide=0;
	int idlink =0; int allpage=0;
	StringBuilder jsonsb =null; StringBuilder jsonidml = null;
	String regex = "(.)*(\\d)(.)*";
    Pattern pattern = Pattern.compile(regex);
    String storypath = null;
    String nocharstyle = null;
    JsonElement jelementparagraph = null;JsonParser jsonparser =null;
    static int DEFAULT_USER_SPACE_UNIT_DPI=0;
    int PRETTY_PRINT_INDENT_FACTOR = 0;
    String xmltojsonhost=null;
    String fontstyleFt=""; 
    String positionstyleFt="Superscript";
    Gson gson =null;
    int loop = 0; int point = 0;
    String ftsymbol="@ueft@";
    String brsymbol="@uebr@";
    ArrayList<String> parameterlist=null;
    public JsonObject linkingAdmin(Connection linkingcon, String h3, File pdffile, File idmlfile, String idmlfilename,
    		String startpage, String endpage, String iduser ) throws Exception{
    	long start = System.nanoTime();
		JsonObject jsonobjreturn=null;
		JsonObject tmpjsonobject = null; 
		JsonArray tmpjsonarray = null;
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		
		String nowdt=null;StringBuilder tmpsb=null; String tmpstr=null;
		String zipfile=null; String zippath=null; String jsonpath=null;
		String deszipfile=null; String imagefile=null; String picturepdf=null;
		String originalpath =null; String extractepub=null;
		
		PDDocument document=null;
		int fix=0;
		try {
			linkingCon=linkingcon;
			nowdt = dateFormat.format(date);
			tmpsb = new StringBuilder(); gson= new Gson();
			jsonparser = new JsonParser();
			jsonobjreturn = new JsonObject();
			rs = Excecute.query("select detail,name from config ", stmt, linkingCon);
			while(rs.next()) {
				if(rs.getString("name").trim().equals("importfile")) {
					tmpjsonobject = (JsonObject) jsonparser.parse(rs.getString("detail"));
					System.out.println(tmpjsonobject.toString());
					zipfile = tmpjsonobject.get("zipfile").getAsString();
					zippath = tmpjsonobject.get("zippath").getAsString();
					deszipfile = tmpjsonobject.get("deszipfile").getAsString();
					imagefile = tmpjsonobject.get("imagefile").getAsString();
					originalpath = tmpjsonobject.get("originalfilepath").getAsString();
					fix = tmpjsonobject.get("picturepdf").getAsInt();
					extractepub = tmpjsonobject.get("extractepub").getAsString();
				}
			}
			
			boolean extract = extractFixLayoutEPUB(originalpath, idmlfilename,  zippath,  zipfile,  extractepub);
			if(extract) {
				document = PDDocument.load(pdffile);
				allpage = document.getNumberOfPages();
				int total = Integer.parseInt(endpage)-Integer.parseInt(startpage)+1;
				if(allpage == total) {
					jsonobjreturn.addProperty("numberofpage",allpage);
					parameterlist.add(startpage);
					parameterlist.add(endpage);
					parameterlist.add(nowdt);
					parameterlist.add(iduser);
					tmpjsonarray = new JsonArray();
					tmpjsonobject = new JsonObject();
					tmpjsonobject.addProperty("name", pdffile.getName());
					tmpjsonarray.add(tmpjsonobject);
					tmpjsonobject=null;tmpjsonobject = new JsonObject();
					tmpjsonobject.addProperty("name", idmlfile.getName());
					tmpjsonarray.add(tmpjsonobject);
					tmpjsonobject=null;
					parameterlist.add(gson.toJson(tmpjsonarray));
					parameterlist.add(h3);
					parameterlist.add(Integer.toString(allpage));
					tmpstr=this.mangeLink(1);
					idlink=0;
					rs = Excecute.query(tmpstr, stmt, linkingCon);
					while (rs.next()) {
						idlink=rs.getInt("id");
						break;
					}
					if(idlink==0) {
						throw new Exception("", new Throwable("ID Link==0"));
					}else {
						print("ID Link ->"+idlink);
						setPDF2Base64(document,allpage,Integer.parseInt(startpage),Integer.parseInt(endpage));				
						getYposition(allpage,pdffile.getAbsolutePath(),jsonpath, document);
						readpdfwithpdfbox(document,allpage,jsonpath);
						readIDML(deszipfile,jsonpath,storypath,parameterlist);
					}
				}else {
					throw new Exception("", new Throwable("allpage != total"));
				}
			}
	    	return jsonobjreturn;
		}catch(Exception e) {
			e.printStackTrace();
			throw new Exception("", new Throwable(e));
		}	
    }
    public boolean extractFixLayoutEPUB(String originalpath,String idmlfilename, String zippath, String zipfile, String extractepub) {
		try {
			File f= new File(originalpath+idmlfilename);
			
			File zfile = new File(zippath+zipfile);
			if(zfile.exists()) {
				zfile.delete();
			}
			if (f.renameTo(zfile)) {
	           System.out.println("Rename succesful");
	           return unzip(zippath+zipfile, extractepub);
	        } else {
	           System.out.println("Rename failed");
	           return false;
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	        return false;
	    }
	 }
	public JsonObject linkingAdmin_(Connection linkingcon, String h3, File pdffile, File idmlfile, String idmlfilename,
		String startpage, String endpage, String iduser ) throws Exception{
		long start = System.nanoTime();
		JsonObject jsonobjreturn=null; 
		JsonObject tmpjsonobject = null; 
		JsonArray tmpjsonarray = null;
		String nowdt=null; PDDocument document=null;
		String idmlarea =null; String idmlmaster=null;  String deszipfile=null;	String imagefile=null;int picturepdf =0;
		String zippath=null; String zipfile=null; String extractidml=null; String jsonpath=null;
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		StringBuilder tmpsb=null; String tmpstr=null; int tmpint=0;
		try {
			linkingCon=linkingcon;
			nowdt = dateFormat.format(date);
			tmpsb = new StringBuilder(); gson= new Gson();
			jsonparser = new JsonParser();
			jsonobjreturn = new JsonObject();
			parameterlist = new ArrayList<String>();
			rs = Excecute.query("select detail,name from config ", stmt, linkingCon);
			while(rs.next()) {
				if(rs.getString("name").trim().equals("importfile")) {
					tmpjsonobject = (JsonObject) jsonparser.parse(rs.getString("detail"));
					idmlarea = tmpjsonobject.get("idmlarea").getAsString();
					idmlmaster= tmpjsonobject.get("idmlmaster").getAsString();
					zipfile = tmpjsonobject.get("zipfile").getAsString();
					zippath = tmpjsonobject.get("zippath").getAsString();
					deszipfile = tmpjsonobject.get("deszipfile").getAsString();
					imagefile = tmpjsonobject.get("imagefile").getAsString();
					picturepdf = tmpjsonobject.get("picturepdf").getAsInt();
					fix = picturepdf;
					extractidml = tmpjsonobject.get("extractidml").getAsString();
					jsonpath= tmpjsonobject.get("jsonpath").getAsString();
				}
				if(rs.getString("name").trim().equals("space")) {
					tmpjsonobject=(JsonObject) jsonparser.parse(rs.getString("detail"));
					mapspace = gson.fromJson(tmpjsonobject, Map.class);
				}
				if(rs.getString("name").trim().equals("paragraphstyle")) {
					tmpjsonobject=(JsonObject) jsonparser.parse(rs.getString("detail"));
					mapheader = gson.fromJson(tmpjsonobject, Map.class);
				}
				if(rs.getString("name").trim().equals("inddlayout")) {
					tmpjsonobject = (JsonObject) jsonparser.parse(rs.getString("detail"));
					header = tmpjsonobject.get("header").getAsInt();
					contentlslide = tmpjsonobject.get("contentlslide").getAsInt();
					contentrslide = tmpjsonobject.get("contentrslide").getAsInt();
					DEFAULT_USER_SPACE_UNIT_DPI= tmpjsonobject.get("DEFAULT_USER_SPACE_UNIT_DPI").getAsInt();
					PRETTY_PRINT_INDENT_FACTOR=tmpjsonobject.get("PRETTY_PRINT_INDENT_FACTOR").getAsInt();
					storypath = tmpjsonobject.get("storypath").getAsString();
					nocharstyle = tmpjsonobject.get("nocharstyle").getAsString();
					xmltojsonhost = tmpjsonobject.get("xmltojsonhost").getAsString();
				}
			}
			if(idmlarea!=null) {
				System.out.println("space map : "+mapspace.toString());
				System.out.println("header map : "+mapheader.toString());
			}
			boolean ext = this.extractidmlfile(extractidml, h3, idmlfile, idmlarea, idmlmaster, zippath, zipfile);
			//boolean ext=true;
			if(ext) {
				document = PDDocument.load(pdffile);
				allpage = document.getNumberOfPages();
				int total = Integer.parseInt(endpage)-Integer.parseInt(startpage)+1;
				if(allpage == total) {
					jsonobjreturn.addProperty("numberofpage",allpage);
					parameterlist.add(startpage);
					parameterlist.add(endpage);
					parameterlist.add(nowdt);
					parameterlist.add(iduser);
					tmpjsonarray = new JsonArray();
					tmpjsonobject = new JsonObject();
					tmpjsonobject.addProperty("name", pdffile.getName());
					tmpjsonarray.add(tmpjsonobject);
					tmpjsonobject=null;tmpjsonobject = new JsonObject();
					tmpjsonobject.addProperty("name", idmlfile.getName());
					tmpjsonarray.add(tmpjsonobject);
					tmpjsonobject=null;
					parameterlist.add(gson.toJson(tmpjsonarray));
					parameterlist.add(h3);
					parameterlist.add(Integer.toString(allpage));
					tmpstr=this.mangeLink(1);
					idlink=0;
					rs = Excecute.query(tmpstr, stmt, linkingCon);
					while (rs.next()) {
						idlink=rs.getInt("id");
						break;
					}
					if(idlink==0) {
						throw new Exception("", new Throwable("ID Link==0"));
					}else {
						print("ID Link ->"+idlink);
						setPDF2Base64(document,allpage,Integer.parseInt(startpage),Integer.parseInt(endpage));				
						getYposition(allpage,pdffile.getAbsolutePath(),jsonpath, document);
						readpdfwithpdfbox(document,allpage,jsonpath);
						readIDML(deszipfile,jsonpath,storypath,parameterlist);
					}
				}else {
					throw new Exception("", new Throwable("allpage != total"));
				}
			}else {
				throw new Exception("", new Throwable(""));
			}
			jsonobjreturn.addProperty("idlink",idlink);
			
			long diff = System.nanoTime() - start;
			System.out.println("Time : "+diff);
		}catch(Exception e) {
			e.printStackTrace();
			throw new Exception("", new Throwable(e));
		}
		return jsonobjreturn;
	}
	
	public boolean readIDML(String deszipfile, String jsonpath, String storypath,
			ArrayList<String> parameterlist) throws  Exception {
		if(createMapinddparagraph(deszipfile,jsonpath,storypath,parameterlist)) {
//			print("mapinddparagraph size : " + mapinddparagraph.size());
//          print("jsmapinddparagraph size : " + jsmapinddparagraph.size());
            if(analysisPDFIDML(jsonpath)) {
            	return true;
            }else {
            	return false;
            }
		}else {
			throw new Exception("", new Throwable("cannot create Map converting xml to json "));
		}
		
	}
	private String ridoffSpace(String s){
        s = s.replaceAll(" ", "");
        for (Map.Entry<String, String> entry : mapspace.entrySet()) {
            s = s.replaceAll(entry.getKey(), "");
        }
        return s;
    }
	public boolean analysisPDFIDML(String jsonpath) {
		boolean returnValue = false; boolean insert=false;boolean ba=false;
		boolean isAppendixNote=false;
		int allpage=0;int result=0; int minxinline=0;
		
		JsonElement jelementparagraph = null; JsonElement jelementparagraph1 = null;
		JsonObject jsonobj = null; JsonObject jsonobj1 = null; 
		
		JsonElement pdfroot =null;
		JsonElement jpageelement = null; JsonElement jlineelement = null;
		
		ArrayList<Integer> p=null; ArrayList<String> contentindd =null;
		ArrayList<String> fsindd = null;
		ArrayList<String> psindd =null;ArrayList<String> chindd =null;
		ArrayList<String> paindd =null;ArrayList<String> tmpcontent = null;
		ArrayList<JsonElement> footnote = null;ArrayList<JsonElement> jsfootnote =null;
		
		
		String tmpstr=""; String minus="";String strline=""; 
		String tmp1=""; String tmp2=""; String tmpftnumber =""; 
		String [] nosarr = null; String[] sarr=null;
		String ridoffspaceindd=""; String ridoffspacelinecontent="";
		
        
        StringBuilder tmpindd =null; StringBuilder inddsb = null;
        StringBuilder characterarray = null; StringBuilder tmpsb=null;
		try {
			pdfroot = jsonparser.parse(jsonsb.toString()); //readpdfwithpdfbox()
			allpage = pdfroot.getAsJsonObject().get("allpage").getAsInt();
			p=new ArrayList<Integer>();
			contentindd = new ArrayList<String>();
			fsindd = new ArrayList<String>();
			psindd = new ArrayList<String>();
			chindd = new ArrayList<String>();
			paindd = new ArrayList<String>();
			tmpcontent = new ArrayList<String>();
			for(int i=0;i<10;i++) {
				p.add(0);
			}
			tmpindd = new StringBuilder();
			inddsb = new StringBuilder();
			characterarray = new StringBuilder();
			tmpsb = new StringBuilder();
			footnote = new ArrayList<JsonElement>();
			jsfootnote = new ArrayList<JsonElement>();
			
			for(int i=0;i<allpage;i++){
				p.set(0, (i+1)); p.set(7, 0); p.set(8, 0);
				jpageelement = pdfroot.getAsJsonObject().get("page_" + p.get(0));
	            p.set(1, jpageelement.getAsJsonObject().get("xyallline").getAsInt());
	            jsfootnote.clear();footnote.clear();
	            if(isAppendixNote){
	                break;
	            }
	            for(int j=0;j<p.get(1);j++){
	            	strline = jpageelement.getAsJsonObject().get("line_"+(j+1)).getAsJsonObject().get("pdfbox").getAsString();
	                ba=false;
	                print(strline);
	                if(strline.substring(strline.length()-1, strline.length()).equals("-")){
	                    strline = strline.substring(0, strline.length()-1);
	                    minus="-";
	                }else{
	                    minus="";
	                }
	                ridoffspacelinecontent= ridoffSpace(strline);
	                if(ridoffspacelinecontent.equals("Appendix")||ridoffspacelinecontent.equals("Note")){
	                    print("NOte Appendix");
	                    isAppendixNote=true;
	                    pdfroot.getAsJsonObject().addProperty("noteappendix", (i+1));
	                    break;
	                }
	                if(ridoffspacelinecontent.equals("atthukirabhojātināma,yatrahināmajātassajarāpaññāyissati,byādhi")){
	                    print("79");
	                }
	                print(ridoffspacelinecontent);
	                p.set(2, ridoffspacelinecontent.length());
	                if(jpageelement.getAsJsonObject().get("line_"+(j+1)).getAsJsonObject().get("isft").getAsBoolean()){
	                    //minxinline =jpageelement.getAsJsonObject().get("line_"+(j+1)).getAsJsonObject().get("minxinline").getAsInt();
	                	minxinline =(int) Math.round(jpageelement.getAsJsonObject().get("line_"+(j+1)).getAsJsonObject().get("minxinline").getAsDouble());
	                    if((int)jpageelement.getAsJsonObject().get("line_"+(j+1)).getAsJsonObject().get("seq_1").getAsJsonArray().get(1).getAsDouble()==minxinline){
	                        tmpftnumber=jpageelement.getAsJsonObject().get("line_"+(j+1)).getAsJsonObject().get("seq_1").getAsJsonArray().get(0).getAsString();
	                        if(checkInteger(tmpftnumber)){
	                            minxinline =(int)jpageelement.getAsJsonObject().get("line_"+(j+1)).getAsJsonObject().get("minhinline").getAsDouble(); //minhinline
	                            if((int)jpageelement.getAsJsonObject().get("line_"+(j+1)).getAsJsonObject().get("seq_1").getAsJsonArray().get(3).getAsDouble()==minxinline){
	                                tmpftnumber=jpageelement.getAsJsonObject().get("line_"+(j+1)).getAsJsonObject().get("seq_2").getAsJsonArray().get(0).getAsString();
	                                if(tmpftnumber.trim().isEmpty()){
	                                    if(p.get(8)!=0){
	                                        p.set(7, p.get(7)+1); 
	                                        p.set(8, 0);
	                                    }
	                                }
	                            }
	                        }
	                    }
	                    compareFootnote(p,footnote, jsfootnote,contentindd, fsindd, psindd,  tmpindd,
	                        chindd, paindd,  jelementparagraph,jelementparagraph1,characterarray,  insert, jsonobj,jsonobj1, tmp1, tmp2, strline,
	                        inddsb, jpageelement, minus,  j);
	                    continue;
	                }
	                if(jpageelement.getAsJsonObject().get("line_"+(j+1)).getAsJsonObject().get("header")!=null) continue;
	                contentindd.clear(); fsindd.clear(); psindd.clear(); tmpindd.setLength(0);chindd.clear();paindd.clear();
	                for(int k=p.get(3); k<mapinddparagraph.size();k++){
	                    jelementparagraph = mapinddparagraph.get(k);
	                    jelementparagraph1 = jsmapinddparagraph.get(k);
	                    loop = (jelementparagraph.getAsJsonObject().get("CharacterStyleRange").isJsonArray()?
	                            jelementparagraph.getAsJsonObject().get("CharacterStyleRange").getAsJsonArray().size():1) ;
	                    p.set(6, loop);
	                    for(int l=p.get(4);l<loop;l++){
	                        p.set(9, l);
	                        jsonobj =(jelementparagraph.getAsJsonObject().get("CharacterStyleRange").isJsonArray()?
	                            jelementparagraph.getAsJsonObject().get("CharacterStyleRange").getAsJsonArray().get(l).getAsJsonObject():
	                            jelementparagraph.getAsJsonObject().get("CharacterStyleRange").getAsJsonObject());
	                        jsonobj1 =(jelementparagraph1.getAsJsonObject().get("CharacterStyleRange").isJsonArray()?
	                            jelementparagraph1.getAsJsonObject().get("CharacterStyleRange").getAsJsonArray().get(l).getAsJsonObject():
	                            jelementparagraph1.getAsJsonObject().get("CharacterStyleRange").getAsJsonObject());
	                        if(jsonobj.get("Footnote")==null){
	                            if(jsonobj.get("Content")==null){
	                                p.set(4, p.get(4)+1); p.set(5, 0);
	                                continue;
	                            }else{
	                                characterarray.setLength(0);
	                                if(jsonobj.get("Content").isJsonArray()){
	                                    for(int z=0;z<jsonobj.get("Content").getAsJsonArray().size();z++){
	                                        characterarray.append(jsonobj.get("Content").getAsJsonArray().get(z).getAsString());
	                                    }
	                                }else{
	                                    characterarray.append((jsonobj.get("Content").getAsString().trim().isEmpty()?" ":jsonobj.get("Content").getAsString()));
	                                }
	                                insert = false;
	                                ridoffspaceindd = ridoffSpace(characterarray.toString());
	                                print("lcharacterarray :"+characterarray.toString());
	                                print("ridoffspaceindd : "+ridoffspaceindd);
	                                print(Integer.toString(ridoffspaceindd.length()));
	                                print(p.get(5).toString());
	                                if(ridoffspaceindd.length()>p.get(5)){
	                                    insert=true;
	                                    ridoffspaceindd = ridoffspaceindd.substring(p.get(5));
	                                    if(ridoffspaceindd.length()>0&&ridoffspaceindd.substring(0, 1).equals("-")){
	                                        ridoffspaceindd=ridoffspaceindd.substring(1);
	                                        p.set(5, p.get(5)+1);
	                                    }
	                                    mapSpace(ridoffspaceindd,
	                                            strline,tmpindd.length(), contentindd, nosarr, sarr, tmpsb);
	                                    tmpstr = tmpsb.toString();tmpsb.setLength(0);          
	                                }else{
	                                    p.set(5, 0);p.set(4, p.get(4)+1);
	                                    if(contentindd.size()!=0){
	                                        contentindd.set(contentindd.size()-1, contentindd.get(contentindd.size()-1)+" ");
	                                        tmpindd.append(" ");
	                                    }
	                                    tmpstr="";
	                                }
	                                if(insert){
	                                    contentindd.add(tmpstr);
	                                    chindd.add(jsonobj.get("AppliedCharacterStyle").getAsString());
	                                    paindd.add(jelementparagraph.getAsJsonObject().get("AppliedParagraphStyle").getAsString());
	                                    fsindd.add((jsonobj.get("FontStyle")==null?"":jsonobj.get("FontStyle").getAsString()));
	                                    psindd.add((jsonobj.get("Position")==null?"":jsonobj.get("Position").getAsString()));
	                                } 
	                            }
	                        }else{//is ft
	                           footnote.add(jsonobj.get("Footnote"));
	                           jsfootnote.add(jsonobj1.get("Footnote"));
	                           tmpstr = Integer.toString(footnote.size());
	                           ridoffspaceindd=tmpstr;
	                           contentindd.add(tmpstr);
	                           chindd.add("footnotenumber");
	                           paindd.add("");fsindd.add(fontstyleFt);psindd.add(positionstyleFt);
	                           p.set(5, 0);  
	                        }
	                        if(tmpstr.isEmpty()){
	                            continue;
	                        }
	                        tmp1=ridoffSpace(tmpindd.toString());
	                        result = tmp1.length()+ridoffspaceindd.length();
	                        if(result>p.get(2)||result==p.get(2)){
	                            if(result>p.get(2)){
	                                ridoffspaceindd =ridoffspaceindd.substring(0,p.get(2)-tmp1.length());
	                                mapSpace(ridoffspaceindd, 
	                                            strline,tmpindd.toString().length(), contentindd, nosarr, sarr, tmpsb);
	                                tmpstr= tmpsb.toString(); tmpsb.setLength(0);
	                                p.set(5, ridoffspaceindd.length()+p.get(5));
	                            }else{
	                                p.set(4, p.get(4)+1);
	                                p.set(5,0);
	                            }
	                            tmpindd.append(tmpstr);
	                            contentindd.set(contentindd.size()-1, tmpstr);
	                            tmp1=ridoffSpace(tmpindd.toString());
	                            print(tmp1);
	                            print(ridoffspaceindd);
	                            if(tmp1.equals(ridoffspacelinecontent)){
	                                result=0;
	                            }else{
	                                result=-1;
	                                if(tmp1.length()>0&&tmp1.substring(0, 1).equals("-")){
	                                    p.set(5,p.get(5)+1);
	                                }
	                            }
	                            inddsb.setLength(0);
	                            inddsb.append("[");
	                            for(int m=0;m<contentindd.size();m++){
	                                if(m>0) inddsb.append(",");
	                                inddsb.append("{");
	                                inddsb.append("\"content\"").append(":\"").append(contentindd.get(m)).append("\"");
	                                if(paindd.get(m).trim().isEmpty()==false)
	                                    inddsb.append(",\"para\"").append(":\"").append(paindd.get(m)).append("\"");
	                                if(chindd.get(m).trim().isEmpty()==false)
	                                    inddsb.append(",").append("\"ch\"").append(":\"").append(chindd.get(m)).append("\"");
	                                if(fsindd.get(m).trim().isEmpty()==false){
	                                    inddsb.append(",").append("\"fs\"").append(":\"").append(fsindd.get(m)).append("\"");
	                                }
	                                if(psindd.get(m).trim().isEmpty()==false){
	                                    inddsb.append(",").append("\"ps\"").append(":\"").append(psindd.get(m)).append("\"");
	                                }
	                                inddsb.append("}");
	                                
	                            }
	                            jpageelement.getAsJsonObject().get("line_"+(j+1)).getAsJsonObject().addProperty("result", result);
	                            if(minus.isEmpty()==false){
	                                inddsb.append(",").append("{");
	                                inddsb.append("\"content\"").append(":\"").append(minus).append("\"");
	                                inddsb.append("}");
	                            }
	                            inddsb.append("]");
	                            jpageelement.getAsJsonObject().get("line_"+(j+1)).getAsJsonObject().addProperty("inddpdf", inddsb.toString());
	                            tmpindd.setLength(0);
	                            ba=true;
	                            break;
	                        }else{
	                            tmpindd.append(tmpstr);
	                            if(contentindd.size()==0){
	                                contentindd.add(tmpstr);
	                            }else{
	                                contentindd.set(contentindd.size()-1, tmpstr);
	                            }
	                            p.set(5, 0);
	                            p.set(4, p.get(4)+1);
	                            if(p.get(4)+1>p.get(6)){
	                                break;
	                            }
	                        }
	                      
	                    }
	                    if(p.get(4)+1>p.get(6)){
	                        p.set(3, p.get(3)+1);
	                        p.set(4, 0);
//	                        break;
	                    }else{
	                        if(p.get(4)+1==p.get(6)&&p.get(5)==0){
	                            if(p.get(9)==p.get(4)){
	                                p.set(3, p.get(3)+1);
	                                p.set(4, 0);
	                            }else{
	                                print("789 check ");
	                            }
	                        }
//	                        break;
	                    }
	                    if(ba) break; 
	                }
	            }
			}
			jsonidml.setLength(0);
			jsonidml.append(gson.toJson(pdfroot));
	        writeOutput2File(jsonpath+"inddpdf2.json",jsonidml);
	        parameterlist.add(jsonidml.toString());
	        if(Excecute.updateinsert(mangeLink(4),stmt, linkingCon)>0) {
	        	returnValue= true;
	        }else {
	        	returnValue= false;
	        }
			return returnValue;
		}catch(Exception e) {
			e.printStackTrace();
			return returnValue;
		}finally {
			jelementparagraph = null; jelementparagraph1 = null;jsonobj = null; jsonobj1 = null;
			pdfroot =null;jpageelement = null;jlineelement = null;
			p=null;contentindd =null;fsindd = null;psindd =null;chindd =null;
			paindd =null;tmpcontent = null;
			tmpstr=null; minus=null;strline=null; tmpindd =null;
			inddsb = null;characterarray = null;
			footnote = null; jsfootnote =null;
			tmp1=null; tmp2=null; nosarr = null; sarr=null;
			ridoffspaceindd=null; ridoffspacelinecontent=null;
			tmpftnumber =null; tmpsb=null;
		}
	}
	public boolean createMapinddparagraph(String deszipfile,String jsonpath,
			String storypath,ArrayList<String>parameterlist) throws  Exception {
		String[] storylist = null; File fXmlFile=null; 
		DocumentBuilderFactory dbFactory =null; DocumentBuilder dBuilder =null; Document doc =null;
		Element rootelement=null;
		ukit.json.JSONObject xmlJSONObj=null; String tmpstr="";
		JsonElement jroot =null;JsonObject jobj0 =null;
		JsonElement jele0 =null;JsonElement jelementstory =null;
		JsonElement jelementparagraph =null; 
		BufferedReader br =null; String line=null;
		boolean returnvalue=false;
		
        dbFactory = DocumentBuilderFactory.newInstance();
        dBuilder = dbFactory.newDocumentBuilder();
        fXmlFile = new File(deszipfile + "designmap.xml");
        doc = dBuilder.parse(fXmlFile);
        doc.getDocumentElement().normalize();
        rootelement = doc.getDocumentElement();
        storylist = rootelement.getAttribute("StoryList").split(" ");//use only 0
        tmpstr = IOUtils.toString(new FileInputStream(deszipfile + storypath + storylist[0] + ".xml"), "UTF-8");
        xmlJSONObj = ukit.json.XML.toJSONObject(tmpstr);
        //get data from design.xml and parse to json
        jsonidml = new StringBuilder();
        jsonidml.append(xmlJSONObj.toString(4));
        writeOutput2File(jsonpath+"jsonidml.json", jsonidml);
         //analysis json idml
        jroot = jsonparser.parse(jsonidml.toString());
        jobj0 = jroot.getAsJsonObject();
        jele0 = jobj0.get("idPkg:Story");
        jelementstory = jele0.getAsJsonObject().get("Story");
        jelementparagraph = jelementstory.getAsJsonObject().get("ParagraphStyleRange");
        parameterlist.add(jsonidml.toString());
        mapinddparagraph = new HashMap();
        for (int i = 0; i < jelementparagraph.getAsJsonArray().size(); i++) {
            mapinddparagraph.put(i, jelementparagraph.getAsJsonArray().get(i));
        }
        //new analysis xml 2 json 
        jsonidml.setLength(0);
        if(callx2js(deszipfile+storypath,storylist[0],jsonpath)) {
        	br = new BufferedReader(new FileReader(jsonpath+"x2js.json"));
        	//br = new BufferedReader(new FileReader(jsonpath+"x2js.txt"));
            line = br.readLine();
            while (line != null) {
                jsonidml.append(line);
                jsonidml.append(System.lineSeparator());
                line = br.readLine();
            }
            br.close();
            jroot = jsonparser.parse(jsonidml.toString());
            parameterlist.add(jsonidml.toString());
            jobj0 = jroot.getAsJsonObject();
            jele0 = jobj0.get("Story");//จุดที่แตกต่างกับ idml ฝั่ง จาวา
            jelementstory = jele0.getAsJsonObject().get("Story");
            jelementparagraph = jelementstory.getAsJsonObject().get("ParagraphStyleRange");
            jsonidml.setLength(0);
            jsmapinddparagraph = new HashMap();
            for (int i = 0; i < jelementparagraph.getAsJsonArray().size(); i++) {
                jsmapinddparagraph.put(i, jelementparagraph.getAsJsonArray().get(i));
            }
            if(Excecute.updateinsert(mangeLink(3), stmt, linkingCon)>0) {
                 returnvalue= true;
            }else {
            	returnvalue= false;
            }
        }else {
        	returnvalue= false;
        }
        storylist = null;fXmlFile =null;dbFactory =null;dBuilder =null;doc =null;
        rootelement=null;xmlJSONObj=null; tmpstr=null;
        jroot =null;jobj0 =null;jele0 =null;jelementstory =null;jelementparagraph =null;
        br=null; line=null;
        return returnvalue;
	}
	public boolean callx2js(String path, String name,String jsonpath) throws IOException {
		URL url =null; HttpURLConnection conn =null;
		boolean returnValue =false;
		try{
			print(hostrestful+"/x2js/"+path+"/"+name+"/"+jsonpath);
			url = new URL(hostrestful+"/x2js/"+path+"/"+name+"/"+jsonpath);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");
			if (conn.getResponseCode() != 200) {
				returnValue= false;
				throw new RuntimeException("Failed : HTTP error code : "
						+ conn.getResponseCode());
			}else {
				returnValue= true;
			}
			return returnValue;
		}catch(Exception e) {
			e.printStackTrace();
			return returnValue;
		}finally {
			conn.disconnect();conn =null;
			url =null;
		}
	}
	public void readpdfwithpdfbox(PDDocument document, int allpage, String jsonpath) throws IOException {
        PDFTextStripper reader = null;      
        String lines[] = null; String lslide="";String pageText = "";
        String[] headersplit={"","",""};String tmpstr = null;
        
        JsonObject jsonobj=null;JsonElement jelement=null;
        JsonElement allpageelement=null;JsonElement elementpage = null;
        JsonElement elementlinenumber = null;
        JsonObject tmpjsonobj = null;
        
        
        int j = 0;int dif = 0;int xyallline = 0;
        boolean setft =false;
        
        jelement = jsonparser.parse(jsonsb.toString());
        jsonsb.setLength(0);
        jsonobj = jelement.getAsJsonObject();
        allpageelement = jsonobj.get("allpage");
        print(allpageelement.getAsString());
        
        for (int i = 0; i < allpage; i++) {
            reader = new PDFTextStripper();
            print("============ Page "+(i+1));
            reader.setStartPage(i + 1);
            reader.setEndPage(i + 1);
            elementpage = jsonobj.get("page_" + (i + 1));
            xyallline = elementpage.getAsJsonObject().get("xyallline").getAsInt();
            pageText = reader.getText(document);
            lines = pageText.split("\\r?\\n");
            j = 0;setft =false;
            for (String line : lines) {
                line = clearSpace(1, line);
                if (j < xyallline) {
                    elementlinenumber = elementpage.getAsJsonObject().get("line_" + (j + 1));
                    tmpjsonobj = elementlinenumber.getAsJsonObject();
                    tmpstr = clearSpace(1, tmpjsonobj.get("xy").getAsString().trim()); 
                    dif = Math.abs(tmpstr.length() - line.trim().length());
                    tmpjsonobj.addProperty("pdfbox", line);
                    System.out.println("662 ->"+line);
                    System.out.println(header);
                    if(setft){               
                            tmpjsonobj.addProperty("isft", setft);
                    }else{
                        if(line.substring(0, 1).matches("-?\\d+(\\.\\d+)?")){
                        	System.out.println(tmpjsonobj.get("maxyinline").getAsString());
                             if(tmpjsonobj.get("maxyinline").getAsDouble()<header || tmpjsonobj.get("maxyinline").getAsDouble()==header){
                                tmpjsonobj.addProperty("header", true);
                                splitHeader(tmpjsonobj.get("xy").getAsString().trim(), headersplit);
                                tmpjsonobj.addProperty("np", headersplit[0]);
                                tmpjsonobj.addProperty("hn", headersplit[1]);
                                tmpjsonobj.addProperty("pnp", headersplit[2]);
                                setft=false;
                             }else{
                                 setft=true;
                             }
                        }
                        tmpjsonobj.addProperty("isft", setft);
                    }
                    tmpjsonobj.addProperty("unit", checkUnit(line));
                    tmpjsonobj.addProperty("dif", dif);
                    if (dif > 0) {
                        lslide="";
                        dif = tmpstr.indexOf(line.trim());
                        if (dif > 0) {
                            tmpjsonobj.addProperty("lslide", tmpstr.substring(0, dif));
                            lslide = tmpstr.substring(0, dif);
                        }
                        if (dif + line.trim().length() != tmpstr.length()) {
                            print("line pdf is "+line);
                            print("String "+tmpstr+" Dif is "+dif);
                            if(dif!=-1){
                                tmpjsonobj.addProperty("rslide", tmpstr.substring(lslide.length() + line.trim().length(), tmpstr.length()));
                            }
                        }
                    }
                }
                j++;
            }
            print("============ End of  Page "+(i+1));
            reader = null;
        }
        jsonsb.append(jelement.toString());
//        print("PDF Box sb : "+jsonsb.toString());
        writeOutput2File(jsonpath+"jsonpdf.json", jsonsb);
        reader = null; lines= null; lslide=null;pageText =null;
        headersplit=null;tmpstr = null;jsonobj=null;jelement=null;
        allpageelement=null;elementpage = null;elementlinenumber = null; tmpjsonobj = null;
    }
	private String checkUnit(String line) {
        if (line.contains("[")) {
            if (pattern.matcher(line.substring(line.indexOf("["), line.indexOf("]") + 1)).matches()) {
                return line.substring(line.indexOf("["), line.indexOf("]") + 1);
            }
        }
        return "";
    }
	private static boolean checkInteger(String string){
      try {
        Integer num = Integer.parseInt(string);
        return true;
      } catch (NumberFormatException e) {
        return false;
      }
    }
	private void compareFootnote(ArrayList<Integer> p,ArrayList<JsonElement>footnote,ArrayList<JsonElement>jsfootnote,
            ArrayList<String> contentindd, ArrayList<String>fsindd, ArrayList<String> psindd, StringBuilder tmpindd,
            ArrayList<String> chindd, ArrayList<String> paindd, JsonElement jelementparagraph,JsonElement jelementparagraph1,
            StringBuilder characterarray, boolean insert, JsonObject jsonobj,JsonObject jsonobj1, String tmp1, String tmp2, String strline,
            StringBuilder inddsb, JsonElement jpageelement, String minus, int j){
        int result=0; String tmpft =null; String tmpft1 =null; String tmp3=null; String tmp4=null;
        StringBuilder tmpinddsb =new StringBuilder();
        StringBuilder tmpsbft = new StringBuilder();
        String[] nosarr=null; String[] sarr=null;
        int countzero=0; int countinline=0; 
        tmp2 = ridoffSpace(strline);
//        if(tmp2.equals("2vihedhayanto(B7.8.9K7.8.10.11MT);vihedayanto(B6);vihedhayanto(B10).Cf.Dhp184;MAV")){
//            print("789ft");
//        }
        for(int z=p.get(7);z<footnote.size();z++){
            contentindd.clear(); fsindd.clear(); psindd.clear(); tmpindd.setLength(0);chindd.clear();paindd.clear();
            jelementparagraph=footnote.get(z).getAsJsonObject().get("ParagraphStyleRange").getAsJsonObject().get("CharacterStyleRange");
            jelementparagraph1=jsfootnote.get(z).getAsJsonObject().get("ParagraphStyleRange").getAsJsonObject().get("CharacterStyleRange");
            loop =(jelementparagraph.isJsonArray()?jelementparagraph.getAsJsonArray().size():1);
            p.set(6, loop); 
            characterarray.setLength(0); insert=false; tmpinddsb.length();
            for(int y=0;y<loop;y++){
                jsonobj = jelementparagraph.getAsJsonArray().get(y).getAsJsonObject();
                jsonobj1 = jelementparagraph1.getAsJsonArray().get(y).getAsJsonObject();
                if(jsonobj.get("AppliedCharacterStyle").getAsString().equals("CharacterStyle/Footnote Number")){
                    characterarray.append((z+1));
                    tmpft = Integer.toString(z+1);
                    if(p.get(8)==0)  {
                       insert = true;
                       countinline = countinline+ tmpft.length();
                    }
                    tmp3=ridoffSpace(characterarray.toString());
                    if(tmp3.length()<p.get(8)){
                        continue;
                    }
                }else{
                    tmpft = (jsonobj.get("Content").getAsString().trim().isEmpty()?" ":jsonobj.get("Content").getAsString());
                    if(jsonobj1.get("Content").isJsonPrimitive()){
                        tmpft1 = (jsonobj1.get("Content").getAsString().trim().isEmpty()?" ":jsonobj1.get("Content").getAsString());
                    }else{
                        tmpft1=tmpft;
                        if(y==0&&tmpft1.trim().isEmpty()){
                            tmpft=Integer.toString(z+1);
                            tmpft1=tmpft;
                        }
                    }
                    print("tmpft : "+tmpft);
                    print("tmpft1 : "+tmpft1);
                    if(tmpft.trim().equals(tmpft1.trim())==false){
                        if(tmpft.length()<tmpft1.length()){
                            countzero = countzero+(tmpft1.length()-tmpft.length());
                            countinline =countinline+(tmpft1.length()-tmpft.length());
                            tmpft=tmpft1;
                        }
                    }
                    tmp3=ridoffSpace(characterarray.toString());
                    tmp4 = ridoffSpace(tmpft);
                    if(((tmp3.length()+tmp4.length())-countzero<p.get(8)||(tmp3.length()+tmp4.length())-countzero==p.get(8))){
                        characterarray.append(tmpft);
                        continue;
                    }else{
                        if(characterarray.toString().equals(tmpft)==false && contentindd.size()==0){
                            insert=true;
                        }
                        if(p.get(8)!=0&& contentindd.size()==0){
                            print("p8");
                            if(tmp3.length()+tmp4.length()>p.get(8)){
                                print("2222");
                                if((tmp3.length()+tmp4.length())-p.get(8)==tmp4.length()&&tmpinddsb.length()>0){
                                    break;
                                }else{
                                    result = p.get(8)-tmp3.length();
                                    System.out.println(result);
                                    if(result==0){
                                        mapSpace(tmp4, tmpft,result, contentindd, nosarr, sarr, tmpsbft);
                                        tmpft = tmpsbft.toString();
                                    }else{
                                        characterarray.append(tmp4.substring(0, result));
                                        mapSpace(tmp4.substring(0, result), tmpft,0, contentindd, nosarr, sarr, tmpsbft);
                                        print(tmpft);
                                        tmpft= tmpft.substring(tmpsbft.length());
                                        print(tmpft);
                                    }
                                    tmp4= ridoffSpace(tmpft);
                                }
                                if(tmpft.isEmpty()) break;
                            }
                        }
                    }
                    if(countinline+tmp4.length()-countzero>tmp2.length()){
                        tmp3 = ridoffSpace(tmpinddsb.toString());
                        print("tmp3:"+tmp3);
                        insert=false;
                        if(countinline==tmp2.length()){
                            p.set(8, p.get(8)+(tmp3.length()-countzero));
                            countzero=0;
                            break;
                        }
                        tmp4 = tmp4.substring(0, tmp2.length()-tmp3.length());
                        if(tmp4.length()>0){
                            if(tmp4.equals(tmpft)){
                                tmpft=tmp4;
                            }else{
                                mapSpace(tmp4,tmpft,ridoffSpace(tmpft).indexOf(tmp4), contentindd, nosarr, sarr, tmpsbft);
                                tmpft = tmpsbft.toString();
                            }
                            print("tmp4 : "+tmp4);
                            print(" char : "+characterarray.toString());
                            print(" str : "+strline);
                            print("tmpft : "+tmpft);
                            characterarray.append(tmpft); 
                            tmpinddsb.append(tmpft);
                            contentindd.add(tmpft);
                            chindd.add(jsonobj.get("AppliedCharacterStyle").getAsString());
                            paindd.add("");
                            fsindd.add((jsonobj.get("FontStyle")==null?"":jsonobj.get("FontStyle").getAsString()));
                            psindd.add((jsonobj.get("Position")==null?"":jsonobj.get("Position").getAsString()));
                            countinline=0;
                        }
                        p.set(8, p.get(8)+tmp3.length()+tmp4.length());    
                        break; 
                    }else{
                        characterarray.append(tmpft);insert=true;
                        countinline=countinline+tmp4.length();
                    }  
                }
                if(insert){
                    tmpinddsb.append(tmpft);
                    contentindd.add(tmpft);
                    chindd.add(jsonobj.get("AppliedCharacterStyle").getAsString());
                    paindd.add("");
                    fsindd.add((jsonobj.get("FontStyle")==null?"":jsonobj.get("FontStyle").getAsString()));
                    if(jsonobj.get("AppliedCharacterStyle").getAsString().equals("CharacterStyle/Footnote Number")){
                        psindd.add(positionstyleFt);
                    }else{
                        psindd.add((jsonobj.get("Position")==null?"":jsonobj.get("Position").getAsString()));
                    }
                }
            }
            print(characterarray.toString()); 
            print("focus on line :"+tmpinddsb.toString());
            tmp1=ridoffSpace(tmpinddsb.toString());
            print(tmp1);
            tmpindd.setLength(0);
            if(insert){
                p.set(8, 0);countinline=0;
                p.set(7, p.get(7)+1); 
                countzero=0;
            }
            tmpindd.append(tmp1);
            result = (ridoffSpace(tmpindd.toString()).equals(tmp2)?0:-1);
            if(result!=0){
                print("indd :"+tmpindd.toString());
                print("strsp : "+tmp2);
                print("result ft is -");
            }
            inddsb.setLength(0);inddsb.append("[");
            for(int m=0;m<contentindd.size();m++){
                if(m>0) inddsb.append(",");
                inddsb.append("{");
                inddsb.append("\"content\":\"").append(contentindd.get(m)).append("\"");
                if(paindd.get(m).trim().isEmpty()==false)
                    inddsb.append(",\"para\":\"").append(paindd.get(m)).append("\"");
                if(chindd.get(m).trim().isEmpty()==false)
                    inddsb.append(",\"ch\":\"").append(chindd.get(m)).append("\"");
                if(fsindd.get(m).trim().isEmpty()==false){
                    inddsb.append(",\"fs\":\"").append(fsindd.get(m)).append("\"");
                }
                if(psindd.get(m).trim().isEmpty()==false){
                    inddsb.append(",\"ps\":\"").append(psindd.get(m)).append("\"");
                }
                inddsb.append("}");
            }
            jpageelement.getAsJsonObject().get("line_"+(j+1)).getAsJsonObject().addProperty("result", result);
            if(minus.isEmpty()==false){
                inddsb.append(",").append("{");
                inddsb.append("\"content\":\"").append(minus).append("\"");
                inddsb.append("}");
            }
            inddsb.append("]");
            jpageelement.getAsJsonObject().get("line_"+(j+1)).getAsJsonObject().addProperty("inddpdf", inddsb.toString());
            tmpindd.setLength(0);
            
            break;
        }
    }
	private String clearSpace(int flag, String a) {
		StringBuilder tmpsb = new StringBuilder();
		String[] tmpstrarr = null;
		int count = 0;
		if (flag == 1) {
			tmpstrarr = a.split("");
			for (int i = 0; i < tmpstrarr.length; i++) {
                if (tmpstrarr[i].trim().isEmpty()) {
                    count = count + 1;
                    if (count == 1) {
                        tmpsb.append(tmpstrarr[i]);
                    }
                } else {
                    if (mapspace.containsKey(Interchange.U2U(tmpstrarr[i]))) {
                        if (count < 1) {
                            tmpsb.append(tmpstrarr[i]);
                        }
                        count = count + 1;
                    } else {
                        tmpsb.append(tmpstrarr[i]);
                        count = 0;
                    }
                }
            }
		}
		tmpstrarr=null;
		return tmpsb.toString();
    }
	public void splitHeader(String h, String[] hsp){
        String[] tmpsp=h.split("");
        hsp[0]="";hsp[1]="";hsp[2]="r";
        for(int i=0;i<tmpsp.length;i++){
            if(tmpsp[i].matches("-?\\d+(\\.\\d+)?")){
                hsp[0]=hsp[0]+tmpsp[i];
                if(i==0){
                    hsp[2]="l";
                }
            }else{
                hsp[1]=hsp[1]+tmpsp[i];
            }
        }
        tmpsp=null;
    }
	public void setPDF2Base64(PDDocument document,int allpage,int startpage,int endpage) throws Exception {
		int tmpint=0;
        long lStartTime = System.nanoTime();
		BufferedImage bim = null; String tmpstr=null;
		PDFRenderer reader=new PDFRenderer(document);
	    for (int page = 0; page < allpage; ++page) {
	    	bim = reader.renderImageWithDPI(page, fix, ImageType.RGB);
	    	tmpstr = encodeToString(bim,"png");
	    	parameterlist.add(tmpstr);
	    	tmpint = startpage+page;
	    	parameterlist.add(Integer.toString(tmpint));
	    	parameterlist.add(Integer.toString(page+1));
	    	tmpstr=this.mangeLink( 2);
	    	tmpint = Excecute.updateinsert(tmpstr, stmt, linkingCon);
	    }
	    bim = null;reader=null; tmpstr=null;
	  //end
        long lEndTime = System.nanoTime();
      //time elapsed
        long output = lEndTime - lStartTime;

        System.out.println("Elapsed time in milliseconds: " + output / 1000000);

	}
	public void getYposition(int allpage, String pdffile, String jsonpath, PDDocument document) throws IOException {
            jsonsb = new StringBuilder();
            jsonsb = new GetCharLocationAndSize(pdffile, allpage,document).jsonxyhsb;
            writeOutput2File(jsonpath+"jsonsbyposition.json", jsonsb);
    }
	
    public void writeOutput2File(String f, StringBuilder sb) throws IOException {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(f));

            writer.write(sb.toString());

        } catch (IOException e) {
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
            }
        }
    }

	public static String encodeToString(BufferedImage image, String type) {
	    String imageString = null;
	    ByteArrayOutputStream bos = new ByteArrayOutputStream();

	    try {
	        ImageIO.write(image, type, bos);
	        byte[] imageBytes = bos.toByteArray();

	        Base64.Encoder encoder = Base64.getEncoder();
	        imageString = encoder.encodeToString(imageBytes);

	        bos.close();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	    return imageString;
	}
	public boolean extractidmlfile(String extractidml,String h3,File idmlfromuser, String idmlarea, String idmlmaster,String zippath, String zipfile) {
		try {
			System.out.println(idmlarea);
			System.out.println(idmlmaster);
			FileUtils.copyFile(idmlfromuser, new File(idmlarea+idmlmaster));
			File zfile = new File(zippath+zipfile);
			if(zfile.exists()) {
				zfile.delete();
			}
			if (idmlfromuser.renameTo(zfile)) {
	           System.out.println("Rename succesful");
	           return unzip(zippath+zipfile, extractidml);
	        } else {
	           System.out.println("Rename failed");
	           return false;
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	        return false;
	    }
	 }
	public boolean unzip(String zipFilePath, String destDir) throws IOException {
        File dir = new File(destDir);
        // create output directory if it doesn't exist
        if (dir.exists()) {
        	File directory = new File(destDir);
        	File[] files = directory.listFiles();
        	for (File file : files){
        	   if (file.delete()) {
        		   System.out.println("delete "+file);
        	   }else {
        		   delete(file);
        	       System.out.println("Failed to delete "+file);
        	   }
        	}
        	directory=null;files=null;
        }else {
        	dir.mkdirs();
        }
        FileInputStream fis;
        //buffer for read and write data to file
        byte[] buffer = new byte[1024];
        try {
            fis = new FileInputStream(zipFilePath);
            ZipInputStream zis = new ZipInputStream(fis);
            ZipEntry ze = zis.getNextEntry();
            while (ze != null) {
                String fileName = ze.getName();
                File newFile = new File(destDir + File.separator + fileName);
                new File(newFile.getParent()).mkdirs();
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                //close this ZipEntry
                zis.closeEntry();
                ze = zis.getNextEntry();
            }
            //close last ZipEntry
            zis.closeEntry();
            zis.close();
            fis.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
	private void delete(File file) throws IOException { 
		for (File childFile : file.listFiles()) {
			if (childFile.isDirectory()) {
				delete(childFile);
			} else {
				if (!childFile.delete()) {
					throw new IOException();
				}
			}
		}
		if (!file.delete()) {
			throw new IOException();
		}
	}
	 private String mangeLink(int flag) {
		StringBuilder sb= new StringBuilder();
		switch(flag) {
			case 1:
				 sb.append("insert into link (startpage, endpage, linkdate, iduser, filename, h3fcrid, allpage) ");
				 sb.append("values('");
				 sb.append(parameterlist.get(0)).append("','").append(parameterlist.get(1)).append("','");
				 sb.append(parameterlist.get(2)).append("','").append(parameterlist.get(3)).append("','");
				 sb.append(parameterlist.get(4)).append("','").append(parameterlist.get(5)).append("',");
				 sb.append(parameterlist.get(6)).append(") ");
				 sb.append("RETURNING  id ;");
				 break;
			case 2:
				sb.append("insert into picture (idlink, page, pic, type, seq) values(");
				sb.append(idlink).append(",").append(parameterlist.get(1)).append(",'").append(parameterlist.get(0));
				sb.append("',0,").append(parameterlist.get(2)).append(");");
				break;
			case 3:
				sb.append("insert into jsonidml(idlink,idml,idmljs) values(");
				sb.append(idlink).append(",").append("'").append(parameterlist.get(0)).append("'");
				sb.append(",").append("'").append(parameterlist.get(1)).append("'").append(");");
				break;
			case 4:
				sb.append("insert into jsonpdfidml (idlink, pdfidml) values(").append(idlink).append(",");
				sb.append("'").append(parameterlist.get(0)).append("'").append(");");
				break;
			
		}
		parameterlist.clear();
		return sb.toString();
	}
	public void mapSpace(String nos, String s,int p, ArrayList<String> content, String [] nosarr, String[] sarr, StringBuilder sb){
	       sb.setLength(0); int bb=0; int st =0;
	       if(p==-1) {
	           if(content.size()>1){
	                for(int a=0;a<content.size();a++){
	                    st = st+content.get(a).length();
	                }
	            }
	       }else{
	           st = p;
	       }
	       print("1282 : "+s);
	       print("value st : "+st);
	       if(s.equals("atthu kira bho jāti nāma, yatra hi nāma jātassa jarā paññāyissati, byādhi ")){
	           print("555");
	       }
	       sarr = s.substring(st).split("");
	       nosarr = nos.split("");
	       for(int a=0;a<nosarr.length;a++){
	           if(a>sarr.length) break;
	           for(int b=bb;b<sarr.length;b++){
	               sb.append(sarr[b]);
	               bb=b+1;
	               if(sarr[b].equals(nosarr[a])){
	                   
	                   break;
	               }
	           }
	       }
	    }
	private void print(String s) {
		//System.out.println(s);
	}
	public void testPosition() {
        String aa = "1,[1][1] evam me sutaṃ. ekaṃ samayaṃ bhagavā Sāvatthiyaṃ viharati Jeta-[5]";
        String bb = "[1] evam me sutaṃ. ekaṃ samayaṃ bhagavā Sāvatthiyaṃ viharati Jeta-";
        System.out.println(aa.indexOf(bb));//5
        System.out.println(aa.endsWith(bb));//true if aa +"-" -> false
        System.out.println(aa.indexOf(bb) + bb.length()); //if not something , it will equal 
        System.out.println(aa.subSequence(aa.indexOf(bb) + bb.length(), aa.length()));//[5]
    }
}
