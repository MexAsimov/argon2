package org.agh.edu.argon;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class BasicOperations {

    private static final Charset charset = StandardCharsets.ISO_8859_1; // 1 character = 1 byte
    private static final int rotationBase = 64;
    /*
    Done by default: x^y (Math.pow()), a*b, c-d, g / h, K || L, a XOR b, a mod b, floor, ceil, |A|
     DONE:
      a >>> n (rotation string by n bits) x
      trunc (truncate 64-bit value to 32 least significiant x
      extract(a, i) = the i-th set of 32 bits from bitstring a, starting from 0-th x
      LE32(a) - convert 32-bit integer a to byte string in little endian x
      LE64(a) - convert 64-bit integer a to byte string in little endian x
      int32(s) - 32-bit string s is converted to non-negative integer in little endian x
      int64(s) - 64-bit string s is converted to non-negative integer in little endian x
      zero(P) - generate P-byte zero string x
     */

    public static byte[] leftRot(byte[] s, int i) {
        long value = convertToLong(s, 8);
        i = i % rotationBase;
        return convertToByte((value << i) & 0xFFFFFFFFL | (value >>> (rotationBase-i)), 8);
    }
    public static byte[] rightRot(byte[] s, int i) {
        long value = convertToLong(s, 8);
        i = i % rotationBase;
        return convertToByte((value >>> i) | ((value << (rotationBase-i)) & 0xFFFFFFFFL), 8);
    }

    public static long trunc(long a) {
        long mask = 0xFFFFFFFFL;

        return (a & mask);
    }

    public static long extract(long a, int i) {
        int start = i * 32;
        long mask = 0xFFFFFFFFL << start;

        return (a & mask) >>> start;
    }

    public static byte[] LE32(long a) {
        return convertToByte(a, 4);
    }

    public static byte[] LE64(long a) {
        return convertToByte(a, 8);
    }


    public static long int32(byte[] s) {
        return convertToLong(s, 4);
    }

    public static long int64(byte[] s) {
        return convertToLong(s, 8);
    }

    public static String generateZeroString(int p) {
        byte[] zeroBytes = new byte[p];
        return new String(zeroBytes, charset);
    }

    private static long convertToLong(byte[] byteArray, int noBytes) {

        if (byteArray.length != noBytes) {
            throw new IllegalArgumentException("Illegal string length - should be " + noBytes);
        }

        ByteBuffer buffer = ByteBuffer.wrap(byteArray);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        return buffer.getLong();
    }

    private static byte[] convertToByte(long value, int noBytes) {
        ByteBuffer buffer = ByteBuffer.allocate(noBytes); //
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        try {
            if (noBytes == 8) {
                buffer.putLong(value);
            } else if (noBytes == 4){
                buffer.putInt((int) value);
            }
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Illegal long size - " + value + " - should be " + noBytes);
        }
        buffer.flip();
        byte[] byteArray = buffer.array();

//        StringBuilder sb = new StringBuilder();
//        for (byte b : byteArray) {
//            sb.append(String.format("%02X", b));
//        }

        return byteArray;
    }
}
