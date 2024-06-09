package org.agh.edu.argon;

import java.nio.charset.StandardCharsets;

public class Main {
    public static void main(String[] args) {
        // password
        byte[] P = "Password".getBytes();
        // salt
        byte[] S = "salt".getBytes();
        // parallelismDegree
        int p = 1;
        // tag
        int T = 32;
        // memory size
        int m = 4096;
        // number of passes
        int t = 3;
        // version
        int v = 0x13;
        // Secret value
        byte[] K = "".getBytes();
        // Associated data
        byte[] X = "".getBytes();
        // type - 0 (Argon2d), 1 (Argon2i), 2 (Argon2id)
        int y = 0;





        byte[] result = ArgonAlgorithm.process(
               P, S, p, T, m, t, v, K, X, y);
        System.out.println(new String(result, StandardCharsets.UTF_8));
        System.out.println("Test");
    }
}