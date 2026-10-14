package io.github.themoah.klag.mcp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class McpConfigTest {

  private static McpConfig from(Map<String, String> env) {
    return McpConfig.from(env::get);
  }

  @Test
  void disabledByDefault() {
    assertFalse(from(Map.of()).enabled());
  }

  @Test
  void enabledWhenTrue() {
    assertTrue(from(Map.of("MCP_ENABLED", "true")).enabled());
  }

  @Test
  void defaultPathIsMcp() {
    assertEquals("/mcp", from(Map.of()).path());
  }

  @Test
  void pathGetsLeadingSlash() {
    assertEquals("/custom", from(Map.of("MCP_PATH", "custom")).path());
  }

  @Test
  void authDisabledWhenNoToken() {
    McpConfig c = from(Map.of("MCP_ENABLED", "true"));
    assertFalse(c.authEnabled());
  }

  @Test
  void authEnabledWhenTokenSet() {
    McpConfig c = from(Map.of("MCP_ENABLED", "true", "MCP_AUTH_TOKEN", "secret"));
    assertTrue(c.authEnabled());
    assertEquals("secret", c.authToken());
  }

  @Test
  void blankTokenTreatedAsNoAuth() {
    assertFalse(from(Map.of("MCP_AUTH_TOKEN", "   ")).authEnabled());
  }

  @Test
  void noAllowedOriginsByDefault() {
    assertEquals(List.of(), from(Map.of()).allowedOrigins());
  }

  @Test
  void allowedOriginsAreSplitAndTrimmed() {
    McpConfig c = from(Map.of("MCP_ALLOWED_ORIGINS", " https://ops.example.com , ,http://localhost:3000 "));
    assertEquals(List.of("https://ops.example.com", "http://localhost:3000"), c.allowedOrigins());
  }

  private static final List<String> PROPS = List.of(
    "MCP_ENABLED", "mcp.enabled", "MCP_AUTH_TOKEN", "mcp.auth.token",
    "MCP_PATH", "mcp.path", "MCP_ALLOWED_ORIGINS", "mcp.allowed.origins");

  @AfterEach
  void clearProperties() {
    PROPS.forEach(System::clearProperty);
  }

  @Test
  void fromEnvironmentReadsExactJvmProperties() {
    System.setProperty("MCP_ENABLED", "true");
    System.setProperty("MCP_AUTH_TOKEN", "secret");
    System.setProperty("MCP_PATH", "agents");
    McpConfig c = McpConfig.fromEnvironment();
    assertTrue(c.enabled());
    assertTrue(c.authEnabled());
    assertEquals("secret", c.authToken());
    assertEquals("/agents", c.path());
  }

  @Test
  void fromEnvironmentReadsDottedJvmProperties() {
    System.setProperty("mcp.enabled", "true");
    System.setProperty("mcp.auth.token", "dotted-secret");
    System.setProperty("mcp.allowed.origins", "https://ops.example.com");
    McpConfig c = McpConfig.fromEnvironment();
    assertTrue(c.enabled());
    assertEquals("dotted-secret", c.authToken());
    assertEquals(List.of("https://ops.example.com"), c.allowedOrigins());
  }
}
