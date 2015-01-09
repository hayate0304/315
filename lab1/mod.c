#include <stdio.h>

int main() {
   int a;
   int b;
   int mod;
   printf("This program returns the mod of 2 numbers\n");

   printf("Input first number: ");
   scanf("%d", &a);

   printf("\nInput second number: ");
   scanf("%d", &b);

   b = b - 1;
   mod = a & b;

   printf("\nMod is: %d\n", mod);
}
