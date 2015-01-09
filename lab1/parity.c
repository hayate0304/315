#include <stdio.h>

int main() {
   int num;

   printf("This program computes parity\n");

   printf("Enter a number to check: ");
   scanf("%d", &num);
   num ^= num >> 16;
   num ^= num >> 8;
   num ^= num >> 4;
   num &= 0xf;
   num = (0x9669 >> num) & 1;

   printf("\nparity: %d\n", num);
}
