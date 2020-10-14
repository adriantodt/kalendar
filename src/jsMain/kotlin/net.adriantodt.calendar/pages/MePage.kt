package net.adriantodt.calendar.pages

import js.externals.rome.Rome
import js.externals.rome.rome
import js.externals.rome.romeOptions
import kotlinx.browser.document
import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlinx.dom.addClass
import kotlinx.dom.clear
import kotlinx.dom.removeClass
import kotlinx.html.*
import kotlinx.html.dom.append
import kotlinx.html.js.div
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.adriantodt.calendar.headers
import net.adriantodt.calendar.hideModalBySelector
import net.adriantodt.calendar.model.CalendarEvent
import net.adriantodt.calendar.onModalShown
import net.adriantodt.calendar.rest.events.EventsResponse
import net.adriantodt.calendar.rest.events.crud.CreateEventRequest
import net.adriantodt.calendar.rest.events.crud.PatchEventRequest
import net.adriantodt.calendar.showModalBySelector
import org.w3c.dom.*
import org.w3c.dom.events.EventListener
import org.w3c.dom.events.KeyboardEvent
import org.w3c.fetch.RequestInit
import kotlin.js.Date

/**
 * Função principal da página do usuário.
 * Responsável por iniciar a interatividade da página.
 */
fun mePage() {
    // Obter o Token de acesso. Caso o usuário não tenha o token, voltar ele para a página de landing.
    val token = localStorage["token"]
    if (token == null) {
        window.location.href = "/"
        return
    }

    // Configurar o botão de logout para ser clicável
    (document.getElementById("logoutBtn") as HTMLAnchorElement).onclick = {
        logoutAndRedirect()
    }

    // Rotinas de configuração da página.
    fetchAndFillEvents(token)
    configureCreateEventModal(token)

    // Configurar os modals para focar em botões ou em inputs específicas quando mostradas.
    (document.getElementById("createEventInputTitle") as? HTMLInputElement)?.let { i ->
        onModalShown("#createEventModal", i::focus)
    }
    (document.getElementById("editEventInputTitle") as? HTMLInputElement)?.let { i ->
        onModalShown("#editEventModal", i::focus)
    }
    (document.getElementById("deleteEventSubmit") as? HTMLButtonElement)?.let { b ->
        onModalShown("#deleteEventModal", b::focus)
    }
}

/**
 * Função que busca todos os eventos do Back-end e gera cards para eles.
 */
private fun fetchAndFillEvents(token: String) {
    val row = document.getElementById("nextEventsRow") as HTMLDivElement

    // Criação de uma request de lista de eventos.
    val req = RequestInit(
        method = "get",
        headers = headers(
            "Accept" to "application/json, text/plain, */*",
            "Authorization" to "Bearer $token"
        )
    )

    // Envio da request
    window.fetch("/api/events", req).then { r ->
        if (r.ok) {
            r.text().then { s ->
                // Decodificar a resposta de lista de eventos, e lidar com as respostas
                val list = Json.decodeFromString<EventsResponse>(s).events

                // Remover o que está atualmente na página.
                row.clear()
                if (list.isNotEmpty()) {
                    // Adicionar cards de eventos se a lista não está vazia.
                    for (event in list.sorted()) {
                        appendAndEnableCard(token, row, event)
                    }
                } else {
                    // Se a lista estiver vazia, adicionar um alerta.
                    row.append.div("col") {
                        div("alert alert-secondary text-center") {
                            +"Parece que você não tem nenhum evento! Clique no botão \"Novo\" e crie o seu primeiro evento."
                        }
                    }
                }
            }.catch {
                println("Um erro aconteceu na conversão para texto do GET '/api/events': ${it.stackTraceToString()}")
            }
        } else if (r.status == 403.toShort()) {
            logoutAndRedirect()
        } else {
            println("Resposta inesperada do GET '/api/events': ${r.status} ${r.statusText}")
        }
    }.catch {
        println("Um erro aconteceu no GET '/api/events': ${it.stackTraceToString()}")
    }
}

/**
 * Função que gera um card baseado em um evento.
 */
