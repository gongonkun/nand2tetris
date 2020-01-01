package assembler

import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.io.BufferedWriter

class Assembler(assemblerFilePath: String, binaryFilePath: String) {
  val parser: Parser
  val symboleTable: SymboleTable
  val binaryFile: FileWriter
  init {
    parser = Parser(File(assemblerFilePath))
    symboleTable = SymboleTable()
    binaryFile = FileWriter(binaryFilePath)
  }

  fun assemble() {
    var binaryFileIndex = 0
    val pw = PrintWriter(BufferedWriter(binaryFile))

    while(parser.hasMoreCommands()) {
      parser.advance()

      if(parser.commandType() == Parser.COMMAND_TYPE.L_COMMAND) {
        symboleTable.addEntry(parser.symbol(), binaryFileIndex + 1)
      } else {
        binaryFileIndex++
      }
    }

    parser.reset()

    while(parser.hasMoreCommands()) {
      parser.advance()

      val binary = when(parser.commandType()) {
        Parser.COMMAND_TYPE.A_COMMAND -> convertACommand()
        Parser.COMMAND_TYPE.C_COMMAND -> convertCCommand()
        else -> ""
      }

      if(binary == "") { continue }

      pw.println("%016d".format(binary.toLong()))
    }

    pw.close()
  }

  private fun convertACommand(): String {
    val symbol = parser.symbol()

    
    return if(Regex("\\d+").matches(symbol)) {
      // A命令が数値を指しているなら, そのままバイナリにする
      Integer.toBinaryString(Integer.parseInt(symbol))
    } else {
      if(!symboleTable.contains(symbol)) {
        // シンボルテーブルにまだ無ければ, 新規で作成する
        symboleTable.addEntry(symbol)
      }

      Integer.toBinaryString(symboleTable.getAddress(symbol)!!)
    }
  }

  private fun convertCCommand(): String {
    val a = if(Regex("M").containsMatchIn(parser.currentCommand)) {
      "1"
    } else {
      "0"
    }

    return "111" + a + Code.comp(parser.comp().trim()) + Code.dest(parser.dest().trim()) + Code.jump(parser.jump().trim())
  }
}

fun main(args: Array<String>) {
  val assemblerFilePath = args[0]
  val binaryFilePath = assemblerFilePath.replace("\\..+".toRegex(), ".hack")
  println("Start assemble...")
  Assembler(assemblerFilePath, binaryFilePath).assemble()
  println("Assemble complete!")
}
