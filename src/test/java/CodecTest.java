import xlp.learn.distribute.cache.handler.ByteToMessage;

public class CodecTest {
    
    public static void main(String[] args){
    
        ByteToMessage byteToMessage = new ByteToMessage();
        
        long id = 10;
        
        byte[] bytes = byteToMessage.long2bytes(id);
        
        long rid = byteToMessage.bytes2long(bytes);
        
        System.out.println(rid);
    }
}