private fun appendAndEnableCard(token: String, parent: HTMLElement, event: CalendarEvent) {
    /*
     * Essa função usa a biblioteca KotlinX HTML para geração de HTML do card, inserindo o card no DOM da página.
     * Uma limitação da biblioteca é que não é possível obter os elementos em si dentro da DSL.
     * Para resolver isso, após adicionado ao DOM, um query selector é executado, e um atributo dataset é usado
     * para configurar as ações dos botões.
     */
    val div = parent.append.div("col-sm-12 col-md-6 col-lg-4 p-1") {
        div("card") {
            div("card-body") {
                h5("card-title") { +event.title }
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
            "edit" -> configureEditModal(token, anchor, event)
            "delete" -> configureDeleteModal(token, anchor, event)
        }
    }
}

/**
 * Função de configuração do modal de criação de eventos.
 */
private fun configureCreateEventModal(token: String) {
    // Cache em elementos nomeados por ID
    val title = document.getElementById("createEventInputTitle") as HTMLInputElement
    val description = document.getElementById("createEventInputDescription") as HTMLTextAreaElement
    val start = document.getElementById("createEventInputStartDateTime") as HTMLInputElement
    val end = document.getElementById("createEventInputEndDateTime") as HTMLInputElement
    val submitBtn = document.getElementById("createEventSubmit") as HTMLButtonElement

    // Criar ou obter os controles do Rome
    val startRome = rome(start)
    val endRome = rome(end)

    // Função a ser executada quando o formulário é enviado.
    fun onSubmit() {
        // Se o formulário não é válido, apenas retornar.
        if (!validateEventModal(title, start, startRome, end, endRome)) return

        // Criação de uma request de criação de evento.
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

        // Envio da request
        window.fetch("/api/event", req).then { r ->
            if (r.ok) {
                // Recriar os cards e esconder modal
                fetchAndFillEvents(token)
                hideModalBySelector("#createEventModal")

                // Limpar o modal
                title.value = ""
                description.value = ""
                start.value = ""
                end.value = ""
                startRome.options(romeOptions())
                endRome.options(romeOptions())
            } else if (r.status == 403.toShort()) {
                logoutAndRedirect()
            } else {
                println("Resposta inesperada do POST '/api/event': ${r.status} ${r.statusText}")
            }
        }.catch {
            println("Um erro aconteceu no POST '/api/event': ${it.stackTraceToString()}")
        }
    }

    // Executar o request quando o usuário clicar no botão ou der Enter em alguma das inputs.
    submitBtn.onclick = { onSubmit() }
    title.addEventListener("keyup", EventListener { if ((it as KeyboardEvent).keyCode == 13) onSubmit() })
    start.addEventListener("keyup", EventListener { if ((it as KeyboardEvent).keyCode == 13) onSubmit() })
    end.addEventListener("keyup", EventListener { if ((it as KeyboardEvent).keyCode == 13) onSubmit() })
}

/**
 * Função de configuração do modal de edição de eventos.
 */
private fun configureEditModal(token: String, anchor: HTMLAnchorElement, event: CalendarEvent) {
    // Cache em elementos nomeados por ID
    val title = document.getElementById("editEventInputTitle") as HTMLInputElement
    val description = document.getElementById("editEventInputDescription") as HTMLTextAreaElement
    val start = document.getElementById("editEventInputStartDateTime") as HTMLInputElement
    val end = document.getElementById("editEventInputEndDateTime") as HTMLInputElement
    val submitBtn = document.getElementById("editEventSubmit") as HTMLButtonElement

    // Criar ou obter os controles do Rome
    val startRome = rome(start)
    val endRome = rome(end)

    // Essa função só será executada quando o link for clicado
    anchor.onclick = {
        // Preencher formulário de edição
        title.value = event.title
        startRome.options(romeOptions {
            initialValue = Date(event.startDateTime)
        })
        endRome.options(romeOptions {
            initialValue = Date(event.endDateTime)
        })
        description.value = event.description

        // Função a ser executada quando o formulário é enviado.
        fun onSubmit() {
            // Se o formulário não é válido, apenas retornar.
            if (!validateEventModal(title, start, startRome, end, endRome)) return

            // Criação de uma request de edição de evento.
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

            // Envio da request
            window.fetch("/api/event/${event.id}", req).then { r ->
                if (r.ok) {
                    // Recriar os cards e esconder modal
                    fetchAndFillEvents(token)
                    hideModalBySelector("#editEventModal")
                } else if (r.status == 403.toShort()) {
                    logoutAndRedirect()
                } else {
                    println("Resposta inesperada do PATCH '/api/event/{id}': ${r.status} ${r.statusText}")
                }
            }.catch {
                println("Um erro aconteceu no PATCH '/api/event/{id}': ${it.stackTraceToString()}")
            }
        }

        // Executar o request quando o usuário clicar no botão ou der Enter em alguma das inputs.
        submitBtn.onclick = { onSubmit() }
        title.addEventListener("keyup", EventListener { if ((it as KeyboardEvent).keyCode == 13) onSubmit() })
        start.addEventListener("keyup", EventListener { if ((it as KeyboardEvent).keyCode == 13) onSubmit() })
        end.addEventListener("keyup", EventListener { if ((it as KeyboardEvent).keyCode == 13) onSubmit() })

        // Modal está configurado, mostrar pro usuário
        showModalBySelector("#editEventModal")
    }
}

/**
 * Função de configuração do modal de exclusão de eventos.
 */
private fun configureDeleteModal(token: String, anchor: HTMLAnchorElement, event: CalendarEvent) {
    // Cache do botão de submit
    val submitBtn = document.getElementById("deleteEventSubmit") as HTMLButtonElement

    // Essa função só será executada quando o link for clicado
    anchor.onclick = {
        // Configurar o evento ao clicar o botão
        submitBtn.onclick = {
            // Criação de uma request de exclusão de evento.
            val req = RequestInit(
                method = "delete",
                headers = headers(
                    "Accept" to "application/json, text/plain, */*",
                    "Content-Type" to "application/json",
                    "Authorization" to "Bearer $token"
                )
            )

            // Envio da request
            window.fetch("/api/event/${event.id}", req).then { r ->
                if (r.ok) {
                    fetchAndFillEvents(token)
                    hideModalBySelector("#deleteEventModal")
                } else if (r.status == 403.toShort()) {
                    logoutAndRedirect()
                } else {
                    println("Resposta inesperada do DELETE '/api/event/{id}': ${r.status} ${r.statusText}")
                }
            }.catch {
                println("Um erro aconteceu no DELETE '/api/event/{id}': ${it.stackTraceToString()}")
            }
        }

        // Modal está configurado, mostrar pro usuário
        showModalBySelector("#deleteEventModal")
    }
}

/**
 * Função para limpar o token e enviar o usuário para a página de landing.
 */
private fun logoutAndRedirect() {
    localStorage.removeItem("token")
    window.location.href = "/"
}

/**
 * Função de validação dos modals de criação/edição de eventos.
 */
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
