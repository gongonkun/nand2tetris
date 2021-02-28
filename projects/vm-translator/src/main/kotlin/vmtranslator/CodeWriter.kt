package vmtranslator

import java.io.BufferedWriter
import java.io.FileWriter
import java.io.PrintWriter

class CodeWriter(outputFile: FileWriter) {
  private val pw = PrintWriter(BufferedWriter(outputFile))
  private val DEFAULT_STACK_POINTER = 256
  private var fileName = ""
  /*
   * 比較演算用の条件分岐のラベルを一意に設定する為の変数
   * eq, gt, lt が呼ばれる度にインクリメントされる
   */
  private var comparisonLabelIdentifier = 0
  private var returnLabelIdentifier = 0

  private var prefixVmLabel = "VMLABEL_"

  /**
   * ex: arg = /Hoge/Foo/Bar.vm => this.fileName = Bar
   */
  fun setFileName(fileName: String) {
    this.fileName = fileName.substringBeforeLast('.')
  }

  fun writeBootstrap() {
    pw.println("@${DEFAULT_STACK_POINTER}")
    pw.println("D=A")
    pw.println("@SP")
    pw.println("M=D")

    writeCall("Sys.init", 0)
  }

  fun writeArithmetic(command: String) {
    // 算術コマンドをアセンブリコードに変換し、書き込む
    when(command) {
      "add" -> assemblerCodeOfAdd()
      "sub" -> assemblerCodeOfSub()
      "neg" -> assemblerCodeOfNeg()
      "eq" -> assemblerCodeOfEq()
      "gt" -> assemblerCodeOfGt()
      "lt" -> assemblerCodeOfLt()
      "and" -> assemblerCodeOfAnd()
      "or" -> assemblerCodeOfOr()
      "not" -> assemblerCodeOfNot ()
      else -> throw RuntimeException("予期しない算術コマンド command = $command")
    }
  }

  fun writePush(segment: String, index: Int) {
    // constantの場合はstackにindexで渡されたものを入れるだけ
    if(segment == "constant") {
      pw.println("@${index}")
      pw.println("D=A")
      referenceToStack()
      pw.println("M=D")
      incrementStackPointer()
      return
    }

    val label = convertToLabelBy(segment)

    when(segment) {
      "local","argument", "this", "that" -> {
        pw.println("@$label")
        pw.println("D=M")
        pw.println("@${index}")
        pw.println("A=D+A")
        pw.println("D=M")
        referenceToStack()
        pw.println("M=D")
        incrementStackPointer()
      }

      "temp", "pointer" -> {
        pw.println("@$label")
        pw.println("D=A")
        pw.println("@${index}")
        pw.println("A=D+A")
        pw.println("D=M")
        referenceToStack()
        pw.println("M=D")
        incrementStackPointer()
      }

      "static" -> {
        pw.println("@$label.$index")
        pw.println("D=M")
        referenceToStack()
        pw.println("M=D")
        incrementStackPointer()
      }

      else -> throw RuntimeException("不正なセグメントです segment = $segment")
    }
  }

  fun writePop(segment: String, index: Int) {
    val label = convertToLabelBy(segment)

    when(segment) {
      "local","argument", "this", "that" -> {
        pw.println("@$label")
        pw.println("D=M")
        pw.println("@${index}")
        pw.println("D=D+A")
        pw.println("@R13")
        pw.println("M=D")
        popFromStack()
        pw.println("D=M")
        pw.println("@R13")
        pw.println("A=M")
        pw.println("M=D")
      }

      "temp", "pointer" -> {
        pw.println("@$label")
        pw.println("D=A") // 5
        pw.println("@$index")
        pw.println("D=D+A") // 5 + 6
        pw.println("@R13")
        pw.println("M=D")
        popFromStack()
        pw.println("D=M")
        pw.println("@R13")
        pw.println("A=M")
        pw.println("M=D")
      }

      "static" -> {
        popFromStack()
        pw.println("D=M")
        pw.println("@$label.$index")
        pw.println("M=D")
      }

      else -> throw RuntimeException("不正なセグメントです segment = $segment")
    }
  }

  fun writeLabel(label: String) {
    pw.println("(${prefixVmLabel + label})")
  }

  fun writeGoto(label: String) {
      pw.println("@${prefixVmLabel + label}")
      pw.println("0;JMP")
  }

  fun writeIf(label: String) {
    popFromStack();
    pw.println("D=M");
    pw.println("@${prefixVmLabel + label}")
    pw.println("D;JNE")
  }

  fun writeFunction(functionName: String, numLocals: Int) {
    writeLabel(functionName)
    repeat(numLocals) {
      writePush("constant", 0)
    }
  }

  fun writeCall(functionName: String, numArgs: Int) {
    val returnAddress = "RETURN_LABEL_${returnLabelIdentifier++}"
    // push return address
    pw.println("@${returnAddress}")
    pw.println("D=A")
    referenceToStack()
    pw.println("M=D")
    incrementStackPointer()
    // push current lcl
    pw.println("@LCL")
    pw.println("D=M")
    referenceToStack()
    pw.println("M=D")
    incrementStackPointer()
    // push current arg
    pw.println("@ARG")
    pw.println("D=M")
    referenceToStack()
    pw.println("M=D")
    incrementStackPointer()
    // push current this
    pw.println("@THIS")
    pw.println("D=M")
    referenceToStack()
    pw.println("M=D")
    incrementStackPointer()
    // push current that
    pw.println("@THAT")
    pw.println("D=M")
    referenceToStack()
    pw.println("M=D")
    incrementStackPointer()
    // change arg
    pw.println("@${numArgs + 5}")
    pw.println("D=A")
    pw.println("@SP")
    pw.println("D=M-D")
    pw.println("@ARG")
    pw.println("M=D")
    // change lcl
    pw.println("@SP")
    pw.println("D=M")
    pw.println("@LCL")
    pw.println("M=D")

    writeGoto(functionName)
    pw.println("(${returnAddress})")
  }

