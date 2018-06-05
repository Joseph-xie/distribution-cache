package xlp.learn.distribute.cache.net;

import com.alibaba.fastjson.JSON;
import java.net.Socket;
import java.util.Map;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xlp.learn.distribute.cache.manager.DetailLruManager;
import xlp.learn.distribute.cache.manager.RelationLruManager;
import xlp.learn.distribute.cache.point.AbstractSocketPoint;
import xlp.learn.distribute.cache.protocol.AbstractType;
import xlp.learn.distribute.cache.protocol.SelfType;
import xlp.learn.distribute.cache.util.Utils;

/**
 * Created by lpxie on 2016/8/23.
 */
public class ServerSocketPoint extends AbstractSocketPoint {
    
    private static final Logger logger = LoggerFactory.getLogger(ServerSocketPoint.class);
    
    public ServerSocketPoint() {
        
        super();
    }
    
    public ServerSocketPoint(Socket socket) {
        
        this();
        this.socket = socket;
    }
    
    @Override
    public void process() {
        
        try {
            byte[] types = new byte[SelfType.typeLength];
            byte[] bytes = protocol.read(socket.getInputStream(), types);
            if (bytes.length < 1) {
                return;
            }
            
            if (Utils.bytesEquals(types, SelfType.ping)) {
                protocol.write(socket.getOutputStream(), "pong", SelfType.ping);
                return;
            }
            
            int uuidLength = AbstractType.uuidbyteslength;
            String getUuid = new String(bytes, 0, uuidLength);
            /*if(uuidStr.equals(getUuid)){
                NetCommon.selfIp = socket.getRemoteSocketAddress().toString().split(":")[0].substring(1);
                logger.info(NetCommon.selfIp +" is local machine");
                return;
            }*/
            
            String byteStr = new String(bytes, uuidLength, bytes.length - uuidLength, "utf-8");
            Map entry = JSON.parseObject(byteStr, Map.class);
            
            String srcKey = entry.get("key").toString();
            String srcValue = entry.get("value").toString();
            if (Utils.bytesEquals(types, SelfType.readDetail)) {
                String value = DetailLruManager.getManager().get(srcKey);
                if (value == null) {
                    value = "";
                }
                protocol.write(socket.getOutputStream(), value, SelfType.readDetail);
            } else if (Utils.bytesEquals(types, SelfType.readRelation)) {
                String value = RelationLruManager.getManager().get(srcKey);
                if (value == null) {
                    value = "";
                }
                protocol.write(socket.getOutputStream(), value, SelfType.readRelation);
            } else if (Utils.bytesEquals(types, SelfType.disableDetail)) {
                DetailLruManager.getManager().getMap().remove(srcKey);
                protocol.write(socket.getOutputStream(), "200", SelfType.disableDetail);
            } else if (Utils.bytesEquals(types, SelfType.disableRelation)) {
                RelationLruManager.getManager().getMap().remove(srcKey);
                protocol.write(socket.getOutputStream(), "200", SelfType.disableRelation);
            } else {
                if (Utils.bytesEquals(types, SelfType.writeDetail)) {
                    DetailLruManager.getManager().put(srcKey, srcValue);
                    protocol.write(socket.getOutputStream(), "200", SelfType.writeDetail);
                } else if (Utils.bytesEquals(types, SelfType.writeRelation)) {
                    RelationLruManager.getManager().put(srcKey, srcValue);
                    protocol.write(socket.getOutputStream(), "200", SelfType.writeRelation);
                }
            }
        } catch (Exception exp) {
            logger.warn("inputStream read wrong by :\n" + ExceptionUtils.getStackTrace(exp));
        }
    }
}
