package com.fana.realtimechat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class CreateGroupRequest {

    private String name;
    public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<String> getParticipantUsernames() {
		return participantUsernames;
	}
	public void setParticipantUsernames(List<String> participantUsernames) {
		this.participantUsernames = participantUsernames;
	}
	private List<String> participantUsernames;
}
