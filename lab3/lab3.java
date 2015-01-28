import java.io.*;
import java.util.*;

public class lab3 {
   private static int[] memory = new int[8192];
   private static int pc = 0;
   private static HashMap<String, Integer> registers = new HashMap<String, Integer>();
	private static HashMap<String, Integer> labels = new HashMap<String, Integer>();
	private static ArrayList<String> labelQueue = new ArrayList<String>();
   private static ArrayList<String> program = new ArrayList<String>();

   static {
      registers.put("$0", 0);

      registers.put("$v0", 0);
      registers.put("$v1", 0);

      registers.put("$a0", 0);
      registers.put("$a1", 0);
      registers.put("$a2", 0);
      registers.put("$a3", 0);

      registers.put("$t0", 0);
      registers.put("$t1", 0);
      registers.put("$t2", 0);
      registers.put("$t3", 0);
      registers.put("$t4", 0);
      registers.put("$t5", 0);
      registers.put("$t6", 0);
      registers.put("$t7", 0);

      registers.put("$s0", 0);
      registers.put("$s1", 0);
      registers.put("$s2", 0);
      registers.put("$s3", 0);
      registers.put("$s4", 0);
      registers.put("$s5", 0);
      registers.put("$s6", 0);
      registers.put("$s7", 0);

      registers.put("$t8", 0);
      registers.put("$t9", 0);

      registers.put("$sp", 0);
      registers.put("$ra", 0);
   }

   public static void main(String args[]) {
      Scanner linesc;
      String mode;

      initASM(args[0]);

      if (args.length == 2) {
         mode = "s";
         System.out.println("Script Mode");
         File script = new File(args[1]);
         try {
            linesc = new Scanner(script);
         } catch (FileNotFoundException e) {
            System.out.println("Error reading script file");
            return;
         }
      } else {
         mode = "i";
         System.out.println("Interactive Mode");
         System.out.print("mips> ");
         linesc = new Scanner(System.in);
      }

      while(linesc.hasNextLine()) {
         String line = linesc.nextLine();
         Scanner tokens = new Scanner(line);
         String command = tokens.next();

         if (command.equals("q")) {
            return;
         } else if (command.equals("h")) {
            printHelp();
         }  else if (command.equals("d")) {
            printRegisterStates();
         }  else if (command.equals("s")) {
            if (tokens.hasNextInt()) {
               step(tokens.nextInt());
            } else {
               step(1);
            }
         } else if (command.equals("r")) {
            System.out.println("Finishing program");
         } else if (command.equals("m")) {
            int start = tokens.nextInt();
            int stop = tokens.nextInt();
            printDataMemory(start, stop);
         } else if (command.equals("c")) {
            clearSimulator();
         } else if (command.equals("p")) {
            int linenum = tokens.nextInt();
            System.out.println(program.get(linenum));
         }


         tokens.close();
         if (mode.equals("i")) {
            System.out.print("mips> ");
         }
      }
      linesc.close();
   }

   private static void initASM(String fileName) {
      Scanner asmsc;
      File asm = new File(fileName);
      int numLine = 0;
      String fmtedAsm = "";

      try {
         asmsc = new Scanner(asm);
      } catch (FileNotFoundException e) {
         System.out.println("Error reading asm file");
         return;
      }

		while (asmsc.hasNextLine()) {
			String asmLine = asmsc.nextLine();
			asmLine = removeComments(asmLine);

			int labelIndex = asmLine.indexOf(":");

			/*
			 * This will get rid of blank lines and format labels in a way that
			 * will be much easier to parse
			 */
			if (labelIndex != -1) {
				labelQueue.add(asmLine.substring(0, labelIndex).trim());
				asmLine = asmLine.substring(labelIndex + 1);
			}

			if (!asmLine.trim().isEmpty()) {
				if (labelQueue.size() > 0) {
					String label = labelQueue.remove(0);
					labels.put(label, numLine);
				}

				fmtedAsm = fmtedAsm + asmLine + '\n';
				numLine++;
			}
		}

		fmtedAsm = fmtedAsm.trim();

		// Clean problem areas so a scanner can parse the string
		fmtedAsm = fmtedAsm.replace(",", ", ");
		fmtedAsm = fmtedAsm.replace("$", " $");
		fmtedAsm = fmtedAsm.replace("( $", "($");
		fmtedAsm = fmtedAsm.replaceAll("\t", " ");
		fmtedAsm = fmtedAsm.replaceAll(" +", " ");
		fmtedAsm = fmtedAsm.replaceAll("\n ", "\n");

      Scanner fmSC = new Scanner(fmtedAsm);
      while (fmSC.hasNextLine()) {
         String fmtedLine = fmSC.nextLine();
         program.add(fmtedLine.trim());
      }
   }

	private static String removeComments(String line) {
		int commentIndex = line.indexOf("#");
		if (commentIndex == -1) {
			return line;
		}
		line = line.substring(0, commentIndex);
		return line;
	}

   private static void printHelp() {
      System.out.println("\nh = show help");
      System.out.println("d = dump register state");
      System.out.println("s = single step through the program (i.e. execute 1 instruction and stop)");
      System.out.println("s num = step through num instructions of the program");
      System.out.println("r = run until the program ends");
      System.out.println("m num1 num2 = display data memory from location num1 to num2");
      System.out.println("c = clear all registers, memory, and the program counter to 0");
      System.out.println("q = exit the program\n");
   }

   // This might need to be changed because you can't print
   // hashmap keys in a specific order
   private static void printRegisterStates() {
      System.out.println("\npc = " + pc);
      int i = 0;
      for (String key : registers.keySet()) {
         System.out.print(key + " = " + memory[registers.get(key)]);
         i++;
         if (i % 4 == 0) {
            System.out.println();
         } else {
            System.out.print("\t");
         }
      }

      System.out.println("\n");
   }

   private static void step(int numSteps) {
      System.out.println("\t" + numSteps + " instruction(s) executed");
   }

   private static void printDataMemory(int start, int stop) {
      System.out.println();
      for (int i = start; i <= stop; i++) {
         System.out.println("[" + i + "]" + " = " + memory[i]);
      }
      System.out.println();
   }

   private static void clearSimulator() {
      System.out.println("Simulator reset\n");
      for (String key : registers.keySet()) {
         registers.put(key, 0);
      }
      for (int i = 0; i < memory.length; i++) {
         memory[i] = 0;
      }
      pc = 0;
   }
}
