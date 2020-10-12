package net.adriantodt.calendar.handlers

import com.auth0.jwt.JWTVerifier
import io.javalin.http.Context
import io.javalin.http.Handler
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.adriantodt.calendar.dao.DataAccessObject
import net.adriantodt.calendar.rest.events.crud.GetEventResponse
import net.adriantodt.calendar.rest.events.crud.GetEventResponseStatus

class GetEventHandler(private val dao: DataAccessObject, private val verifier: JWTVerifier) : Handler {
    override fun handle(ctx: Context) {
        val userId = Authorizations.validate(ctx, dao, verifier)

        val id = ctx.pathParam("id")

        val event = dao.getEvent(id)

        if (event == null || event.authorId != userId) {
            ctx.result(
                Json.encodeToString(
                    GetEventResponse(
                        status = GetEventResponseStatus.EVENT_DOES_NOT_EXIST,
                        event = null,
                    )
                )
            )
            return
        }

        ctx.result(
            Json.encodeToString(
                GetEventResponse(
                    status = GetEventResponseStatus.OK,
                    event = event,
                )
            )
        )
    }
}
