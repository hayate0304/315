# Name: Brandon Leventhal and Lance Boettcher
# Section: 1
# Description: This program computes the parity of a number


##include <stdio.h>
#
#int main() {
#   int num;
#
#   printf("This program computes parity\n");
#
#   printf("Enter a number to check: ");
#   scanf("%d", &num);
#   num ^= num >> 16;
#   num ^= num >> 8;
#   num ^= num >> 4;
#   num &= 0xf;
#   num = (0x9669 >> num) & 1;
#
#   printf("\nparity: %d\n", num);
#}


# declare global so programmer can see actual addresses.
.globl welcome
.globl prompt
.globl sumText

#  Data Area (this area contains strings to be displayed during the program)
.data

welcome:
	.asciiz " This program checks the parity of a number \n\n"

prompt:
	.asciiz " Enter an integer: "

sumText: 
	.asciiz " \n Parity = "

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
	ori     $a0, $a0,0x2F
	syscall

	# Read 1st integer from the user (5 is loaded into $v0, then a syscall)
	ori     $v0, $0, 5
	syscall

   sra     $v1, $v0, 16
   xor     $v0, $v0, $v1

   sra     $v1, $v0, 8
   xor     $v0, $v0, $v1

   sra     $v1, $v0, 4
   xor     $v0, $v0, $v1

   andi    $v0, $v0, 0xF

   and     $a1, $0, $a1
   addi    $a1, $a1, 0x6996
   sra     $v0, $a1, $v0
   andi    $v0, $v0, 1

   and     $v1, $v1, $0
   add     $v1, $v0, $0
	# Display the sum text
	ori     $v0, $0, 4
	lui     $a0, 0x1001
	ori     $a0, $a0,0x43
	syscall

	# Display the sum
	# load 1 into $v0 to display an integer`
   nor     $v1, $v1, $0
   andi    $v1, $v1, 1
	ori     $v0, $0, 1
   add     $a0, $v1, $0
	syscall

	# Exit (load 10 into $v0)
	ori     $v0, $0, 10
	syscall

