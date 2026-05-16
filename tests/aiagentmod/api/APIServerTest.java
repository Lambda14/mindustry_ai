package aiagentmod.api;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.DisplayName;
import org.json.JSONObject;
import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Integration tests for the {@link APIServer} class.
 * <p>
 * These tests start an actual HTTP server and verify all API endpoints
 * respond correctly with proper status codes, JSON content, and CORS headers.
 * The server runs on a non-standard test port to avoid conflicts.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class APIServerTest {

    private APIServer server;
    private static final int TEST_PORT = 18089;

    /**
     * Starts the API server on the test port before all tests run.
     */
    @BeforeAll
    void startServer() throws Exception {
        server = new APIServer(TEST_PORT);
        server.start(5000, false); // Don't block

        // Wait for server to be ready
        Thread.sleep(500);
    }

    /**
     * Stops the API server after all tests complete.
     */
    @AfterAll
    void stopServer() {
        if (server != null) {
            server.stop();
        }
    }

    /**
     * Verifies that the server was started successfully and is alive.
     */
    @Test
    @DisplayName("Server should be running after start")
    void testServerRunning() {
        assertNotNull(server, "Server instance should not be null");
        assertTrue(server.isAlive(), "Server should be alive");
    }

    /**
     * Verifies that the root endpoint returns API metadata with name and endpoints.
     */
    @Test
    @DisplayName("GET / should return API info with name and endpoints")
    void testRootEndpoint() throws Exception {
        String response = httpGet("http://localhost:" + TEST_PORT + "/");
        assertNotNull(response, "Response should not be null");
        assertFalse(response.isEmpty(), "Response should not be empty");
        
        JSONObject json = new JSONObject(response);
        assertEquals("AI Agent Mod API", json.getString("name"), 
            "Response should contain API name");
        assertTrue(json.has("endpoints"), "Response should contain endpoints list");
        assertTrue(json.has("version"), "Response should contain version");
    }

    /**
     * Verifies that the /api endpoint also returns API metadata.
     */
    @Test
    @DisplayName("GET /api should return API info")
    void testApiEndpoint() throws Exception {
        String response = httpGet("http://localhost:" + TEST_PORT + "/api");
        assertNotNull(response, "Response should not be null");
        
        JSONObject json = new JSONObject(response);
        assertEquals("AI Agent Mod API", json.getString("name"));
    }

    /**
     * Verifies that the state endpoint returns a valid JSON response.
     */
    @Test
    @DisplayName("GET /api/state should return game state JSON")
    void testStateEndpoint() throws Exception {
        String response = httpGet("http://localhost:" + TEST_PORT + "/api/state");
        assertNotNull(response, "Response should not be null");
        assertFalse(response.isEmpty(), "Response should not be empty");
        
        // Should be valid JSON
        JSONObject json = new JSONObject(response);
        assertNotNull(json);
    }

    /**
     * Verifies that the player endpoint returns a valid JSON response.
     */
    @Test
    @DisplayName("GET /api/player should return player JSON")
    void testPlayerEndpoint() throws Exception {
        String response = httpGet("http://localhost:" + TEST_PORT + "/api/player");
        assertNotNull(response, "Response should not be null");
        assertFalse(response.isEmpty(), "Response should not be empty");
    }

    /**
     * Verifies that the units endpoint returns units array with count.
     */
    @Test
    @DisplayName("GET /api/units should return units array and count")
    void testUnitsEndpoint() throws Exception {
        String response = httpGet("http://localhost:" + TEST_PORT + "/api/units");
        assertNotNull(response, "Response should not be null");
        
        JSONObject json = new JSONObject(response);
        assertTrue(json.has("units"), "Response should have units array");
        assertTrue(json.has("count"), "Response should have count field");
    }

    /**
     * Verifies that the buildings endpoint returns buildings array with count.
     */
    @Test
    @DisplayName("GET /api/buildings should return buildings array and count")
    void testBuildingsEndpoint() throws Exception {
        String response = httpGet("http://localhost:" + TEST_PORT + "/api/buildings");
        assertNotNull(response, "Response should not be null");
        
        JSONObject json = new JSONObject(response);
        assertTrue(json.has("buildings"), "Response should have buildings array");
        assertTrue(json.has("count"), "Response should have count field");
    }

    /**
     * Verifies that the map endpoint returns a valid JSON response.
     */
    @Test
    @DisplayName("GET /api/map should return map JSON")
    void testMapEndpoint() throws Exception {
        String response = httpGet("http://localhost:" + TEST_PORT + "/api/map");
        assertNotNull(response, "Response should not be null");
    }

    /**
     * Verifies that the resources endpoint returns a valid JSON response.
     */
    @Test
    @DisplayName("GET /api/resources should return resources JSON")
    void testResourcesEndpoint() throws Exception {
        String response = httpGet("http://localhost:" + TEST_PORT + "/api/resources");
        assertNotNull(response, "Response should not be null");
    }

    /**
     * Verifies that a request to a non-existent endpoint returns a 404 with error JSON.
     */
    @Test
    @DisplayName("GET /api/nonexistent should return 404 with error")
    void testNotFoundEndpoint() throws Exception {
        URL url = new URL("http://localhost:" + TEST_PORT + "/api/nonexistent");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        
        int responseCode = conn.getResponseCode();
        assertEquals(404, responseCode, "Should return 404 for unknown endpoint");
        
        String response = readResponse(conn);
        JSONObject json = new JSONObject(response);
        assertTrue(json.has("error"), "Error response should contain 'error' field");
        assertTrue(json.has("available"), "Error response should list available endpoints");
        
        conn.disconnect();
    }

    /**
     * Verifies that CORS preflight requests are handled correctly.
     */
    @Test
    @DisplayName("OPTIONS request should return CORS headers")
    void testCorsHeaders() throws Exception {
        URL url = new URL("http://localhost:" + TEST_PORT + "/api/state");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("OPTIONS");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        conn.connect();

        int responseCode = conn.getResponseCode();
        assertEquals(200, responseCode, "OPTIONS should return 200");

        String allowOrigin = conn.getHeaderField("Access-Control-Allow-Origin");
        assertNotNull(allowOrigin, "Should have Access-Control-Allow-Origin header");
        assertEquals("*", allowOrigin, "Should allow all origins");

        String allowMethods = conn.getHeaderField("Access-Control-Allow-Methods");
        assertNotNull(allowMethods, "Should have Access-Control-Allow-Methods header");
        assertTrue(allowMethods.contains("GET"), "Should allow GET");
        assertTrue(allowMethods.contains("POST"), "Should allow POST");

        conn.disconnect();
    }

    /**
     * Verifies that CORS headers are present on GET responses.
     */
    @Test
    @DisplayName("GET response should include CORS headers")
    void testCorsHeadersOnGet() throws Exception {
        URL url = new URL("http://localhost:" + TEST_PORT + "/api/state");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        conn.connect();

        int responseCode = conn.getResponseCode();
        assertEquals(200, responseCode, "GET should return 200");

        String allowOrigin = conn.getHeaderField("Access-Control-Allow-Origin");
        assertNotNull(allowOrigin, "Should have CORS header on GET");
        assertEquals("*", allowOrigin);

        conn.disconnect();
    }

    /**
     * Verifies that POST to the move action endpoint returns a valid result JSON.
     */
    @Test
    @DisplayName("POST /api/action/move should return action result")
    void testPostActionMoveEndpoint() throws Exception {
        String jsonInput = "{\"x\":100,\"y\":200}";
        String response = httpPost("http://localhost:" + TEST_PORT + "/api/action/move", jsonInput);
        assertNotNull(response, "Response should not be null");
        
        JSONObject json = new JSONObject(response);
        assertTrue(json.has("success"), "Response should have success field");
        assertTrue(json.has("message"), "Response should have message field");
        assertTrue(json.has("data"), "Response should have data field");
        assertTrue(json.has("timestamp"), "Response should have timestamp field");
    }

    /**
     * Verifies that POST to the build action endpoint returns a valid result JSON.
     */
    @Test
    @DisplayName("POST /api/action/build should return action result")
    void testPostActionBuildEndpoint() throws Exception {
        String jsonInput = "{\"x\":10,\"y\":10,\"block\":\"conveyor\"}";
        String response = httpPost("http://localhost:" + TEST_PORT + "/api/action/build", jsonInput);
        assertNotNull(response, "Response should not be null");
        
        JSONObject json = new JSONObject(response);
        assertTrue(json.has("success"), "Response should have success field");
        assertTrue(json.has("message"), "Response should have message field");
    }

    /**
     * Verifies that POST to the break action endpoint returns a valid result JSON.
     */
    @Test
    @DisplayName("POST /api/action/break should return action result")
    void testPostActionBreakEndpoint() throws Exception {
        String jsonInput = "{\"x\":10,\"y\":10}";
        String response = httpPost("http://localhost:" + TEST_PORT + "/api/action/break", jsonInput);
        assertNotNull(response, "Response should not be null");
        
        JSONObject json = new JSONObject(response);
        assertTrue(json.has("success"), "Response should have success field");
    }

    /**
     * Verifies that POST to the mine action endpoint returns a valid result JSON.
     */
    @Test
    @DisplayName("POST /api/action/mine should return action result")
    void testPostActionMineEndpoint() throws Exception {
        String jsonInput = "{\"x\":100,\"y\":200}";
        String response = httpPost("http://localhost:" + TEST_PORT + "/api/action/mine", jsonInput);
        assertNotNull(response, "Response should not be null");
        
        JSONObject json = new JSONObject(response);
        assertTrue(json.has("success"), "Response should have success field");
    }

    /**
     * Verifies that POST to the shoot action endpoint returns a valid result JSON.
     */
    @Test
    @DisplayName("POST /api/action/shoot should return action result")
    void testPostActionShootEndpoint() throws Exception {
        String jsonInput = "{\"x\":100,\"y\":200,\"shoot\":true}";
        String response = httpPost("http://localhost:" + TEST_PORT + "/api/action/shoot", jsonInput);
        assertNotNull(response, "Response should not be null");
        
        JSONObject json = new JSONObject(response);
        assertTrue(json.has("success"), "Response should have success field");
    }

    /**
     * Verifies that POST with empty body is handled gracefully.
     */
    @Test
    @DisplayName("POST with empty body should be handled gracefully")
    void testPostEmptyBody() throws Exception {
        String response = httpPost("http://localhost:" + TEST_PORT + "/api/action/move", "");
        assertNotNull(response, "Response should not be null");
        
        JSONObject json = new JSONObject(response);
        assertTrue(json.has("success"), "Response should have success field");
    }

    // ============ Helper Methods ============

    /**
     * Performs an HTTP GET request and returns the response body as a string.
     *
     * @param urlString the URL to request
     * @return the response body
     * @throws Exception if the request fails
     */
    private String httpGet(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        try {
            int responseCode = conn.getResponseCode();
            BufferedReader reader;
            if (responseCode >= 200 && responseCode < 300) {
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            } else {
                reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            }

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            return response.toString();
        } finally {
            conn.disconnect();
        }
    }

    /**
     * Performs an HTTP POST request with JSON body and returns the response body.
     *
     * @param urlString the URL to request
     * @param jsonInput the JSON body to send
     * @return the response body
     * @throws Exception if the request fails
     */
    private String httpPost(String urlString, String jsonInput) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        try {
            if (!jsonInput.isEmpty()) {
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonInput.getBytes(java.nio.charset.StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
            }

            BufferedReader reader;
            int responseCode = conn.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            } else {
                reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            }

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            return response.toString();
        } finally {
            conn.disconnect();
        }
    }

    /**
     * Reads the response body from an HttpURLConnection.
     *
     * @param conn the connection to read from
     * @return the response body
     * @throws Exception if reading fails
     */
    private String readResponse(HttpURLConnection conn) throws Exception {
        BufferedReader reader;
        int responseCode = conn.getResponseCode();
        if (responseCode >= 200 && responseCode < 300) {
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }

        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        return response.toString();
    }
}
