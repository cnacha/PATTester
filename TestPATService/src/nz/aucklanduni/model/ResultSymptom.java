package nz.aucklanduni.model;

import com.google.gson.annotations.SerializedName;

public class ResultSymptom {
	
	@SerializedName("Symptom") 
	private String symptom;
	
	@SerializedName("Scenario") 
	private String scenario;
	
	@SerializedName("LoopIndex") 
	private int loopIndex;
	
	@SerializedName("MemoryUsage") 
	private double memoryUsage;
	
	@SerializedName("TotalTime") 
	private float totalTime;
	
	@SerializedName("NumberOfStates") 
	private int numberOfStates;
	
	public String getSymptom() {
		return symptom;
	}
	public void setSymptom(String symptom) {
		this.symptom = symptom;
	}
	
	public String getScenario() {
		return scenario;
	}
	public void setScenario(String scenario) {
		this.scenario = scenario;
	}
	public int getLoopIndex() {
		return loopIndex;
	}
	public void setLoopIndex(int loopIndex) {
		this.loopIndex = loopIndex;
	}
	public double getMemoryUsage() {
		return memoryUsage;
	}
	public void setMemoryUsage(double memoryUsage) {
		this.memoryUsage = memoryUsage;
	}
	public float getTotalTime() {
		return totalTime;
	}
	public void setTotalTime(float totalTime) {
		this.totalTime = totalTime;
	}
	public int getNumberOfStates() {
		return numberOfStates;
	}
	public void setNumberOfStates(int numberOfStates) {
		this.numberOfStates = numberOfStates;
	}
	
	
}
