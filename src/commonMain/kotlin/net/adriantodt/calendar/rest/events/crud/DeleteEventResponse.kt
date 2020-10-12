package net.adriantodt.calendar.rest.events.crud

import kotlinx.serialization.Serializable

@Serializable
data class DeleteEventResponse(
    val status: DeleteEventResponseStatus
)
