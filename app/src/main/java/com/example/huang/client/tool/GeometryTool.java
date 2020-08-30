package com.example.huang.client.tool;

import com.esri.arcgisruntime.geometry.Part;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.Polyline;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.example.huang.client.entity.Coordinate;

import java.util.List;

public class GeometryTool {

    public static Polyline transformPath(List<Coordinate> path){
        Part part = new Part(SpatialReferences.getWgs84());
        for (Coordinate coordinate : path)
            part.addPoint(new Point(coordinate.getLon(), coordinate.getLat(), coordinate.getAlt()));
        return new Polyline(part);
    }
}
