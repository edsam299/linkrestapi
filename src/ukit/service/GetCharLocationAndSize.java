package ukit.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
 
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

 
/**
* This is an example on how to get the x/y coordinates and size of each character in PDF
*/
public class GetCharLocationAndSize extends PDFTextStripper {

    String path=""; int line =0; int seq=0;int page=0; 
    double maxhinline =0;double maxyinline =0; double minxinline=0;
    double minhinline=0;double minsizeinline=0; double maxsizeinline=0;
    Map<Integer,Integer> linepattern=null; StringBuilder sb = null;
    ArrayList<String> contentline = null; 
    int[] yline={0,0};
    ArrayList<String> tmparrlist = null;
    double xdouble =0; 
    ArrayList<String> content = null;  ArrayList<Integer> hposition=null; 
    ArrayList<Integer> xposition=null; ArrayList<Integer> yposition=null; 
    String tmptype = ""; Gson gson = null;
    
    
    Map<Integer,Integer> mappage = null; Map<String, int[]> mapline=null;
    Map<String,ArrayList<String>> mapleft = null;Map<String,ArrayList<String>> mapright = null;
    Map<String,ArrayList<String>> mapcontent = null;Map<String,ArrayList<String>> mapft = null;
    
    public StringBuilder jsonxyhsb =null;
    
    Map<Integer,Integer> positionxinpage=null;
    StringBuilder key=null; StringBuilder value = null;

