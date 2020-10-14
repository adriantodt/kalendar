package net.adriantodt.calendar

import kotlinx.browser.document
import kotlinx.browser.window
import net.adriantodt.calendar.pages.landingPage
import net.adriantodt.calendar.pages.mePage
import org.w3c.dom.HTMLElement
import org.w3c.dom.get

/**
 * Função Main do Kalendar.
 * Essa função procura um atributo "data-app" no <html> para decidir qual rotina de página iniciar,
 */
fun main() {
    try {
        window.onload = {
            val html = document.head!!.parentElement as HTMLElement
            when (html.dataset["app"]) {
                "landing" -> landingPage()
                "me" -> mePage()
                else -> {
                    throw IllegalStateException("Couldn't recognize the page of the application.")
                }
            }
        }
    } catch (it: Throwable) {
        println("Um erro aconteceu na região principal do código: ${it.stackTraceToString()}")
    }
}