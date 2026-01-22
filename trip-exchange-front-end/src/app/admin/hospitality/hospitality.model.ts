export interface Hospitality {
  serviceId?: number;
  serviceName: string;
  fileName?: string;
  isActive?: boolean;
  uploadFile?: HospitalityFile;
  providerAreaList?: HospitalityProvider[];
  serviceAreaList?: HospitalityServiceArea[];
}

export interface HospitalityFile {
  documentPath?: string;
  documentValue: string;
  fileName: string;
  fileFormat: string;
}

export interface HospitalityProvider {
  hospitalityProviderId?: number;
  providerId: number;
  providerServiceName?: string;
  serviceId?: number;
  providerName?: string;
}

export interface HospitalityServiceArea {
  serviceAreaId?: number;
  serviceId?: number;
  serviceArea?: string; // GeoJSON as string
}

export interface Provider {
  value: number;
  label: string;
}

// GeoJSON types
export interface GeoJsonFeature {
  type: string;
  geometry: {
    type: string;
    coordinates: number[][][];
  };
  properties: Record<string, any>;
}