  fun writeReturn() {
    pw.println("@LCL")
    pw.println("D=M")
    pw.println("@5")
    pw.println("A=D-A")
    pw.println("D=M")
    pw.println("@R14")
    pw.println("M=D")

    popFromStack()
    pw.println("D=M")
    pw.println("@ARG")
    pw.println("A=M")
    pw.println("M=D")

    pw.println("@ARG")
    pw.println("D=M")
    pw.println("@SP")
    pw.println("M=D+1")

    pw.println("@LCL")
    pw.println("D=M")
    pw.println("A=D-1")
    pw.println("D=M")
    pw.println("@THAT")
    pw.println("M=D")

    pw.println("@LCL")
    pw.println("D=M")
    pw.println("@2")
    pw.println("A=D-A")
    pw.println("D=M")
    pw.println("@THIS")
    pw.println("M=D")

    pw.println("@LCL")
    pw.println("D=M")
    pw.println("@3")
    pw.println("A=D-A")
    pw.println("D=M")
    pw.println("@ARG")
    pw.println("M=D")

    pw.println("@LCL")
    pw.println("D=M")
    pw.println("@4")
    pw.println("A=D-A")
    pw.println("D=M")
    pw.println("@LCL")
    pw.println("M=D")

    pw.println("@R14")
    pw.println("A=M")
    pw.println("0;JMP")
  }


  fun close() {
    // 出力ファイルを閉じる
    pw.close()
  }

  private fun assemblerCodeOfAdd() {
    // x + y
    popFromStack()
    pw.println("D=M")
    popFromStack()
    pw.println("M=M+D")
    incrementStackPointer()
  }

  private fun assemblerCodeOfSub() {
    // x - y
    popFromStack()
    pw.println("D=M")
    popFromStack()
    pw.println("M=M-D")
    incrementStackPointer()
  }

  private fun assemblerCodeOfNeg() {
    // - x
    popFromStack()
    pw.println("M=-M")
    incrementStackPointer()
  }

  private fun assemblerCodeOfEq() {
    // x == y
    assemblerCodeOfComparisonBy(command = "eq")
  }

  private fun assemblerCodeOfGt() {
    // x > y
    assemblerCodeOfComparisonBy(command = "gt")
  }

  private fun assemblerCodeOfLt() {
    // x < y
    assemblerCodeOfComparisonBy(command = "lt")
  }

  private fun assemblerCodeOfAnd() {
    // x and y
    popFromStack()
    pw.println("D=M")
    popFromStack()
    pw.println("M=M&D")
    incrementStackPointer()
  }

  private fun assemblerCodeOfOr() {
    // x or y
    popFromStack()
    pw.println("D=M")
    popFromStack()
    pw.println("M=M|D")
    incrementStackPointer()
  }

  private fun assemblerCodeOfNot() {
    // not x
    popFromStack()
    pw.println("M=!M")
    incrementStackPointer()
  }

  private fun assemblerCodeOfComparisonBy(command: String) {
    val assemblerComparisonCommand = when(command) {
      "eq" -> "JEQ"
      "gt" -> "JGT"
      "lt" -> "JLT"
      else -> throw RuntimeException("不明な比較コマンド command = $command")
    }

    comparisonLabelIdentifier++
    val trueLabel = "CompResultTrueIdentifier_$comparisonLabelIdentifier"
    val endLabel = "CompResultEndIdentifier_$comparisonLabelIdentifier"

    popFromStack()
    pw.println("D=M")
    popFromStack()
    pw.println("D=M-D")
    pw.println("@$trueLabel")
    pw.println("D;$assemblerComparisonCommand")

    // falseだった場合は, stackに0を入れる
    referenceToStack()
    pw.println("M=0")
    incrementStackPointer()
    pw.println("@$endLabel")
    pw.println("0;JMP")

    // trueだった場合に,stackに-1を入れる
    pw.println("($trueLabel)")
    referenceToStack()
    pw.println("M=-1")
    incrementStackPointer()
    pw.println("@$endLabel")
    pw.println("0;JMP")

    pw.println("($endLabel)")
  }

  private fun popFromStack() {
    decrementStackPointer()
    referenceToStack()
  }

  private fun referenceToStack() {
    pw.println("@SP")
    pw.println("A=M")
  }

  private fun incrementStackPointer() {
    pw.println("@SP")
    pw.println("M=M+1")
  }

  private fun decrementStackPointer() {
    pw.println("@SP")
    pw.println("M=M-1")
  }

  private fun convertToLabelBy(segment: String): String {
    return when(segment) {
      "local" -> "LCL"
      "argument" -> "ARG"
      "this" -> "THIS"
      "that" -> "THAT"
      "pointer" -> "R3"
      "temp" -> "R5"
      "static" -> this.fileName
      else -> throw RuntimeException("不正なセグメント segment = $segment")
    }
  }
}
