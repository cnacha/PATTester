package nz.aucklanduni.scheduler;

import nz.aucklanduni.model.SubModel;
import nz.aucklanduni.tester.VerifierOnThreadedService;

public class ServiceRunner {
	private int portNumber;
	private boolean isAvailable ;
	private VerifierOnThreadedService thread;
	private SubModel model;
	private boolean isSync;
	
	public ServiceRunner(int portNumber) {
		super();
		this.portNumber = portNumber;
		this.isAvailable = true;
	}
	public int getPortNumber() {
		return portNumber;
	}
	public void setPortNumber(int portNumber) {
		this.portNumber = portNumber;
	}
	public boolean isAvailable() {
		return isAvailable;
	}
	public void setAvailable(boolean isAvailable) {
		this.isAvailable = isAvailable;
	}
	
	public VerifierOnThreadedService getThread() {
		return thread;
	}

	public SubModel getModel() {
		return model;
	}
	public void setModel(SubModel model) {
		this.model = model;
	}
	public boolean isSync() {
		return isSync;
	}
	public void setSync(boolean isSync) {
		this.isSync = isSync;
	}

	public void execute(){
		System.out.println("Start executing model: "+model.getId() +" on Port: "+this.portNumber);
		thread = new VerifierOnThreadedService(model.getComponents(),portNumber, isSync);
		thread.start();
	}
	
}
