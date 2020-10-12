package net.adriantodt.calendar.rest.events.crud

import kotlinx.serialization.Serializable
import net.adriantodt.calendar.model.CalendarEvent

@Serializable
data class CreateEventResponse(
    val status: CreateEventResponseStatus,
    val event: CalendarEvent?
)