    public StringBuilder getJsonXYH(){
        return jsonxyhsb;
    }
    public GetCharLocationAndSize(String path, int allpage, PDDocument document) throws IOException  {
        this.path=path;
        linepattern =new HashMap(); 
        this.setLinePattern();
        int DEFAULT_USER_SPACE_UNIT_DPI = LinkingAdmin.DEFAULT_USER_SPACE_UNIT_DPI;
        /** see PDPage.MM_TO_UNITS (private !!!!) */
        float MM_TO_UNITS = 1 / (10 * 2.54f) * DEFAULT_USER_SPACE_UNIT_DPI;
        float pdrectangle1 = 176 * MM_TO_UNITS;
        float pdrectangle2 = 250 * MM_TO_UNITS;
        content = new ArrayList<String>(); 
        hposition = new ArrayList<Integer>();
        xposition = new ArrayList<Integer>();
        yposition = new ArrayList<Integer>();
        key = new StringBuilder(); 
        value =new StringBuilder();
        positionxinpage = new HashMap();
        gson = new Gson();
        jsonxyhsb = new StringBuilder();
        
        Writer dummy=null; PDPage pagee=null;
        
        jsonxyhsb.append("{"); sb = new StringBuilder();
        jsonxyhsb.append("\"").append("allpage").append("\"").append(":").append(allpage).append(",");
        
        for(int i=0; i<document.getNumberOfPages(); i++) {
            if(positionxinpage.size()>1) {
                positionxinpage.clear();
                value.setLength(0);key.setLength(0);
            }
            pagee = (PDPage) document.getDocumentCatalog().getPages().get((i));
            pagee.setMediaBox(new PDRectangle(pdrectangle1,pdrectangle2));
            this.setSortByPosition( true );
            this.setStartPage( (i+1) );
            this.setEndPage( (i+1) );
            dummy = new OutputStreamWriter(new ByteArrayOutputStream());
            //System.out.println(pagee.getMediaBox().getWidth());
            //System.out.println(pagee.getMediaBox().getHeight());
            xdouble = 0; line=1; page = i+1; seq=0; sb.setLength(0); maxyinline=0;maxhinline=0;minxinline=0;
            jsonxyhsb.append("\"page_").append(page).append("\":{");
            this.writeText(document, dummy);
            //jsonxyhsb.append(",");
            //jsonxyhsb.append("\"seq\":").append(seq).append(",");
            jsonxyhsb.append("\"xy\":\"").append(sb.toString()).append("\"");
//            jsonxyhsb.append("\",");
//            jsonxyhsb.append("\"maxhinline\":").append(maxhinline).append(",");
//            jsonxyhsb.append("\"minhinline\":").append(minhinline).append(",");
//            jsonxyhsb.append("\"minxinline\":").append(minxinline).append(",");
//            jsonxyhsb.append("\"minsizeinline\":").append(minsizeinline).append(",");            
//            jsonxyhsb.append("\"maxsizeinline\":").append(maxsizeinline).append(",");
//            jsonxyhsb.append("\"maxyinline\":").append(maxyinline);
            jsonxyhsb.append("}");
            jsonxyhsb.append(",");
            jsonxyhsb.append("\"xyallline\":").append(line).append("}");
            if(i<allpage-1){jsonxyhsb.append(",");}
        }
        jsonxyhsb.append("}");
        content=null; hposition=null; xposition=null;yposition=null; gson=null;
        linepattern=null;dummy=null;pagee=null;
        key=null; value =null;
    }
//    @Override
    int tmpint=0;
    protected void writeString(String string, List<TextPosition> textPositions) throws IOException {
//        System.out.println(string);
        for (TextPosition text : textPositions) {
//            System.out.println(text.getUnicode()+ " [(X=" + text.getXDirAdj() + ",Y=" +
//                    text.getYDirAdj() + ") height=" + text.getHeightDir() + " width=" +
//                    text.getWidthDirAdj() + "]   "+text.getPageHeight()+" , "+text.getPageWidth()+" , "+text.getFontSize()+
//                    ", "+text.getYScale()+" , "+text.getXScale()+" , "+text.getX()+", "+text.getY()+", "+text.getEndY()+", "+text.getFontSizeInPt());
            tmpint = (int)text.getX();
            if(positionxinpage.containsKey(tmpint)){
                positionxinpage.put(tmpint, positionxinpage.get(tmpint)+1);
            }else{
                positionxinpage.put(tmpint, 1);
            }
            if(xdouble==0){
               maxyinline=text.getYDirAdj();
               minxinline = text.getXDirAdj();
               maxhinline=text.getHeightDir();
               minsizeinline = text.getFontSizeInPt();
               maxsizeinline =  text.getFontSizeInPt(); 
               minhinline = text.getHeightDir();
               jsonxyhsb.append("\"line_").append(line).append("\":{");
//               jsonxyhsb.append("\"line_detail\":{");
            }else{
                if(text.getXDirAdj()<xdouble || text.getYDirAdj()>(maxyinline+maxhinline)){
                	//jsonxyhsb.append(",");
                    //jsonxyhsb.append("\"seq\":").append(seq).append(",");
                    jsonxyhsb.append("\"xy\":\"").append(sb.toString()).append("\"");
//                    jsonxyhsb.append(",");
//                    jsonxyhsb.append("\"maxhinline\":").append(maxhinline).append(",");
//                    jsonxyhsb.append("\"minhinline\":").append(minhinline).append(",");
//                    jsonxyhsb.append("\"minxinline\":").append(minxinline).append(",");
//                    jsonxyhsb.append("\"minsizeinline\":").append(minsizeinline).append(",");            
//                    jsonxyhsb.append("\"maxsizeinline\":").append(maxsizeinline).append(",");
//                    jsonxyhsb.append("\"maxyinline\":").append(maxyinline);
                    jsonxyhsb.append("},");
                    minxinline=text.getXDirAdj();
                    seq = 0; sb.setLength(0);maxyinline=text.getYDirAdj();maxhinline=text.getHeightDir();
                    line= line+1;
                     jsonxyhsb.append("\"line_").append(line).append("\":{");
//                    jsonxyhsb.append("\"line_detail\":{");
                }
            }
            seq = seq+1;
            if(maxyinline<text.getYDirAdj()){
                maxyinline=text.getYDirAdj();
            }
            minsizeinline=(text.getFontSizeInPt()<minsizeinline?text.getFontSizeInPt():minsizeinline);
            maxsizeinline=(text.getFontSizeInPt()>maxsizeinline?text.getFontSizeInPt():maxsizeinline);   
            minhinline=(text.getHeightDir()<minhinline?text.getHeightDir():minhinline);
            maxhinline=(text.getHeightDir()>maxhinline?text.getHeightDir():maxhinline);
//            if(seq>1) jsonxyhsb.append(",");
            sb.append(text.getUnicode());
//            jsonxyhsb.append("\"seq_").append(seq).append("\":[");
//            jsonxyhsb.append("\"").append(text.getUnicode()).append("\",");
//            jsonxyhsb.append(text.getXDirAdj()).append(",");
//            jsonxyhsb.append(text.getYDirAdj()).append(",");
//            jsonxyhsb.append(text.getHeight()).append(",");
//            jsonxyhsb.append(text.getFontSizeInPt()).append(",");
//            jsonxyhsb.append(text.getWidth()).append("]");
            xdouble = text.getXDirAdj();
        }
        //print(new Gson().toJson(contentline));
    }
    private int getLine(double x, double y){
        int tmpy  = (int) y;
        if(linepattern.containsKey(tmpy)){
            return linepattern.get(tmpy);
        }else{
            return 0;
        }
    }
    private void setLinePattern(){
        linepattern.put(59, -1);
        linepattern.put(86, 1);
    }
    private void print(String s) {
		System.out.println(s);
	}
     /**
     * @throws IOException If there is an error parsing the document.
     */
   
//     @Override
//    public void writeString_(String string, List<TextPosition>textPositions){
//        for (TextPosition text : textPositions) {
//            System.out.println(text.getUnicode()+ " [(X=" + text.getXDirAdj() + ",Y=" +
//                    text.getYDirAdj() + ") height=" + text.getHeightDir() + " width=" +
//                    text.getWidthDirAdj() + "]");
//            
//        }
//    }
    
    
}