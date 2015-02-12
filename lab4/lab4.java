// Authors: Lance Boettcher and Brandon Leventhal

import java.io.*;
import java.util.*;

public class lab4 {

	private static int[] memory = new int[8192];
	private static int pc = 0;
   private static int emulatorpc = 0;
   private static String[] pipeline = new String[4];

   // Flags
   private static String mode;
   private static boolean stall = false;
   private static boolean squash = false;
   private static int multSquash = 0;

	private static HashMap<String, Integer> registers = new HashMap<String, Integer>();
	private static HashMap<String, Integer> labels = new HashMap<String, Integer>();
	private static ArrayList<Instruction> program = new ArrayList<Instruction>();

	private static ArrayList<String> R_INSTRUCTIONS = new ArrayList<String>();
	private static ArrayList<String> I_INSTRUCTIONS = new ArrayList<String>();
	private static ArrayList<String> J_INSTRUCTIONS = new ArrayList<String>();

   private static int instructionCount = 0;
   private static int cycleCount = 0;


   public static void main(String args[]) {
      Scanner linesc;

      Formatter formatter = new Formatter(args[0]);
      program = formatter.getProgram();
      labels = formatter.getLabels();
      initPipeLine();

      if (args.length == 2) {
         mode = "s";
         File script = new File(args[1]);

         try {
            linesc = new Scanner(script);
         } catch (FileNotFoundException e) {
            System.out.println("Error reading script file");
            return;
         }
      } else {
         mode = "i";
         linesc = new Scanner(System.in);
      }

      runEmulator(linesc);
   }

   private static void initPipeLine() {
      for (int i = 0; i < 4; i++) {
         pipeline[i] = "empty";
      }
   }

   private static void runEmulator(Scanner linesc) {
      System.out.print("mips> ");

      while (linesc.hasNextLine()) {
         String line = linesc.nextLine();
         if (mode.equals("s")) {
            System.out.println(line);
         }

         Scanner tokens = new Scanner(line);
         String command = tokens.next();

         if (command.equals("q")) {
            tokens.close();
            //return;
            break; // this might need to be changed, not sure yet
         } else if (command.equals("h")) {
            printHelp();
         } else if (command.equals("d")) {
            printRegisters();
         } else if (command.equals("p")) {
            printPipeLine();
         } else if (command.equals("s")) {
            if (tokens.hasNextInt()) {
               // step(tokens.nextInt());
            } else {
               // step(0) or step()
            }
         } else if (command.equals("r")) {
            //while (pc < program.size()) {
               //step();
            //}
         } else if (command.equals("m")) {
            int start = tokens.nextInt();
            int stop = tokens.nextInt();
            printDataMemory(start, stop);
         } else if (command.equals("c")) {
            //clearSimulator();
         } else {
            System.out.println("Command not recognized");
         }

         tokens.close();
         System.out.print("mips> ");
      }
      linesc.close();
   }

	private static void printHelp() {
		System.out.println("\nh = show help");
		System.out.println("d = dump register state");
      System.out.println("p = show pipeline registers");
		System.out.println("s = single step through the program (i.e. execute 1 instruction and stop)");
		System.out.println("s num = step through num instructions of the program");
		System.out.println("r = run until the program ends");
		System.out.println("m num1 num2 = display data memory from location num1 to num2");
		System.out.println("c = clear all registers, memory, and the program counter to 0");
		System.out.println("q = exit the program\n");
	}

	private static void printRegisters() {
		String output = "\npc = " + pc + "\n"
				+ "$0 = " + registers.get("$0") + "          $v0 = " + registers.get("$v0") + "         $v1 = " + registers.get("$v1") + "         $a0 = " + registers.get("$a0") + "\n"
				+ "$a1 = " + registers.get("$a1") + "         $a2 = " + registers.get("$a2") + "         $a3 = " + registers.get("$a3") + "         $t0 = " + registers.get("$t0") + "\n"
				+ "$t1 = " + registers.get("$t1") + "         $t2 = " + registers.get("$t2") + "         $t3 = " + registers.get("$t3") + "         $t4 = " + registers.get("$t4") + "\n"
				+ "$t5 = " + registers.get("$t5") + "         $t6 = " + registers.get("$t6") + "         $t7 = " + registers.get("$t7") + "         $s0 = " + registers.get("$s0") + "\n"
				+ "$s1 = " + registers.get("$s1") + "         $s2 = " + registers.get("$s2") + "         $s3 = " + registers.get("$s3") + "         $s4 = " + registers.get("$s4") + "\n"
				+ "$s5 = " + registers.get("$s5") + "         $s6 = " + registers.get("$s6") + "         $s7 = " + registers.get("$s7") + "         $t8 = " + registers.get("$t8") + "\n"
				+ "$t9 = " + registers.get("$t9") + "         $sp = " + registers.get("$sp") + "         $ra = " + registers.get("$ra") + "\n";

		System.out.println(output);
	}

