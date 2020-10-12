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

/**
 * Handler of POST '/api/register' endpoint.
 */
class RegisterHandler(
    private val dao: DataAccessObject,
    private val algorithm: Algorithm,
    private val generator: NanoflakeLocalGenerator
) : Handler {
    override fun handle(ctx: Context) {
        // Decode the Register Request from the Body of the Request using KotlinX Serialization
        val req = Json.decodeFromString<RegisterRequest>(ctx.body())

        // Check if the username and password are not empty and if there's no user with the same username.
        if (req.username.isEmpty() || req.password.isEmpty() || dao.getUserByUsername(req.username) != null) {
            ctx.contentType("application/json").result(
                Json.encodeToString(
                    RegisterResponse(
                        status = RegisterResponseStatus.USERNAME_ALREADY_EXISTS,
                        token = null
                    )
                )
            )
            return
        }

        // Create the new User, then insert it in the database
        val u = User(
            id = generator.next().withRadix(36),
            username = req.username,
            hashedPassword = DataAccessObject.hashPassword(req.password)
        )
        dao.insertUser(u)

        // Create and encode the Register Response into a Json
        ctx.contentType("application/json").result(
            Json.encodeToString(
                RegisterResponse(
                    status = RegisterResponseStatus.OK,
                    token = JWT.create() // Create and sign a JWT token for authentication.
                        .withIssuer("kalendar")
                        .withSubject(u.id)
                        .withIssuedAt(Date())
                        .sign(algorithm)
                )
            )
        )
    }
}
