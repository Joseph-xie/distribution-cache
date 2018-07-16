package xlp.learn.distribute.cache.support;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import xlp.learn.distribute.cache.result.InvokeResult;

public class DefaultFuture {
    
    static Map<Long,DefaultFuture> FUTURES = new HashMap<>();
    
    InvokeResult response;
    
    Lock lock = new ReentrantLock();
    
    Condition condition = lock.newCondition();
    
    //毫秒
    int timeout;
    
    public DefaultFuture(InvokeResult request,int timeout){
        
        FUTURES.put(request.getId(),this);
        
        this.timeout = timeout;
    }
    
    public Object get(){
    
        if(response != null){
        
            return response;
        }
        
        lock.lock();
        
        try{
        
            //等待设置的超时时间
            condition.await(timeout, TimeUnit.MILLISECONDS);
            
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        
        if(response != null){
            
            return response;
        }
    
        InvokeResult errorRes = new InvokeResult();
    
        errorRes.setMsg("返回错误");
        
        return errorRes;
    }
    
    public static void received(InvokeResult invokeResult){
    
        DefaultFuture future = FUTURES.remove(invokeResult.getId());
        
        if(future != null){
    
            future.doReceived(invokeResult);
        }
    }
    
    void doReceived(InvokeResult response){
        
        lock.lock();
        
        try{
            
            this.response = response;
            
            condition.signal();
            
        }finally {
            
            lock.unlock();
        }
    }
}
