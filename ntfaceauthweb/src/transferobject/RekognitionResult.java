package transferobject;

import com.amazonaws.services.rekognition.model.Face;

public class RekognitionResult {

	private Float confidence;
	private String customerImageName;
	private Face matchedFace;
		
	
	public Float getConfidence() {
		return confidence;
	}
	public void setConfidence(Float confidence) {
		this.confidence = confidence;
	}
	public String getCustomerImageName() {
		return customerImageName;
	}
	public void setCustomerImageName(String customerImageName) {
		this.customerImageName = customerImageName;
	}
	public Face getMatchedFace() {
		return matchedFace;
	}
	public void setMatchedFace(Face matchedFace) {
		this.matchedFace = matchedFace;
	}

	
	
	
}
