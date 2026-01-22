package com.clearinghouse.tds.generated.model;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlElementDecl;
import jakarta.xml.bind.annotation.XmlRegistry;

import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each
 * Java content interface and Java element interface
 * generated in the com.clearingHouse.tds.generated.model package.
 * <p>An ObjectFactory allows you to programatically
 * construct new instances of the Java representation
 * for XML content. The Java representation of XML
 * content can consist of schema derived interfaces
 * and classes representing the binding of schema
 * type definitions, element declarations and model
 * groups.  Factory methods for each of these are
 * provided in this class.
 *
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _TripRequest_QNAME = new QName("", "tripRequest");
    private final static QName _TripRequestResponse_QNAME = new QName("", "tripRequestResponse");
    private final static QName _ClientOrderConfirmation_QNAME = new QName("", "clientOrderConfirmation");
    private final static QName _CustomerInfo_QNAME = new QName("", "customerInfo");
    private final static QName _ProviderOrderConfirmation_QNAME = new QName("", "providerOrderConfirmation");
    private final static QName _VehicleConfirmation_QNAME = new QName("", "vehicleConfirmation");
    private final static QName _TripTask_QNAME = new QName("", "tripTask");
    private final static QName _TripTaskConfirmation_QNAME = new QName("", "tripTaskConfirmation");
    private final static QName _TripTaskCompletion_QNAME = new QName("", "tripTaskCompletion");
    private final static QName _TripScheduledConfirmation_QNAME = new QName("", "tripScheduledConfirmation");
    private final static QName _TripTaskStatus_QNAME = new QName("", "tripTaskStatus");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.clearingHouse.tds.generated.model
     *
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link AddressType }
     *
     */
    public AddressType createAddressType() {
        return new AddressType();
    }

    /**
     * Create an instance of {@link TripRequestType }
     *
     */
    public TripRequestType createTripRequestType() {
        return new TripRequestType();
    }

    /**
     * Create an instance of {@link TripRequestResponseType }
     *
     */
    public TripRequestResponseType createTripRequestResponseType() {
        return new TripRequestResponseType();
    }

    /**
     * Create an instance of {@link ClientOrderConfirmationType }
     *
     */
    public ClientOrderConfirmationType createClientOrderConfirmationType() {
        return new ClientOrderConfirmationType();
    }

    /**
     * Create an instance of {@link CustomerInfoType }
     *
     */
    public CustomerInfoType createCustomerInfoType() {
        return new CustomerInfoType();
    }

    /**
     * Create an instance of {@link ProviderOrderConfirmationType }
     *
     */
    public ProviderOrderConfirmationType createProviderOrderConfirmationType() {
        return new ProviderOrderConfirmationType();
    }

    /**
     * Create an instance of {@link VehicleConfirmationType }
     *
     */
    public VehicleConfirmationType createVehicleConfirmationType() {
        return new VehicleConfirmationType();
    }

    /**
     * Create an instance of {@link TripTaskType }
     *
     */
    public TripTaskType createTripTaskType() {
        return new TripTaskType();
    }

    /**
     * Create an instance of {@link TripTaskConfirmationType }
     *
     */
    public TripTaskConfirmationType createTripTaskConfirmationType() {
        return new TripTaskConfirmationType();
    }

    /**
     * Create an instance of {@link TripTaskCompletionType }
     *
     */
    public TripTaskCompletionType createTripTaskCompletionType() {
        return new TripTaskCompletionType();
    }

    /**
     * Create an instance of {@link TripScheduledConfirmationType }
     *
     */
    public TripScheduledConfirmationType createTripScheduledConfirmationType() {
        return new TripScheduledConfirmationType();
    }

    /**
     * Create an instance of {@link TripTaskStatusType }
     *
     */
    public TripTaskStatusType createTripTaskStatusType() {
        return new TripTaskStatusType();
    }

    /**
     * Create an instance of {@link GeographicLocation }
     *
     */
    public GeographicLocation createGeographicLocation() {
        return new GeographicLocation();
    }

    /**
     * Create an instance of {@link Time }
     *
     */
    public Time createTime() {
        return new Time();
    }

    /**
     * Create an instance of {@link IdType }
     *
     */
    public IdType createIdType() {
        return new IdType();
    }

    /**
     * Create an instance of {@link ManualDescriptionType }
     *
     */
    public ManualDescriptionType createManualDescriptionType() {
        return new ManualDescriptionType();
    }

    /**
     * Create an instance of {@link VehicleDistance }
     *
     */
    public VehicleDistance createVehicleDistance() {
        return new VehicleDistance();
    }


    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TripRequestType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link TripRequestType }{@code >}
     */
    @XmlElementDecl(namespace = "", name = "tripRequest")
    public JAXBElement<TripRequestType> createTripRequest(TripRequestType value) {
        return new JAXBElement<TripRequestType>(_TripRequest_QNAME, TripRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TripRequestResponseType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link TripRequestResponseType }{@code >}
     */
    @XmlElementDecl(namespace = "", name = "tripRequestResponse")
    public JAXBElement<TripRequestResponseType> createTripRequestResponse(TripRequestResponseType value) {
        return new JAXBElement<TripRequestResponseType>(_TripRequestResponse_QNAME, TripRequestResponseType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ClientOrderConfirmationType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link ClientOrderConfirmationType }{@code >}
     */
    @XmlElementDecl(namespace = "", name = "clientOrderConfirmation")
    public JAXBElement<ClientOrderConfirmationType> createClientOrderConfirmation(ClientOrderConfirmationType value) {
        return new JAXBElement<ClientOrderConfirmationType>(_ClientOrderConfirmation_QNAME, ClientOrderConfirmationType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CustomerInfoType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link CustomerInfoType }{@code >}
     */
    @XmlElementDecl(namespace = "", name = "customerInfo")
    public JAXBElement<CustomerInfoType> createCustomerInfo(CustomerInfoType value) {
        return new JAXBElement<CustomerInfoType>(_CustomerInfo_QNAME, CustomerInfoType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ProviderOrderConfirmationType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link ProviderOrderConfirmationType }{@code >}
     */
    @XmlElementDecl(namespace = "", name = "providerOrderConfirmation")
    public JAXBElement<ProviderOrderConfirmationType> createProviderOrderConfirmation(ProviderOrderConfirmationType value) {
        return new JAXBElement<ProviderOrderConfirmationType>(_ProviderOrderConfirmation_QNAME, ProviderOrderConfirmationType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link VehicleConfirmationType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link VehicleConfirmationType }{@code >}
     */
    @XmlElementDecl(namespace = "", name = "vehicleConfirmation")
    public JAXBElement<VehicleConfirmationType> createVehicleConfirmation(VehicleConfirmationType value) {
        return new JAXBElement<VehicleConfirmationType>(_VehicleConfirmation_QNAME, VehicleConfirmationType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TripTaskType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link TripTaskType }{@code >}
     */
    @XmlElementDecl(namespace = "", name = "tripTask")
    public JAXBElement<TripTaskType> createTripTask(TripTaskType value) {
        return new JAXBElement<TripTaskType>(_TripTask_QNAME, TripTaskType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TripTaskConfirmationType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link TripTaskConfirmationType }{@code >}
     */
    @XmlElementDecl(namespace = "", name = "tripTaskConfirmation")
    public JAXBElement<TripTaskConfirmationType> createTripTaskConfirmation(TripTaskConfirmationType value) {
        return new JAXBElement<TripTaskConfirmationType>(_TripTaskConfirmation_QNAME, TripTaskConfirmationType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TripTaskCompletionType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link TripTaskCompletionType }{@code >}
     */
    @XmlElementDecl(namespace = "", name = "tripTaskCompletion")
    public JAXBElement<TripTaskCompletionType> createTripTaskCompletion(TripTaskCompletionType value) {
        return new JAXBElement<TripTaskCompletionType>(_TripTaskCompletion_QNAME, TripTaskCompletionType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TripScheduledConfirmationType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link TripScheduledConfirmationType }{@code >}
     */
    @XmlElementDecl(namespace = "", name = "tripScheduledConfirmation")
    public JAXBElement<TripScheduledConfirmationType> createTripScheduledConfirmation(TripScheduledConfirmationType value) {
        return new JAXBElement<TripScheduledConfirmationType>(_TripScheduledConfirmation_QNAME, TripScheduledConfirmationType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TripTaskStatusType }{@code >}
     *
     * @param value Java instance representing xml element's value.
     * @return the new instance of {@link JAXBElement }{@code <}{@link TripTaskStatusType }{@code >}
     */
    @XmlElementDecl(namespace = "", name = "tripTaskStatus")
    public JAXBElement<TripTaskStatusType> createTripTaskStatus(TripTaskStatusType value) {
        return new JAXBElement<TripTaskStatusType>(_TripTaskStatus_QNAME, TripTaskStatusType.class, null, value);
    }

}
