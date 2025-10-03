package com.limelight.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class BitWriter {

    private final ByteArrayOutputStream stream;

    /**
     * Creates a new writer with an empty buffer.
     */
    public BitWriter() {
        this.stream = new ByteArrayOutputStream();
    }

    /**
     * Creates a new writer with an initial buffer.
     * @param initialBytes The initial bytes to write.
     */
    public BitWriter(byte[] initialBytes) {
        this.stream = new ByteArrayOutputStream();
        if (initialBytes != null) {
            writeBytes(initialBytes);
        }
    }

    /**
     * Writes a single byte to the buffer.
     * @param b The byte to write. Note: only the lower 8 bits of the int are written.
     */
    public void writeByte(int b) {
        stream.write(b);
    }

    /**
     * Writes a 16-bit integer in big-endian order.
     * @param v The 16-bit value to write.
     */
    public void writeUint16(int v) {
        writeByte(v >> 8);
        writeByte(v);
    }

    /**
     * Writes a 32-bit integer in big-endian order.
     * @param v The 32-bit value to write.
     */
    public void writeUint32(long v) {
        writeByte((int) (v >> 24));
        writeByte((int) (v >> 16));
        writeByte((int) (v >> 8));
        writeByte((int) v);
    }

    /**
     * Writes a 16-bit integer in little-endian order.
     * @param v The 16-bit value to write.
     */
    public void writeUint16LE(int v) {
        writeByte(v);
        writeByte(v >> 8);
    }

    /**
     * Writes a 32-bit integer in little-endian order.
     * @param v The 32-bit value to write.
     */
    public void writeUint32LE(long v) {
        writeByte((int) v);
        writeByte((int) (v >> 8));
        writeByte((int) (v >> 16));
        writeByte((int) (v >> 24));
    }

    /**
     * Writes a 64-bit integer in little-endian order.
     * @param v The 64-bit value to write.
     */
    public void writeUint64LE(long v) {
        writeByte((int) v);
        writeByte((int) (v >> 8));
        writeByte((int) (v >> 16));
        writeByte((int) (v >> 24));
        writeByte((int) (v >> 32));
        writeByte((int) (v >> 40));
        writeByte((int) (v >> 48));
        writeByte((int) (v >> 56));
    }

    /**
     * Writes an array of bytes to the buffer.
     * @param bytes The byte array to write.
     */
    public void writeBytes(byte[] bytes) {
        try {
            stream.write(bytes);
        } catch (IOException e) {
            // This exception is not expected to be thrown by ByteArrayOutputStream
            throw new RuntimeException("Error writing bytes to memory buffer", e);
        }
    }

    /**
     * Returns a copy of the bytes written to the buffer so far.
     * @return The byte array.
     */
    public byte[] toByteArray() {
        return stream.toByteArray();
    }

    /**
     * Returns the current size of the buffer.
     * @return The number of bytes written.
     */
    public int size() {
        return stream.size();
    }
}