package com.clearinghouse.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class InputDTO {

    String lastSyncDateTime;
    List<Integer> tripTicketIds;


    @Override
    public String toString() {
        return "InputDTO [lastSyncDateTime=" + lastSyncDateTime + ", tripTicketIds=" + tripTicketIds + "]";
    }
}
