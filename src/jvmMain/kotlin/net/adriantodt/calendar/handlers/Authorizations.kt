package net.adriantodt.calendar.handlers

import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.exceptions.JWTVerificationException
import io.javalin.http.Context
import io.javalin.http.ForbiddenResponse
import net.adriantodt.calendar.dao.DataAccessObject

/**
 * Object which validates "Authorization" headers.
 */
object Authorizations {
    fun validate(ctx: Context, dao: DataAccessObject, verifier: JWTVerifier): String {
        // The validation starts by extracting the "Authorization" header from the context.
        val authorization = ctx.header("Authorization") ?: throw ForbiddenResponse()

        // Its format must be "Bearer <JWT token here>"
        if (!authorization.startsWith("Bearer ")) throw ForbiddenResponse()

        // The JWT token is extracted from the header and validated by the JWT library.
        val userId = try {
            verifier.verify(authorization.removePrefix("Bearer ")).subject ?: throw ForbiddenResponse()
        } catch (e: JWTVerificationException) {
            throw ForbiddenResponse()
        }

        // Lastly, the User ID must exist in the database.
        if (dao.getUser(userId) == null) throw ForbiddenResponse()

        // If all the above conditions match, the user ID is returned.
        return userId
    }

}