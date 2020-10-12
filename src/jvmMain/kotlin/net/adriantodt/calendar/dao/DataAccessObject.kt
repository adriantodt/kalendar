package net.adriantodt.calendar.dao

import net.adriantodt.calendar.model.CalendarEvent
import net.adriantodt.calendar.model.User
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.*

/**
 * Interface to interact with a database.
 */
interface DataAccessObject {
    //region get operations

    fun getEvent(id: String): CalendarEvent?
    fun getEventsByAuthor(authorId: String): List<CalendarEvent>
    fun getUser(id: String): User?
    fun getUserByUsername(username: String): User?

    //endregion

    //region insert operations

    fun insertEvent(event: CalendarEvent)
    fun insertUser(user: User)

    //endregion

    //region update operations

    fun updateEvent(event: CalendarEvent)

    //endregion

    //region delete operations

    fun deleteEvent(id: String)

    //endregion

    companion object {
        private val rnd = SecureRandom()
        private val decoder = Base64.getDecoder()
        private val encoder = Base64.getEncoder()

        /**
         * Hashes a password using salt sourced from a SecureRandom.
         */
        fun hashPassword(password: String): String {
            // Generate a random salt
            val salt = ByteArray(16).also(rnd::nextBytes)
            // Create a SHA-256 digest, then add the salt first
            val digest = MessageDigest.getInstance("SHA-256")
            digest.update(salt)
            // Encode the salt and the hash
            return encoder.encodeToString(salt) + ":" + encoder.encodeToString(digest.digest(password.toByteArray()))
        }

        /**
         * Checkes the password by re-calculating the hash with the same salt.
         */
        fun checkPassword(hashed: String, plaintext: String): Boolean {
            // Reverse the parts from the hashed password
            val (salt, hash) = hashed.split(':', limit = 2)
            // Create a SHA-256 digest, then decode and add the salt first
            val digest = MessageDigest.getInstance("SHA-256")
            digest.update(decoder.decode(salt))
            // Compare the digested hash with the original hash
            return encoder.encodeToString(digest.digest(plaintext.toByteArray())) == hash
        }
    }
}