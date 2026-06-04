package com.fpt.swp.sealhackathonbe.team.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateTeamRequest {
    @NotNull(message = "Event ID is required")
    private Integer eventID;

    @NotNull(message = "Catetory ID is required")
    private Integer catetoryID;

    @NotBlank(message = "Team name is required")
    @Size(max = 300, message = "Team name must not exceed: 300 characters")
    private String teamName;

    public  Integer getEventID() {
        return eventID;
    }

    public void setEventID(Integer eventID) {
        this.eventID = eventID;
    }

    public Integer getCatetoryID() {
        return catetoryID;
    }

    public void setCatetoryID(Integer catetoryID) {
        this.catetoryID = catetoryID;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }


}
