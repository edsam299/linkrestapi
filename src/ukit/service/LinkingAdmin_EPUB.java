package ukit.service;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ukitsd.editing.connection.ConnectionFactory;

public class LinkingAdmin_EPUB {

	String hostrestful="http://localhost:8082";
	Connection linkingCon=null;
	Statement stmt=null; ResultSet rs =null;
	JsonParser jsonparser =null;
	Gson gson =null;
	static ArrayList<String> parameterlist=null;
	StringBuilder jsonsb=null;
	int idlink =0;int allpage=0;int fix=0;
	public static void main(String[] args) throws Exception {
		LinkingAdmin_EPUB o = new LinkingAdmin_EPUB();
		String toc="toc.xhtml";
//		o.idlink = 184; o.linkingCon=ConnectionFactory.getConnectionPostgres();
//		o.parameterlist=new ArrayList<String>();
//		String line = o.mangeLink(11);
//		o.rs = Excecute.query(line, o.stmt, o.linkingCon);
//		while(o.rs.next()) {
//			System.out.println(o.rs.getString("name"));
//			
//		}
		String h3="DN02000002";
		File pdffile=new File("D:\\test\\originalfile\\a.pdf");
		String idmlfilename="a.idml";
		idmlfilename="a.epub";
		File idmlfile=new File("D:\\test\\originalfile\\"+idmlfilename);
		String startpage="1";
		String endpage="52";
		String iduser="15";
		String status="Edited";
		
		String jj = o.linkingAdmin(ConnectionFactory.getConnectionPostgres(),h3, pdffile, idmlfile, idmlfilename, startpage, endpage, iduser,ConnectionFactory.getConnectionEp(),status);
		//System.out.println(new Gson().toJson(jj));
	}
	
