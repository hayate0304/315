package lab6;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Scanner;

public class lab6 {
   
   private static int[][][] cache;
   
   //Used for printing the results
   private static int hits = 0;
   private static int totalAddresses = 0;  
   private static int cacheNumber = 1; 
      
   public static void main(String[] args) {
      
      String filename = args[0];
      
      run(filename, 2048, 1, 1);
      run(filename, 2048, 1, 2);
      run(filename, 2048, 1, 4);
      run(filename, 2048, 2, 1);
      run(filename, 2048, 4, 1);
      run(filename, 2048, 4, 4);
      run(filename, 4096, 1, 1);
                  
   }
   
   private static void run(String fileName, int cacheSize, int mapping, int blockLength) {
      int cacheLength = (cacheSize/(blockLength * 16));
      
      //Initialize everything
      cache = new int[mapping][cacheLength][blockLength];
      hits = 0;
      totalAddresses = 0;
      
      Scanner addressScanner;
      File addressFile = new File(fileName);

      try {
         addressScanner = new Scanner(addressFile);
      } catch (FileNotFoundException e) {
         System.out.println("Error reading address file");
         return;
      }

      while (addressScanner.hasNextLine()) {
         Scanner lineScanner = new Scanner(addressScanner.nextLine());
         
         //Skip the integer
         lineScanner.nextInt();
                  
         String binaryAddress = intToBinaryString(Integer.parseInt(lineScanner.next(), 16), 32);

         int byteOffset = Integer.parseInt(binaryAddress.substring(30, 32), 2);
         int blockOffset = Integer.parseInt(binaryAddress.substring(28, 30), 2);         
         int index = Integer.parseInt(binaryAddress.substring(16, 28), 2);
         int tag = Integer.parseInt(binaryAddress.substring(0, 16), 2);

         Address address = new Address(tag, index, blockOffset, byteOffset);
         
         cacheAddress(address, cacheLength, blockLength);
         
         totalAddresses++;
           
         lineScanner.close();
      }
      
      printResults(cacheSize, mapping, blockLength);
      cacheNumber++;
      addressScanner.close();
   }
   
   private static void cacheAddress(Address address, int cacheLength, int blockLength) {

      int blockOffset = (address.index % blockLength);
      int index = ((address.index/blockLength) % cacheLength);
      
      boolean found = false;
            
      //Check for a hit
      outerLoop:
      for(int i=0; i<cache.length; i++) {
         for(int q=0; q<blockLength; q++) {
            if(cache[i][index][q] == address.index) {
               found = true;
               hits++;
               
               //Move the way to the front of the array if not already there
               if((cache.length > 1) && (i != 0)) {
                  int[] tempRow = cache[0][index];
                  cache[0][index] = cache[i][index];
                  
                  for(int j=1; j<cache.length; j++) {
                     cache[j][index] = tempRow;
                     
                     if(j < (cache.length-1)) {
                        tempRow = cache[j+1][index];
                     }
                  }
               }
               break outerLoop;
            }
         }
      }
      
      //No hit, Update the cache
      if(!found) {
         boolean full = true;
         
         for(int i=0; i<cache.length; i++) {
            if(cache[i][index][blockOffset] == 0) {
               fillRow(i, index, blockOffset, address.index);
               full = false;
               break;
            }
         }
         
         if(full) {
            int[] tempRow = cache[0][index];
            fillRow(0, index, blockOffset, address.index);
            
            for(int j=1; j<cache.length; j++) {
               cache[j][index] = tempRow;
               
               if(j < (cache.length-1))
                  tempRow = cache[j+1][index];
            }
         }
      }
      
      //Uncomment these for debugging
      System.out.println(address.toString());
      System.out.println("Index: " + index + " Hits: " + hits);
      printCache();
     
   }
   
   //Fill a single way. For example, fill the array at cache[32][43] with all the values near "value"
   private static void fillRow(int mapping, int index, int blockOffset, int value) {
      cache[mapping][index][blockOffset] = value;
      
      int tempIndex = value;
      for(int i=(blockOffset+1); i<cache[mapping][index].length; i++) {
         tempIndex = tempIndex + 1;
         cache[mapping][index][i] = tempIndex;
      }
      
      tempIndex = value;
      for(int i=(blockOffset-1); i>-1; i--) {
         tempIndex = tempIndex - 1;
         cache[mapping][index][i] = tempIndex;
      }
   }
   
   //Convert to a binary string of length "size"
   private static String intToBinaryString(Integer integer, int size) {      
      String binaryString = Integer.toBinaryString(integer);
      
      while(binaryString.length() != size) {
         binaryString = "0" + binaryString;
      }

      return binaryString;
   }
   
   private static void printResults(int cacheSize, int associativity, int blockSize) {
      double hitRate = ((double)hits/(double)totalAddresses * 100);

      String output = 
         "Cache #" + cacheNumber + "\n" +
         "Cache Size: " + cacheSize + "B Associativity: " + associativity + " Block size: " + blockSize + "\n" +
         "Hits: " + hits + " Hit Rate: " + new DecimalFormat("##.##").format(hitRate) + "%\n" + 
         "---------------------------";
      
      System.out.println(output);
   }
   
   //Just for debugging. Only prints nonzero cache entries
   private static void printCache() {
      int q = 0;
      for (int[] row : cache[0])
      {
         if(row[0] != 0) {
            System.out.print(q + ": " + Arrays.toString(row));
                        
            for(int i=1; i<cache.length; i++) {
               System.out.print(" " + Arrays.toString(cache[i][q]));
            }
            
            System.out.print("\n");
         }
         q++;  
      }
   }
   
   private static class Address {
      int tag;
      int index;
      int blockOffset;
      int byteOffset;
      
      public Address(int tag, int index, int blockOffset, int byteOffset) {
         this.tag = tag;
         this.index = index;
         this.blockOffset = blockOffset;
         this.byteOffset = byteOffset;
      }
      
      public String toString() {
         return "tag: " + tag + " index: " + index + " blockOffset: " + blockOffset + " byteOffset: " + byteOffset;
      }
   }

}
