package nachos.threads;

import java.util.HashMap;
import java.util.LinkedList;
import nachos.machine.*;

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>,
 * and multiple threads can be waiting to <i>listen</i>. But there should never
 * be a time when both a speaker and a listener are waiting, because the two
 * threads can be paired off at this point.
 */
public class Communicator {
    /**
     * Allocate a new communicator.
     */
    public Communicator() {
    }

    /**
     * Wait for a thread to listen through this communicator, and then transfer
     * <i>word</i> to the listener.
     *
     * <p>
     * Does not return until this thread is paired up with a listening thread.
     * Exactly one listener should receive <i>word</i>.
     *
     * @param	word	the integer to transfer.
     */
    public void speak(int word) {
    	conditionLock.acquire();
        
    	numSpeakers++;
        
        Message message = new Message(word);
        
        if(numListeners == 0) {
            messages.add(message);
            Condition2 thisSpeakerWait = new Condition2(conditionLock);
            sleepers.put(message, thisSpeakerWait);
            thisSpeakerWait.sleep();    //no listeners, go to sleep
        } else {
            listenerWait.wake();        //there is a listener
            wokenMessages.add(message);
            numListeners--;
            numSpeakers--;
        }
        
        
        
    	conditionLock.release();
    }

    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return	the integer transferred.
     */    
    public int listen() {
    	conditionLock.acquire();
    	
    	int listenedMessage = 0;
        
        numListeners++;
        
        if (numSpeakers == 0) {
            listenerWait.sleep();
            Message message = wokenMessages.removeFirst();
            listenedMessage = message.value;
        } else {
            Message message = messages.removeFirst();
            listenedMessage = message.value;
            Condition2 speakerVar = sleepers.get(message);
            speakerVar.wake();
            numSpeakers--;
            numListeners--;
        }
    	
    	conditionLock.release();
        return listenedMessage;
    }
    
    //***
    private int numSpeakers = 0;
    private int numListeners = 0;
    private LinkedList<Message> messages = new LinkedList();
    private LinkedList<Message> wokenMessages = new LinkedList();
    private HashMap<Message, Condition2> sleepers = new HashMap();
    private Lock conditionLock = new Lock();
    private Condition2 listenerWait = new Condition2(conditionLock);
    
    private class Message {
        private int value;
        public Message(int word) {
            value = word;
        }
    }
    //***
}
