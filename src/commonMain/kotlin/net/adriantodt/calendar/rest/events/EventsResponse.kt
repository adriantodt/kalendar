package net.adriantodt.calendar.rest.events

import kotlinx.serialization.Serializable
import net.adriantodt.calendar.model.CalendarEvent

@Serializable
data class EventsResponse(
    val events: List<CalendarEvent>
)