   private static void printPipeLine() {
      System.out.println();
      String firstLine = String.format("%-10s %-10s %-10s %-10s %-10s", "pc", "if/id", "id/exe", "exe/mem", "mem/wb");
      System.out.println(firstLine);
      String secondLine = String.format("%-10d %-10s %-10s %-10s %-10s", pc, pipeline[0], pipeline[1], pipeline[2], pipeline[3]);
      System.out.println(secondLine + "\n");
   }

   private static void step() {

      if (squash == true)  {
         pipeline[3] = pipeline[2];
         pipeline[2] = pipeline[1];
         pipeline[1] = pipeline[0];
         pipeline[0] = "squash";
         squash = false;
         cycleCount++;
      } else if (stall == true) {
         pipeline[3] = pipeline[2];
         pipeline[2] = pipeline[1];
         pipeline[1] = "stall";

         cycleCount++;
         stall = false;
      } else if (multSquash > 0) {
         if (multSquash == 1) {
            pipeline[3] = pipeline[2];
            pipeline[2] = "squash";
            pipeline[1] = "squash";
            pipeline[0] = "squash";

            // UPDATE PC HERE FOR THE BRANCH CMD


         } else {
            pipeline[3] = pipeline[2];
            pipeline[2] = pipeline[1];
            pipeline[1] = pipeline[0];
            pipeline[0] = program.get(pc).getInstruction();
         }
         cycleCount++;
         multSquash--;
      } else {
         pipeline[3] = pipeline[2];
         pipeline[2] = pipeline[1];
         pipeline[1] = pipeline[0];
         pipeline[0] = program.get(pc).getInstruction();
         cycleCount++;
         //simulatorStep();
      }
   }

   private static void step(int numSteps) {

   }

	private static void simulatorStep() {
		Instruction i = program.get(pc);
		String instruction = i.getInstruction();

		ImmediateInstruction ii = null;
		RegisterInstruction ri = null;
		JumpInstruction ji = null;
		int rs;
		int rt;

		switch(instruction) {
			case "addi":
				ii = (ImmediateInstruction)i;
				rs = registers.get(ii.rs);
				registers.put(ii.rt, (rs + ii.imm));
				pc++;
				break;

			case "beq":
				ii = (ImmediateInstruction)i;
				rs = registers.get(ii.rs);
				rt = registers.get(ii.rt);

				if(rs == rt) {
					pc = ii.imm;
				}
				else {
					pc++;
				}

				break;

			case "bne":
				ii = (ImmediateInstruction)i;
				rs = registers.get(ii.rs);
				rt = registers.get(ii.rt);

				if(rs != rt) {
					pc = ii.imm;
				}
				else {
					pc++;
				}

				break;

			case "lw":
				ii = (ImmediateInstruction)i;
				rs = registers.get(ii.rs);
				registers.put(ii.rt, memory[rs + ii.imm]);
				pc++;
				break;

			case "sw":
				ii = (ImmediateInstruction)i;
				rs = registers.get(ii.rs);
				rt = registers.get(ii.rt);
				memory[rs + ii.imm] = rt;
				pc++;
				break;

			case "and":
				ri = (RegisterInstruction)i;
				rs = registers.get(ri.rs);
				rt = registers.get(ri.rt);
				registers.put(ri.rd, (rs & rt));
				pc++;
				break;

			case "or":
				ri = (RegisterInstruction)i;
				rs = registers.get(ri.rs);
				rt = registers.get(ri.rt);
				registers.put(ri.rd, (rs | rt));
				pc++;
				break;

			case "add":
				ri = (RegisterInstruction)i;
				rs = registers.get(ri.rs);
				rt = registers.get(ri.rt);
				registers.put(ri.rd, (rs + rt));
				pc++;
				break;

			case "sll":
				ri = (RegisterInstruction)i;
				rs = registers.get(ri.rs);
				registers.put(ri.rd, (rs << ri.shamt));
				pc++;
				break;

			case "sub":
				ri = (RegisterInstruction)i;
				rs = registers.get(ri.rs);
				rt = registers.get(ri.rt);
				registers.put(ri.rd, (rs - rt));
				pc++;
				break;

			case "slt":
				ri = (RegisterInstruction)i;
				rs = registers.get(ri.rs);
				rt = registers.get(ri.rt);

				if(rs < rt) {
					registers.put(ri.rd, 1);
				}
				else {
					registers.put(ri.rd, 0);
				}

				pc++;
				break;

			case "j":
				ji = (JumpInstruction)i;
            // probably need temp pc
				pc = ji.address;
				break;

			case "jal":
				ji = (JumpInstruction)i;
				registers.put("$ra", (pc + 1));
            // Might need temp pc here to keep printing correct
				pc = ji.address;
				break;
			case "jr":
            // same as above, temp pc
				pc = registers.get("$ra");
				break;
		}
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
      initPipeLine();
      instructionCount = 0;
      cycleCount = 0;
      stall = false;
      squash = false;
      multSquash = 0;
	}

   private static void printStats() {
      System.out.println("\nProgram complete");
      double cpi;
      if (instructionCount == 0) {
         cpi = 0.0;
      } else {
         cpi = cycleCount / (double)instructionCount;
      }

      System.out.println("CPI = " + cpi + "\tCycles = " + cycleCount + "\tInstructions = " + instructionCount);
   }
}

