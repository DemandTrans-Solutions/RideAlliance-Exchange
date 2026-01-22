package com.clearinghouse.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author shankarI
 */
@Getter
@Setter
@NoArgsConstructor
public class MasterDTO {

    private int id;
    private String name;

    @Override
    public String toString() {
        return "MasterDTO [id=" + id + ", name=" + name + "]";
    }

}
