package aiagentmod.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link ActionQueue} class.
 * <p>
 * Tests cover queue operations including enqueue, execute, clear, and
 * exception handling. The ActionQueue is a thread-safe queue that
 * schedules actions for execution on the main game thread.
 */
class ActionQueueTest {

    private ActionQueue queue;

    /**
     * Creates a fresh ActionQueue instance before each test.
     */
    @BeforeEach
    void setUp() {
        queue = new ActionQueue();
    }

    /**
     * Verifies that a newly created queue is empty and has size zero.
     */
    @Test
    @DisplayName("New queue should be empty with size 0")
    void testQueueInitiallyEmpty() {
        assertTrue(queue.isEmpty(), "New queue should be empty");
        assertEquals(0, queue.size(), "New queue should have size 0");
    }

    /**
     * Verifies that enqueuing an action increases queue size
     * and executing it processes the action and empties the queue.
     */
    @Test
    @DisplayName("Enqueue should add action and executeNext should process it")
    void testEnqueueAndExecute() {
        final boolean[] executed = {false};
        queue.enqueue("test", () -> {
            executed[0] = true;
            return ActionResult.ok("done");
        });

        assertFalse(queue.isEmpty(), "Queue should not be empty after enqueue");
        assertEquals(1, queue.size(), "Queue should have size 1");

        ActionResult result = queue.executeNext();
        assertNotNull(result, "Result should not be null");
        assertTrue(executed[0], "Action should have been executed");
        assertTrue(queue.isEmpty(), "Queue should be empty after execution");
        assertEquals(0, queue.size(), "Queue should have size 0");
    }

    /**
     * Verifies that multiple actions can be enqueued and executed.
     */
    @Test
    @DisplayName("executeAll should process all queued actions")
    void testExecuteMultiple() {
        final int[] count = {0};

        for (int i = 0; i < 5; i++) {
            queue.enqueue("test", () -> {
                count[0]++;
                return ActionResult.ok("done");
            });
        }

        assertEquals(5, queue.size(), "Queue should have 5 items");

        int executed = queue.executeAll(10);
        assertEquals(5, executed, "Should execute all 5 actions");
        assertEquals(5, count[0], "Count should be 5 after all executed");
        assertTrue(queue.isEmpty(), "Queue should be empty");
    }

    /**
     * Verifies that executeAll respects the maximum action limit.
     */
    @Test
    @DisplayName("executeAll should respect the maxActions limit")
    void testExecuteAllWithLimit() {
        final int[] count = {0};

        for (int i = 0; i < 10; i++) {
            queue.enqueue("test", () -> {
                count[0]++;
                return ActionResult.ok("done");
            });
        }

        int executed = queue.executeAll(3);
        assertEquals(3, executed, "Should execute only 3 actions");
        assertEquals(3, count[0], "Count should be 3");
        assertEquals(7, queue.size(), "Queue should have 7 remaining");
        assertFalse(queue.isEmpty(), "Queue should not be empty");
    }

    /**
     * Verifies that executeAll with limit 0 executes nothing.
     */
    @Test
    @DisplayName("executeAll with limit 0 should execute nothing")
    void testExecuteAllWithZeroLimit() {
        queue.enqueue("test", () -> ActionResult.ok("done"));
        int executed = queue.executeAll(0);
        assertEquals(0, executed, "Should execute 0 actions");
        assertEquals(1, queue.size(), "Queue should still have 1 item");
    }

    /**
     * Verifies that clear removes all pending actions.
     */
    @Test
    @DisplayName("clear should remove all pending actions")
    void testClear() {
        queue.enqueue("test", () -> ActionResult.ok("done"));
        queue.enqueue("test", () -> ActionResult.ok("done"));
        queue.enqueue("test", () -> ActionResult.ok("done"));

        assertEquals(3, queue.size(), "Queue should have 3 items");

        queue.clear();
        assertTrue(queue.isEmpty(), "Queue should be empty after clear");
        assertEquals(0, queue.size(), "Queue size should be 0 after clear");
    }

    /**
     * Verifies that executeNext returns null when the queue is empty.
     */
    @Test
    @DisplayName("executeNext on empty queue should return null")
    void testExecuteNextOnEmpty() {
        assertTrue(queue.isEmpty(), "Queue should be empty");
        ActionResult result = queue.executeNext();
        assertNull(result, "Result should be null for empty queue");
    }

    /**
     * Verifies that exceptions thrown by actions are caught and logged
     * rather than propagated.
     */
    @Test
    @DisplayName("executeAll should catch and handle action exceptions gracefully")
    void testQueueExceptionHandling() {
        queue.enqueue("error", () -> {
            throw new RuntimeException("Test exception");
        });

        // Should not throw despite the action throwing an exception
        assertDoesNotThrow(() -> queue.executeAll(1), 
            "Queue should not propagate action exceptions");
        assertTrue(queue.isEmpty(), "Failed action should still be dequeued");
    }

    /**
     * Verifies that multiple exception-throwing actions are all handled.
     */
    @Test
    @DisplayName("executeAll should handle multiple exceptions in sequence")
    void testMultipleExceptionHandling() {
        queue.enqueue("error1", () -> { throw new RuntimeException("Error 1"); });
        queue.enqueue("error2", () -> { throw new RuntimeException("Error 2"); });
        queue.enqueue("ok", () -> ActionResult.ok("success"));

        int executed = queue.executeAll(10);
        assertEquals(3, executed, "Should execute all 3 actions");
        assertTrue(queue.isEmpty(), "Queue should be empty");
    }

    /**
     * Verifies that action results are returned correctly.
     */
    @Test
    @DisplayName("executeNext should return the action's ActionResult")
    void testResultReturned() {
        queue.enqueue("test", () -> ActionResult.ok("test success", 
            new org.json.JSONObject().put("value", 42)));

        ActionResult result = queue.executeNext();
        assertNotNull(result, "Result should not be null");
        assertTrue(result.success, "Result should be successful");
        assertEquals("test success", result.message, "Message should match");
        assertEquals(42, result.data.getInt("value"), "Data should contain value");
    }

    /**
     * Verifies that actions with different types can coexist in the queue.
     */
    @Test
    @DisplayName("Queue should handle mixed action types")
    void testMixedActionTypes() {
        final boolean[] moveExecuted = {false};
        final boolean[] shootExecuted = {false};

        queue.enqueue("move", () -> {
            moveExecuted[0] = true;
            return ActionResult.ok("moved");
        });
        queue.enqueue("shoot", () -> {
            shootExecuted[0] = true;
            return ActionResult.ok("shot");
        });

        assertEquals(2, queue.size());

        queue.executeAll(2);
        assertTrue(moveExecuted[0], "Move action should execute");
        assertTrue(shootExecuted[0], "Shoot action should execute");
    }

    /**
     * Verifies FIFO ordering of the queue.
     */
    @Test
    @DisplayName("Queue should process actions in FIFO order")
    void testFifoOrder() {
        final int[] order = {0};
        final int[] firstOrder = {-1};
        final int[] secondOrder = {-1};

        queue.enqueue("first", () -> {
            firstOrder[0] = order[0]++;
            return ActionResult.ok("first");
        });
        queue.enqueue("second", () -> {
            secondOrder[0] = order[0]++;
            return ActionResult.ok("second");
        });

        queue.executeAll(2);
        assertEquals(0, firstOrder[0], "First action should execute first");
        assertEquals(1, secondOrder[0], "Second action should execute second");
    }
}
