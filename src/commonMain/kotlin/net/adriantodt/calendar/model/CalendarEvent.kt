package net.adriantodt.calendar.model

import kotlinx.serialization.Serializable
import net.adriantodt.utils.comparing

@Serializable
data class CalendarEvent(
    /**
     * Expects a Nanoflake in Base36.
     */
    val id: String,
    /**
     * Expects a Nanoflake in Base36.
     */
    val authorId: String,
    /**
     * Expects a UTF-8 string with a description.
     */
    val title: String,
    /**
     * Expects a UTF-8 string with a description.
     */
    val description: String,
    /**
     * Expects a Timestamp in milliseconds.
     */
    val startDateTime: Long, // Used to be a Date and Time formatted in IS0 8601, or `YYYY-MM-DDTHH:mm:ss.sssZ`
    /**
     * Expects a Timestamp in milliseconds.
     */
    val endDateTime: Long, // Used to be a Date and Time formatted in IS0 8601, or `YYYY-MM-DDTHH:mm:ss.sssZ`
) : Comparable<CalendarEvent> {
    override fun compareTo(other: CalendarEvent) = comparing(other) {
        by { it.startDateTime }
        by { it.endDateTime }
        by { it.title }
        by { it.id }
    }
}