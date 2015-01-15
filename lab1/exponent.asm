# Name: Brandon Leventhal and Lance Boettcher
# Section: 1
# Description: This program computes the exponentiation of a number


##include <stdio.h>
#int exponent(void) {
#   int x, y;
#
#   printf("input base:");
#   scanf("%d", &x);
#
#   printf("input power:");
#   scanf("%d", &y);
#
#   if(y == 0) {
#      return 1;
#   }
#   
#   int ret = x;
#   int tmp = x;
#
#   for(int i = 1; i < y; i++) {
#      for(int j = 1; j < x; j++) { 
#         ret = ret + tmp; 
#      }
#      tmp = ret;
#   }
#   
#   return ret;
#}
#
#int main(void) { 
#
#   printf("answer: %d\n", exponent());
#   return 0;
#
#}


# declare global so programmer can see actual addresses.
.globl welcome
.globl prompt
.globl sumText

#  Data Area (this area contains strings to be displayed during the program)
.data

welcome:
	.asciiz " This program does exponentiation of two numbers \n\n"

prompt:
	.asciiz " Enter an integer: "

sumText: 
	.asciiz " \n Result = "

#Text Area (i.e. instructions)
.text

main:

	# Display the welcome message (load 4 into $v0 to display)
	ori     $v0, $0, 4			

	# This generates the starting address for the welcome message.
	# (assumes the register first contains 0).
	lui     $a0, 0x1001
	syscall

	# Display prompt
	ori     $v0, $0, 4

	# This is the starting address of the prompt (notice the
	# different address from the welcome message)
	lui     $a0, 0x1001
	ori     $a0, $a0,0x34
	syscall

	# Read 1st integer from the user (5 is loaded into $v0, then a syscall)
	ori     $v0, $0, 5
	syscall

	# Clear $s0 for the sum
	ori     $s0, $0, 0

	# Add 1st integer to sum
	# (could have put 1st integer into $s0 and skipped clearing it above)
	addu    $s0, $v0, $s0

	# Display prompt (4 is loaded into $v0 to display)
	# 0x22 is hexidecimal for 34 decimal (the length of the previous welcome message)
	ori     $v0, $0, 4
	lui     $a0, 0x1001
	ori     $a0, $a0,0x34
	syscall

	# Read 2nd integer
	ori	$v0, $0, 5
	syscall
	# $v0 now has the value of the second integer


   # s0 has first value and s1 has second value
   addu     $s1, $0, $v0

   # set up arguments for subroutine
   addu $a0, $s0, $0
   addu $a1, $s1, $0
   jal  exponent

   # s1 is the result of the exponentiation
   add  $s1, $v0, $0

	# Display the result
	ori     $v0, $0, 4
	lui     $a0, 0x1001
	ori     $a0, $a0,0x48
	syscall



	# show result
	# load 1 into $v0 to display an integer
	ori     $v0, $0, 1
	add 	  $a0, $s1, $0
	syscall

	# Exit (load 10 into $v0)
	ori     $v0, $0, 10
	syscall

exponent:

   addi    $sp, $sp, -20
   # Save registers I plan on using
   sw      $ra, 0($sp)
   sw      $s0, 4($sp)
   sw      $s1, 8($sp)
   sw      $s2, 12($sp)
   sw      $s3, 16($sp)

   # load registers with arguments
   addu    $s0, $a0, $0
   addu    $s1, $a0, $0
   addu    $s2, $a1, $0

   # if equal then return 1
   bne     $a1, $0, continue
   addi    $v0, $0, 1
   addi    $sp, $sp, 20
   # unaltered so jump straight back
   jr      $ra



continue:
   addi    $s3, $0, 1

loop:
   slt     $t0, $s3, $s2
   beq     $t0, $0, return
   addu    $a0, $s0, $0
   addu    $a1, $s1, $0
   jal     multi
   addu    $s0, $v0, $0
   # increment for loop
   addi    $s3, $s3, 1
   j       loop

return:
   addu    $v0, $0, $s0
   lw      $ra, 0($sp)
   lw      $s0, 4($sp)
   lw      $s1, 8($sp)
   lw      $s2, 12($sp)
   lw      $s3, 16($sp)
   addi    $sp, $sp, 20
   jr      $ra

mreturn:
   jr      $ra

multi:
   and     $v0, $0, $0
   and     $t0, $0, $0

mloop:
   slt     $t1, $t0, $a1
   beq     $t1, $0, mreturn
   addu    $v0, $v0, $a0
   addi    $t0, $t0, 1
   j       mloop

