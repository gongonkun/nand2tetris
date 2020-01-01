package assembler

import java.io.File

class Parser(val inputFile: File) {
  val inputLines = inputFile.readLines()
  var inputLinesIndex = 0
  var currentCommand = ""

  enum class COMMAND_TYPE {
    A_COMMAND,
    C_COMMAND,
    L_COMMAND,
    COMMENT
  }

  fun hasMoreCommands(): Boolean {
    return this.inputLinesIndex < this.inputLines.size
  }

  fun reset() {
    this.inputLinesIndex = 0
  }

  fun advance() {
    this.currentCommand = this.inputLines.get(inputLinesIndex++).trim()
    this.currentCommand = this.currentCommand.replace("(?=//)(.*)".toRegex(), "").trim()
  }

  fun commandType(): COMMAND_TYPE {
    return when {
      Regex("^//.*").matches(this.currentCommand) || this.currentCommand == "" -> COMMAND_TYPE.COMMENT
      Regex("^@[0-9a-zA-Z_]+").matches(this.currentCommand) -> COMMAND_TYPE.A_COMMAND
      Regex("^.+(=.+;.*|;.*|=.*)").matches(this.currentCommand) -> COMMAND_TYPE.C_COMMAND
      Regex("^\\(.+\\)$").matches(this.currentCommand) -> COMMAND_TYPE.L_COMMAND

      else -> throw Exception() // TODO: 適切な例外処理
    }
  }

  fun symbol(): String {
    return when(this.commandType()) {
      COMMAND_TYPE.A_COMMAND -> this.currentCommand.replace("\\(|\\)".toRegex(), "")
      COMMAND_TYPE.C_COMMAND, COMMAND_TYPE.COMMENT -> ""
      COMMAND_TYPE.L_COMMAND -> this.currentCommand.replace("@", "")
    }
  }

  fun dest(): String {
    return Regex("^(.*)(?=\\=)").find(this.currentCommand)?.value ?: ""
  }

  fun comp(): String {
    val match = Regex("(?<=\\=)(.*)").find(this.currentCommand)?.value ?: this.currentCommand

    return Regex("(.*)(?=;)").find(match)?.value ?: match
  }

  fun jump(): String {
    return Regex("(?<=;)(.*)(?=//)").find(this.currentCommand)?.value ?: ""
  }
}
 