package net.adriantodt.calendar.rest.events.crud

import kotlinx.serialization.Serializable

@Serializable
data class PatchEventRequest(
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
)
