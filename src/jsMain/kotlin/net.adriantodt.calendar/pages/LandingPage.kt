package net.adriantodt.calendar.pages

import kotlinx.browser.document
import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlinx.dom.addClass
import kotlinx.dom.removeClass
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.adriantodt.calendar.onModelShown
import net.adriantodt.calendar.rest.login.LoginRequest
import net.adriantodt.calendar.rest.login.LoginResponse
import net.adriantodt.calendar.rest.login.LoginResponseStatus
import net.adriantodt.calendar.rest.register.RegisterRequest
import net.adriantodt.calendar.rest.register.RegisterResponse
import net.adriantodt.calendar.rest.register.RegisterResponseStatus
import net.adriantodt.utils.headers
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.EventListener
import org.w3c.dom.events.KeyboardEvent
import org.w3c.dom.get
import org.w3c.dom.set
import org.w3c.fetch.RequestInit

fun landingPage() {
    localStorage["token"]?.let { // token ->
        //sessionStorage["token"] = token
        window.location.href = "/me"
        return
    }
    loginModal()
    registerModal()
}

private fun registerModal() {
    val username = document.getElementById("registerInputUsername") as HTMLInputElement
    val password = document.getElementById("registerInputPassword") as HTMLInputElement
    val submitBtn = document.getElementById("registerSubmit") as HTMLButtonElement

    onModelShown("#registerModal") {
        username.focus()
    }

    fun onSubmit() {
        username.removeClass("is-valid", "is-invalid")

        val loginReq = RegisterRequest(
            username.value,
            password.value
        )

        val req = RequestInit(
            method = "post",
            headers = headers(
                "Accept" to "application/json, text/plain, */*",
                "Content-Type" to "application/json"
            ),
            body = Json.encodeToString(loginReq)
        )

        window.fetch("/api/register", req).then { r ->
            if (r.ok) {
                r.text().then { s ->
                    val login = Json.decodeFromString<RegisterResponse>(s)
                    when (login.status) {
                        RegisterResponseStatus.OK -> {
                            username.addClass("is-valid")
                            val token = login.token ?: error("Internal: Register response is OK but no token was sent.")
                            localStorage["token"] = token
                            // if (remindCheck.checked) // This doesn't work atm

                            window.location.href = "/me"
                        }
                        RegisterResponseStatus.USERNAME_ALREADY_EXISTS -> {
                            username.addClass("is-invalid")
                        }
                    }
                }
            }
        }
    }

    submitBtn.onclick = { onSubmit() }
    username.addEventListener("keyup", EventListener { if ((it as KeyboardEvent).keyCode == 13) onSubmit() })
    password.addEventListener("keyup", EventListener { if ((it as KeyboardEvent).keyCode == 13) onSubmit() })
}


private fun loginModal() {
    val username = document.getElementById("loginInputUsername") as HTMLInputElement
    val password = document.getElementById("loginInputPassword") as HTMLInputElement
    val submitBtn = document.getElementById("loginSubmit") as HTMLButtonElement

    onModelShown("#loginModal") {
        username.focus()
    }

    fun onSubmit() {
        username.removeClass("is-valid", "is-invalid")

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

        window.fetch("/api/login", req).then { r ->
            if (r.ok) {
                r.text().then { s ->
                    val login = Json.decodeFromString<LoginResponse>(s)
                    when (login.status) {
                        LoginResponseStatus.OK -> {
                            username.addClass("is-valid")
                            val token = login.token ?: error("Internal: Register response is OK but no token was sent.")
                            localStorage["token"] = token
                            // if (remindCheck.checked) // This doesn't work atm

                            window.location.href = "/me"
                        }
                        LoginResponseStatus.INCORRECT_CREDENTIALS -> {
                            username.addClass("is-invalid")
                        }
                    }
                }
            }
        }
    }

    submitBtn.onclick = { onSubmit() }
    username.addEventListener("keyup", EventListener { if ((it as KeyboardEvent).keyCode == 13) onSubmit() })
    password.addEventListener("keyup", EventListener { if ((it as KeyboardEvent).keyCode == 13) onSubmit() })
}
