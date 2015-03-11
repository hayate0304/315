package lab6;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Scanner;

public class lab6 {
   
   private static CacheEntry[][][] cache;
   
   //Used for printing the results
   private static int hits = 0;
   private static int totalAddresses = 0;  
   private static int cacheNumber = 1; 
   
   private static int blockOffsetLength;
   private static int indexLength;
      
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
      int cacheLength = (cacheSize/((blockLength * 16) * mapping));
      
      //Initialize everything
      hits = 0;
      totalAddresses = 0;
      cache = new CacheEntry[mapping][cacheLength][blockLength];
      
      //Fill the cache with zeros and invalids
      for(int i=0; i<cache.length; i++) {
         for(int j=0; j<cache[i].length; j++) {
            for(int q=0; q<cache[i][j].length; q++) {
               cache[i][j][q] = new CacheEntry();
            }
         }
      }
      
      blockOffsetLength = (blockLength / 2);
      if (blockLength == 1) { 
         blockOffsetLength = 0;   //Direct Mapped
      }
      
      indexLength = (int)(Math.log(cacheLength)/Math.log(2));
      
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
                  
         //Get the hex as an integer
         int address = lineScanner.nextInt(16);
                  
         cacheAddress(address, cacheLength, blockLength);
         
         totalAddresses++;
           
         lineScanner.close();
      }
      
      printResults(cacheSize, mapping, blockLength);
      cacheNumber++;
      
      addressScanner.close();
   }
   
   private static void cacheAddress(int address, int cacheLength, int blockLength) {
      
      int index = address << (32 - (indexLength + blockOffsetLength + 2));
      index = index >>> (32 - indexLength);
      
      int tag = address >>> (indexLength + blockOffsetLength + 2);

//      System.out.println("Tag: " + tag + " Index: " + index);
      
      int blockOffset = (index % blockLength);

      boolean found = false;
            
      //Check for a hit
      outerLoop:
      for(int i=0; i<cache.length; i++) {
         for(int q=0; q<blockLength; q++) {
            if((cache[i][index][0].value == tag) && (cache[i][index][0].valid)) {
               found = true;
               hits++;
               
               //Move the way to the front of the array if not already there
               //The most recently used way should always be at the front of the array
               if((cache.length > 1) && (i != 0)) {
                  CacheEntry[] tempRow = cache[0][index];
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
         CacheEntry[] tempRow = cache[0][index];
         fillRow(0, index, blockOffset, tag, address);
         
         for(int j=1; j<cache.length; j++) {
            cache[j][index] = tempRow;
            
            if(j < (cache.length-1)) {
               tempRow = cache[j+1][index];
            }
         }
      }
      
      //Uncomment these for debugging
 //     System.out.println("Mod Index: " + index + " Hits: " + hits);
 //     printCache();
     
   }
   
   //Fill a single way
   private static void fillRow(int mapping, int index, int blockOffset, int tag, int address) {
      cache[mapping][index][blockOffset] = new CacheEntry(tag);
      
      int tempAddress = address;
      for(int i=(blockOffset+1); i<cache[mapping][index].length; i++) {
         tempAddress = tempAddress + 1;
         cache[mapping][index][i] = new CacheEntry(tempAddress >>> (indexLength + blockOffsetLength + 2));
      }
      
      tempAddress = address;
      for(int i=(blockOffset-1); i>-1; i--) {
         tempAddress = tempAddress - 1;
         cache[mapping][index][i] = new CacheEntry(tempAddress >>> (indexLength + blockOffsetLength + 2));
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
      for (CacheEntry[] row : cache[0])
      {
         if(row[0].valid) {
            System.out.print(q + ": " + Arrays.toString(row));
                        
            for(int i=1; i<cache.length; i++) {
               System.out.print(" " + Arrays.toString(cache[i][q]));
            }
            
            System.out.print("\n");
         }
         q++;  
      }
   }
   
   private static class CacheEntry {
      int value = 0; 
      boolean valid = false;
      
      public CacheEntry() {
         this.value = 0;
         this.valid = false;
      }
      
      public CacheEntry(int value) {
         this.value = value;
         this.valid = true;
      }
      
      public String toString() {
         return String.valueOf(value);
      }
   }

}
