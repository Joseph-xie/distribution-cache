package xlp.learn.distribute.cache.route;

import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xlp.learn.distribute.cache.util.FNV132Hash;

/**
 * Created by lpxie on 2016/8/27.
 */
public class ConsistentHashingWithVN {
    
    private static final int VIRTUAL_NODES = 100;
    
    static FNV132Hash fnv132Hash = new FNV132Hash();
    
    private static Logger logger = LoggerFactory.getLogger(ConsistentHashingWithVN.class);
    
    private static List<String> realNodes = new LinkedList<String>();
    
    private static SortedMap<Long, String> virtualNodes = new TreeMap<Long, String>();
    
    public static void init(String[] servers) {
        
        for (int i = 0; i < servers.length; i++) {
            realNodes.add(servers[i]);
        }
        
        for (String str : realNodes) {
            for (int i = 0; i < VIRTUAL_NODES; i++) {
                String virtualNodeName = str + "&&VN" + String.valueOf(i);
                long hash = fnv132Hash.hash(virtualNodeName);
                logger.info("虚拟节点[" + virtualNodeName + "]被添加, hash值为" + hash);
                virtualNodes.put(hash, virtualNodeName);
            }
        }
    }
    
    public static String route(String node) {
        
        long hash = fnv132Hash.hash(node);
        SortedMap<Long, String> subMap = virtualNodes.tailMap(hash);
        Long i = subMap.firstKey();
        String virtualNode = subMap.get(i);
        return virtualNode.substring(0, virtualNode.indexOf("&&"));
    }
}
