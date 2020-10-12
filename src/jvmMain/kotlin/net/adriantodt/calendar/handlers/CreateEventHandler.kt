package net.adriantodt.calendar.handlers

import com.auth0.jwt.JWTVerifier
import com.github.nanoflakes.NanoflakeGenerator
import io.javalin.http.Context
import io.javalin.http.Handler
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.adriantodt.calendar.dao.DataAccessObject
import net.adriantodt.calendar.model.CalendarEvent
import net.adriantodt.calendar.rest.events.crud.CreateEventRequest
import net.adriantodt.calendar.rest.events.crud.CreateEventResponse
import net.adriantodt.calendar.rest.events.crud.CreateEventResponseStatus

class CreateEventHandler(
    private val dao: DataAccessObject,
    private val generator: NanoflakeGenerator,
    private val verifier: JWTVerifier
) : Handler {
    override fun handle(ctx: Context) {
        val userId = Authorizations.validate(ctx, dao, verifier)

        val eventReq = Json.decodeFromString<CreateEventRequest>(ctx.body())

        val id = generator.next()

        val event = CalendarEvent(
            id = id.withRadix(36),
            authorId = userId,
            title = eventReq.title,
            description = eventReq.description,
            startDateTime = eventReq.startDateTime,
            endDateTime = eventReq.endDateTime
        )

        dao.insertEvent(event)

        ctx.result(
            Json.encodeToString(
                CreateEventResponse(
                    status = CreateEventResponseStatus.OK,
                    event = event
                )
            )
        )
    }
}
