import java.io.*;
import java.util.*;

public class lab2 {

   private static ArrayList<String> I_INSTRUCTIONS = new ArrayList<>(Arrays.asList("addi", "beq", "bne", "lw", "sw"));
   private static ArrayList<String> R_INSTRUCTIONS = new ArrayList<>(Arrays.asList("and", "or", "add", "sll", "sub", "slt"));
   private static ArrayList<String> J_INSTRUCTIONS = new ArrayList<>(Arrays.asList("j", "jr", "jal"));
   
   private static final String[] REGISTERS = {"$s0"};

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

      while(sc.hasNextLine()) {
         String asmLine = sc.nextLine();
         asmLine = removeComments(asmLine);

         int labelIndex = asmLine.indexOf(":");

         /* This will get rid of blank lines and format labels in a way that
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
      fmtedAsm = fmtedAsm.replaceAll("\t", " ");
      fmtedAsm = fmtedAsm.replaceAll(" +", " ");
      fmtedAsm = fmtedAsm.replaceAll("\n ", "\n");


     // System.out.println("FMT ASM: \n" + fmtedAsm);
      translateAssembly(fmtedAsm, labels);
   }

   private static void translateAssembly(String fmtedAsm, HashMap<String, Integer> labels) {
      Scanner sc = new Scanner(fmtedAsm);
      Scanner line;
      int lineNum = 0;
      
      while(sc.hasNextLine()) {
         String nextline = sc.nextLine();
         line = new Scanner(nextline);
         
         String instruction = line.next().trim();
         
         ArrayList<String> arguments = new ArrayList<String>();
         
         while(line.hasNext()) {
    		 arguments.add(line.next().replace(",", ""));
    	 }

         if(R_INSTRUCTIONS.contains(instruction)) {
        	 if(arguments.size() != 3) {
        		 //Invalid Instruction?
        	 }
        	 
        	 String rd = arguments.get(0);
        	 String rs = arguments.get(1);
        	 String rt = arguments.get(2);
        	 
        	 String opcode = "";
        	 String shamt = "";
        	 String funct = "";
        	 
        	 rFormatPrinter(opcode, rs, rt, rd, shamt, funct);
         }
         
         else if(I_INSTRUCTIONS.contains(instruction)) {
        	 if(arguments.size() != 3) {
        		 //Invalid Instruction?
        	 }
        	 
        	 String rt = arguments.get(0);
        	 String rs = arguments.get(1);
        	 String imm = arguments.get(2);
        	 
        	 String opcode = "";
        	 
        	 iFormatPrinter(opcode, rs, rt, imm);
         }
         
         else if(J_INSTRUCTIONS.contains(instruction)) {
        	 if(arguments.size() != 1) {
        		 //Invalid Instruction?
        	 }
        	 
        	 String address = arguments.get(0);
        	 
        	 String opcode = "";
        	 
        	 jFormatPrinter(opcode, address);
         }
         
         else {
        	 //Unknown Instruction
        	 System.out.println("Bad Instruction: " + nextline + "\nOn Line: " + lineNum);
         }
         
         lineNum++;
      }
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

      System.out.println(opcode + rs + rt + rd + shamt + funct);
   }

   private static void iFormatPrinter(String opcode, String rs, String rt,
         String imm) {
	   
	   System.out.println(opcode + rs + rt + imm);
   }

   private static void jFormatPrinter(String opcode, String address) {

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
}
