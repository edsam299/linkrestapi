package ukit.service;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author pudn_sorn
 */
public class Interchange {
    public static boolean checkU2E(String s){
        //true = unicode
         Pattern p = Pattern.compile("\\\\u([0-9A-Fa-f]{4})");
        String res =null;Matcher m =null;
        try {
            if (s == null || s.isEmpty()) {
                return false;
            }
            res = s;m = p.matcher(res);
            if(m.find()) return true;
            else return false;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        } finally {
            p=null; m=null; res=null;
        }
    }
    public static String U2U(String s) {
        Pattern p = Pattern.compile("\\\\u([0-9A-Fa-f]{4})");
        String res =null;Matcher m =null;
        try {
            if (s == null || s.isEmpty()) {
                return "";
            }
            res = s;m = p.matcher(res);
            while (m.find()) {
                res = res.replaceAll("\\" + m.group(0),
                java.lang.Character.toString((char) Integer.parseInt(m.group(1), 16)));
            }
            return res;
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        } finally {
            p=null; m=null; res=null;
        }

        
    }//endof u2u
     public static String convert(String str) {// roman and other 2 unicode
        
        StringBuffer ostr = new StringBuffer();

        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);

//            if ((ch >= 0x0020) && (ch <= 0x007e)) // Does the char need to be converted to unicode? 
//            {
//                ostr.append(ch);					// No.
//            } else // Yes.
//            {
                ostr.append("\\u");				// standard unicode format.
                String hex = Integer.toHexString(str.charAt(i) & 0xFFFF);	// Get hex value of the char. 
                for (int j = 0; j < 4 - hex.length(); j++) // Prepend zeros because unicode requires 4 digits
                {
                    ostr.append("0");
                }
                ostr.append(hex.toLowerCase());		// standard unicode format.
                //ostr.append(hex.toLowerCase(Locale.ENGLISH));
//            }
        }
  
      
        return new String(ostr);		//Return the stringbuffer cast as a string.

    }//end of convert
}
