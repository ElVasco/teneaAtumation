package org.tenea.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserInfoResponse {

    private int id;

    @JsonProperty("id_virtual_card")
    private int idVirtualCard;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdVirtualCard() { return idVirtualCard; }
    public void setIdVirtualCard(int idVirtualCard) { this.idVirtualCard = idVirtualCard; }
}
