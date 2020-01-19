package main

import java.io.BufferedWriter
import java.io.FileWriter
import java.io.PrintWriter

class CodeWriter(outputFile: FileWriter) {
  private val pw = PrintWriter(BufferedWriter(outputFile))
  private val DEFAULT_STACK_POINTER = 256
  /*
   * 比較演算用の条件分岐のラベルを一意に設定する為の変数
   * eq, gt, lt が呼ばれる度にインクリメントされる
   */
  private var comparisonLabelIdentifier = 0

  fun setFileName(fileName: String) {
    // 新しいVMファイルの変換がスタートされた
    pw.println("@${DEFAULT_STACK_POINTER}")
    pw.println("D=A")
    pw.println("@SP")
    pw.println("M=D")
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
      "not" -> assemblerCodeOfNot()
      else -> throw RuntimeException("予期しない算術コマンド command = $command")
    }
  }

  fun writePushPop(command: String, segment: String, index: Int) {
    // push pop命令をアセンブリコードに変換し、書き込む
    // 一旦以下の場合のみを考える
    // push constant 8
    if(segment == "constant") {
      pw.println("@${index}")
      pw.println("D=A")
      referenceToStack()
      pw.println("M=D")
    }

    if(command == "push") {
      incrementStackPointer()
    }
  }

  fun close() {
    // 出力ファイルを閉じる
    pw.println("(END)") // TODO: プログラムの総行数(メモリポインタの値)を取得し無限ループさせたい
    pw.println("@END")
    pw.println("0;JMP")
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
    // - y
    popFromStack()
    pw.println("M=-M")
    incrementStackPointer()

  }

  private fun assemblerCodeOfEq() {
    // x == y
    assemblerCodeOfComparisonBy("eq")
  }

  private fun assemblerCodeOfGt() {
    // x > y
    assemblerCodeOfComparisonBy("gt")
  }

  private fun assemblerCodeOfLt() {
    // x < y
    assemblerCodeOfComparisonBy("lt")
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
}



