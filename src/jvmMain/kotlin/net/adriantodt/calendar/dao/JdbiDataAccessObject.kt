package net.adriantodt.calendar.dao

import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import net.adriantodt.calendar.model.CalendarEvent
import net.adriantodt.calendar.model.User
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.*
import org.jdbi.v3.jackson2.Jackson2Config
import org.jdbi.v3.jackson2.Jackson2Plugin
import org.jdbi.v3.postgres.PostgresPlugin

class JdbiDataAccessObject(url: String) : DataAccessObject {
    private val jdbi = Jdbi.create(url)
        .installPlugin(PostgresPlugin())
        .installPlugin(Jackson2Plugin())
        .installPlugin(KotlinPlugin())
        .apply { getConfig(Jackson2Config::class.java).mapper.registerKotlinModule() }

    init {
        jdbi.useHandleUnchecked { h ->
            h.execute(
                """
                CREATE TABLE IF NOT EXISTS kalendar_user(
                    id TEXT PRIMARY KEY,
                    username TEXT UNIQUE NOT NULL,
                    hashedPassword TEXT NOT NULL
                )
            """
            )

            h.execute(
                """
                CREATE TABLE IF NOT EXISTS kalendar_event(
                    id TEXT PRIMARY KEY,
                    authorId TEXT REFERENCES kalendar_user(id) ON DELETE CASCADE,
                    title TEXT NOT NULL,
                    description TEXT NOT NULL,
                    startDateTime BIGINT NOT NULL,
                    endDateTime BIGINT NOT NULL
                )
            """
            )
        }
    }

    override fun getEvent(id: String): CalendarEvent? {
        return jdbi.withHandleUnchecked {
            it.createQuery("SELECT * FROM kalendar_event WHERE id = :id")
                .bind("id", id)
                .mapTo<CalendarEvent>()
                .findOne()
                .orElse(null)
        }
    }

    override fun getEventsByAuthor(authorId: String): List<CalendarEvent> {
        return jdbi.withHandleUnchecked {
            it.createQuery("SELECT * FROM kalendar_event WHERE authorid = :authorId")
                .bind("authorId", authorId)
                .mapTo<CalendarEvent>()
                .list()
        }
    }

    override fun getUser(id: String): User? {
        return jdbi.withHandleUnchecked {
            it.createQuery("SELECT * FROM kalendar_user WHERE id = :id")
                .bind("id", id)
                .mapTo<User>()
                .findOne()
                .orElse(null)
        }
    }

    override fun getUserByUsername(username: String): User? {
        return jdbi.withHandleUnchecked {
            it.createQuery("SELECT * FROM kalendar_user WHERE username = :username")
                .bind("username", username)
                .mapTo<User>()
                .findOne()
                .orElse(null)
        }
    }

    override fun insertEvent(event: CalendarEvent) {
        jdbi.useHandleUnchecked {
            it.createUpdate(
                """
                INSERT INTO kalendar_event VALUES (:id, :authorId, :title, :description, :startDateTime, :endDateTime)
                """
            )
                .bindKotlin(event)
                .execute()
        }
    }

    override fun insertUser(user: User) {
        jdbi.useHandleUnchecked {
            it.createUpdate(
                """
                INSERT INTO kalendar_user VALUES (:id, :username, :hashedPassword)
                """
            )
                .bindKotlin(user)
                .execute()
        }
    }

    override fun updateEvent(event: CalendarEvent) {
        jdbi.useHandleUnchecked {
            it.createUpdate(
                """
                UPDATE kalendar_event SET 
                    title = :title,
                    description = :description,
                    startDateTime = :startDateTime,
                    endDateTime = :endDateTime
                WHERE id = :id
                """
            )
                .bindKotlin(event)
                .execute()
        }
    }

    override fun deleteEvent(id: String) {
        jdbi.useHandleUnchecked {
            it.execute("DELETE FROM kalendar_event WHERE id = ?", id)
        }
    }
}