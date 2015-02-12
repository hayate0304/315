
public class ImmediateInstruction extends Instruction {
	String rs; 
	String rt;
	int imm;
	
	public ImmediateInstruction(String instruction, String rs, String rt, int imm) {
		super.instruction = instruction;
		
		this.rs = rs;
		this.rt = rt;
		this.imm = imm;
	}
	
	public String getRs() {
		return rs;
	}
	
	public void setRs(String rs) {
		this.rs = rs;
	}
	
	public String getRt() {
		return rt;
	}
	
	public void setRt(String rt) {
		this.rt = rt;
	}
	
	public int getImm() {
		return imm;
	}
	
	public void setImm(int imm) {
		this.imm = imm;
	}
	
	public String toString() {
		return instruction + " " + rs + " " + rt + " " + imm;
	}
}