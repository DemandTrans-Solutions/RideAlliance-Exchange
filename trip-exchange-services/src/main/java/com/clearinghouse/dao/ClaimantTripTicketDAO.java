package com.clearinghouse.dao;

import com.clearinghouse.dto.ClaimantTripTicketDTO;
import com.clearinghouse.entity.ClaimantTripTicket;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class ClaimantTripTicketDAO extends AbstractDAO<Integer, ClaimantTripTicket> {


    private final ModelMapper claimantTripModelMapper;


    public ClaimantTripTicket createClaimantTripTicket(ClaimantTripTicket claimantTripTicket) {
        add(claimantTripTicket);
        return claimantTripTicket;

    }


    public ClaimantTripTicket updateClaimantTripTicket(ClaimantTripTicket claimantTripTicket) {
        update(claimantTripTicket);
        return claimantTripTicket;
    }


    public Object toDTO(Object bo) {

        ClaimantTripTicket claimantTripTicketBO = (ClaimantTripTicket) bo;

        ClaimantTripTicketDTO claimantTripTicketDTO = claimantTripModelMapper.map(claimantTripTicketBO, ClaimantTripTicketDTO.class);

        return claimantTripTicketDTO;
    }


    public ClaimantTripTicketDTO convertToDTO(ClaimantTripTicket claimantTripTicket) {
        // TODO Auto-generated method stub
        return (ClaimantTripTicketDTO) toDTO(claimantTripTicket);
    }
}
