package net.adriantodt.calendar.pages

import kotlinx.browser.document
import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlinx.dom.addClass
import kotlinx.dom.removeClass
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.adriantodt.calendar.headers
import net.adriantodt.calendar.onModalShown
import net.adriantodt.calendar.rest.login.LoginRequest
import net.adriantodt.calendar.rest.login.LoginResponse
import net.adriantodt.calendar.rest.login.LoginResponseStatus
import net.adriantodt.calendar.rest.register.RegisterRequest
import net.adriantodt.calendar.rest.register.RegisterResponse
import net.adriantodt.calendar.rest.register.RegisterResponseStatus
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.EventListener
import org.w3c.dom.events.KeyboardEvent
import org.w3c.dom.get
import org.w3c.dom.set
import org.w3c.fetch.RequestInit

/**
 * Função principal da página de landing e login.
 * Responsável por iniciar a interatividade da página.
 */
fun landingPage() {
    // Caso o usuário já tenha um Token, levar ele para a página do usuário.
    if (localStorage["token"] != null) {
        window.location.href = "/me"
        return
    }

    // Configuração dos Modals
    configureLoginModal()
    configureRegisterModal()
}

/**
 * Função de configuração do modal de registro.
 */
private fun configureRegisterModal() {
    // Cache em elementos nomeados por ID
    val username = document.getElementById("registerInputUsername") as HTMLInputElement
    val password = document.getElementById("registerInputPassword") as HTMLInputElement
    val submitBtn = document.getElementById("registerSubmit") as HTMLButtonElement

    // Função a ser executada quando o formulário é enviado.
    fun onSubmit() {
        // Remover todas as classes de validação, antes de enviar o formulário.
        username.removeClass("is-valid", "is-invalid")

        // Criação de uma request de registro do usuário.
        val req = RequestInit(
            method = "post",
            headers = headers(
                "Accept" to "application/json, text/plain, */*",
                "Content-Type" to "application/json"
            ),
            body = Json.encodeToString(
                RegisterRequest(
                    username = username.value,
                    password = password.value
                )
            )
        )

        // Envio da request
        window.fetch("/api/register", req).then { r ->
            if (r.ok) {
                r.text().then { s ->
                    // Decodificar a resposta de registro de usuário, e lidar com as respostas
                    val login = Json.decodeFromString<RegisterResponse>(s)
                    when (login.status) {
                        RegisterResponseStatus.OK -> {
                            // Adicionar classe de validação para indicar o sucesso.
                            username.addClass("is-valid")

                            // Guardar o token no localStorage e redirecionar usuário.
                            val token = login.token ?: error("Internal: Register response is OK but no token was sent.")
                            localStorage["token"] = token
                            window.location.href = "/me"
                        }
                        RegisterResponseStatus.USERNAME_ALREADY_EXISTS -> {
                            // Adicionar classe de validação para indicar o erro.
                            username.addClass("is-invalid")
                        }
                    }
                }.catch {
                    println("Um erro aconteceu no POST '/api/register': ${it.stackTraceToString()}")
                }
            } else {
                println("Resposta inesperada do POST '/api/register': ${r.status} ${r.statusText}")
            }
        }.catch {
            println("Um erro aconteceu no POST '/api/register': ${it.stackTraceToString()}")
        }
    }

    // Configurar o modal para focar na Input de usuário quando mostrada.
    onModalShown("#registerModal") { username.focus() }

    // Executar o request quando o usuário clicar no botão ou der Enter em alguma das inputs.
    submitBtn.onclick = { onSubmit() }
    username.addEventListener("keyup", EventListener { if ((it as KeyboardEvent).keyCode == 13) onSubmit() })
    password.addEventListener("keyup", EventListener { if ((it as KeyboardEvent).keyCode == 13) onSubmit() })
}

/**
 * Função de configuração do modal de login.
 */
private fun configureLoginModal() {
    // Cache em elementos nomeados por ID
    val username = document.getElementById("loginInputUsername") as HTMLInputElement
    val password = document.getElementById("loginInputPassword") as HTMLInputElement
    val submitBtn = document.getElementById("loginSubmit") as HTMLButtonElement

    // Função a ser executada quando o formulário é enviado.
    fun onSubmit() {
        // Remover todas as classes de validação, antes de enviar o formulário.
        username.removeClass("is-valid", "is-invalid")

        // Criação de uma request de registro do usuário.
        val req = RequestInit(
            method = "post",
            headers = headers(
                "Accept" to "application/json, text/plain, */*",
                "Content-Type" to "application/json"
            ),
            body = Json.encodeToString(
                LoginRequest(
                    username.value,
                    password.value
                )
            )
        )

        // Envio da request
        window.fetch("/api/login", req).then { r ->
            if (r.ok) {
                r.text().then { s ->
                    // Decodificar a resposta de registro de usuário, e lidar com as respostas
                    val login = Json.decodeFromString<LoginResponse>(s)
                    when (login.status) {
                        LoginResponseStatus.OK -> {
                            // Adicionar classe de validação para indicar o sucesso.
                            username.addClass("is-valid")

                            // Guardar o token no localStorage e redirecionar usuário.
                            val token = login.token ?: error("Internal: Login response is OK but no token was sent.")
                            localStorage["token"] = token
                            // if (remindCheck.checked) // This doesn't work atm

                            window.location.href = "/me"
                        }
                        LoginResponseStatus.INCORRECT_CREDENTIALS -> {
                            // Adicionar classe de validação para indicar o erro.
                            username.addClass("is-invalid")
                        }
                    }
                }.catch {
                    println("Um erro aconteceu no POST '/api/login': ${it.stackTraceToString()}")
                }
            } else {
                println("Resposta inesperada do POST '/api/login': ${r.status} ${r.statusText}")
            }
        }.catch {
            println("Um erro aconteceu no POST '/api/login': ${it.stackTraceToString()}")
        }
    }

    // Configurar o modal para focar na Input de usuário quando mostrada.
    onModalShown("#loginModal") { username.focus() }

    // Executar o request quando o usuário clicar no botão ou der Enter em alguma das inputs.
    submitBtn.onclick = { onSubmit() }
    username.addEventListener("keyup", EventListener { if ((it as KeyboardEvent).keyCode == 13) onSubmit() })
    password.addEventListener("keyup", EventListener { if ((it as KeyboardEvent).keyCode == 13) onSubmit() })
}
