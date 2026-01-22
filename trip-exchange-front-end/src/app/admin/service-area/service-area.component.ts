import { Component, OnInit, ViewChild, AfterViewInit } from '@angular/core';
import { Router } from '@angular/router';
import { Message } from 'primeng/api';
import { ConstantService } from '../../shared/service/constant-service';
import { Logger } from '../../shared/service/default-log.service';
import { ServiceAreaService } from './service-area.service';
import { TokenService } from '../../shared/service/token.service';
import { NotificationEmitterService } from '../../shared/service/notification-emitter.service';
import { LocalStorageService } from '../../shared/service/local-storage.service';
import { ListService } from '../../shared/service/list.service';
import { ServiceArea } from './service-area';
import { NgForm } from '@angular/forms';
import * as L from 'leaflet';
// Import leaflet-draw separately to ensure it's available when needed
import 'leaflet-draw';
// Import toGeoJSON for KML handling
// @ts-ignore: No type definitions for @mapbox/togeojson
import * as togeojson from '@mapbox/togeojson';
// Import shapefile for SHP handling
// @ts-ignore: No type definitions for shapefile
import * as shapefile from 'shapefile';
// Import proj4 for coordinate transformations
import proj4 from 'proj4';
declare module 'leaflet' {
  export const Draw: {
    Event: {
      CREATED: string;
      EDITED: string;
      DELETED: string;
    };
  };

  namespace Control {
    class Draw extends L.Control {
      constructor(options?: any);
    }
  }
}


@Component({
  selector: 'service-area',
  templateUrl: 'service-area.component.html',
  styleUrls: ['service-area.component.scss'],
  providers: [ServiceAreaService, ListService],
})
export class ServiceAreaComponent implements OnInit, AfterViewInit {
  @ViewChild('serviceForm', { static: false }) serviceForm: NgForm;

  public leafletMap: any;
  public featureGroup: any;
  public drawControl: any;
  public editableLayers: any;
  public polygonLayer: any;
  public pageName: string;
  public editServiceIndex: number;
  public editServiceObj: any;
  public service: any;
  public serviceList: any[] = []; // Initialize as empty array to ensure it's always an array
  public coordinates: any;
  public shapeCoords: any;
  public array: Array<any>;
  public initialCoordinates: any; // Now will store GeoJSON
  public saveButton: boolean = false;
  public clearAllPoints: boolean = false;
  public height: boolean = true;
  public role = this._localStorage.get('Role');
  public providerPartnersName = this._localStorage.get('adminChoiceProviderName');
  public id: string = 'displayGrid';
  public showMapInstructions: boolean = false; // Controls visibility of map instructions

  // Add properties for provider selection
  public providersList: any[] = [];
  public providerPartnersId: any = null;

