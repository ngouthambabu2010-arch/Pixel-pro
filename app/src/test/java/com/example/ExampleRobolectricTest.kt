package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.LmcConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Pixel Cam", appName)
  }

  @Test
  fun `verify lmc config serialization and deserialization`() {
    val defaultConfig = LmcConfig.defaultConfigs[0]
    val xml = defaultConfig.toXml()
    assertTrue(xml.contains("<camera_config"))
    assertTrue(xml.contains("color_science"))

    val parsed = LmcConfig.fromXml(xml)
    assertEquals(defaultConfig.name, parsed.name)
    assertEquals(defaultConfig.saturation, parsed.saturation, 0.01f)
    assertEquals(defaultConfig.burstFrames, parsed.burstFrames)
  }

  @Test
  fun `verify custom config save and persistence`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val customConfig = LmcConfig(
        id = "custom_test_1",
        name = "Test_Profile.xml",
        author = "Tester",
        saturation = 1.35f,
        contrast = 1.20f,
        edgeStrength = 1.50f,
        noiseReductionLevel = "ULTRA",
        isCustom = true
    )
    com.example.ui.config.saveConfigToDisk(context, customConfig)

    val savedFile = java.io.File(context.filesDir, "LMC8.4/Test_Profile.xml")
    assertTrue(savedFile.exists())

    val loaded = LmcConfig.fromXml(savedFile.readText(), "loaded_test")
    assertEquals("Test_Profile.xml", loaded.name)
    assertEquals(1.35f, loaded.saturation, 0.01f)
    assertEquals("ULTRA", loaded.noiseReductionLevel)
  }
}
