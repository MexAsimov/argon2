package org.agh.edu.argon;

public class Main {
    public static void main(String[] args) {
        String password = "";
        String salt = "";
        int parallelismDegree = 1;
        String tag = "";
        int memorySize = 4096;
        int noPasses = 1;
        Byte version = Byte.parseByte("0x13");
        String secretValue = "";
        String assocData = "";
        ArgonVariant type = ArgonVariant.ARGON2id;
        String result = ArgonAlgorithm.process(
                password, salt, parallelismDegree, tag, memorySize, noPasses, version, secretValue, assocData,type);
        System.out.println(result);
    }
}