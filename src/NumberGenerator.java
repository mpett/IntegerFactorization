/**
 * Created with IntelliJ IDEA.
 * User: martinpettersson
 * Date: 2013-11-05
 * Time: 23:14
 * To change this template use File | Settings | File Templates.
 */

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Random;


public class NumberGenerator {
    private final static int BREAKPOINT = 90;
    public static void main(String[] args) throws FileNotFoundException{
        PrintWriter writer = new PrintWriter("test.in");
        Random random = new Random();

        for(int i = 2; i < BREAKPOINT; i++) {
            String word = "";
            for(int j = 0; j < i; j++) {
                int n = random.nextInt(9);
                word += "" + n;
            }
            writer.println(word);
        }

        for(int i = 0; i < (102-BREAKPOINT); i++) {
            String word = "";
            for(int j = 0; j < BREAKPOINT; j++) {
                int n = random.nextInt(9);
                word += "" + n;
            }
            writer.println(word);
        }

        writer.close();
    }
}
