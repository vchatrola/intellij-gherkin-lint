package com.vchatrola.plugin.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PluginUtilsTest {

  @Test
  void getFirstKeywordToken_handlesTag() {
    assertEquals("@", PluginUtils.getFirstKeywordToken("@smoke"));
  }

  @Test
  void getFirstKeywordToken_handlesAsteriskStep() {
    assertEquals("*", PluginUtils.getFirstKeywordToken("* user logs in"));
  }

  @Test
  void getFirstKeywordToken_handlesStandardKeyword() {
    assertEquals("Given", PluginUtils.getFirstKeywordToken("Given a user exists"));
  }
}
