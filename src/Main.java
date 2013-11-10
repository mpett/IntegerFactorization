import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.security.SecureRandom;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.BitSet;

/**
 * Created with IntelliJ IDEA.
 * User: martinpettersson
 * Date: 2013-10-26
 * Time: 23:40
 * To change this template use File | Settings | File Templates.
 */
public class Main {
    // Constants
    private final static SecureRandom   r = new SecureRandom();
    private final static BigInteger     ZERO  = BigInteger.ZERO;
    private final static BigInteger     ONE  = BigInteger.ONE;
    private final static BigInteger     TWO  = new BigInteger("2");
    private final static String         TEST_FILE = "test.in";
    private final static double         NANOSECONDS_TO_SECONDS = 1000000000.0;
    private final static long           TIME_LIMIT = 214444444;
    private final static int            BITLENGTH_BREAKPOINT = 30;
    private final static int            CERTAINTY_FACTOR = 5;
    // Fields
    private static Kattio io = new Kattio(System.in, System.out);
    private static ArrayList<BigInteger> factors;
    private static boolean failed, test, matrix = false;
    private static boolean[] markedRows;

    public static void main(String[] args) {
        if(args.length != 0) {
            if(args[0].equals("test")) test = true;
            if(args[0].equals("matrix")) matrix = true;
        }
        if(test) {
            try {
                testFactoring();
            } catch (IOException e) {
                System.err.println("File not found");
            }
        } else if(matrix) {
            int rows = io.getInt();
            int cols = io.getInt();
            boolean[][] inputMatrix = new boolean[rows][cols];
            for(int i = 0; i < rows; i++) {
                for(int j = 0; j < cols; j++) {
                    if(io.getInt() == 0)
                        inputMatrix[i][j] = false;
                    else
                        inputMatrix[i][j] = true;
                }
            }

            for(int i = 0; i < rows; i++) {
                for(int j = 0; j < cols; j++) {
                    System.err.print(inputMatrix[i][j] + " ");
                } System.err.println();
            }

            BitSet[] bitArray = gaussGF2(inputMatrix);
            outputGauss(bitArray, inputMatrix[0].length);
        }
        else
            kattisFactoring();
        io.close();
    }

    public static void testFactoring() throws IOException {
        System.out.println("------- TEST MODE -------\n");
        System.out.println("Factoring...");
        int score = 0;
        int total = 0;
        int omg = 0;
        String word;
        BufferedReader br = new BufferedReader(new FileReader(TEST_FILE));
        long start = System.nanoTime();
        while((word = br.readLine()) != null) {
            total++;
            factors = new ArrayList<BigInteger>();
            failed = false;
            BigInteger n = new BigInteger(word);
            // Input integer is already prime: Stop here.
            // if(n.isProbablePrime(CERTAINTY_FACTOR)) continue;
            // Break to Pollard's Rho if input integer is more than 10 digits long.
            if(n.bitLength() <= BITLENGTH_BREAKPOINT) trialDivision(n); else factorRho(n);
            if(failed) {
                testOut("Failed", total);
                continue;
            } else {
                testOut("Success! :D", total);
                score++;
            }
        }
        long end = System.nanoTime();
        double seconds = (double)(end - start) / NANOSECONDS_TO_SECONDS;
        System.out.println("\nManaged to factor " + score +  " numbers out of " + total + ".");
        System.out.println("Total time for factoring " + total + " numbers: " + seconds + " seconds.");
        System.out.println("OMG: " + omg);
        System.out.println("\n------- TEST END --------");
        br.close();
    }

    public static void kattisFactoring() {
        while(io.hasMoreTokens()) {
            factors = new ArrayList<BigInteger>();
            failed = false;
            String word = io.getWord();
            BigInteger n = new BigInteger(word);
            // Input integer is already prime: Stop here.
            if(n.isProbablePrime(CERTAINTY_FACTOR)) { System.out.println(n + "\n"); continue; }
            // Break to Pollard's Rho if input integer is more than 10 digits long.
            if(n.bitLength() <= BITLENGTH_BREAKPOINT) trialDivision(n); else factorBrent(n);
            if(failed) { fail(); continue; } else output(factors);
        }
    }

    public static BigInteger pollardRho(BigInteger n) {
        BigInteger d = ONE;
        BigInteger c  = new BigInteger(n.bitLength(), r);
        BigInteger x  = new BigInteger(n.bitLength(), r);
        BigInteger y = x;
        if (n.mod(TWO).compareTo(ZERO) == 0) return TWO;
        long startTime = System.nanoTime();
        while(d.compareTo(ONE) == 0) {
            if(System.nanoTime() - startTime > TIME_LIMIT) { failed = true; break; }
            x = x.multiply(x).mod(n).add(c).mod(n);
            y = y.multiply(y).mod(n).add(c).mod(n);
            y = y.multiply(y).mod(n).add(c).mod(n);
            d = x.subtract(y).gcd(n);
        }
        return d;
    }

