package main

import java.io.File

class Parser(val inputFile: File) {
  var currentCommand = ""
  enum class COMMAND_TYPE {
    C_ARITHMETIC,
    C_PUSH,
    C_POP,
    C_LABEL,
    C_GOTO,
    C_IF,
    C_RETURN,
    C_CALL
  }

  private val inputLines = inputFile.readLines()
  private var inputLinesIndex = 0
  private val arithmeticCommands = listOf(
    "add",
    "sub",
    "neg",
    "eq",
    "gt",
    "lt",
    "and",
    "or",
    "not"
  )

  fun hasMoreCommands(): Boolean {
    return this.inputLinesIndex < this.inputLines.size
  }

  fun advance() {
    var tmpCommand = ""

    while(tmpCommand.isEmpty() && this.hasMoreCommands()) {
      tmpCommand = this.inputLines.get(inputLinesIndex++)
      tmpCommand = tmpCommand.replace("//.*".toRegex(), "").trim()
    }

    this.currentCommand = tmpCommand.trim()
  }

  /**
   * ex: push constant 1 -> constant
   *     add             -> add
   */
  fun arg1(): String {
    return when(this.commandType()) {
      COMMAND_TYPE.C_ARITHMETIC -> currentCommand
      COMMAND_TYPE.C_PUSH -> currentCommand.split(" ")[1]
      else -> throw RuntimeException()
    }
  }

  /**
   * ex: push constant 1 -> 1
   */
  fun arg2(): Int {
    return when(this.commandType()) {
      COMMAND_TYPE.C_PUSH -> Integer.parseInt(currentCommand.split(" ")[2])
      else -> throw RuntimeException("不正なcommand typeです. currentCommand = ${this.currentCommand}")
    }
  }

  fun commandType(): COMMAND_TYPE {
    val command = currentCommand.split(" ")[0]

    return when {
      arithmeticCommands.contains(command) -> COMMAND_TYPE.C_ARITHMETIC
      command == "push" -> COMMAND_TYPE.C_PUSH
      else -> throw RuntimeException("不正なcommandTypeです. currentCommand = ${this.currentCommand}")
    }
  }
}