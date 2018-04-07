package com.aashley

import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths

fun main(args: Array<String>) {
    when {
        args.size > 1 -> println("Usage: jlox [script]")
        args.size == 1 -> runFile(args[0])
        else -> repl()
    }
}

var hadError = false

fun error(line: Int, message: String) {
    report(line, "", message)
}

private fun report(line: Int, where: String, message: String) {
    System.err.println("[line $line] Error $where: $message")
    hadError = true
}

private fun runFile(path: String) {
    val bytes = Files.readAllBytes(Paths.get(path))
    run(String(bytes, Charset.defaultCharset()))

    if (hadError) {
        System.exit(1)
    }
}

private fun run(source: String) {
    val scanner = Scanner(source)
    val tokens: List<Token> = scanner.scanTokens()

    tokens.forEach {
        println(it)
    }
}

private fun repl() {
    val input = InputStreamReader(System.`in`)
    val reader = BufferedReader(input)

    while(true) {
        print("λ> ")
        run(reader.readLine())

        hadError = false
    }
}