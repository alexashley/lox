package com.aashley

import com.aashley.extensions.charAt

class Scanner(private val source: String) {
    private val nullChar = '\u0000'
    private val numericCharRange = '0'..'9'
    private val lowerAlphaCharRange = 'a'..'z'
    private val upperAlphaCharRange = 'A'..'Z'
    private val keywords = hashMapOf(
            "and" to TokenType.AND,
            "class" to TokenType.CLASS,
            "else" to TokenType.ELSE,
            "false" to TokenType.FALSE,
            "for" to TokenType.FOR,
            "fun" to TokenType.FUN,
            "if" to TokenType.IF,
            "nil" to TokenType.NIL,
            "or" to TokenType.OR,
            "print" to TokenType.PRINT,
            "return" to TokenType.RETURN,
            "super" to TokenType.SUPER,
            "this" to TokenType.THIS,
            "true" to TokenType.TRUE,
            "var" to TokenType.VAR,
            "while" to TokenType.WHILE
    )

    private val tokens = mutableListOf<Token>()
    private var start = 0
    private var current = 0
    private var line = 1

    fun scanTokens(): List<Token> {
        while (!atEnd()) {
            start = current
            scanToken()
        }

        tokens.add(Token(TokenType.EOF, "", null, line))

        return tokens.toList()
    }

    private fun scanToken() {
        val character = advance()

        val tokenType = when (character) {
            '(' -> TokenType.LEFT_PAREN
            ')' -> TokenType.RIGHT_PAREN
            '{' -> TokenType.RIGHT_BRACE
            '}' -> TokenType.LEFT_BRACE
            ',' -> TokenType.COMMA
            '.' -> TokenType.DOT
            '-' -> TokenType.MINUS
            '+' -> TokenType.PLUS
            ';' -> TokenType.SEMICOLON
            '*' -> TokenType.STAR
            '!' -> if (lookAheadMatches('=')) TokenType.BANG_EQUAL else TokenType.BANG
            '=' -> if (lookAheadMatches('=')) TokenType.EQUAL_EQUAL else TokenType.EQUAL
            '<' -> if (lookAheadMatches('=')) TokenType.LESS_EQUAL else TokenType.LESS
            '>' -> if (lookAheadMatches('=')) TokenType.GREATER_EQUAL else TokenType.GREATER
            '/' -> if (lookAheadMatches('/')) TokenType.COMMENT else TokenType.SLASH
            '"' -> TokenType.STRING
            ' ', '\r', '\t' -> TokenType.WHITESPACE
            '\n' -> TokenType.NEWLINE
            in numericCharRange -> TokenType.NUMBER
            in lowerAlphaCharRange -> TokenType.IDENTIFIER
            in upperAlphaCharRange -> TokenType.IDENTIFIER
            '_' -> TokenType.IDENTIFIER
            else -> TokenType.UNKNOWN
        }

        when (tokenType) {
            TokenType.COMMENT -> while (peek() != '\n' && !atEnd()) {
                advance()
            }
            TokenType.NEWLINE -> line++
            TokenType.UNKNOWN -> error(line, "Unexpected character: $character")
            TokenType.STRING -> {
                scanString()
            }
            TokenType.NUMBER -> {
                scanNumber()
            }
            TokenType.IDENTIFIER -> {
                scanIdentifier()
            }
            TokenType.WHITESPACE -> {
                // skip
            }
            else -> {
                addToken(tokenType, null)
            }
        }
    }

    private fun addToken(tokenType: TokenType, literal: Any?) {
        val text = source.substring(start, current)
        tokens.add(Token(tokenType, text, literal, line))
    }

    private fun atEnd() = current >= source.length

    private fun advance(): Char {
        current += 1
        return source.charAt(current - 1)
    }

    private fun peek(): Char {
        if (atEnd()) {
            return nullChar
        }

        return source.charAt(current)
    }

    private fun peekNext(): Char {
        if (current + 1 >= source.length) {
            return nullChar
        }

        return source.charAt(current + 1)
    }

    private fun lookAheadMatches(expected: Char): Boolean {
        if (atEnd() || source.charAt(current) != expected) {
            return false
        }

        current++
        return true
    }

    private fun scanString() {
        while (peek() != '"' && !atEnd()) {
            if (peek() == '\n') {
                line++
            }
            advance()
        }

        if (atEnd()) {
            error(line, "Unterminated string.")
        }

        advance()

        val value = source.substring(start + 1, current - 1)
        addToken(TokenType.STRING, value)
    }

    private fun scanNumber() {
        while (isDigit(peek())) {
            advance()
        }

        if (peek() == '.' && isDigit(peekNext())) {
            advance()

            while (isDigit(peek())) {
                advance()
            }
        }

        val number = source.substring(start, current).toDouble()
        addToken(TokenType.NUMBER, number)
    }

    private fun scanIdentifier() {
        while (isAlphaNumeric(peek())) {
            advance()
        }

        val text = source.substring(start, current)
        val tokenType = keywords[text] ?: TokenType.IDENTIFIER

        addToken(tokenType, null)
    }

    private fun isDigit(char: Char) = char in numericCharRange

    private fun isAlpha(c: Char) = c in upperAlphaCharRange || c in lowerAlphaCharRange || c == '_'

    private fun isAlphaNumeric(c: Char) = isAlpha(c) || isDigit(c)
}