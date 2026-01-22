package com.clearinghouse.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class KMLParser {

	/*
	public static void main(String[] args) {
		log.debug("This is KML test");
		final Kml kml = Kml.unmarshal(new File("D:\\ClearingHouse\\KML_Samples - CC.kml"));
		// final Kml kml = Kml.unmarshal(new
		// File("D:\\ClearingHouse\\Wheaton-Winfield_cnR.kml"));
		// final Kml kml = Kml.unmarshal(new
		// File("D:\\ClearingHouse\\LoneTree_cnR.kml"));

		// final Kml kml = Kml
		// .unmarshal(new
		// File("D:\\ClearingHouse\\shareFile\\Service_Area_KMLs\\ARDC_Service_Area.kml"));
		// .unmarshal(new
		// File("D:\\ClearingHouse\\shareFile\\Service_Area_KMLs\\Via_Service_Area.kml"));

		// final Kml kml = Kml
		// .unmarshal(new
		// File("D:\\ClearingHouse\\shareFile\\Service_Area_KMLs\\ARDC_Service_Area.kml"),false);

		List<String> latLongList = new ArrayList<String>();
		final Document document = (Document) kml.getFeature();
		// log.debug(document.getName());
		List<Feature> t = document.getFeature();
		if (t == null) {
			throw new InvalidKMLFileException("Invalid KML File Dataa");
		}
		for (Object o : t) {
			Folder f = (Folder) o;
			List<Feature> featureList = f.getFeature();

			for (Feature feature : featureList) {
				log.debug("feature name: " + feature.getName());

				if (feature instanceof Placemark) {
					Placemark placemark = (Placemark) feature;
					Geometry geometry = placemark.getGeometry();
					log.debug("placemark name: " + placemark.getName());
					// log.debug(parseGeometry(geometry));
					if (geometry instanceof MultiGeometry) {
						MultiGeometry multiGeometry = (MultiGeometry) geometry;
						List<Geometry> geometryList = multiGeometry.getGeometry();
						log.debug("geometryList size: " + geometryList.size());
						for (Geometry geoItr : geometryList) {
							log.debug(parseGeometry(geoItr));
							latLongList.add(parseGeometry(geoItr));
						}
					} else {
						log.debug(parseGeometry(geometry));
						log.debug(parseGeometry(geometry));
					}
				} else if (feature instanceof Folder) {
					log.debug("Folder name: " + feature.getName());
					Folder folder = (Folder) feature;
					List<Feature> folderFeatureList = folder.getFeature();
					for (Feature folderfeature : folderFeatureList) {
						log.debug("feature name: " + feature.getName());
						if (folderfeature instanceof Placemark) {
							Placemark placemark = (Placemark) folderfeature;
							Geometry geometry = placemark.getGeometry();
							log.debug("placemark name: " + placemark.getName());
							if (geometry instanceof MultiGeometry) {
								MultiGeometry multiGeometry = (MultiGeometry) geometry;
								List<Geometry> geometryList = multiGeometry.getGeometry();
								log.debug("geometryList size: " + geometryList.size());
								for (Geometry geoItr : geometryList) {
									log.debug(parseGeometry(geoItr));
									latLongList.add(parseGeometry(geoItr));
								}
							} else {
								log.debug(parseGeometry(geometry));
								latLongList.add(parseGeometry(geometry));
							}
						}
					}
				}
			}
		}
		log.debug("\n \n all polygon size" + latLongList.size());
		log.debug("\n all polygon " + latLongList);

	}

	private static String parseGeometry(Geometry geometry) throws InvalidKMLFileException {
		String latLong = "";
		if (geometry != null) {
			if (geometry instanceof Polygon) {

				Polygon polygon = (Polygon) geometry;

				Boundary outerBoundaryIs = polygon.getOuterBoundaryIs();
				if (outerBoundaryIs != null) {
					LinearRing linearRing = outerBoundaryIs.getLinearRing();
					if (linearRing != null) {
						List<Coordinate> coordinates = linearRing.getCoordinates();
						if (coordinates != null) {
							// for (Coordinate coordinate : coordinates) {
							for (int i = 0; i < coordinates.size(); i++) {
								latLong += parseCoordinate(coordinates.get(i));
								if (i < coordinates.size() - 1) {
									latLong += ",";
								}
							}
						}
					}
				}
			} else {
				throw new InvalidKMLFileException("Invalid KML File Data");
			}
		}
		if (latLong == "") {
			throw new InvalidKMLFileException("Invalid KML File Data");
		}
		System.out.print("latLong++" + latLong);
		return latLong;
	}

	private static String parseCoordinate(Coordinate coordinate) throws InvalidKMLFileException {
		if (coordinate != null) {
			log.debug(
					String.valueOf(coordinate.getLatitude()) + " " + String.valueOf(coordinate.getLongitude()));
			return String.valueOf(coordinate.getLatitude()) + " " + String.valueOf(coordinate.getLongitude());
		} else {
			throw new InvalidKMLFileException("Invalid KML File Data");
		}
	}

	public static List<String> getServiceAreaFromFile(String filePath) throws InvalidKMLFileException {
		//String serviceArea = null;
		List<String> latLongList = new ArrayList<String>();
		log.debug("This is KML test");
		try {
			final Kml kml = Kml.unmarshal(new File(filePath));
			final Document document = (Document) kml.getFeature();
			log.debug(document.getName());
			List<Feature> t = document.getFeature();
			if (t == null) {
				throw new InvalidKMLFileException("Invalid KML File Data");
			}
			for (Object o : t) {
				Folder f = (Folder) o;
				List<Feature> featureList = f.getFeature();

				for (Feature feature : featureList) {
					log.debug("feature name: " + feature.getName());

					if (feature instanceof Placemark) {
						Placemark placemark = (Placemark) feature;
						Geometry geometry = placemark.getGeometry();
						log.debug("placemark name: " + placemark.getName());
						if (geometry instanceof MultiGeometry) {
							MultiGeometry multiGeometry = (MultiGeometry) geometry;
							List<Geometry> geometryList = multiGeometry.getGeometry();
							log.debug("geometryList size: " + geometryList.size());
							for (Geometry geoItr : geometryList) {
								latLongList.add(parseGeometry(geoItr));
							}
						} else {
							latLongList.add(parseGeometry(geometry));
						}
					} else if (feature instanceof Folder) {
						log.debug("Folder name: " + feature.getName());
						Folder folder = (Folder) feature;
						List<Feature> folderFeatureList = folder.getFeature();
						for (Feature folderfeature : folderFeatureList) {
							log.debug("feature name: " + feature.getName());
							if (folderfeature instanceof Placemark) {
								Placemark placemark = (Placemark) folderfeature;
								Geometry geometry = placemark.getGeometry();
								log.debug("placemark name: " + placemark.getName());
								if (geometry instanceof MultiGeometry) {
									MultiGeometry multiGeometry = (MultiGeometry) geometry;
									List<Geometry> geometryList = multiGeometry.getGeometry();
									log.debug("geometryList size: " + geometryList.size());
									for (Geometry geoItr : geometryList) {
										latLongList.add(parseGeometry(geoItr));
									}
								} else {
									latLongList.add(parseGeometry(geometry));
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			log.debug(e.getMessage());
			throw new InvalidKMLFileException("Invalid KML File Data");
		}
		return latLongList;
	}

	 */


    public static List<String> getServiceAreaFromFile(String filePath) {
        log.error("getServiceAreaFromFile is not implemented!!!!!!!!");
        return new ArrayList<>();
    }

}
