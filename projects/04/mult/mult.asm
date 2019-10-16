// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Mult.asm

// Multiplies R0 and R1 and stores the result in R2.
// (R0, R1, R2 refer to RAM[0], RAM[1], and RAM[2], respectively.)

// r2 = 0
@2
M=0

// if (r0 == 0) return
@0
D=M
@END
D;JEQ

// if (r1 == 0) return
@1
D=M
@END
D;JEQ

// r3 = r1
@1
D=M
@3
M=D

// while(true) {
(LOOP)
  // r2 += r0
  @0
  D=M
  @2
  M=M+D

  // r3 -= 1
  @3
  M=M-1
  @3
  D=M

  // if (r3 <= 0) break
  @END
  D;JLE

// }
  @LOOP
  0;JMP
(END)
@END
0;JMP
