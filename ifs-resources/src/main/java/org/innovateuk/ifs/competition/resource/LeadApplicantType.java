package org.innovateuk.ifs.competition.resource;

/**
 * This enum defines the options for the lead applicant type for a competition.
 */
public enum LeadApplicantType {
	BUSINESS("business", "Business"),
	RESEARCH("research", "Research"),
	RTO("rto", "Research and technology organisation (RTO's)");

	private String code;
	private String name;
	
	LeadApplicantType(String code, String name) {
		this.code = code;
		this.name = name;
	}
	
	public String getCode() {
		return code;
	}
	
	public String getName() {
		return name;
	}
	
	public static LeadApplicantType fromCode(String code) {
		for(LeadApplicantType type: LeadApplicantType.values()){
			if(type.getCode().equals(code)){
				return type;
			}
		}
		return null;
	}
}
