package io.github.cdsap.parsetimeline

fun String.removeExtension(): String {
    val index = this.indexOfFirst { it == '.' }
    return if (index != -1) this.substring(0, index) else this
}
