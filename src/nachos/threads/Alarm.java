package nachos.threads;

import java.util.PriorityQueue;

import nachos.machine.*;

/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {
    /**
     * Allocate a new Alarm. Set the machine's timer interrupt handler to this
     * alarm's callback.
     *
     * <p><b>Note</b>: Nachos will not function correctly with more than one
     * alarm.
     */

  public class SleepThread{
    private KThread waitThread;
    private long waitTime;
    public SleepThread(KThread wThread, long wTime) {
      waitThread= wThread;
      waitTime= wTime;
    }
    
    public long getTime(){
      return waitTime;
    }
    
    public KThread getThread(){
      return waitThread;
    }
  }
  
  
  PriorityQueue<SleepThread> sleepQueue= new PriorityQueue<SleepThread>();
  
    public Alarm() {
	Machine.timer().setInterruptHandler(new Runnable() {
		public void run() { timerInterrupt(); }
	    });
    }

    /**
     * The timer interrupt handler. This is called by the machine's timer
     * periodically (approximately every 500 clock ticks). Causes the current
     * thread to yield, forcing a context switch if there is another thread
     * that should be run.
     */
    public void timerInterrupt() {
      Lib.assertTrue(Machine.interrupt().disabled());
	    
	    while(sleepQueue.peek()!= null && sleepQueue.peek().getTime()<= Machine.timer().getTime()){
	      sleepQueue.remove().getThread().ready();
	    }
	    
	    Machine.interrupt().enable();
	    KThread.yield();
	
    }

    /**
     * Put the current thread to sleep for at least <i>x</i> ticks,
     * waking it up in the timer interrupt handler. The thread must be
     * woken up (placed in the scheduler ready set) during the first timer
     * interrupt where
     *
     * <p><blockquote>
     * (current time) >= (WaitUntil called time)+(x)
     * </blockquote>
     *
     * @param	x	the minimum number of clock ticks to wait.
     *
     * @see	nachos.machine.Timer#getTime()
     */
    public void waitUntil(long x) {
	// for now, cheat just to get something working (busy waiting is bad)
	long wakeTime = Machine.timer().getTime() + x;
	
  Machine.interrupt().disable();
  SleepThread waitingThread= new SleepThread(KThread.currentThread(), wakeTime);
  sleepQueue.add(waitingThread);
  KThread.currentThread().sleep();
  
  Machine.interrupt().enable();
  
	/*while (wakeTime > Machine.timer().getTime())
	    KThread.yield();*/
    }
}