	 public String linkingAdmin(Connection linkingcon, String h3, File pdffile, File idmlfile, String idmlfilename,
	    		String startpage, String endpage, String iduser,Connection ep, String status ) throws Exception{
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
		int tmpint=0;
			
		PDDocument document=null;
		StringBuilder sbepub=null;
		ResultSet rs = null; Statement stmt=null;
		
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
					//System.out.println(tmpjsonobject.toString());
					zipfile = tmpjsonobject.get("zipfile").getAsString();
					zippath = tmpjsonobject.get("zippath").getAsString();
					deszipfile = tmpjsonobject.get("deszipfile").getAsString();
					imagefile = tmpjsonobject.get("imagefile").getAsString();
					originalpath = tmpjsonobject.get("originalfilepath").getAsString();
					fix = tmpjsonobject.get("picturepdf").getAsInt();
					extractepub = tmpjsonobject.get("extractepub").getAsString();
					jsonpath= tmpjsonobject.get("jsonpath").getAsString();
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
						print("ID Link ->"+idlink+" jsonpath>"+jsonpath); 
						setPDF2Base64(document,allpage,Integer.parseInt(startpage),Integer.parseInt(endpage));				
						getYposition(allpage,pdffile.getAbsolutePath(),jsonpath, document);
						readpdfwithpdfbox(document, total, jsonpath);
						insertEpub2DB(extractepub);
						sbepub = getEPUB(extractepub,Integer.toString(idlink),jsonpath);
						if(sbepub.length()!=0) {
							//update status in every unit to ep
							rs = ConnectionFactory.query("select idseries,idbasetext,id from sutta where fcrid='"+h3+"';", stmt, ep);
							int idseries=0; int idbasetext=0; int idsutta=0;
							while(rs.next()) { 
								idseries = rs.getInt("idseries");
								idbasetext = rs.getInt("idbasetext");
								idsutta = rs.getInt("id");
								break;
							}
							if(idseries==0 && idbasetext==0 && idsutta==0) {
								throw new Exception("", new Throwable("idseries==0 && idbasetext==0 && idsutta==0"));
							}else {
								rs = ConnectionFactory.query("select idh from header where idseries="+idseries+" and idbasetext="+idbasetext+" and idsutta="+idsutta, stmt, ep);
								ArrayList<Integer> suttalist = new ArrayList<Integer>();
								while(rs.next()) {
									suttalist.add(rs.getInt("idh"));
								}
								if(suttalist.size()==0) {
									throw new Exception("", new Throwable("suttalist is 0!"));
								}else {
									tmpstr="";
									for(int k=0;k<suttalist.size();k++) {
										if(k!=0) tmpstr=tmpstr+",";
										tmpstr=tmpstr+suttalist.get(k);
									}
									//System.out.println("suttalist is "+tmpstr);
									tmpstr = "update chkunit set status =3 where idh in ("+tmpstr+");";
									tmpint = Excecute.updateinsert(tmpstr, stmt, ep);
									tmpstr = "update link set statusaction='"+status+"' where id="+idlink+";";
									tmpint = Excecute.updateinsert(tmpstr, stmt, linkingCon);
									return Integer.toString(idlink);
								}
								
							}
						}else {
							throw new Exception("", new Throwable("jsonpdfepub is 0 length!"));
						}
					}
				}else {
					throw new Exception("", new Throwable("allpage != total"));
				}
			}
			return "";
		}catch(Exception e) {
			e.printStackTrace();
			throw new Exception("", new Throwable(e));
		}	
	 }
	 public StringBuilder getEPUB(String extractepub, String idlink,String jsonpath) throws IOException {
		URL url =null; HttpURLConnection conn =null;
		StringBuilder returnValue =null; String output="";
		try{
			extractepub = URLEncoder.encode(extractepub, StandardCharsets.UTF_8.toString());
			jsonpath = URLEncoder.encode(jsonpath, StandardCharsets.UTF_8.toString());
			returnValue = new StringBuilder();
			print(hostrestful+"/getEPUBDetail/"+extractepub+"/"+idlink+"/"+jsonpath);
			url = new URL(hostrestful+"/getEPUBDetail/"+extractepub+"/"+idlink+"/"+jsonpath);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Accept", "application/json");
			if (conn.getResponseCode() != 200) {
				returnValue.append("false");
				throw new RuntimeException("Failed : HTTP error code : "
						+ conn.getResponseCode());
			}else {
				BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream()),"UTF8"));
			    while ((output = br.readLine()) != null) {
			    	returnValue.append(output);
			    }
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
	 	 private void writeOutput2File(String f, StringBuilder sb) throws IOException {
		 print(f);
	        BufferedWriter writer = null;
	        try {
	            //writer = new BufferedWriter(new FileWriter(f));
	        	writer=Files.newBufferedWriter(Paths.get(f));
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
	 private void getYposition(int allpage, String pdffile, String jsonpath, PDDocument document) throws IOException {
         jsonsb = new StringBuilder();
         jsonsb = new GetCharLocationAndSize(pdffile, allpage,document).jsonxyhsb;
         //System.out.println(jsonsb.toString());
         writeOutput2File(jsonpath+"jsonsbyposition.json", jsonsb);
	 }
	 private void setPDF2Base64(PDDocument document,int allpage,int startpage,int endpage) throws Exception {
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

	        //System.out.println("Elapsed time in milliseconds: " + output / 1000000);

		}
	 public  String encodeToString(BufferedImage image, String type) {
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
	 public boolean extractFixLayoutEPUB(String originalpath,String idmlfilename, String zippath, String zipfile, String extractepub) {
		try {
			File f= new File(originalpath+idmlfilename);
			FileUtils.copyFile(f, new File(originalpath+"backup_"+idmlfilename));	
			File zfile = new File(zippath+zipfile);
			if(zfile.exists()) {
				zfile.delete();
			}
			if (f.renameTo(zfile)) {
		         // System.out.println("Rename succesful");
		          return unzip(zippath+zipfile, extractepub);
		    } else {
		         //System.out.println("Rename failed");
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
	 private boolean insertEpub2DB(String destDir) {
		 //System.out.println("ue311 "+destDir);
		 String line=null; File folder =null;
		 Scanner sc=null;FileInputStream inputStream=null;
		 File[] files =null; File[] childfile=null;String [] field=null;
		 File[] childfile1=null; ArrayList<File> xhtmllist = null;
		 StringBuilder image=null;   StringBuilder xhtml=null;
		 StringBuilder font=null;   StringBuilder contentopf=null;
		 StringBuilder sb =null; int tmpint=0;byte [] byteImage=null;
		 try {
			 folder = new File(destDir);
			 files = folder.listFiles();
			 image= new StringBuilder();xhtml= new StringBuilder();
		     font= new StringBuilder();contentopf= new StringBuilder();
		     sb = new StringBuilder();
		     for (File file : files){
		    	 parameterlist.clear();
		    	 if(file.getName().equals("META-INF")) {
		            childfile = file.listFiles();
		            for(int i=0;i<childfile.length;i++) {
		            	field=childfile[i].getName().split("\\.");
		            	parameterlist.add(field[0]);
		            	sb.setLength(0);
		            	getDetailFile(sb, childfile[i], inputStream, sc);
		              	   parameterlist.add(sb.toString());
		            }
		            line = mangeLink(6);
		  		    tmpint = Excecute.updateinsert(line, stmt, linkingCon);
		         }
		    	 if(file.getName().equals("mimetype")) {
		         	parameterlist.add(Integer.toString(idlink));
		         	sb.setLength(0);
		         	getDetailFile(sb, file, inputStream, sc);
		         	parameterlist.add(sb.toString());
		         	line = mangeLink(5);
				    tmpint = Excecute.updateinsert(line, stmt, linkingCon);
		         }
		    	 if(file.getName().equals("OEBPS")) {
		    		 xhtmllist = new ArrayList<File>();
		    		 childfile = file.listFiles();
		    		 for(int i=0;i<childfile.length;i++) {
		    			 if(childfile[i].isFile()) {
		    				 field = childfile[i].getName().split("\\.");
		    				 if(field[1].equals("opf")) {//one file
		    					 contentopf.setLength(0);
		    					 getDetailFile(contentopf, childfile[i], inputStream, sc);
		    				 }else if(field[1].equals("xhtml")) {
		    					 xhtmllist.add(childfile[i]);
		    					 if(xhtml.length()>0) xhtml.append(",");
		    					 	xhtml.append(childfile[i].getName());
		            			}
		            		}else {//directory
		            			if(childfile[i].getName().equals("font")) {
		            				childfile1 = childfile[i].listFiles();
		            				for(int j=0;j<childfile1.length;j++) {
		            					if(font.length()>0) font.append(",");
		            					font.append(childfile1[j].getName());
		            				}
		            			}
		            			if(childfile[i].getName().equals("css")) {//oebps_css
		            				parameterlist.clear();
		            				childfile1 = childfile[i].listFiles();
		            				for(int j=0;j<childfile1.length;j++) {
		            					parameterlist.add(childfile1[j].getName());
		            					sb.setLength(0);
		                    			getDetailFile(sb, childfile1[j], inputStream, sc);
		                          	    parameterlist.add(sb.toString());
		            				}
		            				line = mangeLink(7);
		            			    tmpint = Excecute.updateinsert(line, stmt, linkingCon);
		            			}
		            			if(childfile[i].getName().equals("image")) {//oebps_image
		            				childfile1 = childfile[i].listFiles();
		            				for(int j=0;j<childfile1.length;j++) {
		            					parameterlist.clear();
		            					if(image.length()>0) image.append(",");
		            					image.append(childfile1[j].getName());
		            					parameterlist.add(childfile1[j].getName());
		            					byteImage = ImageToByte(childfile1[j]);
		                          	    parameterlist.add(org.postgresql.util.Base64.encodeBytes(byteImage));
		                          	    line = mangeLink(8);
		                          	    tmpint = Excecute.updateinsert(line, stmt, linkingCon);
		            				}
		            			}
		            		}
		            	}
		            }
		     }//end for file
		     if(xhtmllist!=null) {
	        	for(int i=0;i<xhtmllist.size();i++) {
	        		parameterlist.clear();
					parameterlist.add(xhtmllist.get(i).getName());
					sb.setLength(0);
	        		getDetailFile(sb, xhtmllist.get(i), inputStream, sc);
	              	parameterlist.add(sb.toString());
	              	parameterlist.add(Integer.toString((i+1)));
					line = mangeLink(9);
				    tmpint = Excecute.updateinsert(line, stmt,linkingCon);
	        	}
	        	parameterlist.clear();
	        	parameterlist.add(xhtml.toString());
	        	parameterlist.add(font.toString());
	        	parameterlist.add(contentopf.toString());
	        	parameterlist.add(image.toString());
	        	line = mangeLink(10);
			    tmpint = Excecute.updateinsert(line, stmt, linkingCon);
		     }
			 return true;
		 }catch(Exception ex) {
			 return false;
		 }finally {
			 line=null; folder =null;sc=null;inputStream=null;
			 files =null; childfile=null;field=null;
			 childfile1=null;  xhtmllist = null;
			 image=null;  xhtml=null;  font=null;    contentopf=null;
			 sb =null;  byteImage=null;
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
				case 5:
					sb.append("insert into epub(idlink, mimetype) values(");
					sb.append(parameterlist.get(0)).append(",'").append(parameterlist.get(1)).append("');");
					break;
				case 6:
					sb.append("insert into epub_meta_inf(idlink");
					for(int i=0;i<parameterlist.size()/2;i++) {
						sb.append(",");
						sb.append(parameterlist.get(i*2));
					}
					
					sb.append(") values(").append(idlink);
					for(int i=0;i<parameterlist.size()/2;i++) {
						sb.append(",");
						sb.append("'").append(parameterlist.get(i*2+1)).append("'");
					}
					sb.append(");");
					break;
				case 7://oebps_css
					sb.append("insert into oebps_css(idlink,name,detail) values(");
					sb.append(idlink).append(",'").append(parameterlist.get(0)).append("','").append(parameterlist.get(1)).append("');");
					break;
				case 8://oebps_image
					sb.append("insert into oebps_image(idlink,name,detail) values(");
					sb.append(idlink).append(",'").append(parameterlist.get(0)).append("','").append(parameterlist.get(1)).append("');");
					break;
				case 9://oebps_xhtml
					sb.append("insert into oebps_xhtml(idlink,name,detail,seq) values(");
					sb.append(idlink).append(",'").append(parameterlist.get(0)).append("','").append(parameterlist.get(1)).append("',").append(parameterlist.get(2)).append(");");
					break;
				case 10://oebps
					sb.append("insert into oebps(idlink,xhtml,font,contentopf,imageoebps) values(");
					sb.append(idlink).append(",'").append(parameterlist.get(0)).append("',");
					sb.append("'").append(parameterlist.get(1)).append("'");
					sb.append(",'").append(parameterlist.get(2)).append("'");
					sb.append(",'").append(parameterlist.get(3)).append("'").append(");");
					break;	
				case 11://xhtml
					sb.append("select name , detail from oebps_xhtml where idlink=").append(idlink);
					break;
				
			}
			parameterlist.clear();
			return sb.toString();
		}
	 private byte [] ImageToByte(File file) throws FileNotFoundException{
	        FileInputStream fis = new FileInputStream(file);
	        ByteArrayOutputStream bos = new ByteArrayOutputStream();
	        byte[] buf = new byte[1024];
	        try {
	            for (int readNum; (readNum = fis.read(buf)) != -1;) {
	                bos.write(buf, 0, readNum);      
	                System.out.println("read " + readNum + " bytes,");
	            }
	        } catch (IOException ex) {
	        }
	        byte[] bytes = bos.toByteArray();
	     
	     return bytes; 
	    }
		private void getDetailFile(StringBuilder sb, File f, InputStream inputStream, Scanner sc) throws FileNotFoundException {
			String line=null;
			inputStream = new FileInputStream(f);
	  	    sc = new Scanner(inputStream, "UTF-8");
	  	    sb.setLength(0);
	  	    while (sc.hasNextLine()) {
	 	      line = sc.nextLine();
	 	      //System.out.println(line);
	 	      sb.append(line);
	 	    }
		}
		String regex = "(.)*(\\d)(.)*";
	    Pattern pattern = Pattern.compile(regex);
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
	                if (j < xyallline) {
	                    elementlinenumber = elementpage.getAsJsonObject().get("line_" + (j + 1));
	                    tmpjsonobj = elementlinenumber.getAsJsonObject();
	                    tmpstr = tmpjsonobj.get("xy").getAsString().trim(); 
	                    //dif = Math.abs(tmpstr.length() - line.trim().length());
	                    tmpjsonobj.addProperty("pdfbox", line);
	                    
	                    //tmpjsonobj.addProperty("unit", checkUnit(line));
	                   // tmpjsonobj.addProperty("dif", dif);
//	                    if (dif > 0) {
//	                        lslide="";
//	                        dif = tmpstr.indexOf(line.trim());
//	                        if (dif > 0) {
//	                            tmpjsonobj.addProperty("lslide", tmpstr.substring(0, dif));
//	                            lslide = tmpstr.substring(0, dif);
//	                        }
//	                        if (dif + line.trim().length() != tmpstr.length()) {
//	                            print("line pdf is "+line);
//	                            print("String "+tmpstr+" Dif is "+dif);
//	                            if(dif!=-1){
//	                                tmpjsonobj.addProperty("rslide", tmpstr.substring(lslide.length() + line.trim().length(), tmpstr.length()));
//	                            }
//	                        }
//	                    }
	                }
	                j++;
	            }
	            print("============ End of  Page "+(i+1));
	            reader = null;
	        }
	        jsonsb.append(jelement.toString());
//	        print("PDF Box sb : "+jsonsb.toString());
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
		private void print(String s) {
			System.out.println(s);
		}
}
