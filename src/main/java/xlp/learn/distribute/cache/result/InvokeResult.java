package xlp.learn.distribute.cache.result;

import java.util.concurrent.atomic.AtomicLong;

public class InvokeResult {
    
    static AtomicLong aLong = new AtomicLong();
    
    long id;
    byte[] types;
    Object msg;
    
    public InvokeResult(){
        
        this.id = aLong.getAndIncrement();
    }
    
    public InvokeResult(long id){
        
        this.id = id;
    }
    
    public byte[] getTypes() {
        
        return types;
    }
    
    public void setTypes(byte[] types) {
        
        this.types = types;
    }
    
    public Object getMsg() {
        
        return msg;
    }
    
    public void setMsg(Object msg) {
        
        this.msg = msg;
    }
    
    public long getId() {
        
        return id;
    }
    
    public void setId(long id) {
        
        this.id = id;
    }
}
