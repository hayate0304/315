
public class JumpInstruction extends Instruction {
	int address;
	
	public JumpInstruction(String instruction, int address) {
		super.instruction = instruction;
		
		this.address = address;
	}
	
	public int getAddress() {
		return address;
	}
	
	public void setAddress(int address) {
		this.address = address;
	}
	
	@Override
	public String toString() {
		return instruction + " " + address;
	}
}
