package net.adriantodt.calendar

import js.externals.jquery.jQuery

fun onModelShown(selector: String, block: () -> Unit) {
    /*
     * This is my attempt to try to use as little jQuery as possible.
     * Sadly, interfacing with Bootstrap requires this.
     */
    jQuery(selector).on("shown.bs.modal") { _, _ ->
        block()
    }
}