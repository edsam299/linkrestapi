package ukitsd.editing.connection;

import java.util.Properties;

public class FileProperties {
	private static FileProperties instance = null;
	   protected FileProperties() {
	      // Exists only to defeat instantiation.
	   }
	   public static FileProperties getInstance() {
//		   System.out.println("instance "+instance);
	      if(instance == null) {
	         instance = new FileProperties();	      }
//	      System.out.println("instance "+instance);
	      return instance;
	   }

	   public Properties getConfigProperties( ) {
			Properties properties=null;
			try{
				properties = new Properties();
				properties.load(getClass().getClassLoader().getResourceAsStream("config.properties"));
				return properties;
			}catch(Exception e){
				return null;
			}finally{
				properties=null;
			}
	   }

}