    public static void factorRho(BigInteger n) {
        if (n.compareTo(ONE) == 0 || failed) return;
        // Return if we have found a non-trivial factor.
        if (n.isProbablePrime(CERTAINTY_FACTOR)) {
            factors.add(n);
            return;
            // Branch to trial division if n is small.
        } else if (n.bitLength() <= BITLENGTH_BREAKPOINT) {
            trialDivision(n);
            return;
        }
        // Recursion!
        BigInteger d = pollardRho(n);
        factorRho(d);
        factorRho(n.divide(d));
    }

    public static void factorBrent(BigInteger n) {
        if (n.compareTo(ONE) == 0 || failed) return;
        // Return if we have found a non-trivial factor.
        if (n.isProbablePrime(CERTAINTY_FACTOR)) {
            factors.add(n);
            return;
        // Branch to trial division if n is small.
        } else if (n.bitLength() <= BITLENGTH_BREAKPOINT) {
            trialDivision(n);
            return;
        }
        // Recursion!
        BigInteger d = pollardRhoBrent(n);
        factorBrent(d);
        factorBrent(n.divide(d));
    }

    public static BigInteger pollardRhoBrent(BigInteger n) {
        if (n.mod(TWO).compareTo(BigInteger.ZERO) == 0) return TWO;
        BigInteger y = new BigInteger(n.bitLength(), r);
        BigInteger c = new BigInteger(n.bitLength(), r);
        BigInteger m = new BigInteger(n.bitLength(), r);
        BigInteger g = ONE;
        BigInteger r = ONE;
        BigInteger q = ONE;
        BigInteger ys = ONE;
        long startTime = System.nanoTime();
        while(g.compareTo(ONE) == 0) {
            if(System.nanoTime() - startTime > TIME_LIMIT) { failed = true; break; }
            BigInteger x = y;
            for(BigInteger i = ZERO; i.compareTo(r) < 0; i = i.add(ONE))
                y = y.multiply(y).mod(n).add(c).mod(n);
            BigInteger k = ZERO;
            while(k.compareTo(r) < 0 && g.compareTo(ONE) == 0) {
                if(System.nanoTime() - startTime > TIME_LIMIT) { failed = true; break; }
                ys = y;
                for(BigInteger i = ZERO; i.compareTo(m.min(r.subtract(k))) < 0; i = i.add(ONE)) {
                    y = y.multiply(y).mod(n).add(c).mod(n);
                    q = q.multiply(x.subtract(y)).mod(n);
                }
                g = q.gcd(n);
                k = k.add(m);
            }
            r = r.multiply(TWO);
            if(g.compareTo(n) == 0) {
                while(true) {
                    ys = ys.multiply(ys).mod(n).add(c).mod(n);
                    g = x.subtract(ys).gcd(n);
                    if(g.compareTo(ONE) > 0)
                        break;
                }
            }
        }
        return g;
    }

    private static void trialDivision(BigInteger n) {
        BigInteger d = TWO;
        long startTime = System.nanoTime();
        while(n.compareTo(ONE) == 1) {
            if(System.nanoTime() - startTime > TIME_LIMIT) { failed = true; return; }
            while(n.mod(d).equals(ZERO)) {
                factors.add(d);
                n = n.divide(d);
            }
            d = d.add(ONE);
            if(d.multiply(d).compareTo(n) == 1) {
                if(n.compareTo(ONE) == 1)
                    factors.add(n);
                break;
            }
        }
    }

    private static BitSet[] gaussGF2(boolean[][] inputMatrix) {
        BitSet[] bitArray = new BitSet[inputMatrix[0].length];
        for(int i = 0; i < inputMatrix[0].length; i++) {
            bitArray[i] = new BitSet(inputMatrix.length);
            for(int j = 0; j < inputMatrix.length; j++) {
                if(inputMatrix[j][i]) {
                    bitArray[i].set(j);
                }
            }
        }
        markedRows = new boolean[inputMatrix.length];
        for(int col = 0; col < bitArray.length; col++) {
            int nextSetBit = bitArray[col].nextSetBit(0);
            if(nextSetBit == -1) continue;
            markedRows[nextSetBit] = true;
            for(int c = 0; c < bitArray.length; c++) {
                if(c == col) continue;
                if(bitArray[c].get(nextSetBit)) bitArray[c].xor(bitArray[col]);
            }
        }
        return bitArray;
    }

    private static void outputGauss(BitSet[] bitArray, int bitlength) {

        for(int i = 0; i < bitlength; i++) {
            for(int j = 0; j < bitArray.length; j++) {
                if(bitArray[j].get(i))
                    System.out.print(1 + " ");
                else
                    System.out.print(0 + " ");
            } System.out.println(" " + markedRows[i]);
        }
    }

    private static void output(ArrayList<BigInteger> bigList) {
        for(BigInteger n : bigList) {
            System.out.println(n);
        }
        System.out.println();
    }

    private static void testOut(String condition, int total) { System.out.println("Test case #" + total + ": " + condition); }

    private static void fail() { System.out.println("fail\n"); }
}
