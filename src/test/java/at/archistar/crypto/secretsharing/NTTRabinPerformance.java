package at.archistar.crypto.secretsharing;

import at.archistar.crypto.decode.DecoderFactory;
import at.archistar.crypto.decode.ErasureDecoderFactory;
import at.archistar.crypto.exceptions.WeakSecurityException;
import at.archistar.crypto.math.GFFactory;
import at.archistar.crypto.math.bc.BCGF256;
import at.archistar.crypto.math.gf256.GF256;
import at.archistar.crypto.math.gf257.GF257;
import at.archistar.crypto.math.gf257.GF257Factory;
import at.archistar.crypto.math.ntt.AbstractNTT;
import at.archistar.crypto.math.ntt.NTTDit2;
import at.archistar.crypto.math.ntt.NTTSlow;
import at.archistar.crypto.math.ntt.NTTTextbook;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 *
 * @author andy
 */
@RunWith(value = Parameterized.class)
public class NTTRabinPerformance {
    
    private final String description;
    private final byte[][][] input;
    private final SecretSharing algorithm;
    
    private static final int TEST_SIZE = 4 * 1024 * 1024;
    
     private static byte[][] createArray(int size, int elementSize) {
        byte[][] result = new byte[size / elementSize][elementSize];

        for (int i = 0; i < size / elementSize; i++) {
            for (int j = 0; j < elementSize; j++) {
                result[i][j] = 1;
            }
        }

        return result;
    }
    
    @Parameterized.Parameters
    public static Collection<Object[]> data() throws WeakSecurityException, NoSuchAlgorithmException {
        
        System.err.println("Data-Size per Test: " + TEST_SIZE/1024/1024 + "MByte");

        byte[][][] secrets = new byte[4][][];
        secrets[0] = createArray(TEST_SIZE, 4 * 1024);       // typical file system block size
        secrets[1] = createArray(TEST_SIZE, 128 * 1024);     // documents
        secrets[2] = createArray(TEST_SIZE, 512 * 1024);     // documents, pictures (jpegs)
        secrets[3] = createArray(TEST_SIZE, 4096 * 1024);    // audio, high-quality pictures

        final int n = 4;
        final int k = 3;

        GFFactory gffactory = new GF257Factory();
        int generator = 3;
        AbstractNTT nttSlow = new NTTSlow(gffactory.createHelper());
        AbstractNTT nttTextbook = new NTTTextbook(gffactory.createHelper());
        DecoderFactory decoderFactory = new ErasureDecoderFactory(gffactory);
        
        Object[][] data = new Object[][]{
           {"rabin, gf256", secrets, new RabinIDS(n, k, decoderFactory, new GF256())},
           {"rabin, bcgf256", secrets, new RabinIDS(n, k, decoderFactory, new BCGF256())},
           {"rabin, gf257", secrets, new RabinIDS(n, k, decoderFactory, new GF257())},
           {"ntt-rabin, gf257, NTTSlow", secrets, new NTTRabinIDS(n, k, generator, gffactory, nttSlow, decoderFactory)},
           {"ntt-rabin, gf257, NTTTextbook", secrets, new NTTRabinIDS(n, k, generator, gffactory, nttTextbook, decoderFactory)},
           {"ntt-rabin, gf257, NTTDit2", secrets, new NTTRabinIDS(n, k, generator, gffactory, new NTTDit2(gffactory.createHelper()), decoderFactory)},
        };

        return Arrays.asList(data);
    }

    public NTTRabinPerformance(String description, byte[][][] input, SecretSharing algorithm) {
        this.description = description;
        this.input = input;
        this.algorithm = algorithm;
    }

    @Test
    public void testPerformanceEncode() throws Exception {

        for (int i = 0; i < input.length; i++) {
            double sumShare = 0;
            
            for (byte[] data : this.input[i]) {
                /* test construction */
                long beforeShare = System.currentTimeMillis();
                algorithm.share(data);
                long betweenOperations = System.currentTimeMillis();

                sumShare += (betweenOperations - beforeShare);
            }
            double fragmentSize = ((double)this.input[i][0].length)/1024;
            double testSize = fragmentSize * this.input[i].length;
            double shareSpeed = testSize / (sumShare / 1000.0);
        
            System.err.format("Performance(%s, %.1fkB fragment size): share: %.3fkByte/sec\n", description, fragmentSize, shareSpeed);
        }
    }

}
