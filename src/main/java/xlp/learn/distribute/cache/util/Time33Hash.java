package xlp.learn.distribute.cache.util;


import xlp.learn.distribute.cache.Hash;

/**
 * Created by lpxie on 2016/8/27.
 */
public class Time33Hash implements Hash {
    
    int hash = 5381;//magic constant
    
    //hash(i) = hash(i-1) * 33 + str[i]
    //hash(0)+hash(1)+...hash(n) = hash(str)
    public long time33(String key) {
        
        if (key == null) {
            return -1;
        }
        for (int i = 0; i < key.length(); i++) {//hash<<5 == hash* 33
            int cc = key.charAt(i);
            hash += (hash << 5) + cc;
        }
        hash &= 0x7fffffff;
        return hash;
    }
    
    @Override
    public long hash(String key) {
        
        return time33(key);
    }
}
