import java.util.concurrent.Semaphore;

import redis.clients.jedis.Jedis;

/**
 * Simple Distributed Lock Service for demo purposes.
 * Shows basic concept: check if key exists, set lock, perform work, unlock.
 */
public class DistributedLockService {
    
    private Jedis jedis;
    
    public DistributedLockService(String host, int port) {
        this.jedis = new Jedis(host, port);
    }
    
    /**
     * Try to acquire lock. Returns true if successful.
     */
    public boolean lock(String lockKey) {
        // Check if key exists, if not set it (atomic operation)
        // SETNX = SET if Not eXists
        Long result = jedis.setnx(lockKey, "locked");
        return result == 1; // 1 means key was set (lock acquired)
    }
    
    /**
     * Release the lock by deleting the key.
     */
    public void unlock(String lockKey) {
        jedis.del(lockKey);
    }
    
    /**
     * Close Redis connection.
     */
    public void close() {
        if (jedis != null) {
            jedis.close();
        }
    }
    
    /**
     * Demo example
     */
    public static void main(String[] args) {
        DistributedLockService lockService = new DistributedLockService("localhost", 6379);

        String lockKey = "my-critical-section-lock";
        
        // Try to acquire the lock
        if (!lockService.lock(lockKey)) {
            System.out.println("Could not acquire lock - another process is using it");
        }
        else {
            System.out.println("Lock acquired!");
            
            try {
                performCriticalAction();
                
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                // Always release the lock
                lockService.unlock(lockKey);
                System.out.println("Lock released!");
            }
        }
        
        lockService.close();
    }


    private static void performCriticalAction() throws InterruptedException {
        // === CRITICAL SECTION ===
        System.out.println("Performing critical operation...");
        Thread.sleep(2000); // Simulate work
        System.out.println("Critical operation completed!");
        // === END CRITICAL SECTION ===
    }
}