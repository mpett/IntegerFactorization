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
    public static void main(String[] args) throws FileNotFoundException{
        PrintWriter writer = new PrintWriter("test.in");
        Random random = new Random();

        for(int i = 2; i < 35; i++) {
            String word = "";
            for(int j = 0; j < i; j++) {
                int n = random.nextInt(9);
                word += "" + n;
            }
            writer.println(word);
        }

        for(int i = 0; i < 67; i++) {
            String word = "";
            for(int j = 0; j < 35; j++) {
                int n = random.nextInt(9);
                word += "" + n;
            }
            writer.println(word);
        }

        writer.close();
    }
}
