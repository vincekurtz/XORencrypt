import java.util.Scanner;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.lang.Character;

/**
 * A simple interface for encrypting messages with XOR and exploiting weaknesses in
 * XOR encryption to guess the key. Completed for Programming II assginment 1 at
 * Goshen College. 
 *
 * @author  Vince Kurtz
 * @version 1.0
 * @since 2016-09-17
 */
public class XORencrypt {
    // Set encoding for the message
    static Charset encoding = StandardCharsets.US_ASCII;

    /**
     * Parses command line arguments to execute the proper programs, failing
     * gracefully on bad input. 
     *
     * @param args  command line arguments passed by user
     */
    public static void main(String[] args) {
        try {
            if ( args[0].toLowerCase().equals("readtest")) {
                readTest(args[1]);
            } else if ( args[0].equals("xor")) {
                xor(args[1], args[2]);
            } else if (args[0].equals("analyze")) {
                analyze(args[1], args[2]);
            } else if (args[0].equals("crack")) {
                crack(args[1]);
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

    /** Prints basic usage information */
    public static void help() {
        System.out.println("Usage:\n"
                + "    help -- get help\n"
                + "    readTest <filepath> -- cat a file\n"
                + "    xor <filepath> <cipher> -- XOR text in file with cipher\n"
                + "    analyze <filepath> <num buckets> -- give character frequencies for text in file for each bucket\n"
                + "    crack <filepath> -- attempt to decrypt the given encrypted file\n");
    }

    /** 
     * Prints the contents of the specified file to stdout 
     *
     * @param fpath path of the file that will be printed
     * */
    public static void readTest(String fpath) {
        String contents = readFile(fpath);
        System.out.print(contents);  // Use print instead of println to avoid adding an extra newline
    }

    /**
     * Encrypts the contents of a given file by XORing each bit with the bits
     * of a given cipher. Prints the resulting encrypted message to stdout.
     *
     * @param fpath     path to the file that contains the message to encrypted
     * @param cipher    string used as key/password
     */
    public static void xor(String fpath, String cipher) {
        String message = readFile(fpath);
        String encrypted = xorStrings(message, cipher);

        System.out.print(encrypted);

    }

    /**
     * Exploits weaknesses in the XOR encryption scheme to guess characters
     * in the key. Reads an encrypted message from a file, splits the message
     * into a number of bins, and XORs the most common character from each bin
     * with a space to recover characters from the key.
     *
     * @param fpath path to the file that contains an encrypted message
     * @param bins  number of sections to split the message into; guessed length of the key
     *
     * @return a list of characters that are likely in the key (aka password aka cipher)
     */
    public static ArrayList<String> analyze(String fpath, String bins) {
        int N = Integer.parseInt(bins);  

        String encrypted = readFile(fpath);
        ArrayList<String> common = new ArrayList<String>(0);     // most common character in each bin
        ArrayList<String> decrypted = new ArrayList<String>(0);  // common characters xor-ed with " " => probably in the key

        common = binAnalysis(encrypted, N);

        // xor each most common character with " " to get a character in the key
        for (int i=0; i<common.size(); i++) {
            decrypted.add( xorStrings(common.get(i), " ") );
        }

        // print those characters which are (probably) in the key
        System.out.print("\nLikely characters include: ");
        System.out.println(decrypted);

        return decrypted;
    }

    /** 
     * Provides a unified interface to decrypt a message encrypted by an unknown
     * key, with the aid of user input.
     *
     * @param fpath path to the file that contains an encrypted message
     */
    public static void crack(String fpath) {
        // Java type checking requires these to be initialized before catch loops
        int bins = 0;
        String pass_guess = "";
        String decrypted = "";

        ArrayList<String> likely_chars;
        String encrypted = readFile(fpath);
        Scanner reader = new Scanner(System.in);

        // Ask user for number of bins
        try {
            System.out.print("How many password characters should I guess? (enter an integer): ");
            bins = reader.nextInt();
        }
        catch (Exception e) {
            System.out.println("\nError: Bad Input. Bin size must be an integer.");
            System.exit(0);
        }

        // Get likely key characters from the message with the given number of bins. 
        String N = bins + "";   // The analyze method must take bins as a string
        likely_chars = analyze(fpath, N);

        // Prompt user for password guess
        try {
            System.out.print("\nEnter a password guess: ");
            pass_guess = reader.next();
        }
        catch (Exception e) {
            System.out.println("\nError: Bad Input");
            System.exit(0);
        }

        // XOR guessed password with encrypted message
        System.out.println("\nAttempting decryption...");
        decrypted = xorStrings(encrypted, pass_guess);
        
        System.out.println("\n\n------------------------ Begin Decrypted Message -------------------------\n\n");
        System.out.print(decrypted);
        System.out.println("\n\n------------------------- End Decrypted Message --------------------------\n\n");
    }

    /* ----------------------------- Helper Functions -------------------------------------- */

    /**
     * Loads the contents of a file into a string.
     *
     * @param fpath path to the file that is to be loaded
     */
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
            return "failed";  // java type checking requires that a string be returned no matter what
        }
    }

    /**
     * Finds and returns the most common character in the given ASCII string.
     *
     * @param s the ASCII string
     */
    private static String mostCommon(String s) {
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

    /**
     * Encrypts a message by xor-ing all bits in the given message with 
     * bits in the given cipher.
     *
     * @param message   the message to be xored
     * @param cipher    the key to xor with the message
     *
     * @return the string resulting form the xor process
     */
    private static String xorStrings(String message, String cipher) {
        String ciphertxt = new String("");  // the cipher repeated until it's the length of the message
        String encrypted;
        byte[] cbite;  // byte versions of the cipher, message, and encrypted message
        byte[] mbite;
        byte[] ebite;

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
        cbite = ciphertxt.getBytes(encoding);
        mbite = message.getBytes(encoding);

        // XOR cipher and message strings
        ebite = new byte[mbite.length];
        for (int j=0; j < mbite.length; j++) {
            ebite[j] = (byte) (((int) cbite[j]) ^ ((int) mbite[j]));
        }

        // Get final encrypted message
        encrypted = new String(ebite, encoding);

        return encrypted;
    }

    /** 
     * Breaks the given string into N bins of (roughly) equal size,
     * returning the most common character in each bin.
     *
     * @param s the string to break into bins
     * @param N the number of bins
     *
     * @return the string broken into N bins, in an ArrayList
     */
    private static ArrayList<String> binAnalysis(String s, int N) {
        ArrayList<String> substrings = new ArrayList<String>();
        ArrayList<String> most_common = new ArrayList<String>();
        String temp_common;
        int bin_length = s.length() / N;

        // Break the string into bins
        int start = 0;
        int end = bin_length;
        for (int i=0; i<N; i++) {
            substrings.add(s.substring(start,end));
            start = start + bin_length;
            end = end + bin_length;
        }

        // Find the most common character in each bin
        for (String substr : substrings) {
            temp_common = mostCommon(substr);

            // Remove duplicates
            while (most_common.contains(temp_common)) {
                substr = substr.replace(mostCommon(substr), "");
                temp_common = mostCommon(substr);
            }
            most_common.add(temp_common);
        }

        return most_common;
    }
}
