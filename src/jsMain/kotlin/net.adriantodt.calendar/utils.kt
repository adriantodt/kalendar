package net.adriantodt.calendar

import org.w3c.fetch.Headers

/**
 * Utility method to create a "Headers" class by using a mapOf-like function.
 */
fun headers(vararg pairs: Pair<String, String>) = Headers().apply {
    for ((k, v) in pairs) append(k, v)
}