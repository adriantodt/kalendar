package net.adriantodt.calendar.handlers.mock

import com.github.nanoflakes.NanoflakeGenerator
import com.github.nanoflakes.currentTimeMillis
import io.javalin.http.Context
import io.javalin.http.Handler
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.adriantodt.calendar.model.CalendarEvent
import net.adriantodt.calendar.rest.events.EventsResponse
import kotlin.random.Random
import kotlin.random.nextInt

class MockEventsHandler(private val generator: NanoflakeGenerator) : Handler {
    private val titles = arrayOf(
        "My cool event", "Amazing event", "Important Reminder", "Friend's Birthday", "Anniversary"
    )
    private val descriptions = arrayOf(
        "", "Don't. Forget.", "It'll be amazing.", "This is really, really important."
    )

    override fun handle(ctx: Context) {
        val millis = currentTimeMillis()
        val mock = (0..Random.nextInt(2..6)).map {
            val start = millis + Random.nextLong(80000000, 2560000000)
            val end = start + Random.nextLong(80000000, 2560000000)

            CalendarEvent(
                id = generator.next().withRadix(36),
                authorId = "",
                title = titles.random(),
                description = descriptions.random(),
                startDateTime = start,
                endDateTime = end,
            )
        }

        ctx.result(Json.encodeToString(EventsResponse(mock)))
    }
}
