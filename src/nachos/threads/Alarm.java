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
        //***
        long now = Machine.timer().getTime();
        
        AlarmEntry headEntry = waitQueue.peek();
        while(headEntry != null && headEntry.timeToWake <= now) {
            headEntry = waitQueue.poll();
            headEntry.wake();
            headEntry = waitQueue.peek();
        }
        //***
        
	KThread.currentThread().yield();
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
        //***
        long now = Machine.timer().getTime();
        AlarmEntry entry = new AlarmEntry(now + x);
        waitQueue.add(entry);
        entry.sleep();
    }
    
    //***
    private PriorityQueue<AlarmEntry> waitQueue = new PriorityQueue();
    
    /**
     * Inner class to make PriorityQueue sort by time
     */
    private class AlarmEntry implements Comparable<AlarmEntry> {
        
        private long timeToWake;
        private Lock conditionLock = new Lock();
        private Condition2 conditionVar = new Condition2(conditionLock);
        
        public AlarmEntry(long timeToWake) {
            this.timeToWake = timeToWake;
        }
        
        public void sleep() {
            conditionLock.acquire();
            conditionVar.sleep();
            conditionLock.release();
        }
        
        public void wake() {
            conditionLock.acquire();
            conditionVar.wake();
            conditionLock.release();
        }
        
        @Override
        public int compareTo(AlarmEntry other) {
            //compareTo can't return longs, so can't just return the difference,
            //since it might not fit in an int
            
            long diff = timeToWake - other.timeToWake;
            if(diff < 0) {
                return -1;
            } else if (diff > 0) {
                return 1;
            } else {
                return 0;
            }
        }
        
    }
    //***
}
