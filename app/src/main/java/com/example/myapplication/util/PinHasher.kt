package com.example.myapplication.util

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

object PinHasher {
    private const val SALT_LENGTH = 16
    private const val ALGORITHM = "SHA-256"

    fun hash(pin: String, salt: ByteArray? = null): String {
        val actualSalt = salt ?: ByteArray(SALT_LENGTH).also {
            SecureRandom().nextBytes(it)
        }
        val md = MessageDigest.getInstance(ALGORITHM)
        md.update(actualSalt)
        val digest = md.digest(pin.toByteArray())
        val encodedSalt = Base64.getEncoder().encodeToString(actualSalt)
        val encodedHash = Base64.getEncoder().encodeToString(digest)
        return "$encodedSalt:$encodedHash"
    }

    fun verify(pin: String, storedHash: String): Boolean {
        val parts = storedHash.split(":")
        if (parts.size != 2) return false
        val salt = Base64.getDecoder().decode(parts[0])
        return hash(pin, salt) == storedHash
    }
}
