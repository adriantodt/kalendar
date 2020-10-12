package net.adriantodt.calendar.handlers

import com.auth0.jwt.JWTVerifier
import io.javalin.http.Context
import io.javalin.http.Handler
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.adriantodt.calendar.dao.DataAccessObject
import net.adriantodt.calendar.rest.events.EventsResponse

/**
 * Handler of GET '/api/events' endpoint.
 */
class EventsHandler(
    private val dao: DataAccessObject,
    private val verifier: JWTVerifier
) : Handler {
    override fun handle(ctx: Context) {
        // Validate the token and get the userId
        val userId = Authorizations.validate(ctx, dao, verifier)

        // Get all events associated with the author,
        // then create and encode the Patch Event Response into a Json
        ctx.contentType("application/json").result(
            Json.encodeToString(EventsResponse(dao.getEventsByAuthor(userId)))
        )
    }
}
