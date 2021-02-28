package vmtranslator

import java.io.File
import java.io.FileWriter
import java.io.FilenameFilter

class App {
  fun translate(vmFilePath: String) {
    val vmFiles = readVmFiles(vmFilePath) ?: return

    val codeWriter = CodeWriter(FileWriter(outputFile(vmFilePath)))
    vmFiles.forEach {
      val parser = Parser(it)
      codeWriter.setFileName(it.name)
      codeWriter.writeBootstrap()

      while(parser.hasMoreCommands()) {
        parser.advance()

        when(parser.commandType()) {
          Parser.COMMAND_TYPE.C_ARITHMETIC -> codeWriter.writeArithmetic(parser.arg1())
          Parser.COMMAND_TYPE.C_LABEL -> codeWriter.writeLabel(parser.arg1())
          Parser.COMMAND_TYPE.C_GOTO -> codeWriter.writeGoto(parser.arg1())
          Parser.COMMAND_TYPE.C_IF -> codeWriter.writeIf(parser.arg1())
          Parser.COMMAND_TYPE.C_FUNCTION -> codeWriter.writeFunction(parser.arg1(), parser.arg2())
          Parser.COMMAND_TYPE.C_CALL -> codeWriter.writeCall(parser.arg1(), parser.arg2())
          Parser.COMMAND_TYPE.C_RETURN -> codeWriter.writeReturn()
          Parser.COMMAND_TYPE.C_PUSH -> codeWriter.writePush(parser.arg1(), parser.arg2())
          Parser.COMMAND_TYPE.C_POP -> codeWriter.writePop(parser.arg1(), parser.arg2())
        }
      }
    }

    codeWriter.close()
  }

  private fun outputFile(vmFilePath: String): File {
    val file = File(vmFilePath)
    return if (file.isDirectory) {
      File(vmFilePath + "/${file.name}.asm")
    } else {
      File(vmFilePath.replace("\\.vm".toRegex(), "") + ".asm")
    }
  }

  private fun readVmFiles(path: String): Array<File>? {
    class VmFileFilter: FilenameFilter {
      override fun accept(file: File, filePath: String): Boolean = filePath.endsWith(".vm")
    }

    val file = File(path)

    return if (file.isDirectory) {

      File(path).listFiles(VmFileFilter())
    } else {
      arrayOf(File(path))
    }
  }
}

fun main(args: Array<String>) {
  App().translate(args[0])
}
