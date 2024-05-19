package org.agh.edu.argon;

import org.agh.edu.argon.BasicOperations.*;
import org.bouncycastle.crypto.digests.Blake2bDigest;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ArgonAlgorithm {
    /**
     * @param password          Message string - password for password hashing application (length < 2^32)
     * @param salt              Unique for each password (recommended 16 bytes) (length < 2^32)
     * @param parallelismDegree Determines how many independent computational chains can be run (range 1..2^24-1)
     * @param tag               Tag (length 4..2^32-1)
     * @param memorySize        Integer value of memory size in bytes (range 8*{@code parallelismDegree}..2^32-1)
     * @param noPasses          Used to tune the running time independently of the memory size) (range 1..2^32-1)
     * @param version           Number must be one byte 0x13
     * @param secretValue       Optional argument (length < 2^32-1)
     * @param assocData         Optional argument for associated data (length < 2^32-1)
     * @param type              Type of algorithm - enum for Argon2d, Argon2i, Argon2id
     */

    public static String process(String password, String salt, int parallelismDegree, String tag, int memorySize,
                                 int noPasses, Byte version, String secretValue, String assocData, int type) {


        return "";
    }

    public static String process(String password, String salt, int parallelismDegree, String tag, int memorySize,
                                 int noPasses, Byte version, String secretValue, int type) {
        return ArgonAlgorithm.process(
                password, salt, parallelismDegree, tag, memorySize, noPasses, version, secretValue,null, type);
    }

    public static String process(String password, String salt, int parallelismDegree, String tag, int memorySize,
                                 int noPasses, Byte version, int type) {
        return ArgonAlgorithm.process(
                password, salt, parallelismDegree, tag, memorySize, noPasses, version, null,null, type);
    }

    private static byte[] concatenate(byte[]... arrays) {
        int length = 0;
        for (byte[] array : arrays) {
            length += array.length;
        }
        byte[] result = new byte[length];
        int pos = 0;
        for (byte[] array : arrays) {
            System.arraycopy(array, 0, result, pos, array.length);
            pos += array.length;
        }
        return result;
    }

    public static byte[] hashH0(int p, int T, int m, int t, int v, int y, byte[] P, byte[] S, byte[] K, byte[] X) throws NoSuchAlgorithmException {
        byte[] buffer = concatenate(
                BasicOperations.LE32(p),
                BasicOperations.LE32(T),
                BasicOperations.LE32(m),
                BasicOperations.LE32(t),
                BasicOperations.LE32(v),
                BasicOperations.LE32(y),
                BasicOperations.LE32(P.length), P,
                BasicOperations.LE32(S.length), S,
                BasicOperations.LE32(K.length), K,
                BasicOperations.LE32(X.length), X);
        final Blake2bDigest digest = new Blake2bDigest(512);
        digest.update(buffer, 0, buffer.length);
        final byte[] out = new byte[digest.getDigestSize()];
        digest.doFinal(out, 0);
        return out;
    }

}
