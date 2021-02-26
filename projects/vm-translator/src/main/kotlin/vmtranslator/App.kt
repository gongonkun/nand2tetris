package vmtranslator

import java.io.File
import java.io.FileWriter
import java.io.FilenameFilter

class App {
  fun translate(vmFilePath: String) {
    val vmFiles = readVmFiles(vmFilePath) ?: return

    val codeWriter = CodeWriter(FileWriter(File(assemblerFileName(vmFilePath))))
    vmFiles.forEach {
      val parser = Parser(it)
      codeWriter.setFileName(it.path)

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
    }

    codeWriter.close()
  }

  private fun assemblerFileName(vmFile: String): String = vmFile.replace("\\.vm".toRegex(), ".asm")

  private fun readVmFiles(path: String): Array<File>? {
    class VmFileFilter: FilenameFilter {
      override fun accept(file: File, filePath: String): Boolean = filePath.endsWith(".vm")
    }

    val file = File(path)

    return if (file.isDirectory) {
      arrayOf(File(path))
    } else {
      File(path).listFiles(VmFileFilter())
    }
  }
}

fun main(args: Array<String>) {
  App().translate(args[0])
}
