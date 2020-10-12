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

class PatchEventHandler(private val dao: DataAccessObject, private val verifier: JWTVerifier) : Handler {
    override fun handle(ctx: Context) {
        val userId = Authorizations.validate(ctx, dao, verifier)

        val id = ctx.pathParam("id")

        val event = dao.getEvent(id)

        if (event == null || event.authorId != userId) {
            ctx.result(
                Json.encodeToString(
                    PatchEventResponse(
                        status = PatchEventResponseStatus.EVENT_DOES_NOT_EXIST,
                        event = null
                    )
                )
            )
            return
        }

        val eventReq = Json.decodeFromString<PatchEventRequest>(ctx.body())

        val patchedEvent = CalendarEvent(
            id = id,
            authorId = event.authorId,
            title = eventReq.title,
            description = eventReq.description,
            startDateTime = eventReq.startDateTime,
            endDateTime = eventReq.endDateTime
        )

        dao.updateEvent(patchedEvent)

        ctx.result(
            Json.encodeToString(
                PatchEventResponse(
                    status = PatchEventResponseStatus.OK,
                    event = patchedEvent
                )
            )
        )
    }
}
