#include <stdio.h>

void divide(void) {
   int a, b, div;

   printf("Input most significant:");
   scanf("%d", &a);
   
   printf("Input least significant:");
   scanf("%d", &b);

   printf("Input 32-bit divisor:");
   scanf("%d", &div);

   int pow = 0;
   int cmp = 1;
   while((div & cmp) == 0) {
      pow++;
      cmp = cmp << 1;
   }
   
   int msb = (a >> pow);
   int lsb = ((b >> pow) | (a << (32-pow)));

   printf("%d, %d / %d =  %d, %d\n", a, b, div, msb, lsb);
}

int main() {

   divide();
   return 0;
}
