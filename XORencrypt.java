import java.util.Scanner;
import java.util.Arrays;
import java.io.File;
import java.nio.file.Files;
import java.util.Base64;  // Includes encoder and decoder
import java.nio.charset.Charset;  // for ascii encoding
import java.nio.charset.StandardCharsets;

public class XORencrypt {
	public static void main(String[] args) {
		//printFile(args);
		
		try {
			if ( args[0].toLowerCase().equals("readtest")) {
				readTest(args[1]);
			} else if ( args[0].equals("xor")) {
				xor(args[1], args[2]);
			} else if (args[0].equals("analyze")) {
				analyze(args[1], args[2]);
			} else {
				help();
			}
		} catch (Error e) {
			// Fallback to the help function if args aren't right or something
			help();
			e.printStackTrace();
			
		}
	}

	public static void help() {
		System.out.println("Usage:\n"
				+ "    help -- get help\n"
				+ "    readTest <filepath> -- cat a file\n"
				+ "    xor <filepath> <cipher> -- XOR text in file with cipher\n"
				+ "    analyze <filepath> <num buckets> -- give character frequencies for text in file for each bucket");
	}
	
	public static void readTest(String fpath) {
		// Prints the contents of the specified file to stdout
		Scanner data = null;
		
		try {
			data = new Scanner(new File(fpath));
			while (data.hasNextLine()) {
				System.out.println(data.nextLine());
			}	
		} catch (Exception e){
			System.out.println("something went wrong with file reading");
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	public static void xor(String fpath, String cipher) {
		String message = readFile(fpath);

        byte[] bite = cipher.getBytes();
		System.out.println(Arrays.toString(bite));
		
		//c = a ^ b;
		
	}
	
	public static void analyze(String fpath, String buckets) {
		System.out.println("analysis method");
	}
	
	// HELPER FUNCTIONS

    private static String readFile(String fpath) {
        Charset encoding = StandardCharsets.US_ASCII;
		Scanner data = null;
		
		try {
            byte[] encoded = Files.readAllBytes(Paths.get(fpath));
            return new String(encoded, encoding);
		}
        catch (Exception e){
			System.out.println("something went wrong with file reading");
			e.printStackTrace();
			System.exit(0);
		}

    }
	
	private static int xorChar(char a, char b) {
		// Xor two characters and return the result
		int aInt = Character.getNumericValue(a);
		int bInt = Character.getNumericValue(b);
		int cInt = ( aInt ^ bInt);
		
		//c = (char) cInt;
		
		return cInt;
		
	}
}
