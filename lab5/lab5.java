import java.io.*;
import java.util.*;

public class lab5 {
	private static int[] memory = new int[8192];
	private static int pc = 0;
	
	private static HashMap<String, Integer> registers = new HashMap<String, Integer>();
	private static HashMap<String, Integer> labels = new HashMap<String, Integer>();
	private static ArrayList<String> labelQueue = new ArrayList<String>();
	private static ArrayList<Instruction> program = new ArrayList<Instruction>();
	
	private static ArrayList<String> R_INSTRUCTIONS = new ArrayList<String>();
	private static ArrayList<String> I_INSTRUCTIONS = new ArrayList<String>();
	private static ArrayList<String> J_INSTRUCTIONS = new ArrayList<String>();

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
		
		I_INSTRUCTIONS.add("addi");
		I_INSTRUCTIONS.add("beq");
		I_INSTRUCTIONS.add("bne");
		I_INSTRUCTIONS.add("lw");
		I_INSTRUCTIONS.add("sw");

		R_INSTRUCTIONS.add("and");
		R_INSTRUCTIONS.add("or");
		R_INSTRUCTIONS.add("add");
		R_INSTRUCTIONS.add("sll");
		R_INSTRUCTIONS.add("sub");
		R_INSTRUCTIONS.add("slt");
		R_INSTRUCTIONS.add("jr");

