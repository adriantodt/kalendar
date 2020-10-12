package net.adriantodt.calendar.dao

import net.adriantodt.calendar.model.CalendarEvent
import net.adriantodt.calendar.model.User
import java.security.MessageDigest
import java.util.*

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
        fun hashPassword(password: String): String {
            return Base64.getEncoder().encodeToString(
                MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
            )
        }
    }
}