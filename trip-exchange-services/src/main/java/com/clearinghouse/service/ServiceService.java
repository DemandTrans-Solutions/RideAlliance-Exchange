/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.service;

import com.clearinghouse.dao.*;
import com.clearinghouse.dto.*;
import com.clearinghouse.entity.Provider;
import com.clearinghouse.entity.ServiceArea;
import com.clearinghouse.entity.TripTicket;
import com.clearinghouse.exceptions.InvalidKMLFileException;
import com.clearinghouse.exceptions.ServiceAreaActiveCheckException;
import com.clearinghouse.util.KMLParser;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author chaitanyaP
 */
@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class ServiceService implements IConvertBOToDTO, IConvertDTOToBO {


    private final ServiceDAO serviceDAO;


    private final ServiceAreaDAO serviceAreaDAO;


    private final TripTicketDAO tripTicketDAO;


    private final TripClaimDAO tripClaimDAO;


    private final ModelMapper serviceModelMapper;


    private final GeoJSONService geoJSONService;

    private final HospitalityAreaProviderDAO hospitalityAreaProviderDAO;


    private final ModelMapper modelMapper;
    private final ProviderService providerService;


    public List<ServiceDTO> findAllServicearea() {

        List<com.clearinghouse.entity.Service> serviceareaList = serviceDAO.findAllSerivearea();

        List<ServiceDTO> serviceDTOList = new java.util.ArrayList<>();
        for (com.clearinghouse.entity.Service service : serviceareaList) {
            ServiceDTO serviceDTO = (ServiceDTO) toDTO(service);
            serviceDTOList.add(serviceDTO);
        }

        return serviceDTOList;

    }


    public List<ServiceDTO> findServiceareaByProviderId(int providerId) {
        List<com.clearinghouse.entity.Service> serviceareaByProviderList = serviceDAO.findAllSeriveareaByProviderId(providerId);
        List<ServiceDTO> serviceDTOByProviderIdList = new java.util.ArrayList<>();
        for (com.clearinghouse.entity.Service service : serviceareaByProviderList) {

            ServiceDTO serviceDTO = (ServiceDTO) toDTO(service);
            if (service.getHospitalServiceAreas() != null) {


                serviceDTO.setServiceAreaList(service
                        .getHospitalServiceAreas().parallelStream().map(p ->
                                new ServiceAreaDTO(p.getServiceAreaId(),
                                        geoJSONService.toGeoJSON(p.getServiceAreaGeometry()),
                                        p.getServiceName(),
                                        p.getService().getServiceId()
                                )
                        )
                        .collect(Collectors.toSet()));
            }
            serviceDTOByProviderIdList.add(serviceDTO);
        }
        return serviceDTOByProviderIdList;
    }


    public ServiceDTO findServiceareaByServiceId(int serviceId) {
        return (ServiceDTO) toDTO(serviceDAO.findServiceareaByServiceId(serviceId));
    }


    public ServiceDTO createServicearea(ServiceDTO serviceDTO) {

        com.clearinghouse.entity.Service service = (com.clearinghouse.entity.Service) toBO(serviceDTO);
        service.setProviderSelected(false);
        service.setHospitalityArea(false);
        com.clearinghouse.entity.Service createObjTripTicket = serviceDAO.createServicearea(service);
        return (ServiceDTO) toDTO(createObjTripTicket);
    }


    public ServiceDTO updateServicearea(ServiceDTO serviceDTO) {

        com.clearinghouse.entity.Service service = (com.clearinghouse.entity.Service) toBO(serviceDTO);
        com.clearinghouse.entity.Service serviceUpdated = serviceDAO.updateServicearea(service);
        /* check which feild are updated */
        return (ServiceDTO) toDTO(serviceUpdated);
    }


    public CheckServiceareaDTO checkServicearea(CheckServiceareaDTO checkServiceareaDTOObj) {
        boolean checkServiceArea = false;
        int serviceId = 0;
        /*
         * fetch the service area of that claimant provider and check wheather trip
         * ticket latlongs are in that
         */

        TripTicket tripTicket = tripTicketDAO.findTripTicketByTripTicketId(checkServiceareaDTOObj.getTripTicketId());
        List<com.clearinghouse.entity.Service> serviceAreaList = serviceDAO.findAllSeriveareaByProviderId(checkServiceareaDTOObj.getClaimantProviderId());

        if (serviceAreaList.isEmpty()) {
            throw new ServiceAreaActiveCheckException("no active service area ");
        }

        serviceId = findServiceForTripTicket(serviceAreaList, tripTicket);
        if (serviceId == 0) checkServiceArea = true;

        boolean isClaimPresent = tripClaimDAO.isClaimPresent(checkServiceareaDTOObj.getTripTicketId(),
                checkServiceareaDTOObj.getClaimantProviderId());
        checkServiceareaDTOObj.setIsAlredyClaimed(isClaimPresent);
        checkServiceareaDTOObj.setIsEligibleForService(checkServiceArea);
        checkServiceareaDTOObj.setServiceId(serviceId);

        return checkServiceareaDTOObj;
    }


    protected int findServiceForTripTicket(List<com.clearinghouse.entity.Service> serviceAreaList, TripTicket tripTicket) {
        int serviceId = 0;
        // check pickup and drop off belongs to servicearea
        var pickupLatitude = tripTicket.getPickupAddress().getLatitude();
        var pickupLongitude = tripTicket.getPickupAddress().getLongitude();
        var dropOffLatitude = tripTicket.getDropOffAddress().getLatitude();
        var dropOffLongitude = tripTicket.getDropOffAddress().getLongitude();
        if ((!serviceAreaList.isEmpty()) && (pickupLatitude != 0)) {
            for (com.clearinghouse.entity.Service service : serviceAreaList) {
                /**
                 * check is service area is active
                 */
                if (service.isActive() && !service.isHospitalityArea() && !service.isProviderSelected()) {
                    // first check if pu and dropoff are in the service boundaries
                    if (serviceDAO.checkAddressInService(service, pickupLatitude, pickupLongitude)) {
                        if (serviceDAO.checkAddressInService(service, dropOffLatitude, dropOffLongitude)) {

                            serviceId = service.getServiceId();
                        }
                    }
                    if (serviceId == 0 && service.getHospitalServiceAreas() != null && !service.getHospitalServiceAreas().isEmpty()) {
                        for (ServiceArea serviceArea : service.getHospitalServiceAreas()) {
                            //String serviceArea = serviceArea.getServiceArea();

                            /* if latlongs of PU and DoF ar nul then sa filter ll not be applied */
//                      check for PUA
                            if (serviceDAO.checkAddressInServicearea(serviceArea, pickupLatitude, pickupLongitude)) {
//                        check for DOFFA
                                if (serviceDAO.checkAddressInServicearea(serviceArea, dropOffLatitude, dropOffLongitude)) {

                                    serviceId = service.getServiceId();
                                }
                            }
                        }
                    }
                }
            }
        }
        return serviceId;
    }

    @Override
    public Object toDTO(Object bo) {
        var serviceBO = (com.clearinghouse.entity.Service) bo;
        ServiceDTO serviceDTO = serviceModelMapper.map(serviceBO, ServiceDTO.class);
        log.debug("ServiceSErvice toDTO: {} with serviceAreaList {}", serviceDTO, serviceDTO.getServiceAreaList());
        return serviceDTO;
    }

    @Override
    public Object toBO(Object dto) {
        var serviceDTO = (ServiceDTO) dto;
        var serviceBO = serviceModelMapper.map(serviceDTO, com.clearinghouse.entity.Service.class);
        return serviceBO;
    }

    @Override
    public Object toDTOCollection(Object boCollection) {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
        // Tools | Templates.
    }


    public boolean isLastActiveSerivearea(IsLastActiveSeriveareaDTO isLastActiveSeriveareaDTO) {

        boolean result = true;

        List<com.clearinghouse.entity.Service> services = serviceDAO.findAllSeriveareaByProviderId(isLastActiveSeriveareaDTO.getProviderId());

        for (com.clearinghouse.entity.Service service : services) {
            if (service.getServiceId() != isLastActiveSeriveareaDTO.getServiceId()) {
                if (service.isActive()) {
                    result = false;
                    break;
                }
            }
        }
        return result;
    }


    public ServiceDTO createHospitalityServicearea(ServiceDTO serviceDTO) {
        com.clearinghouse.entity.Service serviceBO = (com.clearinghouse.entity.Service) toBO(serviceDTO);
        UploadFile uploadFile = serviceDTO.getUploadFile();
        serviceBO.setProvider(null);
        String filePath = null;
		/*
		if (uploadFile.getDocumentValue() != null && uploadFile.getFileFormat() != null && serviceBO != null
				&& uploadFile.getFileName() != null) {
			filePath = createFileForHospitality(serviceDTO.getUploadFile());
			serviceBO.setUploadFilePath(filePath);
			serviceBO.setHospitalityArea(true);
			uploadFile.setDocumentPath(filePath);
		}*/
        Set<Provider> providerList = serviceDTO.getProviderIdList().parallelStream().map(id -> new Provider(id))
                .collect(Collectors.toSet());
        log.debug("providerlist" + providerList.size());
        // String serviceArea = XMLParser.getServiceAreaFromFile(filePath);
        // serviceBO.setServiceArea(serviceArea);
        // serviceBO.setHospitalAreaProvider(providerList);
        com.clearinghouse.entity.Service createObjTripTicket = serviceDAO.createServicearea(serviceBO);
        ServiceDTO serviceDTOUpdated = (ServiceDTO) toDTO(createObjTripTicket);
        // serviceDTOUpdated.setProviderIdList(serviceBO.getHospitalAreaProvider().parallelStream().map(p->p.getProviderId()).collect(Collectors.toSet()));
        serviceDTOUpdated.setUploadFile(uploadFile);
        return serviceDTOUpdated;
    }


	/*
	public String createFileForHospitality(UploadFile uploadFile) {
		String directory = hospitalityFilePath;
		String documentPath = null;
		Boolean result = false;
		result = fileGenerateService.createFolderByFolderPath(directory);
		if (result) {
			documentPath = directory + uploadFile.getFileName() + "." + uploadFile.getFileFormat();
			uploadFile.setDocumentPath(documentPath);
			result = fileGenerateService.createFileByFilePath(uploadFile);
		}
		return documentPath;
	}*/


    public List<ServiceDTO> findAllHosptalityServicearea() {
        List<com.clearinghouse.entity.Service> serviceareaList = serviceDAO.findAllHospitalityServicearea();
        List<ServiceDTO> serviceDTOList = serviceareaList.stream()
                .map(service -> (ServiceDTO) toDTO(service))
                .collect(Collectors.toList());

        return serviceDTOList;
    }


    public List<ServiceDTO> findHospitalityServiceareaByProviderId(List<Integer> providerIds) {
        List<com.clearinghouse.entity.Service> serviceareaByProviderList = serviceDAO.findHospitalityServiceareaByProviderId(providerIds);
        List<ServiceDTO> serviceDTOByProviderIdList = new ArrayList<>();
        for (com.clearinghouse.entity.Service service : serviceareaByProviderList) {
            ServiceDTO serviceDTO = (ServiceDTO) toDTO(service);
            serviceDTOByProviderIdList.add(serviceDTO);
        }

        return serviceDTOByProviderIdList;

    }


    public ServiceDTO hospitalityServiceAreaFileUpload(ServiceDTO serviceDTO) {
        com.clearinghouse.entity.Service serviceBO = (com.clearinghouse.entity.Service) toBO(serviceDTO);
        UploadFile uploadFile = serviceDTO.getUploadFile();
        String filePath = null;

		/*
		if (uploadFile != null) {
			if (uploadFile.getDocumentValue() != null && uploadFile.getFileFormat() != null && serviceBO != null
					&& uploadFile.getFileName() != null) {
				filePath = createFileForHospitality(serviceDTO.getUploadFile());
				serviceBO.setUploadFilePath(filePath);
				serviceBO.setHospitalityArea(true);
				serviceBO.setFileName(uploadFile.getFileName());
				uploadFile.setDocumentPath(filePath);
			}
		} else {
			throw new InvalidKMLFileException("Please Upload KML File");
		}*/
        List<String> serviceAreaList = KMLParser.getServiceAreaFromFile(filePath);
        // serviceBO.setServiceArea(serviceArea);
        serviceBO.setProvider(null);
        serviceBO.setProviderSelected(false);
        serviceBO.setHospitalityArea(true);
		/*
		if (serviceDTO.getProviderAreaList() != null) {
			if (serviceDTO.getProviderAreaList().size() > 0) {
				Set<HospitalityAreaProvider> providerList = serviceDTO.getProviderAreaList().parallelStream()
						.map(p -> new HospitalityAreaProvider(p.getProviderId())).collect(Collectors.toSet());
				serviceBO.setHospitalAreaProvider(providerList);
				serviceBO.setProviderSelected(true);
			}
		}*/

        serviceBO.setServiceName(uploadFile.getFileName());
        com.clearinghouse.entity.Service createObjTripTicket = serviceDAO.createServicearea(serviceBO);
        if (serviceAreaList != null) {
            if (serviceAreaList.size() > 0) {
				/* TODO this needs to be fixed
				Set<ServiceArea> serviceAreaBOList = serviceAreaList.parallelStream()
						.map(p -> new ServiceArea(p, createObjTripTicket)).collect(Collectors.toSet());
				serviceBO.setHospitalServiceAreas(serviceAreaBOList);
				for (ServiceArea serviceAreaBO : serviceAreaBOList) {
					serviceAreaDAO.createServicearea(serviceAreaBO);
				}*/
            }
        }
        ServiceDTO serviceDTOUpdated = (ServiceDTO) toDTO(createObjTripTicket);
        if (serviceBO.getHospitalAreaProvider() != null) {
            serviceDTOUpdated.setProviderAreaList(serviceBO.getHospitalAreaProvider().parallelStream()
                    .map(p -> new HospitalityAreaProviderDTO(p.getHospitalityProviderId(),
                            createObjTripTicket.getServiceId(), p.getProvider().getProviderId(),
                            p.getProviderServiceName()))
                    .collect(Collectors.toSet()));
        }
        if (serviceBO.getHospitalServiceAreas() != null) {
            serviceDTOUpdated.setServiceAreaList(createObjTripTicket
                    .getHospitalServiceAreas().parallelStream().map(p ->
                            new ServiceAreaDTO(p.getServiceAreaId(),
                                    geoJSONService.toGeoJSON(p.getServiceAreaGeometry()),
                                    p.getServiceName(),
                                    p.getService().getServiceId()))
                    .collect(Collectors.toSet()));


        }
        serviceDTOUpdated.setUploadFile(uploadFile);
        return serviceDTOUpdated;
    }


    public ServiceDTO hospitalityServiceAreaUpdate(ServiceDTO serviceDTO) {
        var serviceBO = (com.clearinghouse.entity.Service) toBO(serviceDTO);
        var updateObj = serviceDAO.updateServicearea(serviceBO);
        var serviceDTOUpdated = (ServiceDTO) toDTO(updateObj);
        return serviceDTOUpdated;
    }


    public List<MasterDTO> listOfHosptalitySAName() {
        List<MasterDTO> listOfHospService = serviceDAO.listOfHosptalitySAName();
        return listOfHospService;
    }


    public List<com.clearinghouse.entity.Service> findAllHosptalityServiceareabyIds(List<String> serviceIds) {
        List<Integer> newServiceIdList = serviceIds.stream().map(s -> Integer.parseInt(s)).collect(Collectors.toList());
        List<com.clearinghouse.entity.Service> serviceareaList = serviceDAO.findAllHospitalityServiceareaByIds(newServiceIdList);
        return serviceareaList;
    }


    public ServiceDTO createServiceAreaWithFileUpload(ServiceDTO serviceDTO) {
        com.clearinghouse.entity.Service serviceBO = (com.clearinghouse.entity.Service) toBO(serviceDTO);
        UploadFile uploadFile = serviceDTO.getUploadFile();
        List<String> serviceAreaList = new ArrayList<String>();
        String filePath = null;

        if (uploadFile != null && serviceDTO.getServiceArea() == null) {
            if (uploadFile.getDocumentValue() != null && uploadFile.getFileFormat() != null && serviceBO != null
                    && uploadFile.getFileName() != null) {
				/*
				filePath = createFileForHospitality(serviceDTO.getUploadFile());
				serviceBO.setUploadFilePath(filePath);
				serviceBO.setHospitalityArea(true);
				serviceBO.setFileName(uploadFile.getFileName());
				uploadFile.setDocumentPath(filePath);
				serviceAreaList = KMLParser.getServiceAreaFromFile(filePath);*/
            } else {
                throw new InvalidKMLFileException("Please Upload KML File");
            }
        } else if (uploadFile == null && serviceDTO.getServiceArea() != null) {
            serviceAreaList.add(serviceDTO.getServiceArea());
        } else {
            throw new InvalidKMLFileException("Please Upload KML File or Draw Service Area on map");
        }
        serviceBO.setProviderSelected(false);
        serviceBO.setHospitalityArea(false);
        com.clearinghouse.entity.Service createdService = serviceDAO.createServicearea(serviceBO);
        if (serviceAreaList != null) {
            if (serviceAreaList.size() > 0) {
				/* TODO this needs to be fixed
				Set<ServiceArea> serviceAreaBOList = serviceAreaList.parallelStream()
						.map(p -> new ServiceArea(p, createdService)).collect(Collectors.toSet());
				serviceBO.setHospitalServiceAreas(serviceAreaBOList);
				for (ServiceArea serviceAreaBO : serviceAreaBOList) {
					serviceAreaDAO.createServicearea(serviceAreaBO);
				}*/
            }
        }
        ServiceDTO serviceDTOUpdated = (ServiceDTO) toDTO(createdService);
        if (serviceBO.getHospitalServiceAreas() != null) {
            serviceDTOUpdated.setServiceAreaList(createdService
                    .getHospitalServiceAreas().parallelStream().map(p ->
                            new ServiceAreaDTO(p.getServiceAreaId(),
                                    geoJSONService.toGeoJSON(p.getServiceAreaGeometry()),
                                    p.getServiceName(),
                                    p.getService().getServiceId()))
                    .collect(Collectors.toSet()));
        }
        serviceDTOUpdated.setUploadFile(uploadFile);
        return serviceDTOUpdated;
    }


    public ServiceDTO updateServiceAreaWithFileUpload(ServiceDTO serviceDTO) {
        com.clearinghouse.entity.Service serviceBO = (com.clearinghouse.entity.Service) toBO(serviceDTO);
        UploadFile uploadFile = serviceDTO.getUploadFile();
        List<String> serviceAreaList = new ArrayList<String>();
        String filePath = null;
        if (uploadFile != null && serviceDTO.getServiceArea() == null) {
            if (uploadFile.getDocumentValue() != null && uploadFile.getFileFormat() != null && serviceBO != null
                    && uploadFile.getFileName() != null) {
				/*
				filePath = createFileForHospitality(serviceDTO.getUploadFile());
				serviceBO.setUploadFilePath(filePath);
				serviceBO.setHospitalityArea(true);
				serviceBO.setFileName(uploadFile.getFileName());
				uploadFile.setDocumentPath(filePath);
				serviceAreaList = KMLParser.getServiceAreaFromFile(filePath);*/
            } else {
                throw new InvalidKMLFileException("Please Upload KML File");
            }
        } else if (uploadFile == null && serviceDTO.getServiceArea() != null) {
            serviceAreaList.add(serviceDTO.getServiceArea());
        } else {
            throw new InvalidKMLFileException("Please Upload KML File or Draw Service Area on map");
        }
        serviceBO.setProviderSelected(false);
        serviceBO.setHospitalityArea(false);
        com.clearinghouse.entity.Service updatedService = serviceDAO.updateServicearea(serviceBO);
        if (serviceAreaList != null) {
			/* TODO this needs to be fixed
			if (serviceAreaList.size() > 0) {
				Set<ServiceArea> serviceAreaBOList = serviceAreaList.parallelStream()
						.map(p -> new ServiceArea(p, updatedService)).collect(Collectors.toSet());
				serviceBO.setHospitalServiceAreas(serviceAreaBOList);
				for (ServiceArea serviceAreaBO : serviceAreaBOList) {
					serviceAreaDAO.updateServicearea(serviceAreaBO);
				}
			}*/
        }
        ServiceDTO serviceDTOUpdated = (ServiceDTO) toDTO(updatedService);
        if (serviceBO.getHospitalServiceAreas() != null) {
            serviceDTOUpdated.setServiceAreaList(updatedService
                    .getHospitalServiceAreas().parallelStream().map(p ->
                            new ServiceAreaDTO(p.getServiceAreaId(),
                                    geoJSONService.toGeoJSON(p.getServiceAreaGeometry()),
                                    p.getServiceName(),
                                    p.getService().getServiceId()))
                    .collect(Collectors.toSet()));
        }
        serviceDTOUpdated.setUploadFile(uploadFile);
        return serviceDTOUpdated;
    }


    public CheckServiceareaDTO checkMedicalServicearea(CheckServiceareaDTO checkServiceareaDTOObj) {
        boolean checkServiceArea = false;
        int serviceId = 0;
        /*
         * fetch the medical service area of that claimant provider and check wheather
         * trip ticket latlongs are in that
         */

        TripTicket tripTicket = tripTicketDAO.findTripTicketByTripTicketId(checkServiceareaDTOObj.getTripTicketId());

        var medicalServiceAreaList = serviceDAO.findAllMedicalServiceAreasByProviderId(checkServiceareaDTOObj.getClaimantProviderId());

        if (medicalServiceAreaList.isEmpty()) {
            throw new ServiceAreaActiveCheckException("no active medical service area for the provider");
        }

        // check pickup and drop off belongs to servicearea
        var pickupLatitude = tripTicket.getPickupAddress().getLatitude();
        var pickupLongitude = tripTicket.getPickupAddress().getLongitude();
        var dropOffLatitude = tripTicket.getDropOffAddress().getLatitude();
        var dropOffLongitude = tripTicket.getDropOffAddress().getLongitude();

        if ((!medicalServiceAreaList.isEmpty()) && (pickupLatitude != 0)) {
            /**
             * check is service area is active
             */
            for (ServiceArea serviceArea : medicalServiceAreaList) {
                // String serviceArea = service.getServiceArea();

                /* if latlongs of PU and DoF ar nul then medical sa filter ll not be applied */
//                    check for PUA
                if (serviceDAO.checkAddressInServicearea(serviceArea, pickupLatitude, pickupLongitude)) {
//                        check for DOFFA
                    if (serviceDAO.checkAddressInServicearea(serviceArea, dropOffLatitude, dropOffLongitude)) {

                        checkServiceArea = true;
                        serviceId = serviceArea.getServiceAreaId();

                    }
                }
            }
        }

        boolean isClaimPresent = tripClaimDAO.isClaimPresent(checkServiceareaDTOObj.getTripTicketId(),
                checkServiceareaDTOObj.getClaimantProviderId());
        checkServiceareaDTOObj.setIsAlredyClaimed(isClaimPresent);
        checkServiceareaDTOObj.setIsEligibleForService(checkServiceArea);
        checkServiceareaDTOObj.setServiceId(serviceId);

        return checkServiceareaDTOObj;
    }


    public List<String> validateServiceAreaFileUpload(UploadFile uploadFile) {
        List<String> serviceAreaList = new ArrayList<String>();
        String filePath = null;
        if (uploadFile != null) {
            if (uploadFile.getDocumentValue() != null && uploadFile.getFileFormat() != null
                    && uploadFile.getFileName() != null) {
				/*
				filePath = createFileForHospitality(uploadFile);
				uploadFile.setDocumentPath(filePath);
				serviceAreaList = KMLParser.getServiceAreaFromFile(filePath);*/
            } else {
                throw new InvalidKMLFileException("Please Upload KML File");
            }
        } else {
            throw new InvalidKMLFileException("Please Upload KML File or Draw Service Area on map");
        }

        return serviceAreaList;
    }

}
