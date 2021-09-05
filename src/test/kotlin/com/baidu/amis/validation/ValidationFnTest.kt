package com.baidu.amis.validation;

import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.TextNode
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ValidationFnTest {
    @Test
    fun testIsRequired() {
        assertEquals(ValidationFn.isEmptyString(null), false)
        assertEquals(ValidationFn.isRequired(TextNode("a")), true)
        assertEquals(ValidationFn.isRequired(TextNode("")), false)
        assertEquals(ValidationFn.isRequired(NullNode.getInstance()), false)
    }

    @Test
    fun testIsExisty() {
        assertEquals(ValidationFn.isEmptyString(null), false)
        assertEquals(ValidationFn.isExisty(TextNode("")), true)
        assertEquals(ValidationFn.isExisty(NullNode.getInstance()), false)
    }

    @Test
    fun testIsEmptyString() {
        assertEquals(ValidationFn.isEmptyString(null), false)
        assertEquals(ValidationFn.isEmptyString(TextNode("a")), false)
        assertEquals(ValidationFn.isEmptyString(TextNode("")), true)
    }

    @Test
    fun testIsEmail() {
        assertEquals(ValidationFn.isEmptyString(null), false)
        assertEquals(ValidationFn.isEmail(TextNode("aa@bb.com")), true)
        assertEquals(ValidationFn.isEmail(TextNode("aabb.com")), false)
    }

    @Test
    fun testIsSpecialWords() {
        assertEquals(ValidationFn.isSpecialWords(TextNode("ab")), true)
        assertEquals(ValidationFn.isSpecialWords(TextNode("aabbÂÄBC")), true)
        assertEquals(ValidationFn.isSpecialWords(TextNode("中文")), false)
    }

    @Test
    fun testMaximum() {
        assertEquals(ValidationFn.maximum(TextNode("10"), 20.0), true)
        assertEquals(ValidationFn.maximum(TextNode("30"), 20.0), false)
    }

}