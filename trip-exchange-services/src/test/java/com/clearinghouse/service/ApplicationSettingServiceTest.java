package com.clearinghouse.service;

import com.clearinghouse.dao.ApplicationSettingDAO;
import com.clearinghouse.dto.ApplicationSettingDTO;
import com.clearinghouse.entity.ApplicationSetting;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ApplicationSettingServiceTest {

    @Mock
    private ApplicationSettingDAO applicationSettingDAO;

    @Mock
    private ModelMapper modelMapper;
    
    @Mock
    private Base64.Encoder encoder;
    
    @Mock
    private Base64.Decoder decoder;

    @InjectMocks
    private ApplicationSettingService applicationSettingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Mock static encoder/decoder
        try (var staticMock = mockStatic(Base64.class)) {
            staticMock.when(Base64::getEncoder).thenReturn(encoder);
            staticMock.when(Base64::getDecoder).thenReturn(decoder);
            
            when(encoder.encodeToString(any(byte[].class))).thenReturn("encodedPassword");
            when(decoder.decode(anyString())).thenReturn("decodedPassword".getBytes());
        }
    }

    @Test
    void testFindAllApplicationSettings() {
        List<ApplicationSetting> applicationSettings = new ArrayList<>();
        applicationSettings.add(new ApplicationSetting());
        applicationSettings.add(new ApplicationSetting());

        List<ApplicationSettingDTO> applicationSettingDTOs = new ArrayList<>();
        applicationSettingDTOs.add(new ApplicationSettingDTO());
        applicationSettingDTOs.add(new ApplicationSettingDTO());

        when(applicationSettingDAO.findAllApplicationSettings()).thenReturn(applicationSettings);
        when(modelMapper.map(any(ApplicationSetting.class), eq(ApplicationSettingDTO.class)))
                .thenReturn(new ApplicationSettingDTO());

        List<ApplicationSettingDTO> result = applicationSettingService.findAllApplicationSettings();

        assertEquals(applicationSettingDTOs.size(), result.size());
        verify(applicationSettingDAO).findAllApplicationSettings();
        verify(modelMapper, times(applicationSettings.size())).map(any(ApplicationSetting.class), eq(ApplicationSettingDTO.class));
    }

    @Test
    void testFindApplicationSettingByApplicationId() {
        int applicationSettingId = 1;
        ApplicationSetting applicationSetting = new ApplicationSetting();
        ApplicationSettingDTO applicationSettingDTO = new ApplicationSettingDTO();

        when(applicationSettingDAO.findApplicationSettingById(applicationSettingId)).thenReturn(applicationSetting);
        when(modelMapper.map(applicationSetting, ApplicationSettingDTO.class)).thenReturn(applicationSettingDTO);

        ApplicationSettingDTO result = applicationSettingService.findApplicationSettingByApplicationId(applicationSettingId);

        assertEquals(applicationSettingDTO, result);
        verify(applicationSettingDAO).findApplicationSettingById(applicationSettingId);
        verify(modelMapper).map(applicationSetting, ApplicationSettingDTO.class);
    }

    @Test
    void testUpdateApplicationSetting() {
        ApplicationSettingDTO applicationSettingDTO = new ApplicationSettingDTO();
        ApplicationSetting applicationSetting = new ApplicationSetting();

        when(modelMapper.map(applicationSettingDTO, ApplicationSetting.class)).thenReturn(applicationSetting);
        when(applicationSettingDAO.updateApplicationSetting(applicationSetting)).thenReturn(applicationSetting);
        when(modelMapper.map(applicationSetting, ApplicationSettingDTO.class)).thenReturn(applicationSettingDTO);

        ApplicationSettingDTO result = applicationSettingService.updateApplicationSetting(applicationSettingDTO);

        assertEquals(applicationSettingDTO, result);
        verify(modelMapper).map(applicationSettingDTO, ApplicationSetting.class);
        verify(applicationSettingDAO).updateApplicationSetting(applicationSetting);
        verify(modelMapper).map(applicationSetting, ApplicationSettingDTO.class);
    }

    @Test
    void testToDTO() {
        ApplicationSetting applicationSetting = new ApplicationSetting();
        applicationSetting.setPasswordOfMail("encodedPassword");
        ApplicationSettingDTO applicationSettingDTO = new ApplicationSettingDTO();
        applicationSettingDTO.setPasswordOfMail("decodedPassword");

        when(modelMapper.map(applicationSetting, ApplicationSettingDTO.class)).thenReturn(applicationSettingDTO);

        ApplicationSettingDTO result = (ApplicationSettingDTO) applicationSettingService.toDTO(applicationSetting);

        assertEquals(applicationSettingDTO, result);
        verify(modelMapper).map(applicationSetting, ApplicationSettingDTO.class);
    }

    @Test
    void testToBO() {
        ApplicationSettingDTO applicationSettingDTO = new ApplicationSettingDTO();
        applicationSettingDTO.setPasswordOfMail("decodedPassword");
        ApplicationSetting applicationSetting = new ApplicationSetting();
        applicationSetting.setPasswordOfMail("encodedPassword");

        when(modelMapper.map(applicationSettingDTO, ApplicationSetting.class)).thenReturn(applicationSetting);

        ApplicationSetting result = (ApplicationSetting) applicationSettingService.toBO(applicationSettingDTO);

        assertEquals(applicationSetting, result);
        verify(modelMapper).map(applicationSettingDTO, ApplicationSetting.class);
    }

    @Test
    void testToDTOCollection() {
        try {
            applicationSettingService.toDTOCollection(null);
        } catch (UnsupportedOperationException e) {
            assertEquals("Not supported yet.", e.getMessage());
        }
    }
}
