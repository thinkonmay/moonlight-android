package com.limelight.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class BitReader {

    private final byte[] buf;
    private int pos;
    private boolean eof;

    public BitReader(byte[] b) {
        this.buf = b;
        this.pos = 0;
        this.eof = false;
    }

    public short readByte() {
        if (pos >= buf.length) {
            eof = true;
            return 0;
        }
        // In Java, byte is signed, so we use a short to hold the unsigned value
        return (short) (buf[pos++] & 0xFF);
    }

    public int readUint16() {
        return (readByte() << 8) | readByte();
    }

    public int readUint24() {
        return (readByte() << 16) | (readByte() << 8) | readByte();
    }

    public long readUint32() {
        return ((long) readByte() << 24) | ((long) readByte() << 16) | ((long) readByte() << 8) | readByte();
    }

    public int readUint16LE() {
        return readByte() | (readByte() << 8);
    }

    public long readUint32LE() {
        return readByte() | ((long) readByte() << 8) | ((long) readByte() << 16) | ((long) readByte() << 24);
    }

    public long readUint64LE() {
        ByteBuffer bb = ByteBuffer.wrap(readBytes(8));
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.getLong();
    }

    public byte[] readBytes(int n) {
        if (pos + n > buf.length) {
            eof = true;
            n = buf.length - pos;
        }
        byte[] b = new byte[n];
        for (int i = 0; i < n; i++) {
            b[i] = (byte) readByte();
        }
        return b;
    }

    public byte[] left() {
        return Arrays.copyOfRange(buf, pos, buf.length);
    }

    public boolean isEOF() {
        return eof;
    }
}