#include <stdio.h>
int exponent(void) {
   int x, y;

   printf("input base:");
   scanf("%d", &x);

   printf("input power:");
   scanf("%d", &y);

   if(y == 0) {
      return 1;
   }
   
   int ret = x;
   int tmp = x;

   for(int i = 1; i < y; i++) {
      for(int j = 1; j < x; j++) { 
         ret = ret + tmp; 
      }
      tmp = ret;
   }
   
   return ret;
}

int main(void) { 

   printf("answer: %d\n", exponent());
   return 0;

}
