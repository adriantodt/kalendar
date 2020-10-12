package net.adriantodt.calendar.pages

import Rome
import js.externals.jquery.jQuery
import kotlinx.browser.document
import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlinx.dom.addClass
import kotlinx.dom.removeClass
import kotlinx.html.*
import kotlinx.html.dom.append
import kotlinx.html.js.div
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.adriantodt.calendar.model.CalendarEvent
import net.adriantodt.calendar.onModelShown
import net.adriantodt.calendar.rest.events.EventsResponse
import net.adriantodt.calendar.rest.events.crud.CreateEventRequest
import net.adriantodt.calendar.rest.events.crud.PatchEventRequest
import net.adriantodt.utils.headers
import org.w3c.dom.*
import org.w3c.dom.events.EventListener
import org.w3c.dom.events.KeyboardEvent
import org.w3c.fetch.RequestInit
import rome
import romeOptions
import kotlin.js.Date

fun mePage() {
    val token = localStorage["token"]
    if (token == null) {
        window.location.href = "/landing"
        return
    }

    (document.getElementById("logoutBtn") as HTMLAnchorElement).onclick = {
        logoutAndRedirect()
    }

    fetchAndFill(token)
    createEventModal(token)

    onModelShown("#createEventModal") {
        (document.getElementById("createEventInputTitle") as HTMLInputElement).focus()
    }
    onModelShown("#editEventModal") {
        (document.getElementById("editEventInputTitle") as HTMLInputElement).focus()
    }
    onModelShown("#deleteEventModal") {
        (document.getElementById("deleteEventSubmit") as HTMLButtonElement).focus()
    }
}

private fun fetchAndFill(token: String) {
    val req = RequestInit(
        method = "get",
        headers = headers(
            "Accept" to "application/json, text/plain, */*",
            "Authorization" to "Bearer $token"
        )
    )
    window.fetch("/api/events", req).then { r ->
        if (r.ok) {
            r.text().then { s ->
                val row = document.getElementById("nextEventsRow") as HTMLDivElement
                generateSequence { row.firstChild }.forEach { row.removeChild(it) }
                val list = Json.decodeFromString<EventsResponse>(s).events
                if (list.isNotEmpty()) {
                    for (event in list.sorted()) {
                        appendAndEnableCard(token, row, event)
                    }
                } else {
                    row.append.div("col") {
                        div("alert alert-secondary text-center") {
                            +"Parece que você não tem nenhum evento! Clique no botão \"Novo\" e crie o seu primeiro evento."
                        }
                    }
                }
            }
        }
        if (r.status == 403.toShort()) {
            logoutAndRedirect()
        }
    }
}

private fun appendAndEnableCard(token: String, node: HTMLElement, event: CalendarEvent) {
    val div = node.append.div("col-sm-12 col-md-6 col-lg-4 p-1") {
        div("card") {
            div("card-body") {
                h5("card-title") { +event.title }
                //h6("card-subtitle mb-2 text-muted") { +"""Card subtitle""" }
                if (event.description.isNotBlank()) {
                    p("card-text") { +event.description }
                }
                div {
                    i("material-icons md-inherit") { +"notifications" }
                    +" "
                    +Date(event.startDateTime).toLocaleString("pt-BR")
                }
                div {
                    i("material-icons md-inherit") { +"alarm" }
                    +" "
                    +Date(event.endDateTime).toLocaleString("pt-BR")
                }
                div("text-right") {
                    a(classes = "card-link text-info") {
                        href = "#"
                        attributes["data-linktype"] = "edit"
                        i("material-icons") { +"edit" }
                    }
                    a(classes = "card-link text-danger") {
                        href = "#"
                        attributes["data-linktype"] = "delete"
                        i("material-icons") { +"delete" }
                    }
                }
            }
        }
    }

    for (anchor in div.querySelectorAll("a.card-link").asList().filterIsInstance<HTMLAnchorElement>()) {
        when (anchor.dataset["linktype"]) {
            "edit" -> editModal(token, anchor, event)
            "delete" -> deleteModal(token, anchor, event)
        }
    }
}

private fun hideModalBySelector(selector: String) {
    /*
     * This is my attempt to try to use as little jQuery as possible.
     * Sadly, interfacing with Bootstrap requires this.
     */
    jQuery(selector).asDynamic().modal("hide") // essa função é equivalente a $(selector).modal('hide')
}

private fun showModalBySelector(selector: String) {
    /*
     * This is my attempt to try to use as little jQuery as possible.
     * Sadly, interfacing with Bootstrap requires this.
     */
    jQuery(selector).asDynamic().modal("show") // essa função é equivalente a $(selector).modal('show')
}

