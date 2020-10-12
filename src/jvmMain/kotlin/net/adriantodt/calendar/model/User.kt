package net.adriantodt.calendar.model

data class User(
    /**
     * Expects a Nanoflake in Base36.
     */
    val id: String,
    /**
     * Expects a UTF-8 string with the username.
     */
    val username: String,
    /**
     * Expects a UTF-8 string with the a SHA-256 hashed password encoded in Base64.
     */
    val hashedPassword: String
)