package nz.aucklanduni.model;

import java.util.List;

public class ComponentConfig implements Cloneable{

	private int id;
	private boolean isstartcaller;
	private int[] calls;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public boolean isIsstartcaller() {
		return isstartcaller;
	}
	public void setIsstartcaller(boolean isstartcaller) {
		this.isstartcaller = isstartcaller;
	}
	public int[] getCalls() {
		return calls;
	}
	public void setCalls(int[] calls) {
		this.calls = calls;
	}
	
	public boolean hasCallTo(int compId) {
		if(calls!=null) {
			for(int call: calls) {
				if(call == compId)
					return true;
			}
		}
		return false;
	}
	
	public ComponentConfig clone() throws CloneNotSupportedException {
        return (ComponentConfig)super.clone();
    }
	
}
