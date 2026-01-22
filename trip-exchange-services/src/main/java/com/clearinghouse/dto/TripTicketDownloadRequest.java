package com.clearinghouse.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class TripTicketDownloadRequest {



    public List<Integer> ticketIds;


}