		J_INSTRUCTIONS.add("j");
		J_INSTRUCTIONS.add("jal");	
	}

	public static void main(String args[]) {
		Scanner linesc;
		String mode;

		initASM(args[0]);

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
				return;
			} else if (command.equals("h")) {
				printHelp();
			} else if (command.equals("d")) {
				printRegisters();
			} else if (command.equals("s")) {
				if (tokens.hasNextInt()) {
					step(tokens.nextInt());
				} else {
					step();
               System.out.println("1 instruction(s) executed");
				}
			} else if (command.equals("r")) {
				while(pc < program.size()) {
					step();
				}
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
			//if (mode.equals("i")) {
				System.out.print("mips> ");
			//}
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
		
		translateAssembly(fmtedAsm, labels);
		
		asmsc.close();
	}
	
	//Takes the formatted assembly string, validates, and fills the "program" ArrayList with Instructions
	@SuppressWarnings("resource")
	private static void translateAssembly(String fmtedAsm, HashMap<String, Integer> labels) {
		Scanner sc = new Scanner(fmtedAsm);
		Scanner line;
		int lineNum = 0;

		while (sc.hasNextLine()) {
			String nextline = sc.nextLine();
			line = new Scanner(nextline);

			String instruction = line.next();

			ArrayList<String> arguments = new ArrayList<String>();
			while (line.hasNext()) {
				arguments.add(line.next().replace(",", ""));
			}

			if (R_INSTRUCTIONS.contains(instruction)) {				
				String rs = "";
				String rt = "";
				String rd = "";
				int shamt = 0;

				if (instruction.equals("sll")) {
					if (arguments.size() == 3) {
						rd = arguments.get(0);
						rt = arguments.get(1);
						
						try {
							shamt = Integer.parseInt(arguments.get(2));
						}
						catch(NumberFormatException e) {
							throw new InvalidAssemblyException(
									"Invalid Instruction, Invalid Immediate value: '"
											+ nextline + "'", lineNum);
						}

						if (!(registers.containsKey(rd) && registers.containsKey(rt))) {
							throw new InvalidAssemblyException(
									"Invalid Instruction, Unsupported Register: '"
											+ nextline + "'", lineNum);
						}
					} else {
						throw new InvalidAssemblyException(
								"Invalid Instruction, Incorrect Number of Arguments: '"
										+ nextline + "'", lineNum);
					}
				}
				
				else if (instruction.equals("jr")) {
					if (arguments.size() == 1) {
						rs = arguments.get(0);
						if (!registers.containsKey(rs)) {
							throw new InvalidAssemblyException(
									"Invalid Instruction, Unsupported Register: '"
											+ nextline + "'", lineNum);
						}
					} else {
						throw new InvalidAssemblyException(
								"Invalid Instruction, Incorrect Number of Arguments: '"
										+ nextline + "'", lineNum);
					}
				}
				
				else {
					// All other R type instructions
					if (arguments.size() == 3) {
						rd = arguments.get(0);
						rs = arguments.get(1);
						rt = arguments.get(2);

						if (!(registers.containsKey(rd) && registers.containsKey(rs) && registers.containsKey(rt))) {
							throw new InvalidAssemblyException(
									"Invalid Instruction, Unsupported Register: '"
											+ nextline + "'", lineNum);
						}
					} else {
						throw new InvalidAssemblyException(
								"Invalid Instruction, Incorrect Number of Arguments: '"
										+ nextline + "'", lineNum);
					}
				}
				
				RegisterInstruction r = new RegisterInstruction(instruction, rs, rt, rd, shamt);			
				program.add(r);
			}

			else if (I_INSTRUCTIONS.contains(instruction)) {
				String rs = "";
				String rt = "";
				int imm = 0;

				if (instruction.equals("beq") || instruction.equals("bne")) {
					if (arguments.size() == 3) {
						rs = arguments.get(0);
						rt = arguments.get(1);

						String label = arguments.get(2);

						if (!labels.containsKey(label)) {
							throw new InvalidAssemblyException(
									"Invalid Instruction, Invalid Label: '" + nextline + "'",
									lineNum);
						}

						imm = labels.get(label);

						if (!(registers.containsKey(rs) && registers.containsKey(rt))) {
							throw new InvalidAssemblyException(
									"Invalid Instruction, Unsupported Register: '"
											+ nextline + "'", lineNum);
						}
					} else {
						throw new InvalidAssemblyException(
								"Invalid Instruction, Incorrect Number of Arguments: '"
										+ nextline + "'", lineNum);
					}
				}

				else if (instruction.equals("lw") || instruction.equals("sw")) {
					if (arguments.size() == 2) {
						rt = arguments.get(0);

						// Need to parse: offset($rs)
						String[] location = arguments.get(1).split("[\\(\\)]");
						if (location.length == 2) {
							rs = location[1];
							
							try {
								imm = Integer.parseInt(location[0]);
							}
							catch(NumberFormatException e) {
								throw new InvalidAssemblyException(
										"Invalid Instruction, Invalid Immediate value: '"
												+ nextline + "'", lineNum);
							}

							if (!registers.containsKey(rs)) {
								throw new InvalidAssemblyException(
										"Invalid Instruction, Unsupported Register: '"
												+ nextline + "'", lineNum);
							}
						} else {
							throw new InvalidAssemblyException(
									"Invalid Instruction '" + nextline + "'",
									lineNum);
						}

						if (!registers.containsKey(rt)) {
							throw new InvalidAssemblyException(
									"Invalid Instruction, Unsupported Register: '"
											+ nextline + "'", lineNum);
						}
					} else {
						throw new InvalidAssemblyException(
								"Invalid Instruction, Incorrect Number of Arguments: '"
										+ nextline + "'", lineNum);
					}
				}

				else if(instruction.equals("addi")) {
					if (arguments.size() == 3) {
						rt = arguments.get(0);
						rs = arguments.get(1);
						
						try {
							imm = Integer.parseInt(arguments.get(2));
						}
						catch(NumberFormatException e) {
							throw new InvalidAssemblyException(
									"Invalid Instruction, Invalid Immediate value: '"
											+ nextline + "'", lineNum);
						}

						if (!(registers.containsKey(rs) && registers.containsKey(rt))) {
							throw new InvalidAssemblyException(
									"Invalid Instruction, Unsupported Register: '"
											+ nextline + "'", lineNum);
						}
					} else {
						throw new InvalidAssemblyException(
								"Invalid Instruction, Incorrect Number of Arguments: '"
										+ nextline + "'", lineNum);
					}
				}
				ImmediateInstruction i = new ImmediateInstruction(instruction, rs, rt, imm);
				program.add(i);
			}

			else if (J_INSTRUCTIONS.contains(instruction)) {
				int address = 0;
				
				if (instruction.equals("j") || instruction.equals("jal")) {
					if (arguments.size() == 1) {
						if (labels.containsKey(arguments.get(0))) {
							address = labels.get(arguments.get(0));
						} else {
							throw new InvalidAssemblyException(
									"Invalid Instruction '" + nextline + "'",
									lineNum);
						}
					} else {
						throw new InvalidAssemblyException(
								"Invalid Instruction, Incorrect Number of Arguments: '"
										+ nextline + "'", lineNum);
					}
				}
				
				JumpInstruction j = new JumpInstruction(instruction, address);
				program.add(j);
			}
			
			else {
				throw new InvalidAssemblyException("Unsupported Instruction '"
						+ nextline + "'", lineNum);
			}

			lineNum++;
		}

		sc.close();
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

	private static void step(int numSteps) {
		for(int i=0; i<numSteps; i++) {
			step();
		}

		System.out.println("\t" + numSteps + " instruction(s) executed");
	}

	private static void step() {
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
				pc = ji.address;
				break;

			case "jal":
				ji = (JumpInstruction)i;
				registers.put("$ra", (pc + 1));
				pc = ji.address;
				break;
			case "jr":
				pc = registers.get("$ra");
				//System.out.println("PC = " + pc);
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
	}
}
