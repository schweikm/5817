import org.apache.commons.codec.binary.Base64;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SwingGUI {
/*
	public static void main(String[] args) {
		writeStringToFile("A quick brown fox jumped over the lazy dog");
		readStringFromFile();
	}
*/
	public static void writeStringToFile(String str) {
		try {
			// The encodeBase64 method take a byte[] as the parameter. The byte[] 
			// can be from a simple string like in this example or it can be from
			// an image file data.
			byte[] encoded = Base64.encodeBase64(str.getBytes());
			
			// write the encoded string to file
			BufferedWriter writer = new BufferedWriter(new FileWriter("Base64.txt"));
			
			// Print the encoded string
			String encodedString = new String(encoded);
			writer.write(encodedString);
			writer.close();
		}
		catch(IOException iox) {
			System.err.println("Failed to write file:  " + iox.getMessage());
		}
	}
	
	public static void readStringFromFile() {
		try {
			// Decode a previously encoded string using decodeBase64 method and
			// passing the byte[] of the encoded string.
			BufferedReader reader = new BufferedReader(new FileReader("Base64.txt"));
			byte[] decoded = Base64.decodeBase64(reader.readLine().getBytes());
			reader.close();

			// Convert the decoded byte[] back to the original string and print
			// the result.
			String decodedString = new String(decoded);
			System.out.println(decodedString);
		}
		catch (IOException iox) {
			System.err.println("Failed to read file:  " + iox.getMessage());
		}
	}
}
