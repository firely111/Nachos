package nachos.threads;


import nachos.machine.*;

/**
 * An implementation of condition variables that disables interrupt()s for
 * synchronization.
 *
 * <p>
 * You must implement this.
 *
 * @see	nachos.threads.Condition
 */
public class Condition2 {
    /**
     * Allocate a new condition variable.
     *
     * @param	conditionLock	the lock associated with this condition
     *				variable. The current thread must hold this
     *				lock whenever it uses <tt>sleep()</tt>,
     *				<tt>wake()</tt>, or <tt>wakeAll()</tt>.
     */
    public Condition2(Lock conditionLock) {
	this.condition2Lock = conditionLock;
    }

    /**
     * Atomically release the associated lock and go to sleep on this condition
     * variable until another thread wakes it using <tt>wake()</tt>. The
     * current thread must hold the associated lock. The thread will
     * automatically reacquire the lock before <tt>sleep()</tt> returns.
     */
    public void sleep() {
	Lib.assertTrue(condition2Lock.isHeldByCurrentThread());

	//Machine.interrupt().disable();
	condition2Lock.release();
	Machine.interrupt().disable();
	
  //queue.waitForAccess(KThread.currentThread());
	KThread sleepThread= KThread.currentThread();
	queue.waitForAccess(sleepThread);
	KThread.sleep();
	
	Machine.interrupt().enable();
	condition2Lock.acquire();
	//Machine.interrupt().enable();
    }

    /**
     * Wake up at most one thread sleeping on this condition variable. The
     * current thread must hold the associated lock.
     */
    public void wake() {
	Lib.assertTrue(condition2Lock.isHeldByCurrentThread());
	
	Machine.interrupt().disable();
	
	KThread wakeThread= queue.nextThread();
	if(wakeThread!= null){
	  wakeThread.ready(); 
	}
	
  Machine.interrupt().enable();
    }

    /**
     * Wake up all threads sleeping on this condition variable. The current
     * thread must hold the associated lock.
     */
    public void wakeAll() {
	Lib.assertTrue(condition2Lock.isHeldByCurrentThread());
	
	/*while(!queue.empty()){
	  wake();
	}*/
	KThread wakeAllThread= queue.nextThread();
	while(wakeAllThread!= null){
	  wake();
	}
  Machine.interrupt().enable();
    }


    private ThreadQueue queue= ThreadedKernel.scheduler.newThreadQueue(true);
    private Lock condition2Lock;
}
