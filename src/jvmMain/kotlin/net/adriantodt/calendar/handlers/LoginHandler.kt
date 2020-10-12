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

/**
 * Handler of POST '/api/login' endpoint.
 */
class LoginHandler(
    private val dao: DataAccessObject,
    private val algorithm: Algorithm
) : Handler {
    override fun handle(ctx: Context) {
        // Decode the Login Request from the Body of the Request using KotlinX Serialization
        val req = Json.decodeFromString<LoginRequest>(ctx.body())

        // Check if the username and password are not empty
        if (req.username.isEmpty() || req.password.isEmpty()) {
            ctx.contentType("application/json").result(
                Json.encodeToString(
                    LoginResponse(
                        status = LoginResponseStatus.INCORRECT_CREDENTIALS,
                        token = null
                    )
                )
            )
            return
        }

        // Check if the user exists and the password is valid.
        val user = dao.getUserByUsername(req.username)
        if (user == null || !DataAccessObject.checkPassword(user.hashedPassword, req.password)) {
            ctx.contentType("application/json").result(
                Json.encodeToString(
                    LoginResponse(
                        status = LoginResponseStatus.INCORRECT_CREDENTIALS,
                        token = null
                    )
                )
            )
            return
        }

        // Create and encode the Login Response into a Json
        ctx.contentType("application/json").result(
            Json.encodeToString(
                LoginResponse(
                    status = LoginResponseStatus.OK,
                    token = JWT.create() // Create and sign a JWT token for authentication.
                        .withIssuer("kalendar")
                        .withSubject(user.id)
                        .withIssuedAt(Date())
                        .sign(algorithm)
                )
            )
        )
    }
}
