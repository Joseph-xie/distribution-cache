package xlp.learn.distribute.cache.handler;

public abstract class Codec {
    
    byte[] eof = {-1};
    
    final byte[] start = new byte[]{0011, 0012, 0013};
    
    final byte[] end = new byte[]{0014, 0015, 0016};
    
     public enum Result{
     
         NEED_MORE_INPUT,
         NORMAL;
    }
    
    boolean checkStart(byte[] tmpSync) {
        
        if (tmpSync[0] == start[0] && tmpSync[1] == start[1] && tmpSync[2] == start[2]) {
            return true;
        }
        return false;
    }
    
    boolean checkEnd(byte[] tmpSync) {
        
        if (tmpSync[0] == end[0] && tmpSync[1] == end[1] && tmpSync[2] == end[2]) {
            return true;
        }
        return false;
    }
    
    public byte[] intToBytes(int i) {
        
        byte[] bytes = new byte[4];
        bytes[0] = (byte) ((i >> 24) & 0xFF);
        bytes[1] = (byte) ((i >> 16) & 0xFF);
        bytes[2] = (byte) ((i >> 8) & 0xFF);
        bytes[3] = (byte) (i & 0xFF);
        return bytes;
    }
    
    public byte[] long2bytes(long v) {
        byte[] b = new byte[8];
        b[7] = (byte) v;
        b[6] = (byte) (v >>> 8);
        b[5] = (byte) (v >>> 16);
        b[4] = (byte) (v >>> 24);
        b[3] = (byte) (v >>> 32);
        b[2] = (byte) (v >>> 40);
        b[1] = (byte) (v >>> 48);
        b[0] = (byte) (v >>> 56);
        
        return b;
    }
    
    /**
     * to long.
     *
     * @param b byte array.
     * @return long.
     */
    public long bytes2long(byte[] b) {
        return bytes2long(b, 0);
    }
    
    /**
     * to long.
     *
     * @param b   byte array.
     * @param off offset.
     * @return long.
     */
    public long bytes2long(byte[] b, int off) {
        return ((b[off + 7] & 0xFFL) << 0) +
            ((b[off + 6] & 0xFFL) << 8) +
            ((b[off + 5] & 0xFFL) << 16) +
            ((b[off + 4] & 0xFFL) << 24) +
            ((b[off + 3] & 0xFFL) << 32) +
            ((b[off + 2] & 0xFFL) << 40) +
            ((b[off + 1] & 0xFFL) << 48) +
            (((long) b[off + 0]) << 56);
    }
    
    public int byteArrayToInt(byte[] bytes) {
        
        int value = 0;
        for (int i = 0; i < 4; i++) {
            int shift = (3 - i) * 8;
            value += (bytes[i] & 0x000000ff) << shift;
        }
        return value;
    }
}
