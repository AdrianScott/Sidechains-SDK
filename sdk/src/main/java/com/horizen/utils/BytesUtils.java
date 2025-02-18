package com.horizen.utils;

import com.google.common.io.BaseEncoding;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.google.common.primitives.Shorts;
import javafx.util.Pair;
import scala.Int;

import java.util.Arrays;
import java.util.Collection;

public final class BytesUtils {
    private BytesUtils() {}

    // Get Short value from byte array starting from an offset position without copying an array
    public static short getShort(byte[] bytes, int offset) {
        if(offset < 0 || bytes.length < offset + 2)
            throw new IllegalArgumentException("Value is out of array bounds");

        return Shorts.fromBytes(bytes[offset],
                                bytes[offset + 1]);
    }

    // Get Reversed Short value from byte array starting from an offset position without copying an array
    public static short getReversedShort(byte[] bytes, int offset) {
        if(offset < 0 || bytes.length < offset + 2)
            throw new IllegalArgumentException("Value is out of array bounds");

        return Shorts.fromBytes(bytes[offset + 1],
                                bytes[offset]);
    }

    // Get Int value from byte array starting from an offset position without copying an array
    public static int getInt(byte[] bytes, int offset) {
        if(offset < 0 || bytes.length < offset + 4)
            throw new IllegalArgumentException("Value is out of array bounds");

        return Ints.fromBytes(  bytes[offset],
                                bytes[offset + 1],
                                bytes[offset + 2],
                                bytes[offset + 3]);
    }

    // Get Reversed Int value from byte array starting from an offset position without copying an array
    public static int getReversedInt(byte[] bytes, int offset) {
        if(offset < 0 || bytes.length < offset + 4)
            throw new IllegalArgumentException("Value is out of array bounds");

        return Ints.fromBytes(  bytes[offset + 3],
                                bytes[offset + 2],
                                bytes[offset + 1],
                                bytes[offset]);
    }

    // Get Long value from byte array starting from an offset position without copying an array
    public static long getLong(byte[] bytes, int offset) {
        if(offset < 0 || bytes.length < offset + 8)
            throw new IllegalArgumentException("Value is out of array bounds");

        return Longs.fromBytes( bytes[offset],
                                bytes[offset + 1],
                                bytes[offset + 2],
                                bytes[offset + 3],
                                bytes[offset + 4],
                                bytes[offset + 5],
                                bytes[offset + 6],
                                bytes[offset + 7]);
    }

    // Get Reversed Long value from byte array starting from an offset position without copying an array
    public static long getReversedLong(byte[] bytes, int offset) {
        if(offset < 0 || bytes.length < offset + 8)
            throw new IllegalArgumentException("Value is out of array bounds");

        return Longs.fromBytes( bytes[offset + 7],
                                bytes[offset + 6],
                                bytes[offset + 5],
                                bytes[offset + 4],
                                bytes[offset + 3],
                                bytes[offset + 2],
                                bytes[offset + 1],
                                bytes[offset]);
    }

    // get Bitcoin VarInt value, which length is from 1 to 9 bytes, starting from an offset position without copying an array.
    public static VarInt getVarInt(byte[] bytes, int offset) {
        if(offset < 0 || bytes.length < offset + 1)
            throw new IllegalArgumentException("Value is out of array bounds");

        byte first = bytes[offset];
        switch(first) {
            case (byte)253:
                return new VarInt(BytesUtils.getShort(bytes, offset + 1), 3);

            case (byte)254:
                return new VarInt(BytesUtils.getInt(bytes, offset + 1), 5);

            case (byte)255:
                return new VarInt(BytesUtils.getLong(bytes, offset + 1), 9);

            default:
                return new VarInt(first, 1);
        }
    }

    // get Reversed Bitcoin VarInt value, which length is from 1 to 9 bytes, starting from an offset position without copying an array.
    // Note: ReversedVarInt is stored like VarInt, but "value" part is in little endian (reversed)
    public static VarInt getReversedVarInt(byte[] bytes, int offset) {
        if(offset < 0 || bytes.length < offset + 1)
            throw new IllegalArgumentException("Value is out of array bounds");

        byte first = bytes[offset];
        switch(first) {
            case (byte)253:
                return new VarInt(BytesUtils.getReversedShort(bytes, offset + 1), 3);

            case (byte)254:
                return new VarInt(BytesUtils.getReversedInt(bytes, offset + 1), 5);

            case (byte)255:
                return new VarInt(BytesUtils.getReversedLong(bytes, offset + 1), 9);

            default:
                return new VarInt(first & 0xFF, 1);
        }
    }

    // Get byte array from VarInt value
    public static byte[] fromVarInt(VarInt vi) {
        byte[] res = new byte[vi.size()];
        switch (vi.size()) {
            case 1:
                res[0] = (byte) (vi.value() & 255L);
                break;

            case 3:
                res[0] = (byte)253;
                res[1] = (byte) (vi.value() & 255L);
                res[2] = (byte) ((vi.value() >> 8) & 255L);
                break;

            case 5:
                res[0] = (byte)254;
                res[1] = (byte) (vi.value() & 255L);
                res[2] = (byte) ((vi.value() >> 8) & 255L);
                res[3] = (byte) ((vi.value() >> 16) & 255L);
                res[4] = (byte) ((vi.value() >> 24) & 255L);
                break;

            case 9:
                res[0] = (byte)255;
                res[1] = (byte) (vi.value() & 255L);
                res[2] = (byte) ((vi.value() >> 8) & 255L);
                res[3] = (byte) ((vi.value() >> 16) & 255L);
                res[4] = (byte) ((vi.value() >> 24) & 255L);
                res[5] = (byte) ((vi.value() >> 32) & 255L);
                res[6] = (byte) ((vi.value() >> 40) & 255L);
                res[7] = (byte) ((vi.value() >> 48) & 255L);
                res[8] = (byte) ((vi.value() >> 56) & 255L);
                break;

            default: throw new IllegalArgumentException("Incorrect size of VarInt had been detected:" + vi.size());
        }
        return res;
    }

    // Get reversed copy of byte array
    public static byte[] reverseBytes(byte[] bytes) {
        byte[] res = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++)
            res[i] = bytes[bytes.length - 1 - i];
        return res;
    }

    // Get byte array from hex string;
    public static byte[] fromHexString(String hex) {
        return BaseEncoding.base16().lowerCase().decode(hex.toLowerCase());
    }

    // Get hex string representation of byte array
    public static String toHexString(byte[] bytes) {
        return BaseEncoding.base16().lowerCase().encode(bytes);
    }

    public static boolean contains(Collection<byte[]> collection, byte[] value) {
        for (byte [] v : collection) {
            if (Arrays.equals(v, value))
                return true;
        }
        return false;
    }
}
