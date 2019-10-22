// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Fill.asm

// Runs an infinite loop that listens to the keyboard input.
// When a key is pressed (any key), the program blackens the screen,
// i.e. writes "black" in every pixel;
// the screen should remain fully black as long as the key is pressed. 
// When no key is pressed, the program clears the screen, i.e. writes
// "white" in every pixel;
// the screen should remain fully clear as long as no key is pressed.
@8192
D=A
@max_size
M=D
(INPUT)
  @crr_count
  M=0
  @KBD
  D=M
  @WHITE
  D;JEQ
  @BLACK
  D;JMP

(BLACK)  
  @crr_count
  D=M
  M=M+1
  @SCREEN
  A=A+D
  M=-1

  @crr_count
  D=M
  @max_size
  D=D-M
  @BLACK
  D;JNE
  @INPUT
  0;JMP

(WHITE)
  @crr_count
  D=M
  M=M+1
  @SCREEN
  A=A+D
  M=0

  @crr_count
  D=M
  @max_size
  D=D-M
  @WHITE
  D;JNE
  @INPUT
  0;JMP
