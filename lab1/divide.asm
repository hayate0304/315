# Name: Brandon Leventhal and Lance Boettcher
# Section: 1
# Description: This program divides two numbers

##include <stdio.h>
#
#void divide(void) {
#   int a, b, div;
#
#   printf("Input most significant:");
#   scanf("%d", &a);
#   
#   printf("Input least significant:");
#   scanf("%d", &b);
#
#   printf("Input 32-bit divisor:");
#   scanf("%d", &div);
#
#   int pow = 0;
#   int cmp = 1;
#   while((div & cmp) == 0) {
#      pow++;
#      cmp = cmp << 1;
#   }
#   
#   int msb = (a >> pow);
#   int lsb = ((b >> pow) | (a << (32-pow)));
#
#   printf("%d, %d / %d =  %d, %d\n", a, b, div, msb, lsb);
#}
#
#int main() {
#
#   divide();
#   return 0;
#}


# declare global so programmer can see actual addresses.
.globl welcome
.globl prompt
.globl sumText
.globl comma

#  Data Area (this area contains strings to be displayed during the program)
.data

welcome:
	.asciiz " This program divides two numbers \n\n"

prompt:
	.asciiz " Enter an integer: "

sumText: 
	.asciiz " \n Div = "

comma: 
	.asciiz " , "

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
	
	# This is the starting address of the prompt 
	lui     $a0, 0x1001
	ori     $a0, $a0,0x25
	syscall

	# Read 1st integer from the user (5 is loaded into $v0, then a syscall)
	ori     $v0, $0, 5
	syscall
	
	# Clear $s0 for the msb 
	ori     $s0, $0, 0	

	# Add 1st integer to msb 
	addu    $s0, $v0, $s0

	# Display prompt (4 is loaded into $v0 to display)
	ori     $v0, $0, 4			
	lui     $a0, 0x1001
	ori     $a0, $a0,0x25
	syscall

	# Read 2nd integer from the user (5 is loaded into $v0, then a syscall)
	ori     $v0, $0, 5
	syscall
	
	# Clear $s1 for the lsb 
	ori     $s1, $0, 0	

	# Add 2nd integer to the lsb 
	addu    $s1, $v0, $s1
	
	# Display prompt (4 is loaded into $v0 to display)
	ori     $v0, $0, 4			
	lui     $a0, 0x1001
	ori     $a0, $a0,0x25
	syscall

	# Read 3rd integer from the user (5 is loaded into $v0, then a syscall)
	ori     $v0, $0, 5
	syscall
	
	# Clear $s2 for the divisor
	ori     $s2, $0, 0	

	# Add 3rd integer to the divisor 
	addu    $s2, $v0, $s2
	
	ori 	$t0, $0, 0	# pow = 0
	addi	$t1, $0, 0x01	# cmp = 1

loop: 	
	and 	$t2, $s2, $t1
	bne 	$t2, $0, done	# (while div & cmp == 0)
	addi 	$t0, $t0, 0x01	# pow++
	sll 	$t1, $t1, 0x01	# cmp = cmp <<1
	j 	loop
	
done: 
	srl 	$t3, $s0, $t0	# msb = (a >> pow)
	srl 	$t4, $s1, $t0 	# (b >> pow)
	
	ori     $t5, $0, 32
	sub	$t6, $t5, $t0 	# $t6 = 32 - pow
	sll 	$t7, $s0, $t6 	# $t7 = a << (32 - pow) 
	or 	$t5, $t4, $t7	# $t5 = ((b >> pow) | (a << (32 - pow)))

	# Display the divide text
	ori     $v0, $0, 4			
	lui     $a0, 0x1001
	ori     $a0, $a0,0x39
	syscall
	
	# Display the msb
	ori     $v0, $0, 1			
	add 	$a0, $t3, $0
	syscall
	
	# Display the comma 
	ori     $v0, $0, 4			
	lui     $a0, 0x1001
	ori     $a0, $a0,0x43
	syscall
	
	# Display the lsb
	ori     $v0, $0, 1			
	add 	$a0, $t5, $0
	syscall
	
	# Exit (load 10 into $v0)
	ori     $v0, $0, 10
	syscall

