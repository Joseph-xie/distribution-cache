package xlp.learn.distribute.cache.net;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xlp.learn.distribute.cache.manager.DetailLruManager;
import xlp.learn.distribute.cache.manager.RelationLruManager;
import xlp.learn.distribute.cache.point.Processor;
import xlp.learn.distribute.cache.protocol.AbstractType;
import xlp.learn.distribute.cache.protocol.SelfType;
import xlp.learn.distribute.cache.util.Utils;

/**
 * Created by lpxie on 2016/8/27.
 */
public class ClientProcessor implements Processor {
    
    private Logger logger = LoggerFactory.getLogger(ClientProcessor.class);
    
    public String processSelf(String key, String value, byte[] types) throws IOException {
        
        if (Utils.bytesEquals(types, SelfType.readDetail)) {
            return DetailLruManager.getManager().get(key);
        } else if (Utils.bytesEquals(types, SelfType.readRelation)) {
            return RelationLruManager.getManager().get(key);
        } else if (Utils.bytesEquals(types, SelfType.disableDetail)) {
            DetailLruManager.getManager().getMap().remove(key);
            return "200";
        } else if (Utils.bytesEquals(types, SelfType.disableRelation)) {
            RelationLruManager.getManager().getMap().remove(key);
            return "200";
        } else {
            if (Utils.bytesEquals(types, SelfType.writeDetail)) {
                DetailLruManager.getManager().put(key, value);
                return "200";
            } else if (Utils.bytesEquals(types, SelfType.writeRelation)) {
                RelationLruManager.getManager().put(key, value);
                return "200";
            }
        }
        return "500";
    }
    
    public String process(byte[] bytes, byte[] types) throws IOException {
        
        if (Utils.bytesEquals(types, SelfType.ping)) {
            return "";
        }
        int uuidLength = AbstractType.uuidbyteslength;
        if (bytes.length < uuidLength) {
            return "";
        }
        String byteStr = new String(bytes, uuidLength, bytes.length - uuidLength, "utf-8");
        return byteStr;
    }
}
