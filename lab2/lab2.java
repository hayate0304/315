import java.io.*;
import java.util.*;

public class lab2 {

	private static final Map<String, String> I_INSTRUCTIONS = new HashMap<String, String>();
	private static final Map<String, String> R_INSTRUCTIONS = new HashMap<String, String>();
	private static final Map<String, String> J_INSTRUCTIONS = new HashMap<String, String>();
	private static final Map<String, String> SPECIAL_INSTRUCTIONS = new HashMap<String, String>();
	private static final Map<String, String> REGISTERS = new HashMap<String, String>();

	static {
		I_INSTRUCTIONS.put("addi", "001000");
		I_INSTRUCTIONS.put("beq", "000100");
		I_INSTRUCTIONS.put("bne", "000101");
		I_INSTRUCTIONS.put("lw", "100011");
		I_INSTRUCTIONS.put("sw", "101011");

		R_INSTRUCTIONS.put("and", "100100");
		R_INSTRUCTIONS.put("or", "100101");
		R_INSTRUCTIONS.put("add", "100000");
		R_INSTRUCTIONS.put("sll", "000000");
		R_INSTRUCTIONS.put("sub", "100010");
		R_INSTRUCTIONS.put("slt", "101010");

		J_INSTRUCTIONS.put("j", "000010");
		J_INSTRUCTIONS.put("jal", "000011");

		SPECIAL_INSTRUCTIONS.put("jr", "001000");

		REGISTERS.put("$0", "00000");
		REGISTERS.put("$zero", "00000");

		REGISTERS.put("$v0", "00010");
		REGISTERS.put("$v1", "00011");

		REGISTERS.put("$a0", "00100");
		REGISTERS.put("$a1", "00101");
		REGISTERS.put("$a2", "00110");
		REGISTERS.put("$a3", "00111");

		REGISTERS.put("$t0", "01000");
		REGISTERS.put("$t1", "01001");
		REGISTERS.put("$t2", "01010");
		REGISTERS.put("$t3", "01011");
		REGISTERS.put("$t4", "01100");
		REGISTERS.put("$t5", "01101");
		REGISTERS.put("$t6", "01110");
		REGISTERS.put("$t7", "01111");

		REGISTERS.put("$s0", "10000");
		REGISTERS.put("$s1", "10001");
		REGISTERS.put("$s2", "10010");
		REGISTERS.put("$s3", "10011");
		REGISTERS.put("$s4", "10100");
		REGISTERS.put("$s5", "10101");
		REGISTERS.put("$s6", "10110");
		REGISTERS.put("$s7", "10111");

		REGISTERS.put("$t8", "11000");
		REGISTERS.put("$t9", "11001");

		REGISTERS.put("$sp", "11101");
		REGISTERS.put("$ra", "11111");
	}

