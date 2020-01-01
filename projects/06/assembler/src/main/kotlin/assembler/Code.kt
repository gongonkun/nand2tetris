package assembler

object Code {
  fun dest(nimonic: String): String {
        return when(nimonic) {
      "null", "" -> "000"
      "M" -> "001"
      "D" -> "010"
      "MD" -> "011"
      "A" -> "100"
      "AM" -> "101"
      "AD" -> "110"
      "AMD" -> "111"
      else -> throw Exception()
    }
  }

  fun comp(nimonic: String): String {
    return when(nimonic) {
      "0" -> "101010"
      "1" -> "111111"
      "-1" -> "111010"
      "D" -> "001100"
      "A", "M" -> "110000"
      "!D" -> "001101"
      "!A", "!M" -> "110001"
      "-D" -> "001111"
      "-A", "-M" -> "110011"
      "D+1" -> "011111"
      "A+1", "M+1" -> "110111"
      "D-1" -> "001110"
      "A-1", "M-1" -> "110010"
      "D+A", "D+M" -> "000010"
      "D-A", "D-M" -> "010011"
      "A-D", "M-D" -> "000111"
      "D&A", "D&M" -> "000000"
      "D|A", "D|M" -> "010101"
      else -> throw Exception()
    }
  }

  fun jump(nimonic: String): String {
    return when(nimonic) {
      "null", "" -> "000"
      "JGT" -> "001"
      "JEQ" -> "010"
      "JGE" -> "011"
      "JLT" -> "100"
      "JNE" -> "101"
      "JLE" -> "110"
      "JMP" -> "111"
      else -> throw Exception()
    }
  }
}
