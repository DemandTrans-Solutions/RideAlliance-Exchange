package com.clearinghouse.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.locationtech.jts.geom.Geometry;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class GeoJSONService {

    private static final int WGS84_SRID = 4326;


    private String toWellKnownText(Geometry geometry) {
        return geometry.toString();
    }


    public static String convertGeometryCollectionToFeatureCollection(String geometryCollectionGeoJSON) {
        JSONObject geometryCollection = new JSONObject(geometryCollectionGeoJSON);
        if (!"GeometryCollection".equals(geometryCollection.getString("type"))) {
            throw new IllegalArgumentException("Input must be a GeometryCollection GeoJSON.");
        }

        JSONArray geometries = geometryCollection.getJSONArray("geometries");
        JSONObject featureCollection = new JSONObject();
        featureCollection.put("type", "FeatureCollection");

        JSONArray features = new JSONArray();
        for (int i = 0; i < geometries.length(); i++) {
            JSONObject geometry = geometries.getJSONObject(i);
            JSONObject feature = new JSONObject();
            feature.put("type", "Feature");
            feature.put("geometry", geometry);
            feature.put("properties", new JSONObject()); // Add empty properties or custom properties here
            features.put(feature);
        }

        featureCollection.put("features", features);
        return featureCollection.toString();
    }


    /**
     * Converts a JTS Geometry object to a GeoJSON string representation
     */
    public String toGeoJSON(Geometry geometry) {
        if (geometry == null) {
            return null;
        }

        try {
            // Use GeoJSON writer from JTS
            org.locationtech.jts.io.geojson.GeoJsonWriter writer = new org.locationtech.jts.io.geojson.GeoJsonWriter();
            writer.setEncodeCRS(false);
            var val = writer.write(geometry);
            if (val != null && val.contains("GeometryCollection")) {
                return convertGeometryCollectionToFeatureCollection(val);
            }
            return val;
        } catch (Exception e) {
            log.error("Error converting geometry to GeoJSON: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Converts a GeoJSON string to a JTS Geometry object
     */
    public Geometry fromGeoJSON(String geoJson) {
        if (geoJson == null || geoJson.isEmpty()) {
            return null;
        }

        try {
            org.json.JSONObject jsonObject = new org.json.JSONObject(geoJson);

            // Handle FeatureCollection
            if (jsonObject.has("type") && "FeatureCollection".equals(jsonObject.getString("type"))) {
                if (jsonObject.has("features")) {
                    org.json.JSONArray features = jsonObject.getJSONArray("features");
                    org.locationtech.jts.geom.GeometryFactory geometryFactory = new org.locationtech.jts.geom.GeometryFactory();
                    org.locationtech.jts.geom.GeometryCollection geometryCollection = new org.locationtech.jts.geom.GeometryCollection(
                            features.toList().stream()
                                    .map(feature -> {
                                        try {
                                            org.json.JSONObject featureObject = new org.json.JSONObject((java.util.Map<?, ?>) feature);
                                            if (featureObject.has("geometry")) {
                                                String geometryJson = featureObject.getJSONObject("geometry").toString();
                                                org.locationtech.jts.io.geojson.GeoJsonReader geoJsonReader = new org.locationtech.jts.io.geojson.GeoJsonReader();
                                                var geo = geoJsonReader.read(geometryJson);
                                                if (geo.getSRID() != WGS84_SRID) {
                                                    geo.setSRID(WGS84_SRID);
                                                }
                                                return geo;
                                            }
                                        } catch (Exception e) {
                                            log.error("Error converting feature geometry: {}", e.getMessage(), e);
                                        }
                                        return null;
                                    })
                                    .filter(java.util.Objects::nonNull)
                                    .toArray(org.locationtech.jts.geom.Geometry[]::new),
                            geometryFactory
                    );
                    return geometryCollection;
                }
            }

            // Handle single Feature or direct geometry object
            if (jsonObject.has("type") && "Feature".equals(jsonObject.getString("type"))) {
                if (jsonObject.has("geometry")) {
                    String geometryJson = jsonObject.getJSONObject("geometry").toString();
                    org.locationtech.jts.io.geojson.GeoJsonReader geoJsonReader = new org.locationtech.jts.io.geojson.GeoJsonReader();
                    var geo = geoJsonReader.read(geometryJson);
                    if (geo.getSRID() != WGS84_SRID) {
                        geo.setSRID(WGS84_SRID);
                    }
                    return geo;
                }
            } else {
                org.locationtech.jts.io.geojson.GeoJsonReader geoJsonReader = new org.locationtech.jts.io.geojson.GeoJsonReader();
                return geoJsonReader.read(geoJson);
            }

            return null;
        } catch (Exception e) {
            log.error("Error converting GeoJSON to geometry: {}", e.getMessage(), e);
            return null;
        }
    }

}
