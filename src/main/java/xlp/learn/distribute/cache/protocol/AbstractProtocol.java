package xlp.learn.distribute.cache.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * byte数据类型是8位、有符号的，以二进制补码表示的整数； 最小值是-128（-2^7）； 最大值是127（2^7-1） 例子：byte a = 100，byte b = -50。
 * Created by lpxie on 2016/8/23.
 */
public abstract class AbstractProtocol implements Protocol {
    
    private static Logger logger = LoggerFactory.getLogger(AbstractProtocol.class);
    
    private final byte[] start = new byte[]{0011, 0012, 0013};
    
    private final byte[] end = new byte[]{0014, 0015, 0016};
    
    @Override
    public byte[] read(InputStream inputStream, byte[] bytes) throws IOException {
        
        byte[] dst = new byte[0];
        
        byte[] startOrEnd = new byte[3];
        byte[] lenByte = new byte[4];//length is 4 byte
        try {
            //check start
            inputStream.read(startOrEnd);
            boolean isStart = checkStart(startOrEnd);
            if (!isStart) {
                logger.warn("data is not completely,do not execute:" + new String(startOrEnd));
                return dst;
            }
            //check type
            inputStream.read(bytes);
            //uuid
            int uuidLength = AbstractType.uuidbyteslength;
            byte[] uuidByte = new byte[uuidLength];
            inputStream.read(uuidByte, 0, uuidLength);
            //read data
            inputStream.read(lenByte);
            int realLength = byteArrayToInt(lenByte);
            if (realLength > 0) {
                byte[] messByte = new byte[uuidLength + realLength];
                inputStream.read(messByte, uuidLength, realLength);//first is type
                //putMap uuid
                for (int i = 0; i < uuidLength; i++) {
                    messByte[i] = uuidByte[i];
                }
                dst = messByte;
            }
            //check end ,reuse sync
            inputStream.read(startOrEnd);
            boolean isEnd = checkEnd(startOrEnd);
            if (!isEnd) {
                logger.warn("not complete data");
                return new byte[0];
            }
        } catch (IOException e) {
            logger.warn("AbstractProtocol.read call wrong by " + ExceptionUtils.getStackTrace(e));
            throw new IOException(e);
        }
        return dst;
    }
    
    public boolean write(OutputStream outputStream, String message, byte[] types) {
        
        try {
            byte[] bytes = message.getBytes("utf-8");//
            write(outputStream, bytes, types);
            return true;
        } catch (UnsupportedEncodingException e) {
            logger.warn("AbstractProtocol.write message convert to byte by 'utf-8' failed by "
                            + ExceptionUtils.getStackTrace(e));
        }
        return false;
    }
    
    public boolean write(OutputStream outputStream, byte[] bytes, byte[] types) {
        
        try {
            outputStream.write(start);//同步校验位
            outputStream.write(types);
            outputStream.write(SelfType.uuid);
            
            write(outputStream, bytes);
            
            outputStream.write(end);
            outputStream.flush();
            //add md5 校验位 to do ...
            return true;
        } catch (IOException e) {
            logger.warn("AbstractProtocol.write byte array write to outputStream failed by "
                            + ExceptionUtils.getStackTrace(e));
        }
        return false;
    }
    
    private void write(OutputStream outputStream, byte[] bytes) throws IOException {
        
        int length = bytes.length;
        byte[] lengthByte = intToBytes(length);
        outputStream.write(lengthByte);
        outputStream.write(bytes);
    }
    
    private boolean checkStart(byte[] tmpSync) {
        
        if (tmpSync[0] == start[0] && tmpSync[1] == start[1] && tmpSync[2] == start[2]) {
            return true;
        }
        return false;
    }
    
    private boolean checkEnd(byte[] tmpSync) {
        
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
    
    public int byteArrayToInt(byte[] bytes) {
        
        int value = 0;
        for (int i = 0; i < 4; i++) {
            int shift = (3 - i) * 8;
            value += (bytes[i] & 0x000000ff) << shift;
        }
        return value;
    }
}
