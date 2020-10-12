package net.adriantodt.calendar.handlers

import com.auth0.jwt.JWTVerifier
import io.javalin.http.Context
import io.javalin.http.Handler
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.adriantodt.calendar.dao.DataAccessObject
import net.adriantodt.calendar.rest.events.EventsResponse

class EventsHandler(private val dao: DataAccessObject, private val verifier: JWTVerifier) : Handler {
    override fun handle(ctx: Context) {
        val userId = Authorizations.validate(ctx, dao, verifier)

        ctx.result(
            Json.encodeToString(
                EventsResponse(
                    dao.getEventsByAuthor(userId)
                )
            )
        )
    }
}
