import java.io.*;
import java.util.*;

public class Formatter {
   private String filename;
   private String fmtedAsm = "";

	private HashMap<String, Integer> labels = new HashMap<String, Integer>();
	private ArrayList<String> labelQueue = new ArrayList<String>();
	private ArrayList<Instruction> program = new ArrayList<Instruction>();
	public static HashMap<String, Integer> registers = new HashMap<String, Integer>();

	public static ArrayList<String> R_INSTRUCTIONS = new ArrayList<String>();
	public static ArrayList<String> I_INSTRUCTIONS = new ArrayList<String>();
	public static ArrayList<String> J_INSTRUCTIONS = new ArrayList<String>();



   public Formatter(String filename) {
      this.filename = filename;

      setupRegisters();

		Scanner asmsc;
		File asm = new File(filename);
		int numLine = 0;

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

			if (labelIndex != -1) {
				this.labelQueue.add(asmLine.substring(0, labelIndex).trim());
				asmLine = asmLine.substring(labelIndex + 1);
			}

			if (!asmLine.trim().isEmpty()) {
				if (this.labelQueue.size() > 0) {
					String label = this.labelQueue.remove(0);
					this.labels.put(label, numLine);
				}

				this.fmtedAsm = this.fmtedAsm + asmLine + '\n';
				numLine++;
			}
      }

		this.fmtedAsm = this.fmtedAsm.trim();

		// Clean problem areas so a scanner can parse the string
		this.fmtedAsm = this.fmtedAsm.replace(",", ", ");
		this.fmtedAsm = this.fmtedAsm.replace("$", " $");
		this.fmtedAsm = this.fmtedAsm.replace("( $", "($");
		this.fmtedAsm = this.fmtedAsm.replaceAll("\t", " ");
		this.fmtedAsm = this.fmtedAsm.replaceAll(" +", " ");
		this.fmtedAsm = this.fmtedAsm.replaceAll("\n ", "\n");

		translateAssembly(this.fmtedAsm, this.labels);

		asmsc.close();
   }

   public ArrayList<Instruction> getProgram() {
      return this.program;
   }

   public HashMap<String, Integer> getLabels() {
      return this.labels;
   }

   public HashMap<String, Integer> getRegisters() {
      return registers;
   }

   public ArrayList<String> getRInstr() {
      return R_INSTRUCTIONS;
   }

   public ArrayList<String> getIInstr() {
      return I_INSTRUCTIONS;
   }

   public ArrayList<String> getJInstr() {
      return J_INSTRUCTIONS;
   }

   private String removeComments(String line) {
      int commentIndex = line.indexOf("#");

      if (commentIndex == -1) {
         return line;
      }

		line = line.substring(0, commentIndex);
      return line;
   }

	@SuppressWarnings("resource")
   private void translateAssembly(String fmtedAsm, HashMap<String, Integer> labels) {
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

   private static void setupRegisters() {
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
}
