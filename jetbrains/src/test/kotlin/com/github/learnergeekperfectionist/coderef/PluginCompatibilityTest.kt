package com.github.learnergeekperfectionist.coderef

import java.io.File
import java.util.Properties
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

internal fun parseUntilMajor(untilBuild: String): Int {
    val major = untilBuild.substringBefore('.').substringBefore('-')
    require(major.isNotEmpty() && major.all { it.isDigit() }) {
        "pluginUntilBuild must look like '<digits>.*', got: $untilBuild"
    }
    return major.toInt()
}

class PluginCompatibilityTest {
    @Test
    fun `parseUntilMajor reads the major from a wildcard untilBuild`() {
        assertEquals(262, parseUntilMajor("262.*"))
    }

    @Test
    fun `plugin compatibility includes configured local platform`() {
        val properties = loadGradleProperties()
        val since = properties.getProperty("pluginSinceBuild").toInt()
        val untilBuild = properties.getProperty("pluginUntilBuild")
        val untilMajor = parseUntilMajor(untilBuild)
        val localMajor = majorFromBuildTxt(
            File(properties.getProperty("platformLocalPath"), "Contents/Resources/build.txt"),
        )

        assertTrue(
            "pluginSinceBuild should not be newer than the configured local platform",
            since <= localMajor,
        )
        assertTrue(
            "pluginUntilBuild should cover the configured local platform",
            localMajor <= untilMajor,
        )
        assertEquals(
            "pluginUntilBuild should use a '<major>.*' wildcard",
            "$untilMajor.*",
            untilBuild,
        )
    }

    @Test
    fun `first-class local IDEs fall within the declared compatibility interval`() {
        val properties = loadGradleProperties()
        val since = properties.getProperty("pluginSinceBuild").toInt()
        val untilMajor = parseUntilMajor(properties.getProperty("pluginUntilBuild"))
        val firstClassIdes = listOf(
            File("/Applications/Android Studio.app"),
            File("/Applications/CLion.app"),
        )

        for (app in firstClassIdes) {
            if (!app.isDirectory) {
                continue
            }

            val major = majorFromBuildTxt(File(app, "Contents/Resources/build.txt"))
            assertTrue(
                "${app.name} build $major should fall in [$since, $untilMajor]",
                major in since..untilMajor,
            )
        }
    }

    @Test
    fun `plugin xml depends only on the platform module`() {
        val pluginXml = File(projectDir(), "src/main/resources/META-INF/plugin.xml").readText()
        val depends = Regex("<depends>(.*?)</depends>")
            .findAll(pluginXml)
            .map { it.groupValues[1].trim() }
            .toList()

        assertTrue("plugin.xml should declare at least one <depends>", depends.isNotEmpty())
        for (dependency in depends) {
            assertEquals("com.intellij.modules.platform", dependency)
        }
    }

    private fun loadGradleProperties(): Properties {
        return Properties().apply {
            File(projectDir(), "gradle.properties").inputStream().use(::load)
        }
    }

    private fun projectDir(): File = File(System.getProperty("user.dir"))

    private fun majorFromBuildTxt(buildTxt: File): Int {
        val localBuild = buildTxt.readText().trim()
        return localBuild.substringAfter('-').substringBefore('.').toInt()
    }
}
