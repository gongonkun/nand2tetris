package vmtranslator

import java.io.File
import java.io.FileWriter

class App {
  fun translate(vmFilePath: String) {
    val parser = Parser(File(vmFilePath))
    val codeWriter = CodeWriter(FileWriter(File(assemblerFileName(vmFilePath))))

    codeWriter.setFileName(vmFilePath)

    while(parser.hasMoreCommands()) {
      parser.advance()

      when(parser.commandType()) {
        Parser.COMMAND_TYPE.C_ARITHMETIC -> codeWriter.writeArithmetic(parser.arg1())
        Parser.COMMAND_TYPE.C_LABEL -> codeWriter.writeLabel(parser.arg1())
        Parser.COMMAND_TYPE.C_GOTO -> codeWriter.writeGoto(parser.arg1())
        Parser.COMMAND_TYPE.C_IF -> codeWriter.writeIf(parser.arg1())
        Parser.COMMAND_TYPE.C_PUSH -> codeWriter.writePush(parser.arg1(), parser.arg2())
        Parser.COMMAND_TYPE.C_POP -> codeWriter.writePop(parser.arg1(), parser.arg2())
      }
    }

    codeWriter.close()
  }

  private fun assemblerFileName(vmFile: String): String = vmFile.replace("\\.vm".toRegex(), ".asm")
}

fun main(args: Array<String>) {
  App().translate(args[0])
}
