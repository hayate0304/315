import java.util.*;
import java.io.*;

public class lab6 {
   private static int[][][] cache;
   private static int hits = 0;
   private static int totalAddresses = 0;
   private static int cacheNumber = 1;
   private static int blockOffsetLength;

   public static void main(String args[]) {
      String filename = args[0];
      directmap(filename, 2048, 1);
      directmap(filename, 2048, 2);
      directmap(filename, 2048, 4);
      associative(filename, 2048, 2, 1);
      associative(filename, 2048, 4, 1);
      associative(filename, 2048, 4, 4);
      directmap(filename, 4096, 1);
   }

   public static void directmap(String filename, int cacheSize, int blockLength) {
      int cacheLength = (cacheSize/(blockLength * 4));
      setup(cacheLength, blockLength, 1, false);
      Scanner sc;
      try {
         sc = new Scanner(new File(filename));
      } catch (FileNotFoundException e) {
         System.out.println("Error reading address file");
         return;
      }

      while (sc.hasNextInt()) {
         totalAddresses++;
         sc.nextInt();
         int address = sc.nextInt(16);
         address = address / 4;

         blockOffsetLength = address % blockLength;

         int index = (address/blockLength) % cacheLength;

         if (cache[0][index][blockOffsetLength] != address) {
            int addressMarker = address - blockOffsetLength;
            // Fill in neighboring addresses
            for (int i = 0; i < blockLength; i++) {
               cache[0][index][i] = addressMarker++;
            }
         } else {
            hits++;
         }
      }

      printResults(cacheSize, 1, blockLength);
      sc.close();
      cacheNumber++;
   }

   public static void associative(String filename, int cacheSize, int mapping, int blockLength) {
      int cacheLength = (cacheSize/((blockLength * 4) * mapping));
      setup(cacheLength, mapping, 2, true);

      Scanner sc;
      try {
         sc = new Scanner(new File(filename));
      } catch (FileNotFoundException e) {
         System.out.println("Error reading address file");
         return;
      }

      while (sc.hasNextInt()) {
         totalAddresses++;
         sc.nextInt();
         int address = sc.nextInt(16);
         address = address / 4;
         int index = (address/blockLength) % cacheLength;
         int tag = address / cacheLength;
         boolean notFound = true;

         for (int i = 0; i < mapping; i++) {
            if (cache[0][index][i] == tag) {
               hits++;
               notFound = false;
               boolean innerFound = false;
               for (int j = 0; j < mapping; j++) {
                  if (cache[1][index][j] == i) {
                     innerFound = true;
                     for (int k = j; k < mapping - 1; k++) {
                        cache[1][index][k] = cache[1][index][k+1];
                     }
                     break;
                  }
               }
               // This LRU order, if the order was incorrect it needs to be
               // updated
               if (!innerFound) {
                  for (int k = 0; k < mapping - 1; k++) {
                     cache[1][index][k] = cache[1][index][k+1];
                  }
               }
               // Makes the most recently used tag first in the lru array
               cache[1][index][mapping - 1] = i;
               break;
            }
         }
         if (notFound) {
            cache[0][index][cache[1][index][0]] = tag;
            int save = cache[1][index][0];
            for (int i = 0; i < mapping - 1; i++) {
               cache[1][index][i] = cache[1][index][i+1];
            }
            cache[1][index][mapping-1] = save;
         }
      }
      printResults(cacheSize, mapping, blockLength);
      cacheNumber++;
   }

   private static void setup(int cacheLength, int blockLength, int mapping, boolean associative) {
      // Use a different setup for set associative runs
      cache = new int[mapping][cacheLength][blockLength];
      hits = 0;
      totalAddresses = 0;
      if (associative) {
         // Fills in the order of the LRU
         for (int i = 0; i < cacheLength; i++) {
            for (int j = 0; j < blockLength; j++) {
               cache[1][i][j] = j;
            }
         }
      }
   }

   private static void printResults(int cacheSize, int associativity, int blockSize) {
      double hitRate = ((double)hits/(double)totalAddresses * 100);
      String output = String.format("Cache # %d\nCache size: %dB\tAssociativity: %d\tBlock size: %d\nHits: %d\tHit Rate: %.2f%%\n---------------------------",
            cacheNumber, cacheSize, associativity, blockSize, hits, hitRate);
      System.out.println(output);
   }
}
