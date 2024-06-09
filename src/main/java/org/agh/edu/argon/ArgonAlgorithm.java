package org.agh.edu.argon;

import org.bouncycastle.crypto.digests.Blake2bDigest;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ArgonAlgorithm {
    /**
     * @param P             Message string - password for password hashing application (length < 2^32)
     * @param S             Salt unique for each password (recommended 16 bytes) (length < 2^32)
     * @param p             ParalellismDegree determines how many independent computational chains can be run (range 1..2^24-1)
     * @param T             Tag (length 4..2^32-1)
     * @param m             Memory size - integer value of memory size in bytes (range 8*{@code parallelismDegree}..2^32-1)
     * @param t             Number of passes used to tune the running time independently of the memory size) (range 1..2^32-1)
     * @param v             Version - number must be one byte 0x13
     * @param K             Secret value - optional argument (length < 2^32-1)
     * @param X             Associated data - optional argument for associated data (length < 2^32-1)
     * @param y             Type of algorithm - 0 for Argon2d, 1 for Argon2i, 2 for Argon 2id
     */

    public static byte[] process(byte[] P, byte[] S, int p, int T, int m, int t, int v, byte[] K, byte[] X, int y) {
        // number of slices
        int SL = 4;

        // Establish H_0 (Step 1)
        byte[] H0 = hashH0(p, T, m, t, v, y, P, S, K, X);
        // "Allocating memory" (Step 2) - setting size of computed byte arrays
        int q = memoryAlloc(p, m) / p;
        // Compute B array (Step 3,4,5,6)
        byte[][][] B = computeB(H0, t, p, q, y, SL);
//        // Compute result C (Step 7)
//        byte[] C = computeC(B, p, q);
        // Return hash (Step 8) as H'^T(C)
//        return vlHash(T, C);
        return H0;
    }

    public static byte[] process(byte[] password, byte[] salt, int parallelismDegree, int tag, int memorySize,
                                 int noPasses, int version, byte[] secretValue, int type) {
        return ArgonAlgorithm.process(
                password, salt, parallelismDegree, tag, memorySize, noPasses, version, secretValue,null, type);
    }

    public static byte[] process(byte[] password, byte[] salt, int parallelismDegree, int tag, int memorySize,
                                 int noPasses, int version, int type) {
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

    public static byte[] hashH0(int p, int T, int m, int t, int v, int y, byte[] P, byte[] S, byte[] K, byte[] X) {
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
        return useBlake2b(buffer, 64);
    }

    public static byte[] vlHash(int T, byte[] A) {
        if (T <= 64) {
            byte[] buffer = concatenate(
                    BasicOperations.LE32(T),
                    A);
            return useBlake2b(buffer, T);
        } else {
            int r = (int) (Math.ceil(T/32) - 2);
            byte[][] V = new byte[r+1][64];
            byte[][] W = new byte[r+1][32];
            byte[] buffer = concatenate(
                    BasicOperations.LE32(T),
                    A);
            V[0] = useBlake2b(buffer, 64);
            for (int i=1; i<r; i++) {
                V[i] = useBlake2b(V[i-1], 64);
            }
            V[r] = useBlake2b(V[r-1], T-(32*r));
            byte[] buffer_final = new byte[0];
            for (int i=0; i<r; i++) {
                System.arraycopy(V[i], 0, W[i], 0, 32);
                buffer_final = concatenate(
                        buffer_final,
                        W[i]);
            }
            return concatenate(buffer_final, V[r]);
        }
    }

    public static byte[][][] computeB(byte[] H0, int t, int p, int q, int y, int SL) {
        byte[][][] B = new byte[p][q][];
        int r = 0;


        for (int i=0; i<p; i++) {
            byte[] buffer = concatenate(
                    H0,
                    BasicOperations.LE32(0),
                    BasicOperations.LE32(i));
            B[i][0] = vlHash(1024, buffer);
            byte[] buffer2 = concatenate(
                    H0,
                    BasicOperations.LE32(1),
                    BasicOperations.LE32(i));
            B[i][1] = vlHash(1024, buffer2);
        }
        for (int slice=0;slice<4;slice++) {
            for (int i = 0; i < p; i++) {
                for (int j = 2; j < q; j++) {
                    IndexPair ip = computeDependOnType(B, y, r, i, j, q, t, p);
//                int l = ip.getX();
//                int z = ip.getY();
                    //B[i][j] = computeG(B[i][j-1], B[l][z]);
                }
            }
        }

//        if (t > 1) {
//            for (int pass = 1; pass < t; pass++) {
//                r = pass;
//                for (int i = 0; i < p; i++) {
//                    IndexPair ip = chooseAlgorithmType(B, y, r, i, 0, q, t, p);
//                    int l = ip.getX();
//                    int z = ip.getY();
//                    B[i][0] = BasicOperations.XOR(computeG(B[i][q - 1], B[l][z]), B[i][0]);
//                }
//                for (int i = 0; i < p; i++) {
//                    for (int j = 1; j < q; j++) {
//                        IndexPair ip = chooseAlgorithmType(B, y, r, i, j, q, t, p);
//                        int l = ip.getX();
//                        int z = ip.getY();
//                        B[i][j] = BasicOperations.XOR(computeG(B[i][j - 1], B[l][z]), B[i][j]);
//                    }
//                }
//            }
//        }
        return B;
    }

    private static IndexPair computeDependOnType(
            byte[][][] B, int y, int r, int l, int sl, int mp, int t, int p) {
        int J_1, J_2;
        int q=mp;
        if (y == 1 || (y == 2 && r == 0 && (sl == 0 || sl == 1))) {
            byte[] Z = concatenate(
                    BasicOperations.LE64(r),
                    BasicOperations.LE64(l),
                    BasicOperations.LE64(sl),
                    BasicOperations.LE64(mp),
                    BasicOperations.LE64(t),
                    BasicOperations.LE64(y));
            byte[][] tmp = new byte[(int) (q/(128L*sl))][1024];
            for (int k=1; k<(q/(128*sl)); k++) {
                tmp[k] = concatenate(
                        computeG(BasicOperations.generateZeros(1024),
                                computeG(
                                        BasicOperations.generateZeros(1024),
                                        concatenate(Z, BasicOperations.LE64(k), BasicOperations.generateZeros(968))))
                );
            }
            J_1 = (int) BasicOperations.int32(BasicOperations.extract(tmp[0], 0));
            J_2 = (int) BasicOperations.int32(BasicOperations.extract(tmp[0], 1));
        } else if (y == 0 || y == 2) {
            System.out.println(Arrays.toString(B[l][sl - 1]));
//            J_1 = (int) BasicOperations.int32(BasicOperations.extract(B[l][sl-1], 0));
//            J_2 = (int) BasicOperations.int32(BasicOperations.extract(B[l][sl-1], 1));
//        } else {
//            throw new RuntimeException("Invalid algorithm type");
        }
//        int indexL = J_2 % p; // for first pass and first slice block is taken from current lane
//        int[] WArray = determineSetW(l, sl, q, indexL).stream().mapToInt(Integer::intValue).toArray();
//        int z = selectBlockFromW(WArray, J_1);

        return new IndexPair(1, 1);
    }

    private static Set<Integer> determineSetW(int l, int SL, int q, int indexL) {
        Set<Integer> W = new HashSet<>();
        if (l == indexL) {
            for (int seg=0; seg<SL-1; seg++) {
                for (int block=0; block<q/SL; block++) {
                    W.add(seg * (q/SL) + block);
                }
            }

            for (int block = 0; block < 8; block++) {
                if (block != 0) {
                    W.add((SL - 1) * (q / SL) + block - 1);
                }
            }
        } else {
            for (int seg=0; seg<SL-1; seg++) {
                for (int block=0; block<q/SL; block++) {
                    W.add(seg * (q/SL) + block);
                }
            }
            if (true) { // remove last index if B[i][j] is the first block of a segment
                W.remove((SL-1)*(q/SL) + 8 - 1);
            }
        }
        return W;
    }

    private static int selectBlockFromW(int[] W, int J1) {
        double x = Math.pow((double) J1, 2) / Math.pow(2, 32);
        double y = (W.length * x) / Math.pow(2, 32);
        int zz = W.length - 1 - (int)y;
        return W[zz];
    }

    private static byte[] computeC(byte[][][] B, int p, int m) {
        int q = memoryAlloc(p, m) / p;
        byte[] C = B[0][q-1];

        for (int i=1; i<p; i++) {
            C = BasicOperations.XOR(C, B[i][q-1]);
        }

        return C;
    }

    private static byte[] computeG(byte[] X, byte[] Y) {
        byte[] R = new byte[1024];
        for(int i=0; i<1024; i++) {
            R[i] = (byte) (X[i]^Y[i]);
        }

        byte[][] RMatrix = new byte[64][16];
        for(int i=0; i<64; i++) {
            System.arraycopy(R, i*16, RMatrix[i], 0, 16);
        }

        // RowMatrix - used to apply permutation for each row from RMatrix
        byte[][] RowMatrix = new byte[64][16];
        for (int i=0; i<8; i++) {
            byte[][] row = new byte[8][16];
            for (int j=0; j<8; j++) {
                row[j] = RMatrix[i*8+j];
            }
            byte[][] resultRow = permutationP(row);
            for (int j=0; j<8; j++) {
                RowMatrix[i*8+j] = resultRow[j];
            }
        }

        // ColumnMatrix - used to apply permutation for each column from RowMatrix
        byte[][] ColumnMatrix = new byte[64][16];
        for (int i=0; i<8; i++) {
            byte[][] column = new byte[8][16];
            for(int j=0; j<8; j++) {
                column[j] = RowMatrix[j*8+i];
            }
            byte[][] resultColumn = permutationP(column);
            for (int j=0; j<8; j++) {
                ColumnMatrix[j*8+i] = resultColumn[j];
            }
        }

        // Finally, G output Z XOR R, where ColumnMatrix is Z and RMatrix is R
        byte[] G = new byte[1024];
        for (int i=0; i < 64; i++) {
            for (int j=0; j<16; j++) {
                G[i*16+j] = (byte) (ColumnMatrix[i][j] ^ RMatrix[i][j]);
            }
        }

        return G;
    }

    private static byte[][] permutationP(byte[][] S) {
        long[][] matrix = new long[4][4];
        byte[][] result = new byte[8][16];
        for (int i=0; i<8; i++) {
            long v0 = BasicOperations.convertToLong(S[i], 0);
            long v1 = BasicOperations.convertToLong(S[i], 8);
            matrix[i/2][(i%2)*2] = v0;
            matrix[i/2][(i%2)*2+1] = v1;
        }

        applyPermutation(matrix);

        for (int i=0; i<4; i++) {
            for (int j=0; j<4; j++) {
                byte[] bytes = BasicOperations.convertToBytes(matrix[i][j], 8);
                System.arraycopy(bytes, 0, result[i*2+j/2], (j%2)*8, 8);
            }
        }

        return result;
    }

    private static void applyPermutation(long[][] matrix) {
        GB(matrix, 0, 4, 8, 12);
        GB(matrix, 1, 5, 9, 13);
        GB(matrix, 2, 6, 10, 14);
        GB(matrix, 3, 7, 11, 15);

        GB(matrix, 0, 5, 10, 15);
        GB(matrix, 1, 6, 11, 12);
        GB(matrix, 2, 7, 8, 13);
        GB(matrix, 3, 4, 9, 14);
    }

    private static void GB(long[][] v, int a, int b, int c, int d) {
        int ai = a / 4, aj = a % 4;
        int bi = b / 4, bj = b % 4;
        int ci = b / 4, cj = c % 4;
        int di = d / 4, dj = d % 4;
        long mod_value = 0xFFFFFFFFFFFFFFFFL;


        v[ai][aj] = (v[ai][aj] + v[bi][bj] + 2 * BasicOperations.trunc(v[ai][aj]) * BasicOperations.trunc(v[bi][bj])) & mod_value;
        v[di][dj] = BasicOperations.rightRot(v[di][dj]^v[ai][aj], 32);
        v[ci][cj] = (v[ci][cj] + v[di][dj] + 2 * BasicOperations.trunc(v[ci][cj]) * BasicOperations.trunc(v[di][dj])) & mod_value;
        v[bi][bj] = BasicOperations.rightRot(v[bi][bj]^v[ci][cj], 24);

        v[ai][aj] = (v[ai][aj] + v[bi][bj] + 2 * BasicOperations.trunc(v[ai][aj]) * BasicOperations.trunc(v[bi][bj])) & mod_value;
        v[di][dj] = BasicOperations.rightRot(v[di][dj]^v[ai][aj], 16);
        v[ci][cj] = (v[ci][cj] + v[di][dj] + 2 * BasicOperations.trunc(v[ci][cj]) * BasicOperations.trunc(v[di][dj])) & mod_value;
        v[bi][bj] = BasicOperations.rightRot(v[bi][bj]^v[ci][cj], 63);
    }

    private static int memoryAlloc(int p, int m) {
        return (int) (4 * p * Math.floor(m / (4*p)));
    }

    private static byte[] useBlake2b(byte[] buffer, int digestSize) {
        final Blake2bDigest digest = new Blake2bDigest(digestSize*8);
        digest.update(buffer, 0, buffer.length);
        final byte[] out = new byte[digestSize*8];
        digest.doFinal(out, 0);
        return Arrays.copyOf(out, digestSize);
    }

}
