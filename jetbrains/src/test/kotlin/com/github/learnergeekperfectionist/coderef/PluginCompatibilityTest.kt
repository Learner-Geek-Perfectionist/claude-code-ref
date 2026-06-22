package com.github.learnergeekperfectionist.coderef

import java.io.File
import java.util.Properties
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PluginCompatibilityTest {
    @Test
    fun `plugin compatibility includes configured local platform`() {
        val projectDir = File(System.getProperty("user.dir"))
        val properties = Properties().apply {
            File(projectDir, "gradle.properties").inputStream().use(::load)
        }

        val platformLocalPath = properties.getProperty("platformLocalPath")
        val localBuild = File(platformLocalPath, "Contents/Resources/build.txt").readText().trim()
        val localBuildNumber = localBuild.substringAfter('-')
        val localMajorBuild = localBuildNumber.substringBefore('.').toInt()

        assertTrue(
            "pluginSinceBuild should not be newer than the configured local platform",
            properties.getProperty("pluginSinceBuild").toInt() <= localMajorBuild,
        )
        assertEquals(
            "pluginUntilBuild should cover the configured local platform",
            "$localMajorBuild.*",
            properties.getProperty("pluginUntilBuild"),
        )
    }
}
