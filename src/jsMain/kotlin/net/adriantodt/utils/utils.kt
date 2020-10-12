package net.adriantodt.utils

import org.w3c.fetch.Headers

fun headers(vararg pairs: Pair<String, String>) = Headers().apply {
    for ((k, v) in pairs) append(k, v)
}