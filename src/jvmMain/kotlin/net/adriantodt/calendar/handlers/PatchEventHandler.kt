package net.adriantodt.calendar.handlers

import com.auth0.jwt.JWTVerifier
import io.javalin.http.Context
import io.javalin.http.Handler
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.adriantodt.calendar.dao.DataAccessObject
import net.adriantodt.calendar.model.CalendarEvent
import net.adriantodt.calendar.rest.events.crud.PatchEventRequest
import net.adriantodt.calendar.rest.events.crud.PatchEventResponse
import net.adriantodt.calendar.rest.events.crud.PatchEventResponseStatus

/**
 * Handler of POST '/api/login' endpoint.
 */
class PatchEventHandler(
    private val dao: DataAccessObject,
    private val verifier: JWTVerifier
) : Handler {
    override fun handle(ctx: Context) {
        // Validate the token and get the userId
        val userId = Authorizations.validate(ctx, dao, verifier)

        // Get the event using the path parameter
        val event = dao.getEvent(ctx.pathParam("id"))

        // Check if the event exists and the event's author matches the token's user.
        if (event == null || event.authorId != userId) {
            ctx.contentType("application/json").result(
                Json.encodeToString(
                    PatchEventResponse(
                        status = PatchEventResponseStatus.EVENT_DOES_NOT_EXIST,
                        event = null
                    )
                )
            )
            return
        }

        // Decode the Patch Event Request from the Body of the Request using KotlinX Serialization
        val req = Json.decodeFromString<PatchEventRequest>(ctx.body())

        // Patch a new Calendar Event together, then update it in the database
        val patchedEvent = CalendarEvent(
            id = event.id,
            authorId = event.authorId,
            title = req.title,
            description = req.description,
            startDateTime = req.startDateTime,
            endDateTime = req.endDateTime
        )
        dao.updateEvent(patchedEvent)

        // Create and encode the Patch Event Response into a Json
        ctx.contentType("application/json").result(
            Json.encodeToString(
                PatchEventResponse(
                    status = PatchEventResponseStatus.OK,
                    event = patchedEvent
                )
            )
        )
    }
}
