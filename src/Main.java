import java.io.FileNotFoundException;
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
    private static Kattio io = new Kattio(System.in, System.out);
    private static boolean failed,test = false;
    private final static BigInteger TWO  = new BigInteger("2");
    private final static SecureRandom r = new SecureRandom();
    private static long a,b = 0;
    private static ArrayList<BigInteger> factors;
    private final static long TIME_LIMIT = 214444444;

    public static void main(String[] args) {

        if(args.length != 0) {
            if(args[0].equals("test"))
                test = true;
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

            FileReader reader = new FileReader("test.in");
            BufferedReader br = new BufferedReader(reader);
            String word;

            long start = System.nanoTime();
            while((word = br.readLine()) != null) {
                total++;
                factors = new ArrayList<BigInteger>();
                failed = false;
                BigInteger n = new BigInteger(word);

                if(n.isProbablePrime(5)) {
                    continue;
                }

                if(word.length() <= 10)
                    factor(n);
                else
                    factorRho(n);

                if(failed) {
                    continue;
                } else {
                    score++;
                }

                continue;

            }

            long end = System.nanoTime();
            long elapsedTime = end - start;
            double seconds = (double)elapsedTime / 1000000000.0;

            System.out.println("Managed to factor " + score +  " numbers out of " + total + ".");
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

            if(n.isProbablePrime(5)) {
                System.out.println(n + "\n");
                continue;
            }

            if(word.length() <= 10)
                factor(n);
            else
                factorRho(n);

            if(failed) {
                fail();
                continue;
            }

            output(factors);

            continue;
        }
    }

    public static BigInteger pollardRho(BigInteger n) {
        BigInteger d;
        BigInteger c  = new BigInteger(n.bitLength(), r);
        BigInteger x  = new BigInteger(n.bitLength(), r);
        BigInteger y = x;

        if (n.mod(TWO).compareTo(BigInteger.ZERO) == 0)
            return TWO;

        x = x.multiply(x).mod(n).add(c).mod(n);
        y = y.multiply(y).mod(n).add(c).mod(n);
        y = y.multiply(y).mod(n).add(c).mod(n);
        d = x.subtract(y).gcd(n);

        long startTime = System.nanoTime();
        while(d.compareTo(BigInteger.ONE) == 0) {
           if(System.nanoTime() - startTime > TIME_LIMIT) {
                failed = true;
                break;
            }

            x = x.multiply(x).mod(n).add(c).mod(n);
            y = y.multiply(y).mod(n).add(c).mod(n);
            y = y.multiply(y).mod(n).add(c).mod(n);
            d = x.subtract(y).gcd(n);
        }

        return d;
    }

    public static void factorRho(BigInteger n) {
        if (n.compareTo(BigInteger.ONE) == 0 || failed) return;
        if (n.isProbablePrime(20)) {
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

    private static void fail() {
        System.out.println("fail\n");
    }

    private static ArrayList<Long> longFactor(long n) {
        long d = 2;

        ArrayList<Long> longList = new ArrayList<Long>();

        while(n > 1) {
            while (n % d == 0) {
                longList.add(d);
                n /= d;
            }
            d++;
            if((d*d) > n) {
                if(n > 1)
                    longList.add(n);
                break;
            }
        }

        return longList;
    }

    private static void factor(BigInteger n) {
        BigInteger d = new BigInteger("2");

        long startTime = System.nanoTime();
        while(n.compareTo(BigInteger.ONE) == 1) {
            if(System.nanoTime() - startTime > TIME_LIMIT) {
                failed = true;
                return;
            }

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

    private static void fermat(long n) {
        long s = (long) Math.sqrt((double)n);
        long u = (2 * s)  + 1;
        long v = 1;
        long r = (s * s) - n;


        while(r != 0) {
            while (r > 0) {
                r -= v;
                v += 2;
            }
            while (r < 0) {
                r += u;
                u += 2;
            }
        }

        a = (u + v - 2) / 2;
        b = (u - v) / 2;
    }

    // Output 0 iff 2^n-1 is prime.
    private static BigInteger lucasLehmer(BigInteger n) {
        BigInteger M = n.pow(2).subtract(new BigInteger("1"));
        BigInteger s = new BigInteger("4");

        for(int i = 2; i <= n.intValue(); i++)
            s = (s.multiply(s).subtract(new BigInteger("2"))).mod(M);

        return s;
    }

    private static BigInteger maximalPowerFactorization(BigInteger n, BigInteger d) {
        // e denotes maximum power
        BigInteger e = new BigInteger("0");
        BigInteger f = n;
        while(f.mod(d).equals(new BigInteger("0"))) {
            f = f.divide(d);
            e = e.add(new BigInteger("1"));
        }

        return f;
    }

    private static ArrayList<BigInteger>  pollardRho2(BigInteger n) {
        BigInteger d = new BigInteger("1");
        BigInteger x = new BigInteger("2");
        BigInteger y = new BigInteger("2");

        ArrayList<BigInteger> bigList = new ArrayList<BigInteger>();

        while(d.equals(BigInteger.ONE)) {
            x = f(x,n);
            y = f(f(y,n),n);
            BigInteger g = x.subtract(y);
            g = g.abs();
            d = bigGCD(g, n);
        }

        if(d.equals(n)) {
            System.out.println("fail");

        }

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

    private static long gcd(long a, long b) {
        if(a == 0 || b == 0)
            throw new NumberFormatException("Input parameters must not be 0.");

        while (b != 0) {
            long tmp = b;
            b = a % b;
            a = tmp;
        }

        return a;
    }
}
