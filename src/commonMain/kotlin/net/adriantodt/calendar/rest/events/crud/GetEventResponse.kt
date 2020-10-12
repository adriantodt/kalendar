package net.adriantodt.calendar.rest.events.crud

import kotlinx.serialization.Serializable
import net.adriantodt.calendar.model.CalendarEvent

@Serializable
data class GetEventResponse(
    val status: GetEventResponseStatus,
    val event: CalendarEvent?
)
