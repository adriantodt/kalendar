package net.adriantodt.calendar.handlers

import com.auth0.jwt.JWTVerifier
import io.javalin.http.Context
import io.javalin.http.Handler
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.adriantodt.calendar.dao.DataAccessObject
import net.adriantodt.calendar.rest.events.crud.DeleteEventResponse
import net.adriantodt.calendar.rest.events.crud.DeleteEventResponseStatus

class DeleteEventHandler(private val dao: DataAccessObject, private val verifier: JWTVerifier) : Handler {
    override fun handle(ctx: Context) {
        val userId = Authorizations.validate(ctx, dao, verifier)

        val id = ctx.pathParam("id")

        val event = dao.getEvent(id)

        if (event == null || event.authorId != userId) {
            ctx.result(
                Json.encodeToString(
                    DeleteEventResponse(
                        status = DeleteEventResponseStatus.EVENT_DOES_NOT_EXIST
                    )
                )
            )
            return
        }

        dao.deleteEvent(id)

        ctx.result(
            Json.encodeToString(
                DeleteEventResponse(
                    status = DeleteEventResponseStatus.OK
                )
            )
        )
    }
}
