# Draws a stick figure using Bresenham line and circle drawing algorithms

main:

  addi $sp, $0, 4095	# initialize stack pointer

  #Clear the coordinates pointer
  addi $t0, $0, 2000
  sw $0, 0($t0)

  # Circle(30,100,20) #head
  addi $a0, $0, 30
  addi $a1, $0, 100
  addi $a2, $0, 20

  jal Circle

	# Line(30,80,30,30) #body
  addi $a0, $0, 30
  addi $a1, $0, 80
  addi $a2, $0, 30
  addi $a3, $0, 30

  jal Line

	# Line(20,1,30,30) #left leg
  addi $a0, $0, 20
  addi $a1, $0, 1
  addi $a2, $0, 30
  addi $a3, $0, 30

  jal Line

	# Line(40,1,30,30) #right leg
  addi $a0, $0, 40
  addi $a1, $0, 1
  addi $a2, $0, 30
  addi $a3, $0, 30

  jal Line

	# Line(15,60,30,50) #left arm
  addi $a0, $0, 15
  addi $a1, $0, 60
  addi $a2, $0, 30
  addi $a3, $0, 50

  jal Line

	# Line(30,50,45,60) #right arm
  addi $a0, $0, 30
  addi $a1, $0, 50
  addi $a2, $0, 45
  addi $a3, $0, 60

  jal Line

	# Circle(24,105,3) #left eye
  addi $a0, $0, 24
  addi $a1, $0, 105
  addi $a2, $0, 3

  jal Circle

	# Circle(36,105,3) #right eye
  addi $a0, $0, 36
  addi $a1, $0, 105
  addi $a2, $0, 3

  jal Circle

	# Line(25,90,35,90) #mouth center
  addi $a0, $0, 25
  addi $a1, $0, 90
  addi $a2, $0, 35
  addi $a3, $0, 90

  jal Line

	# Line(25,90,20,95) #mouth left
  addi $a0, $0, 25
  addi $a1, $0, 90
  addi $a2, $0, 20
  addi $a3, $0, 95

  jal Line

	# Line(35,90,40,95) #mouth right
  addi $a0, $0, 35
  addi $a1, $0, 90
  addi $a2, $0, 40
  addi $a3, $0, 95

  jal Line

  j end

Line:
  addi $sp, $sp, -1
  sw $ra, 0($sp)

  # x0 --> a0
  # y0 --> a1
  # x1 --> a2
  # y1 --> a3

  # st --> t4
  # deltax --> t5
  # deltay --> t6
  # error --> t7
  # x --> s4
  # y --> t2
  # ystep --> t3

  add $s0, $0, $a0
  add $s1, $0, $a1
  add $s2, $0, $a2
  add $s3, $0, $a3

  sub $t5, $s3, $s1
  sub $t6, $s2, $s0

  #Abs
  or $t0, $0, $t5
  slt $t1, $t5, $0
  beq $t1, $0, positive1
  sub $t5, $0, $t5

positive1:

  #Abs
  or $t0, $0, $t6
  slt $t1, $t6, $0
  beq $t1, $0, positive2
  sub $t6, $0, $t6

positive2:

  slt $t7, $t6, $t5

  beq $t7, $0, else1

  addi $t4, $0, 1 #st=1
  j endif1
else1:
  add $t4, $0, $0
endif1:

  addi $t5, $0, 1 # Load 1 into t5
  bne $t4, $t5, endif2

  # swap(x0,y0)
  add $t0, $0, $s0
  add $s0, $0, $s1
  add $s1, $0, $t0

  # swap(x1,y1)
  add $t0, $0, $s2
  add $s2, $0, $s3
  add $s3, $0, $t0

endif2:

  slt $t5, $s2, $s0

  beq $t5, $0, endif3

  # swap(x0,x1)
  add $t0, $0, $s0
  add $s0, $0, $s2
  add $s2, $0, $t0

  # swap(y0,y1)
  add $t0, $0, $s1
  add $s1, $0, $s3
  add $s3, $0, $t0

endif3:

  sub $t5, $s2, $s0 #deltax
  sub $t6, $s3, $s1 #deltay

  #Abs of deltay

  or $t0, $0, $t6
  slt $t1, $t6, $0
  beq $t1, $0, positive3
  sub $t6, $0, $t6

positive3:

  add $t7, $0, $0   #error
  add $t2, $0, $s1  #y=y0

  slt $t0, $s1, $s3

  beq $t0, $0, else2

  addi $t3, $0, 1 # ystep = 1

  j endif4

else2:

  addi $t3, $0, -1 # ystep = -1

endif4:

  # for x from x0 to x1
  add $s4, $0, $s0 # s4=x0

forLoop:
  addi $t0, $s2, 1
  beq $s4, $t0, forDone

  addi $t0, $0, 1
  bne $t4, $t0, else3 #if st == 1

  add $a0, $t2, $0
  add $a1, $s4, $0
  jal plot

  j endif5
else3:
  add $a0, $s4, $0
  add $a1, $t2, $0
  jal plot

endif5:

  add $t7, $t7, $t6 # error = error + deltay

  sll $t0, $t7, 1
  addi $t0, $t0, 1

  slt $t0, $t5, $t0

  beq $t0, $0, endif6

  add $t2, $t2, $t3
  sub $t7, $t7, $t5

endif6:
  addi $s4, $s4, 1
  j forLoop

forDone:
  lw $ra, 0($sp)
  addi $sp, $sp, 1
  jr $ra



Circle:
  addi $sp, $sp, -1
  sw $ra, 0($sp)

  add $s0, $0, $a0 # xc
  add $s1, $0, $a1 # yc
  add $s2, $0, $a2 # r

  add $s3, $0, $0 # x
  add $s4, $0, $s2 # y

  sll $t7, $s2, 1
  addi $t6, $0, 3

  sub $s5, $t6, $t7 # g

  sll $t7, $s2, 2
  addi $t6, $0, 10

  sub $s6, $t6, $t7 # diagonalInc

  addi $s7, $0, 6 # rightInc

while:
  addi $t1, $s4, 1
  slt $t0, $s3, $t1

  beq $t0, $0, endWhile

  add $a0, $s0, $s3
  add $a1, $s1, $s4

  jal plot

  add $a0, $s0, $s3
  sub $a1, $s1, $s4

  jal plot

  sub $a0, $s0, $s3
  add $a1, $s1, $s4

  jal plot

  sub $a0, $s0, $s3
  sub $a1, $s1, $s4

  jal plot

  add $a0, $s0, $s4
  add $a1, $s1, $s3

  jal plot

  add $a0, $s0, $s4
  sub $a1, $s1, $s3

  jal plot

  sub $a0, $s0, $s4
  add $a1, $s1, $s3

  jal plot

  sub $a0, $s0, $s4
  sub $a1, $s1, $s3

  jal plot

  addi $t1, $0, -1
  slt $t0, $t1, $s5

  beq $t0, $0, else4

  add $s5, $s5, $s6
  addi $s6, $s6, 8
  addi $s4, $s4, -1

  j endif7

else4:
  add $s5, $s5, $s7
  addi $s6, $s6, 4

endif7:
  addi $s7, $s7, 4
  addi $s3, $s3, 1

  j while

endWhile:
  lw $ra, 0($sp)
  addi $sp, $sp, 1
  jr $ra



plot:
  addi $t1, $0, 2000

  lw $t0, 0($t1)

  sw $a0, 0($t0)
  sw $a1, 1($t0)

  addi $t0, $t0, 2
  sw $t0, 0($t1)

  jr $ra



end:
  add $0, $0, $0
