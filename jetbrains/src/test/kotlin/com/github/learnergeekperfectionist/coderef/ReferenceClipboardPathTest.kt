package com.github.learnergeekperfectionist.coderef

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReferenceClipboardPathTest {
    @Test
    fun `rejects a null path`() {
        assertFalse(ReferenceClipboard.isSupportedFilePath(null))
    }

    @Test
    fun `rejects an empty path`() {
        assertFalse(ReferenceClipboard.isSupportedFilePath(""))
    }

    @Test
    fun `rejects a blank path`() {
        assertFalse(ReferenceClipboard.isSupportedFilePath("   "))
    }

    @Test
    fun `accepts an absolute file path`() {
        assertTrue(ReferenceClipboard.isSupportedFilePath("/tmp/foo.cpp"))
    }
}