private fun createEventModal(token: String) {
    val title = document.getElementById("createEventInputTitle") as HTMLInputElement
    val description = document.getElementById("createEventInputDescription") as HTMLTextAreaElement
    val start = document.getElementById("createEventInputStartDateTime") as HTMLInputElement
    val end = document.getElementById("createEventInputEndDateTime") as HTMLInputElement
    val submitBtn = document.getElementById("createEventSubmit") as HTMLButtonElement

    val startRome = rome(start)
    val endRome = rome(end)

    fun onSubmit() {
        if (!validateEventModal(title, start, startRome, end, endRome)) {
            return
        }

        val req = RequestInit(
            method = "post",
            headers = headers(
                "Accept" to "application/json, text/plain, */*",
                "Content-Type" to "application/json",
                "Authorization" to "Bearer $token"
            ),
            body = Json.encodeToString(
                CreateEventRequest(
                    title = title.value,
                    description = description.value,
                    startDateTime = startRome.getMoment()!!.valueOf().toLong(),
                    endDateTime = endRome.getMoment()!!.valueOf().toLong(),
                )
            )
        )

        window.fetch("/api/event", req).then { r ->
            if (r.ok) {
                fetchAndFill(token)
                hideModalBySelector("#createEventModal")

                // Clear the modal
                title.value = ""
                description.value = ""
                start.value = ""
                end.value = ""
                startRome.options()
                endRome.options()
            }
            if (r.status == 403.toShort()) {
                logoutAndRedirect()
            }
        }
    }

    submitBtn.onclick = { onSubmit() }
    title.addEventListener("keyup", EventListener { if ((it as KeyboardEvent).keyCode == 13) onSubmit() })
    start.addEventListener("keyup", EventListener { if ((it as KeyboardEvent).keyCode == 13) onSubmit() })
    end.addEventListener("keyup", EventListener { if ((it as KeyboardEvent).keyCode == 13) onSubmit() })
}

private fun editModal(token: String, anchor: HTMLAnchorElement, event: CalendarEvent) {
    val title = document.getElementById("editEventInputTitle") as HTMLInputElement
    val description = document.getElementById("editEventInputDescription") as HTMLTextAreaElement
    val start = document.getElementById("editEventInputStartDateTime") as HTMLInputElement
    val end = document.getElementById("editEventInputEndDateTime") as HTMLInputElement
    val submitBtn = document.getElementById("editEventSubmit") as HTMLButtonElement

    val startRome = rome(start)
    val endRome = rome(end)

    anchor.onclick = {
        title.value = event.title
        startRome.options(romeOptions {
            initialValue = Date(event.startDateTime)
        })
        endRome.options(romeOptions {
            initialValue = Date(event.endDateTime)
        })
        description.value = event.description

        fun onSubmit() {
            if (!validateEventModal(title, start, startRome, end, endRome)) {
                return
            }

            val req = RequestInit(
                method = "patch",
                headers = headers(
                    "Accept" to "application/json, text/plain, */*",
                    "Content-Type" to "application/json",
                    "Authorization" to "Bearer $token"
                ),
                body = Json.encodeToString(
                    PatchEventRequest(
                        title = title.value,
                        description = description.value,
                        startDateTime = startRome.getMoment()!!.valueOf().toLong(),
                        endDateTime = endRome.getMoment()!!.valueOf().toLong(),
                    )
                )
            )

            window.fetch("/api/event/${event.id}", req).then { r ->
                if (r.ok) {
                    fetchAndFill(token)
                    hideModalBySelector("#editEventModal")
                }
                if (r.status == 403.toShort()) {
                    logoutAndRedirect()
                }
            }
        }

        submitBtn.onclick = { onSubmit() }
        title.addEventListener("keyup", EventListener { if ((it as KeyboardEvent).keyCode == 13) onSubmit() })
        start.addEventListener("keyup", EventListener { if ((it as KeyboardEvent).keyCode == 13) onSubmit() })
        end.addEventListener("keyup", EventListener { if ((it as KeyboardEvent).keyCode == 13) onSubmit() })

        showModalBySelector("#editEventModal")
    }
}

private fun deleteModal(token: String, anchor: HTMLAnchorElement, event: CalendarEvent) {
    val submitBtn = document.getElementById("deleteEventSubmit") as HTMLButtonElement
    anchor.onclick = {
        submitBtn.onclick = {
            val req = RequestInit(
                method = "delete",
                headers = headers(
                    "Accept" to "application/json, text/plain, */*",
                    "Content-Type" to "application/json",
                    "Authorization" to "Bearer $token"
                )
            )

            window.fetch("/api/event/${event.id}", req).then { r ->
                if (r.ok) {
                    fetchAndFill(token)
                    hideModalBySelector("#deleteEventModal")
                }
                if (r.status == 403.toShort()) {
                    logoutAndRedirect()
                }
            }
        }

        showModalBySelector("#deleteEventModal")
    }
}

private fun logoutAndRedirect() {
    localStorage.removeItem("token")
    window.location.href = "/"
}

private fun validateEventModal(
    title: HTMLInputElement,
    start: HTMLInputElement,
    startRome: Rome,
    end: HTMLInputElement,
    endRome: Rome
): Boolean {
    title.removeClass("is-valid", "is-invalid")
    start.removeClass("is-valid", "is-invalid")
    end.removeClass("is-valid", "is-invalid")

    var mayContinue = true
    if (title.value.isBlank()) {
        mayContinue = false
        title.addClass("is-invalid")
    }
    if (startRome.getMoment() == null) {
        mayContinue = false
        start.addClass("is-invalid")
    }
    if (endRome.getMoment() == null) {
        mayContinue = false
        end.addClass("is-invalid")
    }

    return mayContinue
}
