import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.security.SecureRandom;
import java.io.BufferedReader;
import java.io.FileReader;

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
    private final static BigInteger     TWO  = new BigInteger("2");
    private final static String         TEST_FILE = "test.in";
    private final static double         NANOSECONDS_TO_SECONDS = 1000000000.0;
    private final static long           TIME_LIMIT = 244444444;
    private final static int            BREAKPOINT = 10;
    private final static int            CERTAINTY_FACTOR = 20;
    // Fields
    private static Kattio io = new Kattio(System.in, System.out);
    private static ArrayList<BigInteger> factors;
    private static boolean failed, test = false;

    public static void main(String[] args) {
        if(args.length != 0) {
            if(args[0].equals("test")) test = true;
        }
        if(test) {
            try {
                testFactoring();
            } catch (IOException e) {
                System.err.println("File not found");
            }
        } else
            kattisFactoring();
        io.close();
    }

    public static void testFactoring() throws IOException {
            System.out.println("------- TEST MODE -------\n");
            System.out.println("Factoring...");
            int score = 0;
            int total = 0;
            String word;
            BufferedReader br = new BufferedReader(new FileReader(TEST_FILE));
            long start = System.nanoTime();
            while((word = br.readLine()) != null) {
                total++;
                factors = new ArrayList<BigInteger>();
                failed = false;
                BigInteger n = new BigInteger(word);
                // Input integer is already prime: Stop here.
                if(n.isProbablePrime(CERTAINTY_FACTOR)) continue;
                // Break to Pollard's Rho if input integer is more than 10 digits long.
                if(word.length() <= BREAKPOINT) trialDivision(n); else factorRho(n);
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
            if(word.length() <= BREAKPOINT) trialDivision(n); else factorRho(n);
            if(failed) { fail(); continue; }
            output(factors);
            continue;
        }
    }

    public static BigInteger pollardRho(BigInteger n) {
        BigInteger d = BigInteger.ZERO;
        BigInteger c  = new BigInteger(n.bitLength(), r);
        BigInteger x  = new BigInteger(n.bitLength(), r);
        BigInteger y = x;
        if (n.mod(TWO).compareTo(BigInteger.ZERO) == 0) return TWO;
        long startTime = System.nanoTime();
        do {
            if(System.nanoTime() - startTime > TIME_LIMIT) { failed = true; break; }
            x = x.multiply(x).mod(n).add(c).mod(n);
            y = y.multiply(y).mod(n).add(c).mod(n);
            y = y.multiply(y).mod(n).add(c).mod(n);
            d = x.subtract(y).gcd(n);
        } while(d.compareTo(BigInteger.ONE) == 0);
        return d;
    }

    public static void factorRho(BigInteger n) {
        if (n.compareTo(BigInteger.ONE) == 0 || failed) return;
        if (n.isProbablePrime(CERTAINTY_FACTOR)) {
            factors.add(n);
            return;
        }
        BigInteger d = pollardRho(n);
        factorRho(d);
        factorRho(n.divide(d));
    }

    private static void output(ArrayList<BigInteger> bigList) {
        for(BigInteger n : bigList) {
            System.out.println(n);
        }
        System.out.println();
    }

    private static void trialDivision(BigInteger n) {
        BigInteger d = TWO;
        long startTime = System.nanoTime();
        while(n.compareTo(BigInteger.ONE) == 1) {
            if(System.nanoTime() - startTime > TIME_LIMIT) { failed = true; return; }
            while(n.mod(d).equals(BigInteger.ZERO)) {
                factors.add(d);
                n = n.divide(d);
            }
            d = d.add(BigInteger.ONE);
            if(d.multiply(d).compareTo(n) == 1) {
                if(n.compareTo(BigInteger.ONE) == 1)
                    factors.add(n);
                break;
            }
        }
    }

    // Output 0 iff 2^n-1 is prime.
    private static BigInteger lucasLehmer(BigInteger n) {
        BigInteger M = n.pow(2).subtract(BigInteger.ONE);
        BigInteger s = new BigInteger("4");
        for(int i = 2; i <= n.intValue(); i++)
            s = (s.multiply(s).subtract(TWO)).mod(M);
        return s;
    }

    private static BigInteger maximalPowerFactorization(BigInteger n, BigInteger d) {
        // e denotes maximum power
        BigInteger e = BigInteger.ZERO;
        BigInteger f = n;
        while(f.mod(d).equals(BigInteger.ZERO)) {
            f = f.divide(d);
            e = e.add(BigInteger.ZERO);
        }
        return f;
    }

    private static ArrayList<BigInteger> pollardRho2(BigInteger n) {
        BigInteger d = BigInteger.ONE;
        BigInteger x = TWO;
        BigInteger y = TWO;
        ArrayList<BigInteger> bigList = new ArrayList<BigInteger>();
        while(d.equals(BigInteger.ONE)) {
            x = f(x,n);
            y = f(f(y,n),n);
            BigInteger g = x.subtract(y);
            g = g.abs();
            d = bigGCD(g, n);
        }
        if(d.equals(n)) fail();
        bigList.add(d);
        return bigList;
    }

    private static BigInteger f(BigInteger x, BigInteger n) {
        x = x.multiply(x);
        x = x.subtract(BigInteger.ONE.mod(n));
        return x;
    }

    private static BigInteger bigGCD(BigInteger a, BigInteger b) {
        if(a.equals(BigInteger.ZERO) || b.equals(BigInteger.ZERO))
            throw new NumberFormatException("Input parameters must not be 0.");
        while(!b.equals(BigInteger.ZERO)) {
            BigInteger tmp = b;
            b = a.mod(b);
            a = tmp;
        }
        return a;
    }

    private static void testOut(String condition, int total) { System.out.println("Test case #" + total + ": " + condition); }

    private static void fail() { System.out.println("fail\n"); }
}
