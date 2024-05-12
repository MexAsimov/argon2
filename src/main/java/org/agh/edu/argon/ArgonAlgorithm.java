package org.agh.edu.argon;

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
                                 int noPasses, Byte version, String secretValue, String assocData, ArgonVariant type) {
        return "";
    }

    public static String process(String password, String salt, int parallelismDegree, String tag, int memorySize,
                                 int noPasses, Byte version, String secretValue, ArgonVariant type) {
        return ArgonAlgorithm.process(
                password, salt, parallelismDegree, tag, memorySize, noPasses, version, secretValue,null, type);
    }

    public static String process(String password, String salt, int parallelismDegree, String tag, int memorySize,
                                 int noPasses, Byte version, ArgonVariant type) {
        return ArgonAlgorithm.process(
                password, salt, parallelismDegree, tag, memorySize, noPasses, version, null,null, type);
    }

}
