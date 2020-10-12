package net.adriantodt.calendar.handlers

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.javalin.http.Context
import io.javalin.http.Handler
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.adriantodt.calendar.dao.DataAccessObject
import net.adriantodt.calendar.rest.login.LoginRequest
import net.adriantodt.calendar.rest.login.LoginResponse
import net.adriantodt.calendar.rest.login.LoginResponseStatus
import java.util.*

class LoginHandler(private val dao: DataAccessObject, private val algorithm: Algorithm) : Handler {
    override fun handle(ctx: Context) {
        val loginReq = Json.decodeFromString<LoginRequest>(ctx.body())

        if (loginReq.username.isEmpty() || loginReq.password.isEmpty()) {
            ctx.result(
                Json.encodeToString(
                    LoginResponse(
                        status = LoginResponseStatus.INCORRECT_CREDENTIALS,
                        token = null
                    )
                )
            )
            return
        }

        val user = dao.getUserByUsername(loginReq.username)

        if (user == null || user.hashedPassword != DataAccessObject.hashPassword(loginReq.password)) {
            ctx.result(
                Json.encodeToString(
                    LoginResponse(
                        status = LoginResponseStatus.INCORRECT_CREDENTIALS,
                        token = null
                    )
                )
            )
            return
        }

        val jwt = JWT.create()
            .withIssuer("kalendar")
            .withSubject(user.id)
            .withIssuedAt(Date())
            //.withExpiresAt(Date.from(Instant.now().plus(60, ChronoUnit.DAYS)))
            .sign(algorithm)

        ctx.result(
            Json.encodeToString(
                LoginResponse(
                    status = LoginResponseStatus.OK,
                    token = jwt
                )
            )
        )
    }
}
