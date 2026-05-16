package aiagentmod.action;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

/**
 * Thread-safe queue for actions that need to be executed on the main game thread.
 * All game-modifying actions are queued here and processed during the game update tick.
 */
public class ActionQueue {
    
    private final ConcurrentLinkedQueue<QueuedAction> queue = new ConcurrentLinkedQueue<>();
    
    /**
     * Enqueues an action for later execution.
     * 
     * @param actionType Type identifier for the action
     * @param action The action to execute (lambda returning ActionResult)
     */
    public void enqueue(String actionType, Supplier<ActionResult> action) {
        queue.offer(new QueuedAction(actionType, action));
    }
    
    /**
     * Executes and removes the next action from the queue.
     * 
     * @return The ActionResult, or null if queue is empty
     */
    public ActionResult executeNext() {
        QueuedAction queued = queue.poll();
        if (queued == null) return null;
        return queued.action.get();
    }
    
    /**
     * Executes all queued actions. Should be called on the main game thread.
     * 
     * @param maxActions Maximum number of actions to execute
     * @return Number of actions executed
     */
    public int executeAll(int maxActions) {
        int count = 0;
        while (!queue.isEmpty() && count < maxActions) {
            QueuedAction queued = queue.poll();
            if (queued != null) {
                try {
                    queued.action.get();
                } catch (Exception e) {
                    System.err.println("[AIAgentMod] Action execution error (" + queued.type + "): " + e.getMessage());
                }
                count++;
            }
        }
        return count;
    }
    
    /**
     * Checks if the queue is empty.
     */
    public boolean isEmpty() {
        return queue.isEmpty();
    }
    
    /**
     * Gets the current queue size.
     */
    public int size() {
        return queue.size();
    }
    
    /**
     * Clears all pending actions.
     */
    public void clear() {
        queue.clear();
    }
    
    /**
     * Internal class representing a queued action.
     */
    private static class QueuedAction {
        final String type;
        final Supplier<ActionResult> action;
        final long timestamp;
        
        QueuedAction(String type, Supplier<ActionResult> action) {
            this.type = type;
            this.action = action;
            this.timestamp = System.currentTimeMillis();
        }
    }
}
