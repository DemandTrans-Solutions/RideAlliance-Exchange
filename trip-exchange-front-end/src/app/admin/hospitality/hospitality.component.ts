import { Component, OnInit, OnDestroy, ViewChild } from '@angular/core';
import { NgForm } from '@angular/forms';
import { Router } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

// Import leaflet and leaflet-draw
import * as L from 'leaflet';
import 'leaflet-draw';
// Import toGeoJSON for KML handling
import * as togeojson from '@mapbox/togeojson';

import { NotificationEmitterService } from '../../shared/service/notification-emitter.service';
import { TokenService } from '../../shared/service/token.service';
import { LocalStorageService } from '../../shared/service/local-storage.service';
import { HospitalityService } from './hospitality.service';
import { Hospitality, Provider, GeoJsonFeature } from './hospitality.model';

@Component({
  selector: 'app-hospitality',
  templateUrl: './hospitality.component.html',
  styleUrls: ['./hospitality.component.scss'],
})
export class HospitalityComponent implements OnInit, OnDestroy {
  @ViewChild('hospitalityForm', { static: false }) hospitalityForm!: NgForm;

  private destroy$ = new Subject<void>();

  // Data properties
  serviceList: Hospitality[] = [];
  filteredServiceList: Hospitality[] = [];

  // Leaflet map properties
  leafletMap: L.Map | null = null;
  editableLayers: L.FeatureGroup | null = null;
  drawControl: any;

