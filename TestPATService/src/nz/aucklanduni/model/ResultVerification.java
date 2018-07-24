package nz.aucklanduni.model;

import java.util.List;

public class ResultVerification {

	private long elapseTime;
	private List<ResultSymptom> diagnosisList;
	public long getElapseTime() {
		return elapseTime;
	}
	public void setElapseTime(long elapseTime) {
		this.elapseTime = elapseTime;
	}
	public List<ResultSymptom> getDiagnosisList() {
		return diagnosisList;
	}
	public void setDiagnosisList(List<ResultSymptom> diagnosisList) {
		this.diagnosisList = diagnosisList;
	}
	
	
}