  // Leaflet map options
  public leafletOptions = {
    layers: [
      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution:
          '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
        maxZoom: 19,
      }),
    ],
    zoom: 8,
    center: L.latLng(38.78995, -75.55375),
  };

  // Add new property for file upload
  public uploadedFileName: string = '';

  // Add properties for shapefile processing
  private shpFileData: ArrayBuffer | null = null;
  private prjFileContent: string | null = null;

  constructor(
    public _router: Router,
    public _serviceAreaService: ServiceAreaService,
    public _listService: ListService,
    public _localStorage: LocalStorageService,
    public _notificationService: NotificationEmitterService,
    public _tokenService: TokenService
  ) {
    this.coordinates = [];
    this.shapeCoords = [];
    this.array = [];
  }

  ngOnInit() {
    if (typeof this._tokenService.get() == 'undefined') {
      this._router.navigate(['/login']);
      return;
    }

    // If user is admin, load the providers list
    if (this.role === 'ROLE_ADMIN') {
      this.loadProvidersList();
    }

    // Safely access DOM elements with null checks
    const displayGridElement = document.getElementById('displayGrid');
    const displayElement = document.getElementById('display');

    if (displayGridElement) displayGridElement.style.display = 'block';
    if (displayElement) displayElement.style.display = 'none';

    this.service = [
      {
        serviceId: null,
        providerId: null,
        fundingSourceId: null,
        serviceName: ' ',
        dropOffRate: null,
        pickupRate: null,
        costPerMinute: null,
        costPerMile: null,
        wheelchairSpaceCost: null,
        serviceArea: '{}', // Empty GeoJSON object as string
        isActive: false,
        eligibility: ' ',
        serviceAreaType: ' ',
      },
    ];

    this.getServiceList();
    if (screen.width > 1024) {
      this.height = false;
    }

    // Allow some time for DOM elements to be available if they weren't at initial load
    setTimeout(() => {
      const displayGridElementRetry = document.getElementById('displayGrid');
      const displayElementRetry = document.getElementById('display');

      if (displayGridElementRetry) displayGridElementRetry.style.display = 'block';
      if (displayElementRetry) displayElementRetry.style.display = 'none';
    }, 100);
  }

  // Load providers list from service
  loadProvidersList() {
    this._listService.queryProvider().subscribe(
      response => {
        this.providersList = response;

        // Check if there's an adminChoiceProvider in localStorage
        const savedProviderId = this._localStorage.get('adminChoiceProvider');
        if (savedProviderId) {
          this.providerPartnersId = parseInt(savedProviderId);
          this.providerPartnersName = this._localStorage.get('adminChoiceProviderName');
          this.getServiceList();
        }
      },
      _error => {
        console.error('Error loading providers list:', _error);
        this._notificationService.error('Error Message', 'Failed to load providers list.');
      }
    );
  }

  // Handle provider change event
  onProviderChange(event: any) {
    const providerId = event;
    if (!providerId) {
      // Clear the service list if no provider is selected
      this.serviceList = [];
      this.providerPartnersName = null;
      this._localStorage.clear('adminChoiceProvider');
      this._localStorage.clear('adminChoiceProviderName');
      return;
    }

    // Find provider name
    let providerName = null;
    for (let i = 0; i < this.providersList.length; i++) {
      if (this.providersList[i].providerId == providerId) {
        providerName = this.providersList[i].providerName;
        break;
      }
    }

    // Save selected provider to localStorage
    this._localStorage.set('adminChoiceProvider', providerId);
    this._localStorage.set('adminChoiceProviderName', providerName);
    this.providerPartnersName = providerName;

    // Refresh service list for the selected provider
    this.getServiceList();
  }

  // Add service
  addServiceArea() {
    this.pageName = 'Add';
    this.service = new ServiceArea();

    // Change display mode
    this.id = 'display';

    // The map will be initialized in onMapReady callback
    this.coordinates = [];
  }

  // Get service list
  getServiceList() {
    if (this.role == 'ROLE_ADMIN') {
      this._serviceAreaService.get(this._localStorage.get('adminChoiceProvider')).subscribe(
        response => {
          // Ensure response is treated as an array
          this.serviceList = Array.isArray(response) ? response : [];
        },
        error => {
          // Handle error by setting serviceList to empty array
          console.error('Error fetching services:', error);
          this.serviceList = [];
        }
      );
    } else if (this.role == 'ROLE_PROVIDERADMIN')
      this._serviceAreaService.get(this._localStorage.get('providerId')).subscribe(
        response => {
          // Ensure response is treated as an array
          this.serviceList = Array.isArray(response) ? response : [];
        },
        error => {
          console.error('Error fetching services:', error);
          this._notificationService.error('Error Message', error);
          this.serviceList = [];
        }
      );
  }

  // Edit service
  editservice(serviceObj: any) {
    //console.log('EDIT Button clicked, service object received:', serviceObj);
    this.pageName = 'Edit';
    this.coordinates = [];
    this.editServiceIndex = this.findSelectedServiceIndex(serviceObj);
    this.service = this.serviceList[this.editServiceIndex];
    this.initialCoordinates = this.service.serviceArea;

    // Log the service area data for debugging
    //console.log('Service Area GeoJSON data:', this.service.serviceArea);

    // Extract coordinates from GeoJSON and populate this.coordinates
    try {
        const geoJson = typeof this.service.serviceArea === 'string'
            ? JSON.parse(this.service.serviceArea)
            : this.service.serviceArea;

        if (geoJson.geometry && geoJson.geometry.type === 'Polygon') {
            this.coordinates = geoJson.geometry.coordinates[0].map(([lng, lat]: [number, number]) => `${lat},${lng}`);
        } else {
            console.warn('Unsupported GeoJSON type:', geoJson.geometry?.type);
        }
    } catch (error) {
        console.error('Error parsing GeoJSON:', error);
    }

    // Change display mode
    this.id = 'display';

    // The map will be initialized in onMapReady callback
    this.editServiceObj = Object.assign({}, this.service);
    //console.log('Edit service complete, service object:', this.service);

    // Ensure the map is resized after editing
    setTimeout(() => {
      if (this.leafletMap) {
        this.leafletMap.invalidateSize();
        console.log('Map size invalidated after editing service.');
      }
    }, 100);
  }

  // Update coordinates from Leaflet layer
  updateCoordinatesFromLayer(layer: any) {
    try {
      const geoJson = layer.toGeoJSON();
      this.coordinates = geoJson.geometry.coordinates[0].map((coord: [number, number]) => `${coord[1]},${coord[0]}`);
      console.log('Updated coordinates geoJSON:', geoJson);
      console.log('Updated coordinates from layer:', this.coordinates);
    } catch (error) {
      console.error('Error updating coordinates from layer:', error);
    }
  }

  // Process GeoJSON data and render on the map
  processGeoJsonData(geoJson: any) {
   // console.log('Processing GeoJSON data:', geoJson);
    try {
      if (geoJson.type === 'FeatureCollection') {
        geoJson.features.forEach((feature: any, _index: number) => {
          //console.log(`Processing feature at index ${index}:`, feature);
          this.processFeature(feature);
        });
      } else if (geoJson.type === 'Feature') {
        //console.log('Processing single feature:', geoJson);
        this.processFeature(geoJson);
      } else if (geoJson.type === 'Polygon' || geoJson.type === 'MultiPolygon') {
        //console.log('Processing geometry directly:', geoJson.type);
        this.addGeometryToMap(geoJson);
      } else {
        console.error('Unsupported GeoJSON type:', geoJson.type);
      }
    } catch (error) {
      console.error('Error processing GeoJSON data:', error);
    }
  }

  processFeature(feature: any) {
    try {
      const geometry = feature.geometry;
      console.log('Feature geometry:', geometry);

      if (geometry.type === 'GeometryCollection') {
        geometry.geometries.forEach((subGeometry: any, _index: number) => {
          //console.log(`Processing sub-geometry at index ${index}:`, subGeometry);
          this.addGeometryToMap(subGeometry);
        });
      } else {
        this.addGeometryToMap(geometry);
      }
    } catch (error) {
      console.error('Error processing feature:', error);
    }
  }

  addGeometryToMap(geometry: any) {
    console.log('Adding geometry to map:', geometry);
    try {
      if (geometry.type === 'Polygon') {
        // Transform coordinates to [latitude, longitude] format
        const transformedCoordinates = this.processPolygonCoordinates(geometry.coordinates);

        const layer = L.polygon(transformedCoordinates, {
          color: '#FF0000',
          fillColor: '#FF0000',
          fillOpacity: 0.35,
          weight: 2,
        });

        this.editableLayers.addLayer(layer);
        console.log('Polygon added to map:', layer);

        // Extract coordinates and populate this.coordinates
        this.coordinates = geometry.coordinates[0].map(([lng, lat]: [number, number]) => `${lat},${lng}`);

        // Adjust map view to fit the bounds of the added layer
        try {
          const bounds = layer.getBounds();
          if (bounds && bounds.isValid()) {
            this.leafletMap.fitBounds(bounds);
            console.log('Map view adjusted to fit bounds:', bounds);
          } else {
            console.warn('Invalid bounds for layer');
          }
        } catch (boundsError) {
          console.warn('Could not get valid bounds from layer:', boundsError);
        }
      } else if (geometry.type === 'MultiPolygon') {
        //console.log('Processing MultiPolygon geometry');
        // Handle each polygon in the MultiPolygon
        const layers = geometry.coordinates.map((polygonCoords: [number, number][][]) => {
          // For MultiPolygon, each coordinate set is already a polygon
          const transformedCoordinates = this.processPolygonCoordinates(polygonCoords);
          return L.polygon(transformedCoordinates, {
            color: '#FF0000',
            fillColor: '#FF0000',
            fillOpacity: 0.35,
            weight: 2,
          });
        });

        // Add all polygon layers to the feature group
        const featureGroup = L.featureGroup(layers);
        this.editableLayers.addLayer(featureGroup);
        console.log('MultiPolygon added to map:', featureGroup);

        // Store coordinates from all polygons
        this.coordinates = geometry.coordinates.flatMap((polygon: [number, number][][]) =>
          polygon[0].map((coord: [number, number]) => `${coord[1]},${coord[0]}`)
        );

        // Fit bounds to show all polygons
        try {
          const bounds = featureGroup.getBounds();
          if (bounds && bounds.isValid()) {
            this.leafletMap.fitBounds(bounds);
            console.log('Map view adjusted to fit MultiPolygon bounds:', bounds);
          } else {
            console.warn('Invalid bounds for MultiPolygon');
          }
        } catch (boundsError) {
          console.warn('Could not get valid bounds from MultiPolygon:', boundsError);
        }
      } else {
        console.warn('Unsupported geometry type:', geometry.type);
      }
    } catch (error) {
      console.error('Error adding geometry to map:', error);
    }
  }

  private convertCoordinatesToLatLng(coords: [number, number][]): L.LatLng[] {
    return coords.map((coord: [number, number]) => L.latLng(coord[1], coord[0]));
  }

  private processPolygonCoordinates(coords: [number, number][][]): L.LatLng[][] {
    return coords.map((ring: [number, number][]) =>
      ring.map((coord: [number, number]) => L.latLng(coord[1], coord[0]))
    );
  }

  // Called when Leaflet map is ready
  onMapReady(map: L.Map) {
    console.log('Leaflet map is ready');
    this.leafletMap = map;

    // Create a feature group for the editable layers
    this.editableLayers = new L.FeatureGroup();
    map.addLayer(this.editableLayers);

    // Make sure the Draw object exists on L
    if (!(L as any).Draw) {
      console.error('Leaflet.Draw is not available. Please check imports.');
      return;
    }

    // Configure the draw control with proper initialization
    const drawOptions: any = {
      position: 'topright',
      draw: {
        polyline: false,
        circle: false,
        rectangle: false,
        marker: false,
        circlemarker: false,
        polygon: {
          allowIntersection: false,
          drawError: {
            color: '#e1e100',
            message: '<strong>Error:</strong> Shape edges cannot cross!',
          },
          shapeOptions: {
            color: '#FF0000',
            fillColor: '#FF0000',
            fillOpacity: 0.35,
            weight: 2,
          },
        },
      },
      edit: {
        featureGroup: this.editableLayers,
        remove: true,
      },
    };

    // Correctly instantiate the Draw control
    this.drawControl = new (L.Control as any).Draw(drawOptions);
    map.addControl(this.drawControl);

    // Event handling for drawing using any type assertion to bypass TypeScript errors
    map.on((L as any).Draw.Event.CREATED, (e: any) => {
      const layer = e.layer;

      // Remove existing polygon if any
      this.editableLayers.clearLayers();

      // Add the new layer to the feature group
      this.editableLayers.addLayer(layer);

      // Store the coordinates
      this.updateCoordinatesFromLayer(layer);

      // Enable the save button
      this.saveButton = true;
      this.clearAllPoints = true;
    });

    // Event handling for editing
    map.on((L as any).Draw.Event.EDITED, (e: any) => {
      const layers = e.layers;
      layers.eachLayer((layer: any) => {
        this.updateCoordinatesFromLayer(layer);
      });

      // Enable the save button
      this.saveButton = true;
      this.clearAllPoints = true;
    });

    // Event handling for deleting
    map.on((L as any).Draw.Event.DELETED, () => {
      this.coordinates = [];
      this.saveButton = true;
      this.clearAllPoints = true;
    });

    // If editing, create polygon from existing coordinates
    if (this.pageName === 'Edit' && this.initialCoordinates && this.initialCoordinates.trim()) {
      try {
        //console.log('Processing service area coordinates:', this.initialCoordinates);

        // Parse the GeoJSON string to object
        let geoJson;
        try {
          geoJson =
            typeof this.initialCoordinates === 'string'
              ? JSON.parse(this.initialCoordinates)
              : this.initialCoordinates;

          console.log('Parsed GeoJSON:', geoJson);
        } catch (parseError) {
          console.error('Error parsing GeoJSON:', parseError);
          geoJson = null;
        }

        if (geoJson) {
          this.processGeoJsonData(geoJson);
        }
      } catch (error) {
        console.error('Error processing initial coordinates:', error);
      }
    } else if (this.pageName === 'Add') {
      // For add, just center the map
      this.leafletMap.setView([38.78995, -75.55375], 8);
    }

    // Add listener for zoomend to ensure map fits container
    map.on('zoomend', () => {
      map.invalidateSize();
      console.log('Map size invalidated after zoomend.');
    });
  }

  ngAfterViewInit() {
    if (this.leafletMap) {
      this.leafletMap.invalidateSize();
      console.log('Leaflet map size invalidated to fit container.');
    }
  }

  // to find the index of selected service
  findSelectedServiceIndex(serviceData: any): number {
    return this.serviceList.indexOf(serviceData);
  }

  // save add/edit service
  addEditService(serviceObj: any) {
    console.log('UPDATE/SAVE Button clicked!');
    console.log('serviceObj passed to addEditService:', serviceObj);
    console.log('Current pageName:', this.pageName);
    console.log('Current service form valid?', this.serviceForm?.form?.valid);

    console.log('Service object type:', typeof serviceObj);

    // Check if serviceObj is an array and handle it appropriately
    if (Array.isArray(serviceObj)) {
      console.log('serviceObj is an array! Taking first element');
      serviceObj = serviceObj[0];
    }

    // Prepare the GeoJSON data for the service area
    let serviceAreaGeoJson: any;

    // If we have coordinates from drawing or editing
    if (this.editableLayers && this.editableLayers.getLayers().length > 0) {
      // Get all layers as GeoJSON
      const layers = this.editableLayers.getLayers();

      if (layers.length === 1) {
        // Single polygon case
        serviceAreaGeoJson = layers[0].toGeoJSON();
      } else {
        // Multiple polygons case - create a MultiPolygon
        const polygons = layers.map((layer: L.Layer) => {
          const geoJson = (layer as L.Polygon).toGeoJSON();
          return geoJson.geometry.coordinates[0] as [number, number][];
        });
        serviceAreaGeoJson = {
          type: 'Feature',
          geometry: {
            type: 'MultiPolygon',
            coordinates: polygons.map((coords: [number, number][]) => [coords]),
          },
          properties: {},
        };
      }

      console.log('Created GeoJSON object:', serviceAreaGeoJson);
    } else if (this.initialCoordinates) {
      // Use existing GeoJSON if no new drawing was made
      try {
        serviceAreaGeoJson =
          typeof this.initialCoordinates === 'string'
            ? JSON.parse(this.initialCoordinates)
            : this.initialCoordinates;

        console.log('Using existing GeoJSON:', serviceAreaGeoJson);
      } catch (error) {
        console.error('Error parsing existing GeoJSON:', error);
        serviceAreaGeoJson = {
          type: 'Feature',
          geometry: { type: 'Polygon', coordinates: [[]] },
          properties: {},
        };
      }
    } else {
      // Default empty GeoJSON if nothing available
      serviceAreaGeoJson = {
        type: 'Feature',
        geometry: {
          type: 'Polygon',
          coordinates: [[]],
        },
        properties: {},
      };
    }

    if (this.pageName == 'Add') {
      console.log('Executing ADD operation');
      serviceObj.serviceId = 0;
      if (this.role == 'ROLE_ADMIN') {
        serviceObj.providerId = parseInt(this._localStorage.get('adminChoiceProvider'));
        console.log('Admin role detected, using providerId:', serviceObj.providerId);
      } else {
        serviceObj.providerId = parseInt(this._localStorage.get('providerId'));
        console.log('Provider admin role detected, using providerId:', serviceObj.providerId);
      }
      serviceObj.fundingSourceId = null;
      serviceObj.serviceArea = JSON.stringify(serviceAreaGeoJson); // Store GeoJSON as string
      serviceObj.isActive = true;
      serviceObj.serviceAreaType = ' ';
      this.service = serviceObj;
      console.log('Saving new service with data:', this.service);

      this._serviceAreaService.save(this.service).subscribe(
        response => {
          console.log('Service saved successfully, response:', response);
          if (typeof this.serviceList == 'undefined') {
            this.serviceList = [];
            this.array.push(response);
            this.serviceList = this.array;
          } else {
            this.serviceList.unshift(response);
          }
          this._notificationService.success('Success Message', 'Service saved successfully.');
          this.id = 'displayGrid';
        },
        error => {
          console.error('Error saving service:', error);
          this._notificationService.error('Error Message', error);
        }
      );
    } else {
      console.log('Executing UPDATE operation');
      console.log('Original service object:', JSON.stringify(this.service));

      // Check if this.service is an array and handle it appropriately
      let serviceToUpdate;
      if (Array.isArray(this.service)) {
        console.log('this.service is an array! Using first element');
        serviceToUpdate = Object.assign({}, this.service[0]);
      } else {
        serviceToUpdate = Object.assign({}, this.service);
      }

      // Update the serviceArea property with GeoJSON string
      serviceToUpdate.serviceArea = JSON.stringify(serviceAreaGeoJson);

      console.log('Updated service object being sent to API:', JSON.stringify(serviceToUpdate));
      console.log('Service ID for update:', serviceToUpdate.serviceId);

      try {
        this._serviceAreaService.update(serviceToUpdate).subscribe(
          response => {
            console.log('Service updated successfully, response:', response);
            this._notificationService.success('Success Message', 'Service updated successfully.');
            this.id = 'displayGrid';
            this.serviceList[this.editServiceIndex] = response;
          },
          error => {
            console.error('Error updating service:', error);
            // Detailed error logging
            if (error.error) {
              console.error('Error details:', error.error);
            }
            if (error.message) {
              console.error('Error message:', error.message);
            }
            if (error.status) {
              console.error('Error status:', error.status);
            }
            this._notificationService.error('Error Message', error);
          }
        );
      } catch (catchError) {
        console.error('Exception caught during service update:', catchError);
        this._notificationService.error(
          'Error Message',
          'An error occurred while updating the service.'
        );
      }
    }
  }

  // update the status activate/deactivate
  updateStatus(serviceObj: any) {

      if ( serviceObj.isActive === undefined || serviceObj.isActive === null) {
        serviceObj.isActive = true;
      } else {
        serviceObj.isActive = !serviceObj.isActive;
      }
      console.log('Updating status for service:', serviceObj);
      this._serviceAreaService.update(serviceObj).subscribe(
        response => {
          console.log('Service saved successfully, response:', response);
          const index = this.serviceList.findIndex(service => service.serviceId === response.serviceId);
          if (index !== -1) {
            this.serviceList[index] = response; // Replace the existing service with the updated one
          } else {
            this.serviceList.unshift(response); // Add the updated service if it doesn't exist
          }
          this._notificationService.success('Success Message', 'Service saved successfully.');
          this.id = 'displayGrid';
        },
        error => {
          console.error('Error saving service:', error);
          this._notificationService.error('Error Message', error);
        }
      );



  }

  // Download GeoJSON data
  downloadGeoJSON(service: any) {
    let geoJsonData: any;

    // If we're in edit mode and have a drawn polygon
    if (this.editableLayers && this.editableLayers.getLayers().length > 0) {
      const layer = this.editableLayers.getLayers()[0];
      geoJsonData = layer.toGeoJSON();
    }
    // If we're in list view or have existing coordinates from the service
    else if (service.serviceArea) {
      try {
        geoJsonData =
          typeof service.serviceArea === 'string'
            ? JSON.parse(service.serviceArea)
            : service.serviceArea;
      } catch (error) {
        console.error('Error parsing GeoJSON:', error);
        this._notificationService.error(
          'Error',
          'Unable to generate GeoJSON file from the current service area.'
        );
        return;
      }
    }
    // If we have coordinates in initialCoordinates (backup check)
    else if (this.initialCoordinates) {
      try {
        geoJsonData =
          typeof this.initialCoordinates === 'string'
            ? JSON.parse(this.initialCoordinates)
            : this.initialCoordinates;
      } catch (error) {
        console.error('Error parsing GeoJSON:', error);
        this._notificationService.error(
          'Error',
          'Unable to generate GeoJSON file from the current service area.'
        );
        return;
      }
    } else {
      this._notificationService.error('Error', 'No service area polygon available to download.');
      return;
    }

    // Create the download
    try {
      const blob = new Blob([JSON.stringify(geoJsonData, null, 2)], {
        type: 'application/geo+json',
      });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `${service.serviceName || 'service-area'}.geojson`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
    } catch (error) {
      console.error('Error creating download:', error);
      this._notificationService.error('Error', 'Unable to create download file.');
    }
  }

  // revert from edit page
  cancelEdit() {
    this.id = 'displayGrid';
    if (this.editableLayers) {
      this.editableLayers.clearLayers();
    }
  }

  // clear extra points marked on map
  clearAllPoint() {
    if (this.editableLayers) {
      // Clear all layers from the editable group
      this.editableLayers.clearLayers();

      // Reset flags
      this.saveButton = false;
      this.clearAllPoints = false;

      // For Edit mode, restore the original shape if available
      if (this.pageName === 'Edit' && this.initialCoordinates && this.initialCoordinates.trim()) {
        try {
          // Parse the GeoJSON string to object
          const geoJson =
            typeof this.initialCoordinates === 'string'
              ? JSON.parse(this.initialCoordinates)
              : this.initialCoordinates;

          console.log('Parsed GeoJSON for reset:', geoJson);

          // Check if valid GeoJSON with polygon
          if (
            geoJson &&
            geoJson.type === 'Feature' &&
            geoJson.geometry &&
            geoJson.geometry.type === 'Polygon' &&
            geoJson.geometry.coordinates &&
            geoJson.geometry.coordinates.length > 0
          ) {
            // Extract coordinates from GeoJSON
            const coords = geoJson.geometry.coordinates[0]; // First polygon ring

            // Convert GeoJSON coordinates [lng, lat] to Leaflet's [lat, lng] format
            const leafletLatLngs = coords.map((coord: [number, number]) => [coord[1], coord[0]]);

            if (leafletLatLngs.length > 0) {
              // Create polygon from existing coordinates
              const polygon = L.polygon(leafletLatLngs, {
                color: '#FF0000',
                fillColor: '#FF0000',
                fillOpacity: 0.35,
                weight: 2,
              });

              // Add to editable layers
              this.editableLayers.addLayer(polygon);
              console.log('Polygon restored to map from GeoJSON');

              // Fit bounds to show the polygon
              this.leafletMap.fitBounds(polygon.getBounds());
            }
          }
        } catch (error) {
          console.error('Error restoring original shape:', error);
        }
      }
    }
  }

  // Method for global filtering in PrimeNG table
  filterGlobal(event: any) {
    const _value = event.target.value;
    // Ensure serviceList is always an array even if API returns null/undefined
    if (!this.serviceList) {
      this.serviceList = [];
    }
    // Table will handle the filtering using the value from the input
  }

  // revert to provider selection page for site admin
  back() {
    this._router.navigate(['/admin/adminProviderPartners']);
  }

  // Handle file selection
  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (!file) {
      return;
    }

    this.uploadedFileName = file.name;
    console.log('File selected:', file.name);

    // Check file type
    const fileExtension = file.name.split('.').pop().toLowerCase();

    if (['kml', 'geojson', 'json', 'shp'].indexOf(fileExtension) === -1) {
      this._notificationService.error(
        'Error Message',
        'Unsupported file format. Please upload KML, GeoJSON, or SHP file.'
      );
      return;
    }

    const reader = new FileReader();
    reader.onload = (e: any) => {
      try {
        if (fileExtension === 'kml') {
          this.processKmlFile(e.target.result);
        } else if (fileExtension === 'shp') {
          // Store the shapefile data and wait for .prj file
          this.shpFileData = e.target.result;
          this._notificationService.info(
            'Shapefile Uploaded',
            'Please upload the corresponding .prj file to complete the import.'
          );
        } else {
          this.processGeoJsonFile(e.target.result);
        }
      } catch (error) {
        console.error('Error processing file:', error);
        this._notificationService.error(
          'Error Message',
          'Error processing the file. Please make sure it is a valid KML, GeoJSON, or SHP file.'
        );
      }
    };

    reader.onerror = e => {
      console.error('Error reading file:', e);
      this._notificationService.error('Error Message', 'Error reading the file.');
    };

    // Read the file
    if (fileExtension === 'kml') {
      reader.readAsText(file); // Read as text for XML parsing
    } else if (fileExtension === 'shp') {
      reader.readAsArrayBuffer(file); // Read as ArrayBuffer for binary SHP parsing
    } else {
      reader.readAsText(file); // Read as text for JSON parsing
    }
  }

  // Process KML file
  private processKmlFile(kmlContent: string): void {
    //console.log('Processing KML file...');
    try {
      // Fix common namespace issues in KML before parsing
      let fixedKmlContent = kmlContent;

      // Add missing xsi namespace if schemaLocation is present but xsi is not declared
      if (fixedKmlContent.includes('xsi:schemaLocation') && !fixedKmlContent.includes('xmlns:xsi')) {
        fixedKmlContent = fixedKmlContent.replace(
          /<kml\s+/,
          '<kml xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" '
        );
        console.log('Added missing xsi namespace to KML');
      }

      // Remove redundant xmlns:kml declaration if present
      if (fixedKmlContent.includes('xmlns:kml="http://www.opengis.net/kml/2.2"')) {
        fixedKmlContent = fixedKmlContent.replace(/\s+xmlns:kml="http:\/\/www\.opengis\.net\/kml\/2\.2"/, '');
        console.log('Removed redundant xmlns:kml declaration');
      }

      // Parse KML XML
      const parser = new DOMParser();
      const kmlDoc = parser.parseFromString(fixedKmlContent, 'text/xml');

      // Check for XML parsing errors
      const parserError = kmlDoc.querySelector('parsererror');
      if (parserError) {
        console.error('XML parsing error:', parserError.textContent);
        this._notificationService.error('Error Message', 'Invalid KML file: XML parsing failed');
        return;
      }

      // Convert KML to GeoJSON using togeojson library
      const geoJson = togeojson.kml(kmlDoc);
      console.log('KML converted to GeoJSON:', geoJson);

      // Validate that we have features
      if (!geoJson || !geoJson.features || geoJson.features.length === 0) {
        console.warn('KML conversion resulted in empty GeoJSON');
        console.warn('KML Document structure:', kmlDoc.documentElement);

        // Check if there are any Placemarks in the KML
        const placemarks = kmlDoc.getElementsByTagName('Placemark');
        console.warn('Number of Placemarks found:', placemarks.length);

        if (placemarks.length > 0) {
          this._notificationService.error(
            'Error Message',
            'KML file could not be converted. This may be due to namespace or structure issues.'
          );
        } else {
          this._notificationService.error('Error Message', 'KML file contains no valid geometries');
        }
        return;
      }

      // Clear existing layers
      if (this.editableLayers) {
        this.editableLayers.clearLayers();
      }

      // Process the GeoJSON data
      this.renderGeoJsonOnMap(geoJson);
    } catch (error) {
      console.error('Error processing KML:', error);
      this._notificationService.error(
        'Error Message',
        'Error processing KML file. Please ensure it is a valid KML format.'
      );
    }
  }

  // Process GeoJSON file
  private processGeoJsonFile(jsonContent: string): void {
    //console.log('Processing GeoJSON file...');
    try {
      const geoJson = JSON.parse(jsonContent);
      console.log('Parsed GeoJSON:', geoJson);

      // Clear existing layers
      if (this.editableLayers) {
        this.editableLayers.clearLayers();
      }

      // Process the GeoJSON data
      this.renderGeoJsonOnMap(geoJson);
    } catch (error) {
      console.error('Error processing GeoJSON:', error);
      this._notificationService.error(
        'Error Message',
        'Error processing GeoJSON file. Please ensure it is valid JSON format.'
      );
    }
  }

  // Process Shapefile
  private processShpFile(arrayBuffer: ArrayBuffer, prjContent: string): void {
    console.log('Processing Shapefile...');
    try {
      // Use shapefile library to read the .shp file
      shapefile.read(arrayBuffer).then((geoJson: any) => {
        console.log('Shapefile converted to GeoJSON:', geoJson);

        // Validate that we have features
        if (!geoJson || !geoJson.features || geoJson.features.length === 0) {
          console.warn('Shapefile conversion resulted in empty GeoJSON');
          this._notificationService.error(
            'Error Message',
            'Shapefile contains no valid geometries'
          );
          return;
        }

        // Check if coordinates need reprojection
        // If bbox values are > 180 or < -180, they're likely in a projected coordinate system
        const needsReprojection = geoJson.bbox &&
          (Math.abs(geoJson.bbox[0]) > 180 || Math.abs(geoJson.bbox[1]) > 90 ||
           Math.abs(geoJson.bbox[2]) > 180 || Math.abs(geoJson.bbox[3]) > 90);

        if (needsReprojection) {
          console.log('Shapefile is in a projected coordinate system, reprojecting...');

          try {
            // Define source projection from .prj file content
            const sourceProj = prjContent.trim();
            // Define target projection (WGS84)
            const targetProj = 'EPSG:4326';

            // Reproject all coordinates in the GeoJSON
            const reprojectedGeoJson = this.reprojectGeoJson(geoJson, sourceProj, targetProj);

            console.log('Reprojected GeoJSON:', reprojectedGeoJson);

            // Clear existing layers
            if (this.editableLayers) {
              this.editableLayers.clearLayers();
            }

            // Process the reprojected GeoJSON data
            this.renderGeoJsonOnMap(reprojectedGeoJson);

            this._notificationService.success(
              'Success',
              `Shapefile loaded and reprojected successfully with ${reprojectedGeoJson.features.length} feature(s)`
            );
          } catch (error) {
            console.error('Error reprojecting coordinates:', error);
            this._notificationService.error(
              'Error Message',
              'Error reprojecting coordinates. Please ensure the .prj file is valid.'
            );
          }
        } else {
          // Coordinates are already in WGS84 (lat/lng)
          console.log('Shapefile coordinates appear to be in WGS84');

          // Clear existing layers
          if (this.editableLayers) {
            this.editableLayers.clearLayers();
          }

          // Process the GeoJSON data
          this.renderGeoJsonOnMap(geoJson);

          this._notificationService.success(
            'Success',
            `Shapefile loaded successfully with ${geoJson.features.length} feature(s)`
          );
        }
      }).catch((error: any) => {
        console.error('Error reading Shapefile:', error);
        this._notificationService.error(
          'Error Message',
          'Error reading Shapefile. Please ensure it is a valid .shp file.'
        );
      });
    } catch (error) {
      console.error('Error processing Shapefile:', error);
      this._notificationService.error(
        'Error Message',
        'Error processing Shapefile. Please ensure it is a valid .shp format.'
      );
    }
  }

  // Handle PRJ file selection
  onPrjFileSelected(event: any): void {
    const file = event.target.files[0];
    if (!file) {
      return;
    }

    console.log('PRJ file selected:', file.name);

    // Check file type
    const fileExtension = file.name.split('.').pop().toLowerCase();
    if (fileExtension !== 'prj') {
      this._notificationService.error(
        'Error Message',
        'Please select a .prj file.'
      );
      return;
    }

    // Check if shapefile was uploaded first
    if (!this.shpFileData) {
      this._notificationService.error(
        'Error Message',
        'Please upload a .shp file first.'
      );
      return;
    }

    const reader = new FileReader();
    reader.onload = (e: any) => {
      try {
        this.prjFileContent = e.target.result;
        console.log('PRJ file content:', this.prjFileContent);

        // Now process the shapefile with the projection info
        this.processShpFile(this.shpFileData!, this.prjFileContent!);

        // Clear the stored data
        this.shpFileData = null;
        this.prjFileContent = null;
      } catch (error) {
        console.error('Error reading PRJ file:', error);
        this._notificationService.error(
          'Error Message',
          'Error reading the .prj file.'
        );
      }
    };

    reader.onerror = e => {
      console.error('Error reading PRJ file:', e);
      this._notificationService.error('Error Message', 'Error reading the .prj file.');
    };

    reader.readAsText(file);
  }

  // Reproject GeoJSON coordinates from source to target projection
  private reprojectGeoJson(geoJson: any, sourceProj: string, targetProj: string): any {
    const reprojectCoordinate = (coord: number[]): number[] => {
      try {
        return proj4(sourceProj, targetProj, coord);
      } catch (error) {
        console.error('Error reprojecting coordinate:', coord, error);
        return coord; // Return original if reprojection fails
      }
    };

    const reprojectCoordinates = (coords: any, depth: number): any => {
      if (depth === 0) {
        // Base case: single coordinate [x, y]
        return reprojectCoordinate(coords);
      } else {
        // Recursive case: array of coordinates
        return coords.map((c: any) => reprojectCoordinates(c, depth - 1));
      }
    };

    // Create a deep copy of the GeoJSON
    const reprojected = JSON.parse(JSON.stringify(geoJson));

    // Reproject each feature
    reprojected.features.forEach((feature: any) => {
      if (feature.geometry && feature.geometry.coordinates) {
        const geomType = feature.geometry.type;

        // Determine the depth of coordinate nesting based on geometry type
        let depth = 0;
        if (geomType === 'Point') {
          depth = 0; // [x, y]
        } else if (geomType === 'LineString' || geomType === 'MultiPoint') {
          depth = 1; // [[x, y], [x, y]]
        } else if (geomType === 'Polygon' || geomType === 'MultiLineString') {
          depth = 2; // [[[x, y], [x, y]]]
        } else if (geomType === 'MultiPolygon') {
          depth = 3; // [[[[x, y], [x, y]]]]
        }

        feature.geometry.coordinates = reprojectCoordinates(feature.geometry.coordinates, depth);
      }
    });

    // Recalculate bbox if it exists
    if (reprojected.bbox) {
      delete reprojected.bbox; // Remove old bbox, let Leaflet calculate new one
    }

    return reprojected;
  }

  // Render GeoJSON on map
  renderGeoJsonOnMap(geoJson: any) {
    console.log('Rendering GeoJSON on map:', geoJson);
    try {
      this.processGeoJsonData(geoJson);
    } catch (error) {
      console.error('Error rendering GeoJSON on map:', error);
    }
  }

  // Toggle map instructions visibility
  toggleMapInstructions() {
    this.showMapInstructions = !this.showMapInstructions;
  }

  // Handle file upload
  handleFileUpload(event: any) {
    const file = event.target.files[0];
    if (!file) {
      console.error('No file selected');
      return;
    }

    console.log('File selected:', file.name);

    const reader = new FileReader();
    reader.onload = (e: any) => {
      try {
        const geoJson = JSON.parse(e.target.result);
        console.log('Parsed GeoJSON:', geoJson);
        this.renderGeoJsonOnMap(geoJson);
      } catch (error) {
        console.error('Error parsing GeoJSON file:', error);
      }
    };

    reader.readAsText(file);
  }
}
