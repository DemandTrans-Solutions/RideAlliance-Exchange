package com.clearinghouse.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Prasad J
 */
@Getter
@Setter
@NoArgsConstructor
public class UploadFile {

    public String documentPath;
    public String documentValue;
    public String documentTitle;
    public String fileName;
    private String fileFormat;

    @Override
    public String toString() {
        return "UploadFile [documentPath=" + documentPath + ", documentValue=" + documentValue + ", documentTitle="
                + documentTitle + ", fileName=" + fileName + ", fileFormat=" + fileFormat + "]";
    }

}
