package ukit.rest;

import java.io.BufferedReader;
import java.io.FileReader;

public class ReadFile {
	public static void main(String a[]) {
		try {
//			d:\\test\\jsonsb\\x2js.txt
			FileReader reader = new FileReader("d:\\test\\jsonsb\\x2js.txt");
			BufferedReader br = new BufferedReader(reader);
			String line;
			StringBuilder sb = new StringBuilder();
			while((line = br.readLine())!=null) {
				sb.append(line);
			}
			System.out.println(sb.toString());
			System.out.println("end");
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}
