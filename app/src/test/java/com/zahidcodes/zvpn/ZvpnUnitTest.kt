package com.zahidcodes.zvpn

import com.zahidcodes.zvpn.core.VpnConfigParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Local unit test for ZVPN client logic.
 */
class ZvpnUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testUserVlessConfigParsing() {
    val sampleUri = "vless://162ff214-21c8-4963-a80a-6200cbad787e@rfr2svuxvm.paslanmaz.ir:4060?path=%2F&security=none&encryption=mlkem768x25519plus.native.0rtt.0MBuXdIHo3Oja0bIySO5Y0xBalo340cN5ZJLM5F-Jnw&host=play.google.com&type=httpupgrade#filembad-84"
    val parsed = VpnConfigParser.parse(sampleUri, "127.0.0.1", 443)

    assertEquals("VLESS", parsed.protocol)
    assertEquals("rfr2svuxvm.paslanmaz.ir", parsed.host)
    assertEquals(4060, parsed.port)
    assertEquals("162ff214-21c8-4963-a80a-6200cbad787e", parsed.uuidOrPassword)
    assertEquals("none", parsed.security)
    assertEquals("play.google.com", parsed.sni)
    assertEquals("httpupgrade", parsed.network)
    assertEquals("/", parsed.path)
    assertEquals("filembad-84", parsed.remark)
    assertTrue(parsed.encryption.contains("mlkem768x25519plus"))
  }

  @Test
  fun testPlayUpdateManagerSimulation() {
    val manager = com.zahidcodes.zvpn.core.PlayUpdateManager
    manager.setSimulatedForceUpdate(true)
    val status = manager.updateStatus.value

    assertTrue("Status should be ForceUpdateRequired", status is com.zahidcodes.zvpn.core.PlayUpdateStatus.ForceUpdateRequired)
    val details = (status as com.zahidcodes.zvpn.core.PlayUpdateStatus.ForceUpdateRequired).details
    assertTrue("Should be marked as force update", details.isForceUpdate)
    assertTrue("Latest version code should be greater than current", details.latestVersionCode > details.currentVersionCode)

    manager.setSimulatedForceUpdate(false)
    assertTrue("Status should revert to UpToDate", manager.updateStatus.value is com.zahidcodes.zvpn.core.PlayUpdateStatus.UpToDate)
  }
}
