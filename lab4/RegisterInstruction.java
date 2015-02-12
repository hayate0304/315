
public class RegisterInstruction extends Instruction {
	String rs; 
	String rt; 
	String rd;
	int shamt;
	
	public RegisterInstruction(String instruction, String rs, String rt, String rd, int shamt) {
		this.instruction = instruction;
		
		this.rs = rs;
		this.rt = rt;
		this.rd = rd;
		this.shamt = shamt;
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
	
	public String getRd() {
		return rd;
	}
	
	public void setRd(String rd) {
		this.rd = rd;
	}
	
	public int getShamt() {
		return shamt;
	}
	
	public void setShamt(int shamt) {
		this.shamt = shamt;
	}
	
	@Override
	public String toString() {
		return instruction + " " + rd + " " + rs + " " + rt + " " + shamt;
	}
}
