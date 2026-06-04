package com.example.myapplication.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

class PinHasherTest {
    @Test
    fun hash_returnsConsistentResult() {
        val salt = ByteArray(16)
        val hash1 = PinHasher.hash("1234", salt)
        val hash2 = PinHasher.hash("1234", salt)
        assertEquals(hash1, hash2)
    }

    @Test
    fun hash_differentPinsProduceDifferentHashes() {
        assertNotEquals(PinHasher.hash("1234"), PinHasher.hash("5678"))
    }

    @Test
    fun verify_matchesCorrectPin() {
        val hash = PinHasher.hash("9876")
        assertTrue(PinHasher.verify("9876", hash))
    }

    @Test
    fun verify_rejectsWrongPin() {
        val hash = PinHasher.hash("9876")
        assertFalse(PinHasher.verify("1234", hash))
    }
}
