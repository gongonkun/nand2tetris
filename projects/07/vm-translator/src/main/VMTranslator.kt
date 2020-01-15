package main

import java.io.File
import java.io.FileWriter

object VMTranslator {
  fun translate(vmFilePath: String) {
    val parser = Parser(File(vmFilePath))
    val codeWriter = CodeWriter(FileWriter(File(assemblerFileName(vmFilePath))))

    codeWriter.setFileName(vmFilePath)

    while(parser.hasMoreCommands()) {
      parser.advance()

      when(parser.commandType()) {
        Parser.COMMAND_TYPE.C_ARITHMETIC -> codeWriter.writeArithmetic(parser.arg1())
        Parser.COMMAND_TYPE.C_PUSH -> codeWriter.writePushPop("push", parser.arg1(), parser.arg2())
      }
    }

    codeWriter.close()
  }

  fun assemblerFileName(vmFile: String): String {
    return vmFile.replace("\\.vm".toRegex(), ".asm")
  }
}

fun main(args: Array<String>) {
  VMTranslator.translate(args[0])
}