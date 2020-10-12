package net.adriantodt.calendar.handlers

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.github.nanoflakes.NanoflakeLocalGenerator
import io.javalin.http.Context
import io.javalin.http.Handler
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.adriantodt.calendar.dao.DataAccessObject
import net.adriantodt.calendar.model.User
import net.adriantodt.calendar.rest.register.RegisterRequest
import net.adriantodt.calendar.rest.register.RegisterResponse
import net.adriantodt.calendar.rest.register.RegisterResponseStatus
import java.util.*

class RegisterHandler(
    private val dao: DataAccessObject,
    private val algorithm: Algorithm,
    private val generator: NanoflakeLocalGenerator
) : Handler {
    override fun handle(ctx: Context) {
        val registerReq = Json.decodeFromString<RegisterRequest>(ctx.body())

        if (registerReq.username.isEmpty() || registerReq.password.isEmpty()) {
            ctx.result(
                Json.encodeToString(
                    RegisterResponse(
                        status = RegisterResponseStatus.USERNAME_ALREADY_EXISTS,
                        token = null
                    )
                )
            )
            return
        }

        val user = dao.getUserByUsername(registerReq.username)

        if (user != null) {
            ctx.result(
                Json.encodeToString(
                    RegisterResponse(
                        status = RegisterResponseStatus.USERNAME_ALREADY_EXISTS,
                        token = null
                    )
                )
            )
            return
        }

        val id = generator.next()

        val newUser = User(
            id = id.withRadix(36),
            username = registerReq.username,
            hashedPassword = DataAccessObject.hashPassword(registerReq.password)
        )

        dao.insertUser(newUser)

        val jwt = JWT.create()
            .withIssuer("kalendar")
            .withSubject(id.withRadix(36))
            .withIssuedAt(Date())
            //.withExpiresAt(Date.from(Instant.now().plus(60, ChronoUnit.DAYS)))
            .sign(algorithm)

        ctx.result(
            Json.encodeToString(
                RegisterResponse(
                    status = RegisterResponseStatus.OK,
                    token = jwt
                )
            )
        )
    }
}
