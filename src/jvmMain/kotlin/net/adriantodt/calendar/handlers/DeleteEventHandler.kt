package net.adriantodt.calendar.handlers

import com.auth0.jwt.JWTVerifier
import io.javalin.http.Context
import io.javalin.http.Handler
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.adriantodt.calendar.dao.DataAccessObject
import net.adriantodt.calendar.rest.events.crud.DeleteEventResponse
import net.adriantodt.calendar.rest.events.crud.DeleteEventResponseStatus

/**
 * Handler of DELETE '/api/event/:id' endpoint.
 */
class DeleteEventHandler(
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
                    DeleteEventResponse(
                        status = DeleteEventResponseStatus.EVENT_DOES_NOT_EXIST
                    )
                )
            )
            return
        }

        // Delete the event from the database
        dao.deleteEvent(event.id)

        // Create and encode the Delete Event Response into a Json
        ctx.contentType("application/json").result(
            Json.encodeToString(
                DeleteEventResponse(
                    status = DeleteEventResponseStatus.OK
                )
            )
        )
    }
}