  // Leaflet map options
  leafletOptions = {
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

  // Provider properties
  providers: Provider[] = [];
  providerList: number[] = [];

  // UI state properties
  isEdit = false;
  fileUploaded = false;
  showMap = false;
  pageName = '';
  documentValue: string | null = null;
  fileName: string = '';
  fileType: string = '';
  initialCoordinates: string = '';
  id = 'displayGrid';

  role: string;
  providerId: string;
  currentHospitality: Hospitality = {
    serviceName: '',
    isActive: true,
  };
  globalFilter: string = '';

  constructor(
    private hospitalityService: HospitalityService,
    private _notificationService: NotificationEmitterService,
    private router: Router,
    private _tokenService: TokenService,
    private _localStorage: LocalStorageService
  ) {
    this.role = this._localStorage.get('Role');
    this.providerId = this._localStorage.get('providerId');
  }

  ngOnInit(): void {
    if (typeof this._tokenService.get() == 'undefined') {
      this.router.navigate(['/login']);
      return;
    }

    this.loadHospitalityData();

    if (this.role === 'ROLE_ADMIN') {
      this.loadProviders();
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadHospitalityData(): void {
    this.hospitalityService
      .getAll()
      .pipe(takeUntil(this.destroy$))
      .subscribe(
        data => {
          console.log('Hospitality data loaded:', data);
          this.serviceList = data;
          this.filteredServiceList = data;
        },
        error => {
          console.error('Error loading hospitality data:', error);
          this._notificationService.error('Error', 'Failed to load medical centers data.');
        }
      );
  }

  private loadProviders(): void {
    this.hospitalityService
      .query()
      .pipe(takeUntil(this.destroy$))
      .subscribe(
        data => {
          this.providers = data.map((provider: any) => ({
            value: provider.providerId,
            label: provider.providerName,
          }));
        },
        error => {
          console.error('Error loading providers:', error);
          this._notificationService.error('Error', 'Failed to load providers data.');
        }
      );
  }

  onFilterGlobal(event: any): void {
    const value = event.target.value.toLowerCase();

    this.filteredServiceList = this.serviceList.filter(item => {
      return (
        item.serviceName?.toLowerCase().includes(value) ||
        item.fileName?.toLowerCase().includes(value)
      );
    });
  }

  attachFile(event: any): void {
    const file = event.target.files[0];
    if (!file) return;

    const typeParts = file.name.split('.');
    this.fileName = typeParts[0];
    this.fileType = typeParts[typeParts.length - 1].toLowerCase();

    if (this.fileType === 'kml') {
      const reader = new FileReader();
      reader.onload = (e: any) => {
        this.documentValue = e.target.result;
        this.fileUploaded = true;
      };
      reader.readAsDataURL(file);
    } else {
      this._notificationService.error('Error', 'Please upload a KML file.');
      const fileInput = document.getElementById('file') as HTMLInputElement;
      if (fileInput) {
        fileInput.value = '';
      }
    }
  }

  editHospitality(hospitality: Hospitality): void {
    this.isEdit = true;
    this.pageName = 'Edit';
    this.currentHospitality = { ...hospitality };

    // Handle provider selection if available
    if (hospitality.providerAreaList?.length) {
      const providerArray: number[] = [];
      hospitality.providerAreaList.forEach(element => {
        providerArray.push(element.providerId);
      });
      this.providerList = providerArray;
    } else {
      this.providerList = [];
    }

    this.showMapView();
  }

  onMapReady(map: L.Map): void {
    this.leafletMap = map;
    console.log('[onMapReady] Map initialized:', map);

    // Create a feature group for the editable layers
    this.editableLayers = new L.FeatureGroup();
    map.addLayer(this.editableLayers);
    console.log('[onMapReady] Editable layers created and added to map.');

    // Configure the draw control
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

    // Instantiate the Draw control
    this.drawControl = new (L.Control as any).Draw(drawOptions);
    map.addControl(this.drawControl);
    console.log('[onMapReady] Draw control added.');

    // Event handling for drawing
    map.on((L as any).Draw.Event.CREATED, (e: any) => {
      const layer = e.layer;
      if (this.editableLayers) {
        this.editableLayers.clearLayers();
        this.editableLayers.addLayer(layer);
      }
      this.updateCoordinatesFromLayer(layer);
      console.log('[onMapReady] New layer created and added:', layer);
    });

    // Event handling for editing
    map.on((L as any).Draw.Event.EDITED, (e: any) => {
      const layers = e.layers;
      layers.eachLayer((layer: any) => {
        this.updateCoordinatesFromLayer(layer);
        console.log('[onMapReady] Layer edited:', layer);
      });
    });

    // Event handling for deleting
    map.on((L as any).Draw.Event.DELETED, () => {
      this.initialCoordinates = '';
      console.log('[onMapReady] Layer(s) deleted.');
    });

    // If editing, create polygon from existing coordinates
    console.log('[onMapReady] isEdit:', this.isEdit, 'serviceAreaList:', this.currentHospitality.serviceAreaList);
    if (this.isEdit && this.currentHospitality.serviceAreaList?.length) {
      console.log('[onMapReady] Calling showMedicalCenterOnMap()');
      this.showMedicalCenterOnMap();
    } else {
      console.log('[onMapReady] Not calling showMedicalCenterOnMap:', {
        isEdit: this.isEdit,
        serviceAreaList: this.currentHospitality.serviceAreaList
      });
    }
  }

  showMedicalCenterOnMap(): void {
    if (
      !this.leafletMap ||
      !this.editableLayers ||
      !this.currentHospitality.serviceAreaList?.length
    ) {
      return;
    }

    try {
      const serviceArea = this.currentHospitality.serviceAreaList[0];
      console.log('Displaying service area:', serviceArea);
      if (!serviceArea.serviceArea) return;

      // Try to parse the GeoJSON string
      try {
        const geoJsonData = JSON.parse(serviceArea.serviceArea);
        console.log('[showMedicalCenterOnMap] Parsed GeoJSON:', geoJsonData);

        // Create GeoJSON layer directly from the parsed data
        const geoJsonLayer = L.geoJSON(geoJsonData, {
          style: {
            color: '#FF0000',
            fillColor: '#FF0000',
            fillOpacity: 0.35,
            weight: 2,
          }
        });

        // Clear existing layers
        this.editableLayers.clearLayers();

        // Add the new layer
        this.editableLayers.addLayer(geoJsonLayer);

        // Update stored coordinates
        this.updateCoordinatesFromLayer(geoJsonLayer);

        // Fit bounds to show the polygon
        const bounds = geoJsonLayer.getBounds();
        console.log('[showMedicalCenterOnMap] Layer bounds:', bounds);
        this.leafletMap.fitBounds(bounds);
        return;
      } catch (e) {
        console.error('Error parsing GeoJSON:', e);

        // Fallback: Try to parse as old format (comma-separated lat lng pairs)
        const coordinatesList = serviceArea.serviceArea.split(',');
        const leafletLatLngs: L.LatLngTuple[] = [];
        for (let i = 0; i < coordinatesList.length - 1; i++) {
          const temp = coordinatesList[i].split(' ');
          if (temp.length >= 2) {
            leafletLatLngs.push([parseFloat(temp[0]), parseFloat(temp[1])]);
          }
        }
        console.log('[showMedicalCenterOnMap] Old format Leaflet latlngs:', leafletLatLngs);

        if (leafletLatLngs.length > 0) {
          const polygon = L.polygon(leafletLatLngs, {
            color: '#FF0000',
            fillColor: '#FF0000',
            fillOpacity: 0.35,
            weight: 2,
          });

          // Clear existing layers
          this.editableLayers.clearLayers();

          // Add to editable layers
          this.editableLayers.addLayer(polygon);

          // Update stored GeoJSON
          this.updateCoordinatesFromLayer(polygon);

          // Fit bounds to show the polygon
          const bounds = polygon.getBounds();
          console.log('[showMedicalCenterOnMap] Polygon bounds (old format):', bounds);
          this.leafletMap.fitBounds(bounds);
          return;
        }
      }
    } catch (error) {
      console.error('Error displaying medical center on map:', error);
    }

    // Default center if no valid polygon found
    this.leafletMap.setView([38.78995, -75.55375], 8);
  }

  updateCoordinatesFromLayer(layer: any): void {
    try {
      // Get the GeoJSON representation of the layer
      const geoJson = layer.toGeoJSON();

      // Store the GeoJSON as string representation for serialization
      this.initialCoordinates = JSON.stringify(geoJson);
      console.log('Updated initialCoordinates:', this.initialCoordinates);
    } catch (error) {
      console.error('Error converting layer to GeoJSON:', error);
    }
  }

  processKmlFile(content: string): void {
    try {
      // Parse KML XML
      const parser = new DOMParser();
      const kmlDoc = parser.parseFromString(content, 'text/xml');

      // Process the GeoJSON
      this.renderGeoJsonOnMap(content);
    } catch (error) {
      console.error('Error processing KML:', error);
      this._notificationService.error('Error', 'Invalid KML file format');
    }
  }

  private renderGeoJsonOnMap(content: any): void {
    if (!this.leafletMap || !this.editableLayers) {
      console.error('Map or editable layers not initialized');
      return;
    }

    try {
      // Create GeoJSON from content
      console.log('Rendering GeoJSON:', content);
      const geoJsonLayer = L.geoJSON(content, {
        style: {
          color: '#FF0000',
          fillColor: '#FF0000',
          fillOpacity: 0.35,
          weight: 2,
        },

      });

      this.updateCoordinatesFromLayer(geoJsonLayer);

      // Add to map
      this.editableLayers.addLayer(geoJsonLayer);

      // Fit bounds to show the polygon - validate bounds first
      try {
        const bounds = geoJsonLayer.getBounds();
        if (bounds && bounds.isValid()) {
          this.leafletMap.fitBounds(bounds);
        } else {
          console.warn('GeoJSON layer has invalid bounds, skipping fitBounds');
        }
      } catch (boundsError) {
        console.warn('Could not get valid bounds from GeoJSON layer:', boundsError);
      }

      this._notificationService.success('Success', 'File loaded successfully');
    } catch (error) {
      console.error('Error rendering GeoJSON:', error);
      this._notificationService.error('Error', 'Error rendering the file on the map');
    }
  }

  addProviders(providerList: number[], isCheck: boolean): void {
    this.providerList = providerList.sort();
  }

  showMapView(): void {
    this.id = 'display';
    document.getElementById('display')!.style.display = 'block';
    document.getElementById('displayGrid')!.style.display = 'none';

    // Initialize map after the view has been updated
    setTimeout(() => {
      if (this.leafletMap) {
        this.leafletMap.invalidateSize();
        console.log('[showMapView] Map invalidated. isEdit:', this.isEdit, 'serviceAreaList:', this.currentHospitality.serviceAreaList);
        if (this.isEdit && this.currentHospitality.serviceAreaList?.length) {
          console.log('[showMapView] Calling showMedicalCenterOnMap()');
          this.showMedicalCenterOnMap();
        } else {
          console.log('[showMapView] Not calling showMedicalCenterOnMap:', {
            isEdit: this.isEdit,
            serviceAreaList: this.currentHospitality.serviceAreaList
          });
        }
      } else {
        console.log('[showMapView] leafletMap not initialized');
      }
    }, 100);
  }

  cancelEdit(): void {
    this.id = 'displayGrid';
    document.getElementById('displayGrid')!.style.display = 'block';
    document.getElementById('display')!.style.display = 'none';

    this.isEdit = false;
    this.currentHospitality = { serviceName: '', isActive: true };
    this.providerList = [];
    this.initialCoordinates = '';
    console.log('cancelEdit called, resetting state');
    if (this.editableLayers) {
      this.editableLayers.clearLayers();
    }
  }

  getProviderObj(providerList: number[]): void {
    let providerArea: any[] = [];
    const tempArray: any[] = [];
    const arrayProvider: number[] = providerList.slice();

    providerArea = this.currentHospitality.providerAreaList ?? [];
    if (providerArea == null || providerArea.length == 0) {
      for (let i = 0; i < arrayProvider.length; i++) {
        const obj = {
          providerId: arrayProvider[i],
          providerServiceName: this.currentHospitality.serviceName,
          serviceId: this.currentHospitality.serviceId,
        };
        tempArray.push(obj);
      }
    } else {
      const flag = false;
      providerArea = this.currentHospitality.providerAreaList ?? [];

      // Process existing providers
      for (let inx = 0; inx < providerArea.length; inx++) {
        arrayProvider.forEach(element => {
          if (providerArea[inx].providerId == element) {
            const obj = {
              hospitalityProviderId: providerArea[inx].hospitalityProviderId,
              providerId: element,
              providerServiceName: this.currentHospitality.serviceName,
              serviceId: this.currentHospitality.serviceId,
            };
            const index = arrayProvider.indexOf(providerArea[inx].providerId);
            if (index > -1) {
              arrayProvider.splice(index, 1);
            }
            tempArray.push(obj);
          }
        });
      }

      // Process new providers
      if (arrayProvider != null && arrayProvider.length != 0) {
        arrayProvider.forEach(element => {
          const obj = {
            providerId: element,
            providerServiceName: this.currentHospitality.serviceName,
            serviceId: this.currentHospitality.serviceId,
          };
          tempArray.push(obj);
        });
      }
    }

    this.currentHospitality.providerAreaList = tempArray;
  }

  saveHospitality(): void {
    if (this.hospitalityForm && !this.hospitalityForm.valid) {
      this._notificationService.error('Error', 'Please fill all required fields correctly.');
      return;
    }

    this.getProviderObj(this.providerList);

    // Update service area list with new GeoJSON if available
    console.log('save Hospitality - initialCoordinates:', this.initialCoordinates);
    if (this.initialCoordinates) {
      if (
        this.currentHospitality.serviceAreaList &&
        this.currentHospitality.serviceAreaList.length > 0
      ) {
        // Update existing service area
        this.currentHospitality.serviceAreaList[0].serviceArea = this.initialCoordinates;
      } else {
        // Create new service area
        this.currentHospitality.serviceAreaList = [
          {
            serviceId: this.currentHospitality.serviceId,
            serviceArea: this.initialCoordinates,
          },
        ];
      }
    }

    if (this.documentValue) {
      this.currentHospitality.uploadFile = {
        documentPath: '',
        documentValue: this.documentValue,
        fileName: this.fileName,
        fileFormat: this.fileType,
      };
    }

    this.hospitalityService
      .update(this.currentHospitality)
      .pipe(takeUntil(this.destroy$))
      .subscribe(
        response => {
          this._notificationService.success('Success', 'Medical center saved successfully.');
          this.cancelEdit();
          this.loadHospitalityData();
        },
        error => {
          console.error('Error saving hospitality:', error);
          this._notificationService.error('Error', 'Error saving medical center.');
        }
      );
  }

  mapHospitality(): void {
    if (!this.currentHospitality || !this.currentHospitality.serviceId) {
      this._notificationService.error('Error', 'No service selected');
      return;
    }

    const providerId = parseInt(this.providerId);

    if (this.currentHospitality.providerAreaList != null) {
      const providerAreaList = this.currentHospitality.providerAreaList;
      let providerExists = false;

      // Check if provider already exists in the list
      for (let i = 0; i < providerAreaList.length; i++) {
        if (providerAreaList[i].providerId === providerId) {
          providerExists = true;
          break;
        }
      }

      // Add provider if not already in the list
      if (!providerExists) {
        const providerObj = {
          serviceId: this.currentHospitality.serviceId,
          providerId: providerId,
          providerServiceName: this.currentHospitality.serviceName,
        };
        providerAreaList.push(providerObj);
      }
    } else {
      // Initialize provider area list if not already done
      const providerObj = {
        serviceId: this.currentHospitality.serviceId,
        providerId: providerId,
        providerServiceName: this.currentHospitality.serviceName,
      };
      this.currentHospitality.providerAreaList = [providerObj];
    }

    this.hospitalityService
      .update(this.currentHospitality)
      .pipe(takeUntil(this.destroy$))
      .subscribe(
        response => {
          this._notificationService.success('Success', 'Service area mapped successfully.');
          this.cancelEdit();
          this.loadHospitalityData();
        },
        error => {
          console.error('Error mapping service area:', error);
          this._notificationService.error('Error', 'Service area already mapped.');
        }
      );
  }

  omitSpecialChar(event: KeyboardEvent): boolean {
    const k = event.charCode;
    return (k > 64 && k < 91) || (k > 96 && k < 123) || k == 8 || k == 32 || (k >= 48 && k <= 57);
  }

  // revert to provider selection page for site admin
  back(): void {
    this.router.navigate(['/admin/adminProviderPartners']);
  }

  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (!file) {
      return;
    }

    const reader = new FileReader();
    reader.onload = (e: any) => {
      try {
        const fileExtension = file.name.split('.').pop()?.toLowerCase();

        if (fileExtension === 'kml') {
          // Parse KML file
          const parser = new DOMParser();

          // Fix common namespace issues in KML before parsing
          let kmlContent = e.target.result;

          // Add missing xsi namespace if schemaLocation is present but xsi is not declared
          if (kmlContent.includes('xsi:schemaLocation') && !kmlContent.includes('xmlns:xsi')) {
            kmlContent = kmlContent.replace(
              /<kml\s+/,
              '<kml xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" '
            );
            console.log('Added missing xsi namespace to KML');
          }

          // Remove redundant xmlns:kml declaration if present (some KML files have both default xmlns and xmlns:kml)
          if (kmlContent.includes('xmlns:kml="http://www.opengis.net/kml/2.2"')) {
            kmlContent = kmlContent.replace(/\s+xmlns:kml="http:\/\/www\.opengis\.net\/kml\/2\.2"/, '');
            console.log('Removed redundant xmlns:kml declaration');
          }

          const kmlDoc = parser.parseFromString(kmlContent, 'text/xml');

          // Check for XML parsing errors
          const parserError = kmlDoc.querySelector('parsererror');
          if (parserError) {
            console.error('XML parsing error:', parserError.textContent);
            this._notificationService.error('Error', 'Invalid KML file: XML parsing failed');
            return;
          }

          // Convert KML to GeoJSON
          const geoJson = togeojson.kml(kmlDoc);
          console.log('Converted KML to GeoJSON:', geoJson);

          // Validate that we have features
          if (!geoJson || !geoJson.features || geoJson.features.length === 0) {
            console.warn('KML conversion resulted in empty GeoJSON');
            console.warn('KML Document structure:', kmlDoc.documentElement);

            // Check if there are any Placemarks in the KML
            const placemarks = kmlDoc.getElementsByTagName('Placemark');
            console.warn('Number of Placemarks found:', placemarks.length);

            if (placemarks.length > 0) {
              // There are placemarks but conversion failed - likely a namespace issue
              this._notificationService.error(
                'Error',
                'KML file could not be converted. This may be due to namespace or structure issues.'
              );
            } else {
              this._notificationService.error('Error', 'KML file contains no valid geometries');
            }
            return;
          }

          this.renderGeoJsonOnMap(geoJson);
        } else if (fileExtension === 'geojson' || fileExtension === 'json') {
          // Parse GeoJSON file
          const geoJson = JSON.parse(e.target.result);
          this.renderGeoJsonOnMap(geoJson);
        } else {
          this._notificationService.error(
            'Error',
            'Unsupported file format. Please upload a KML or GeoJSON file.'
          );
        }
      } catch (error) {
        console.error('Error processing file:', error);
        this._notificationService.error(
          'Error',
          'Failed to process the file. Please make sure it is a valid KML or GeoJSON file.'
        );
      }
    };

    // Read file based on its type
    if (file.name.toLowerCase().endsWith('.kml')) {
      reader.readAsText(file);
    } else {
      reader.readAsText(file);
    }
  }
}
