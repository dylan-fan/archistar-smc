package at.archistar.crypto.math.gf257;

import at.archistar.crypto.math.GF;

/**
 *
 * @author andy
 */
public class GF257 implements GF {
    
    private static final int invTable[];
    
    private static final int exp[][];
    
    static {
        invTable = new int[257];
        for (int i = 0; i < 257; i++) {
            invTable[i] = calcInverse(i);
        }
        
        exp = new int[257][257];
        
        for (int i = 0; i < 257; i++) {
            exp[i][0] = 1;
            for (int j = 1; j < 257; j++) {
                exp[i][j] = (exp[i][j-1] * i) % 257;
            }
        }
    }

    @Override
    public int add(int a, int b) {
        return (a+b)%257;
    }

    @Override
    public int sub(int a, int b) {
        int tmp = a -b;
        return ((tmp < 0) ? (tmp + this.getFieldSize()) : tmp)%257;
    }

    @Override
    public int mult(int a, int b) {
        return (a*b)%257;
    }

    @Override
    public int pow(int a, int b) {
        return exp[a][b];
    }

    @Override
    public int div(int a, int b) {
        return mult(a, inverse(b));
    }

    @Override
    public int evaluateAt(int[] coeffs, int x) {
        int degree = coeffs.length -1;
        
        int result = coeffs[degree];
        for (int i = degree - 1; i >= 0; i--) {
            result = add(mult(result, x), coeffs[i]);
        }
        return result;

    }
    
    private static int calcInverse(int a) {
        int t = 0;
        int newt = 1;
        int r = 257;
        int newr = a;
        
        while (newr != 0) {
            int quotient = r / newr;
            int tmp = (t - quotient * newt)%257;
            t = newt;
            newt = tmp;
            
            tmp = (r - quotient * newr)%257;
            r = newr;
            newr = tmp;
        }
        
        if (r > 1 && a != 0) {
            throw new RuntimeException("not invertiable");
        }
        
        if (t < 0) {
            t = t + 257;
        }

        return t;
    }

    @Override
    public int inverse(int a) {
        assert (a != 0);
        return invTable[a];
    }

    @Override
    public int getFieldSize() {
        return 257;
    }
    
    private static final int primitiveRoots[][] = {
        {2, 256},
        {4, 241},
        {8, 64},
        {16, 249},
        {32, 136},
        {64, 81},
        {128, 9},
        {256, 3}
    };
    
    public int primitiveRootOfUnity(int a) {
        for (int[] primitiveRoot : primitiveRoots) {
            if (primitiveRoot[0] == a) {
                return primitiveRoot[1];
            }
        }
        assert(false);
        /* this can never happen -- assertion should be called before */
        return 0;
    }
}
