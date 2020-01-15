package main

import java.io.BufferedWriter
import java.io.FileWriter
import java.io.PrintWriter

class CodeWriter(outputFile: FileWriter) {
  private val pw = PrintWriter(BufferedWriter(outputFile))

  fun setFileName(fileName: String) {
    // 新しいVMファイルの変換がスタートされた
    pw.println("@256")
    pw.println("D=A")
    pw.println("@SP")
    pw.println("M=D")
  }

  fun writeArithmetic(command: String) {
    // 算術コマンドをアセンブリコードに変換し、書き込む
    if(command == "add") {
      decrementStackPointer()
      pw.println("A=M")
      pw.println("D=M")
      decrementStackPointer()
      pw.println("A=M")
      pw.println("M=M+D")
    } else {
      // 一旦考えない
      throw RuntimeException("CodeWriterにおかしな値がきたよ. command = $command")
    }
  }

  fun writePushPop(command: String, segment: String, index: Int) {
    // push pop命令をアセンブリコードに変換し、書き込む
    // 一旦以下の場合のみを考える
    // push constant 8
    if(segment == "constant") {
      pw.println("@${index}")
      pw.println("D=A")
      pw.println("@SP")
      pw.println("A=M")
      pw.println("M=D")
    }

    if(command == "push") {
      incrementStackPointer()
    }
  }

  fun close() {
    // 出力ファイルを閉じる
    pw.println("(END)") // TODO: プログラムの行数(メモリポインタ)を取得し無限ループさせたい
    pw.println("@END")
    pw.println("0;JMP")
    pw.close()
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



// push constant x
// add
// push constant y
//


// @R0
// M=256

// @x
// D=A

// @R0
// A=M
// M=D
// @R0
// M=M+1

// @y
// D=A

// @R0
// A=M
// M=D
// @R0
// M=M+1

// @R0
// D=M
// A=A-1
// D=D+M
// A=A-1
// M=D


