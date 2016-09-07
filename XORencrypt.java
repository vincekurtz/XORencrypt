import java.util.Scanner;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;  // Includes encoder and decoder
import java.nio.charset.Charset;  // for ascii encoding
import java.nio.charset.StandardCharsets;
import java.lang.Character;

public class XORencrypt {
    // Set encoding for the message
    static Charset encoding = StandardCharsets.US_ASCII;

    public static void main(String[] args) {
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
        } catch (ArrayIndexOutOfBoundsException e) {
            // Fallback to the help function if args aren't right
            System.out.println("ERROR: incorrect arguments.\n");
            help();
            e.printStackTrace();
        }
    }

    public static void help() {
        // Print basic usage information
        System.out.println("Usage:\n"
                + "    help -- get help\n"
                + "    readTest <filepath> -- cat a file\n"
                + "    xor <filepath> <cipher> -- XOR text in file with cipher\n"
                + "    analyze <filepath> <num buckets> -- give character frequencies for text in file for each bucket\n");
    }

    public static void readTest(String fpath) {
        // Print the contents of the specified file to stdout
        String contents = readFile(fpath);
        System.out.println(contents);
    }

    public static void xor(String fpath, String cipher) {
        // Encrypt the contents of the given file by XORing each bit with the given cipher.
        // Print the resulting encrypted message to stdout.

        // Read the input file
        String message = readFile(fpath);

        String encrypted = xorStrings(message, cipher);

        System.out.println(encrypted);

    }

    public static void analyze(String fpath, String bins) {
        int N = Integer.parseInt(bins);  

        String encrypted = readFile(fpath);
        ArrayList<String> buckets = new ArrayList<String>(0);
        ArrayList<String> common = new ArrayList<String>(0);
        ArrayList<String> decrypted = new ArrayList<String>(0);  // common characters xor-ed with " " => probably in the key

        // 13 most common characters in written English
        List<String> freq_chars = Arrays.asList(" ", "e", "t", "a", "o", "i", "n", "s", "h", "r", "d", "l", "u");

        common = freqAnalysis(encrypted, N);
        System.out.println(common);

        // xor each most common character with " " to get a character in the key
        for (int i=0; i<common.size(); i++) {
            decrypted.add( xorStrings(common.get(i), freq_chars.get(i)) );
        }

        //// print those characters which are (probably) in the key
        //System.out.println(common);
        System.out.println(decrypted);
    }

    private static String readFile(String fpath) {
        Scanner data = null;

        try {
            byte[] encoded = Files.readAllBytes(Paths.get(fpath));
            return new String(encoded, encoding);
        }
        catch (Exception e){
            System.out.println("ERROR: Something went wrong with file reading.\n");
            e.printStackTrace();
            System.exit(0);
            return "failed";  // java seems to require something to be returned even here. Is there a more elegant way?
        }
    }

    private static String mostCommon(String s) {
        // Find and return the most common character in the given (ASCII) string.

        String most_common;
        int count[] = new int[128];  // there are 128 ASCII characters
        int max_count = 0;
        int max = -1;
        char c;

        // populate an array with character counts
        for (int i=0; i<s.length(); i++) {
            c = s.charAt(i);
            count[c]++;
        }

        // pick the character with the highest count
        for (int i=0; i<128; i++) {
            if (count[i] > max_count) {
                max_count = count[i];
                max = i;
            }
        }

        most_common = Character.toString((char) max);  // convert int to string

        return most_common;
    }

    private static String xorStrings(String message, String cipher) {
        //Encrypt a message by xor-ing all bits with bits in the given cipher
        String ciphertxt = new String("");  // the cipher repeated until it's the length of the message
        String encrypted;

        // variables for generating the cipher text
        String nextletter;
        int n = 0;
        int i;

        // Loop the cipher so it matches the input file length
        while (ciphertxt.length() < message.length()) {
            i = n % cipher.length();

            nextletter = Character.toString(cipher.charAt(i));
            ciphertxt += nextletter;
            n++;
        }

        // Convert both cipher text and message strings to bytes
        byte[] cbite = ciphertxt.getBytes(encoding);
        byte[] mbite = message.getBytes(encoding);

        // XOR cipher and message strings
        byte[] ebite = new byte[mbite.length];
        for (int j=0; j < mbite.length; j++) {
            ebite[j] = (byte) (((int) cbite[j]) ^ ((int) mbite[j]));
        }

        // Get final encrypted message
        encrypted = new String(ebite, encoding);

        return encrypted;
    }

    private static ArrayList<String> freqAnalysis(String s, int N) {
        // Perform frequency analysis on the given string, returning the N most common characters
        // The most common characters in written English *should* be (space)-e-t-a-o-i-n-s-h-r-d-l-u.
        // Thus the most common characters in a (long enough) encrypted message should also correspond to
        // these characters. 

        ArrayList<String> common = new ArrayList<String>();
        String nth = "";  // stores nth most common character

        for (int i=0; i<N; i++) {
            nth = mostCommon(s);
            common.add(nth);
            s = s.replace(nth, "");  // remove this common character to find the next most common
        }

        return common;
    }
}
