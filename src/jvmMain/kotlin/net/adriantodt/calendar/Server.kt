package net.adriantodt.calendar

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.github.nanoflakes.NanoflakeLocalGenerator
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.*
import net.adriantodt.calendar.dao.JdbiDataAccessObject
import net.adriantodt.calendar.handlers.*

fun main() {
    // Common objects for the route handlers are initialized here
    val dao = JdbiDataAccessObject("jdbc:postgresql://localhost:15432/kalendar?user=postgres&password=tokenlabkalendar")
    val generator = NanoflakeLocalGenerator(KALENDAR_EPOCH, 0)
    val algorithm = Algorithm.HMAC256(System.getenv("kalendar_secret") ?: "kalendar_is_very_secret_indeed")
    val verifier = JWT.require(algorithm).withIssuer("kalendar").build()

    // Configure the Javalin app
    val app = Javalin.create { cfg ->
        cfg.addStaticFiles("/static")
        cfg.showJavalinBanner = false
    }

    // Configure the routes
    app.routes {
        path("api") {
            post("login", LoginHandler(dao, algorithm))
            post("register", RegisterHandler(dao, algorithm, generator))
            get("events", EventsHandler(dao, verifier))
            //get("events", MockEventsHandler(generator))
            get("event/:id", GetEventHandler(dao, verifier))
            post("event", CreateEventHandler(dao, generator, verifier))
            patch("event/:id", PatchEventHandler(dao, verifier))
            delete("event/:id", DeleteEventHandler(dao, verifier))
        }
    }

    // Start the app
    app.start(System.getenv("kalendar_port")?.toIntOrNull() ?: 8080)
}