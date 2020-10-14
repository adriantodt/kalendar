/*
 * Esse arquivo é basicamente o cantinho do jQuery.
 *
 * Como interfacear o Bootstrap as vezes requer jQuery, decidi colocar todas as funções em um único arquivo.
 */
package net.adriantodt.calendar

import js.externals.jquery.jQuery

inline fun onModalShown(selector: String, crossinline block: () -> Unit) {
    jQuery(selector).on("shown.bs.modal") { _, _ -> block() }
}

fun hideModalBySelector(selector: String) {
    jQuery(selector).asDynamic().modal("hide") // essa função é equivalente a $(selector).modal('hide')
}

fun showModalBySelector(selector: String) {
    jQuery(selector).asDynamic().modal("show") // essa função é equivalente a $(selector).modal('show')
}