	public static void main(String args[]) {
		File asmFile;
		Scanner sc;
		ArrayList<String> labelQueue = new ArrayList<String>();
		HashMap<String, Integer> labels = new HashMap<String, Integer>();
		String fmtedAsm = "";
		int numLine = 0;

		if (args.length != 1) {
			System.out.println("Invalid number of arguments");
			return;
		}

		asmFile = new File(args[0]);

		try {
			sc = new Scanner(asmFile);
		} catch (FileNotFoundException exception) {
			System.out.println("File not found");
			return;
		}

		while (sc.hasNextLine()) {
			String asmLine = sc.nextLine();
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

		// remove trailing newline
		fmtedAsm = fmtedAsm.trim();

		// Clean problem areas so a scanner can parse the string
		fmtedAsm = fmtedAsm.replace(",", ", ");
		fmtedAsm = fmtedAsm.replace("$", " $");
		fmtedAsm = fmtedAsm.replace("( $", "($");
		fmtedAsm = fmtedAsm.replaceAll("\t", " ");
		fmtedAsm = fmtedAsm.replaceAll(" +", " ");
		fmtedAsm = fmtedAsm.replaceAll("\n ", "\n");

		translateAssembly(fmtedAsm, labels);

		sc.close();
	}

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

			if (R_INSTRUCTIONS.containsKey(instruction)) {
				String rs = "";
				String rt = "";
				String rd = "";
				String shamt = "";

				if (instruction.equals("sll")) {
					if (arguments.size() == 3) {
						rd = arguments.get(0);
						rt = arguments.get(1);
						shamt = numberToBinaryString(arguments.get(2), 5);

						if (REGISTERS.containsKey(rd)
								&& REGISTERS.containsKey(rt)) {
							rd = REGISTERS.get(rd);
							rt = REGISTERS.get(rt);
						} else {
							throw new InvalidAssemblyException(
									"Invalid Instruction, Unsupported Register: '"
											+ nextline + "'", lineNum);
						}
					} else {
						throw new InvalidAssemblyException(
								"Invalid Instruction, Incorrect Number of Arguments: '"
										+ nextline + "'", lineNum);
					}
				} else {
					// All other R type instructions
					if (arguments.size() == 3) {
						rd = arguments.get(0);
						rs = arguments.get(1);
						rt = arguments.get(2);

						if (REGISTERS.containsKey(rd)
								&& REGISTERS.containsKey(rs)
								&& REGISTERS.containsKey(rt)) {
							rd = REGISTERS.get(rd);
							rs = REGISTERS.get(rs);
							rt = REGISTERS.get(rt);
						} else {
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

				// opcode gets sent with the func for R instructions
				rFormatPrinter("000000", rs, rt, rd, shamt,
						R_INSTRUCTIONS.get(instruction));
			}

			else if (I_INSTRUCTIONS.containsKey(instruction)) {
				String rs = "";
				String rt = "";
				String imm = "";

				if (instruction.equals("beq") || instruction.equals("bne")) {
					if (arguments.size() == 3) {
						rs = arguments.get(0);
						rt = arguments.get(1);

						String label = arguments.get(2);

						if (labels.containsKey(label)) {
							imm = intToBinaryString(labels.get(label)
									- (lineNum + 1), 16);
						} else {
							throw new InvalidAssemblyException(
									"Invalid Instruction '" + nextline + "'",
									lineNum);
						}

						if (REGISTERS.containsKey(rs)
								&& REGISTERS.containsKey(rt)) {
							rs = REGISTERS.get(rs);
							rt = REGISTERS.get(rt);
						} else {
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
							imm = numberToBinaryString(location[0], 16);
							rs = location[1];

							if (REGISTERS.containsKey(rs)) {
								rs = REGISTERS.get(rs);
							} else {
								throw new InvalidAssemblyException(
										"Invalid Instruction, Unsupported Register: '"
												+ nextline + "'", lineNum);
							}
						} else {
							throw new InvalidAssemblyException(
									"Invalid Instruction '" + nextline + "'",
									lineNum);
						}

						if (REGISTERS.containsKey(rt)) {
							rt = REGISTERS.get(rt);
						} else {
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
					// Currently Just addi Instruction

					if (arguments.size() == 3) {
						rt = arguments.get(0);
						rs = arguments.get(1);

						imm = numberToBinaryString(arguments.get(2), 16);

						if (REGISTERS.containsKey(rs)
								&& REGISTERS.containsKey(rt)) {
							rs = REGISTERS.get(rs);
							rt = REGISTERS.get(rt);
						} else {
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

				iFormatPrinter(I_INSTRUCTIONS.get(instruction), rs, rt, imm);
			}

			else if (J_INSTRUCTIONS.containsKey(instruction)) {
				String address = "";

				if (arguments.size() == 1) {
					if (labels.containsKey(arguments.get(0))) {
						address = intToBinaryString(
								labels.get(arguments.get(0)), 26);
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

				jFormatPrinter(J_INSTRUCTIONS.get(instruction), address);
			} 
			
			else if (SPECIAL_INSTRUCTIONS.containsKey(instruction)) {
				ArrayList<String> instructions = new ArrayList<String>();

				//jr is the only special instruction supported
				if (instruction.equals("jr")) {
					String rs = "";

					if (arguments.size() == 1) {
						rs = arguments.get(0);
						if (REGISTERS.containsKey(rs)) {
							rs = REGISTERS.get(rs);
						} else {
							throw new InvalidAssemblyException(
									"Invalid Instruction, Unsupported Register: '"
											+ nextline + "'", lineNum);
						}
					} else {
						throw new InvalidAssemblyException(
								"Invalid Instruction, Incorrect Number of Arguments: '"
										+ nextline + "'", lineNum);
					}

					instructions.add("000000");
					instructions.add(rs);
					instructions.add("000000000000000");
					instructions.add(SPECIAL_INSTRUCTIONS.get("jr"));
				}

				specialFormatPrinter(instructions);
			} 
			
			else {
				throw new InvalidAssemblyException("Unsupported Instruction '"
						+ nextline + "'", lineNum);
			}

			lineNum++;
		}

		sc.close();
	}

	// If the instruction does not include one of these arguments then pass an
	// empty string and this function will convert it
	// This is true for all format printing functions
	private static void rFormatPrinter(String opcode, String rs, String rt,
			String rd, String shamt, String funct) {

		if (rs.equals("")) {
			rs = "00000";
		}

		if (rt.equals("")) {
			rt = "00000";
		}

		if (rd.equals("")) {
			rd = "00000";
		}

		if (shamt.equals("")) {
			shamt = "00000";
		}

		System.out.println(opcode + " " + rs + " " + rt + " " + rd + " "
				+ shamt + " " + funct);
	}

	private static void iFormatPrinter(String opcode, String rs, String rt,
			String imm) {

		if (rs.equals("")) {
			rs = "00000";
		}
		if (rt.equals("")) {
			rs = "00000";
		}
		if (imm.equals("")) {
			rs = "00000";
		}

		System.out.println(opcode + " " + rs + " " + rt + " " + imm);
	}

	private static void jFormatPrinter(String opcode, String address) {

		System.out.println(opcode + " " + address);

	}

	private static void specialFormatPrinter(ArrayList<String> instructions) {
		String output = "";

		for (String instruction : instructions) {
			output = output + " " + instruction;
		}

		System.out.println(output.trim());
	}

	// Removes comments from the assembly file
	private static String removeComments(String line) {
		int commentIndex = line.indexOf("#");
		if (commentIndex == -1) {
			return line;
		}
		line = line.substring(0, commentIndex);
		return line;
	}

	// Converts an integer to binary
	private static String intToBinaryString(Integer integer, int size) {
		String binaryString = "";

		binaryString = Integer.toBinaryString(1 << size | integer).substring(1);

		if (integer < 0) {
			// Remove extra 1's from signed binary
			binaryString = binaryString.substring(binaryString.length() - size);
		}

		return binaryString;
	}

	// Converts a Hex or decimal number to binary
	private static String numberToBinaryString(String number, int size) {
		int integer;

		if (number.contains("0x")) {
			// Hex
			integer = Integer.parseInt(number.replace("0x", ""), 16);
		} 
		else {
			integer = Integer.parseInt(number);
		}

		return intToBinaryString(integer, size);
	}

	public static class InvalidAssemblyException extends RuntimeException {
		public InvalidAssemblyException() {
			super();
		}

		public InvalidAssemblyException(String message, int lineNumber) {
			super(message + " on line " + lineNumber);
		}
	}
}
