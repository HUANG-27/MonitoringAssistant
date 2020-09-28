package com.example.huang.client.fragment;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapScaleChangedEvent;
import com.esri.arcgisruntime.mapping.view.MapScaleChangedListener;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleRenderer;
import com.example.huang.client.R;

import com.example.huang.client.config.App2;
import com.example.huang.client.entity.Monitor;
import com.example.huang.client.entity.Target;
import com.example.huang.client.entity.UAV;
import com.example.huang.client.tool.GeometryTool;
import com.example.huang.client.tool.LocateTool;

import java.util.Timer;
import java.util.TimerTask;

public class MapFragment extends Fragment {

    //任务控制
    private TextView textViewMissionTip;
    private TextView textViewMissionCtt;
    //无人机图片
    private ImageView imageViewUAVImage;
    private ImageButton imageButtonGetUAVImage;
    private RelativeLayout layoutImage;
    //地图
    public static MapView mapView;
    private static ArcGISMap map;
    private static GraphicsOverlay locationOverlay;
    private static GraphicsOverlay pathOverlay;

    //坐标
    public static SpatialReference spatialReferenceLL = SpatialReference.create(4326);
    public static SpatialReference spatialReferencePro = SpatialReference.create(3857);


    //显示控制器
    private static Timer timerUpdateDisplay;

    private static final int MAX_MAP_LEVEL = 17;

    public MapFragment(){
        super();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        @SuppressLint("InflateParams")
        View view = inflater.inflate(R.layout.fragment_map, null);
        initView(view);
        cycleDisplay();
        return view;
    }

    @Override
    public void onPause(){
        super.onPause();
        mapView.pause();
    }
    @Override
    public void onResume(){
        super.onResume();
        mapView.resume();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(timerUpdateDisplay != null){
            timerUpdateDisplay.cancel();
            timerUpdateDisplay = null;
        }
        mapView.dispose();
    }


    @SuppressLint("ClickableViewAccessibility")
    private void initView(View view) {
        //任务内容显示
        textViewMissionTip = (TextView)view.findViewById(R.id.textView_mission_tip);
        textViewMissionCtt = (TextView)view.findViewById(R.id.textView_mission_ctt);

        //地图相关内容
        mapView = (MapView) view.findViewById(R.id.mapView);
        map = new ArcGISMap(Basemap.Type.OPEN_STREET_MAP, 36, 104, 15);
        mapView.setMap(map);
        mapView.addMapScaleChangedListener(new MapScaleChangedListener() {
            @Override
            public void mapScaleChanged(MapScaleChangedEvent mapScaleChangedEvent) {
                System.out.println(mapScaleChangedEvent.getSource().getSpatialReference().getWkid());
                //System.out.println(mapScaleChangedEvent.getSource().getScaleX());
                System.out.println(mapView.getMapScale());
            }
        });
//        //服务器地图服务
//        Portal portal = new Portal("server-url");
//        PortalItem portalItem = new PortalItem(portal, "map-id");
//        ArcGISMap map = new ArcGISMap(portalItem);
        //添加Overlay
        locationOverlay = new GraphicsOverlay();
        mapView.getGraphicsOverlays().add(locationOverlay);
        pathOverlay = new GraphicsOverlay();
        mapView.getGraphicsOverlays().add(pathOverlay);

        //地图类型控制
        RadioGroup radioGroupMapType = (RadioGroup) view.findViewById(R.id.radioGroup_mapType);
        radioGroupMapType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radioButton_normalMap:
                        map = new ArcGISMap(Basemap.Type.OPEN_STREET_MAP, 36, 104, 15);
                        mapView.setMap(map);
                        break;
                    case R.id.radioButton_satelliteMap:
                        map = new ArcGISMap(Basemap.Type.IMAGERY_WITH_LABELS_VECTOR, 36, 104, 15);
                        mapView.setMap(map);
                        break;
                    case R.id.radio_button_terrain_map:
                        map = new ArcGISMap(Basemap.Type.TERRAIN_WITH_LABELS_VECTOR, 36, 104, 15);
                        mapView.setMap(map);
                        break;
                    default:
                        break;
                }
            }
        });
        radioGroupMapType.check(R.id.radioButton_normalMap);

        //执行搜索任务时显示图片无人机图片
        imageButtonGetUAVImage = (ImageButton) view.findViewById(R.id.imageButton_getImage);
        imageButtonGetUAVImage.setVisibility(View.INVISIBLE);
        layoutImage = (RelativeLayout) view.findViewById(R.id.rlt_image);
        imageViewUAVImage = (ImageView) view.findViewById(R.id.imageView_tempImage);
        ImageButton imageButtonCloseImageView = (ImageButton) view.findViewById(R.id.imageButton_closeImageView);
        imageButtonGetUAVImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutImage.setVisibility(View.VISIBLE);
                imageViewUAVImage.setImageURI(
                        Uri.parse(App2.monitor.getMission().getUavs().get(0).getImageUri()));
            }
        });
        imageButtonCloseImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutImage.setVisibility(View.INVISIBLE);
                imageViewUAVImage.setImageURI(null);
            }
        });
        //放大查看图片
        //TODO
        imageViewUAVImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                try{
//                    //放大显示
//                    Intent intent = new Intent(context, ShowImageActivity.class);
//                    MapFragment.this.startActivity(intent);
//                }catch (Exception e){
//                    e.printStackTrace();
//                }
            }
        });
    }

    @SuppressLint("ResourceAsColor")
    /**
     * 1-任务提示显示
     * 2-地图内容显示
     * 3-图片显示
     */
    private void updateDisplay(){
        if(App2.monitor == null){
            //1
            textViewMissionTip.setText("未登录，");
            textViewMissionTip.setTextColor(R.color.color_gray1);
            textViewMissionCtt.setText("请先登录");
            textViewMissionCtt.setTextColor(R.color.color_gray1);
            //2
            locationOverlay.getGraphics().clear();
            pathOverlay.getGraphics().clear();
            //显示monitor当前位置
            displayMyMonitorLocation();
            //3
            imageButtonGetUAVImage.setVisibility(View.INVISIBLE);
        }
        else if(App2.monitor.getMission() == null){
            //1
            textViewMissionTip.setText("日常任务：");
            textViewMissionTip.setTextColor(R.color.color_green4);
            textViewMissionCtt.setText("巡逻");
            textViewMissionCtt.setTextColor(R.color.color_green4);
            //2
            locationOverlay.getGraphics().clear();
            pathOverlay.getGraphics().clear();
            //显示monitor当前位置
            displayMyMonitorLocation();
            //3
            imageButtonGetUAVImage.setVisibility(View.INVISIBLE);
        } else{
            switch (App2.monitor.getMission().getType()){
                case 围捕:
                    //1
                    textViewMissionTip.setText("围捕任务：");
                    textViewMissionTip.setTextColor(R.color.color_red1);
                    textViewMissionCtt.setText(App2.monitor.getMission().getName());
                    textViewMissionCtt.setTextColor(R.color.color_red1);
                    //2
                    locationOverlay.getGraphics().clear();
                    pathOverlay.getGraphics().clear();
                    //显示monitor当前位置
                    displayMyMonitorLocation();
                    //显示任务合作monitors位置
                    displayCoMonitorLocations();
                    //显示target位置
                    displayTargetLocation();
                    //显示uavs位置
                    displayUAVLocation();
                    //显示规划的路径
                    //App2.monitor.getPath();
                    //3
                    imageButtonGetUAVImage.setVisibility(View.VISIBLE);
                    break;
                case 搜索:
                    //1
                    textViewMissionTip.setText("搜索任务：");
                    textViewMissionTip.setTextColor(R.color.color_red1);
                    textViewMissionCtt.setText(App2.monitor.getMission().getName());
                    textViewMissionCtt.setTextColor(R.color.color_red1);
                    //2
                    locationOverlay.getGraphics().clear();
                    pathOverlay.getGraphics().clear();
                    //显示monitor当前位置
                    displayMyMonitorLocation();
                    //显示任务合作monitors位置
                    displayCoMonitorLocations();
                    //显示搜索路径
                    displayPaths();
                    //3
                    imageButtonGetUAVImage.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message message){
            super.handleMessage(message);
            updateDisplay();
        }
    };

    private void cycleDisplay(){
        timerUpdateDisplay = new Timer();
        timerUpdateDisplay.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(0);
            }
        }, 0, 1000);
    }

    private void displayMyMonitorLocation(){
        if(LocateTool.myLocation == null)
            return;
        BitmapDrawable bitmapDrawable = (BitmapDrawable)
                ContextCompat.getDrawable(getActivity(), R.drawable.icon_locate_green_64);
        PictureMarkerSymbol markerSymbol = new PictureMarkerSymbol(bitmapDrawable);
        markerSymbol.setHeight(32);
        markerSymbol.setWidth(32);
        markerSymbol.loadAsync();
        markerSymbol.addDoneLoadingListener(new Runnable() {
            @Override
            public void run() {
                Point point = new Point(LocateTool.myLocation.getLon(), LocateTool.myLocation.getLat(), spatialReferenceLL);
                Point proPoint = (Point) GeometryEngine.project(point, spatialReferencePro);
                Graphic graphic = new Graphic(proPoint, markerSymbol);
                locationOverlay.getGraphics().add(graphic);
            }
        });
    }

    private void displayCoMonitorLocations(){
        if(App2.monitor == null)
            return;
        BitmapDrawable bitmapDrawable = (BitmapDrawable)
                ContextCompat.getDrawable(getActivity(), R.drawable.icon_monitor);
        PictureMarkerSymbol markerSymbol = new PictureMarkerSymbol(bitmapDrawable);
        markerSymbol.setHeight(32);
        markerSymbol.setWidth(32);
        markerSymbol.loadAsync();
        markerSymbol.addDoneLoadingListener(new Runnable() {
            @Override
            public void run() {
                if(App2.monitor.getMission().getMonitors() == null)
                    return;
                for (Monitor monitor: App2.monitor.getMission().getMonitors()) {
                    if(monitor.getLocation() != null){
                        Point point = new Point(monitor.getLocation().getLon(), monitor.getLocation().getLat(), spatialReferenceLL);
                        Point proPoint = (Point) GeometryEngine.project(point, spatialReferencePro);
                        Graphic graphic = new Graphic(proPoint, markerSymbol);
                        locationOverlay.getGraphics().add(graphic);
                    }

                }
            }
        });
    }

    private void displayTargetLocation(){
        if(App2.monitor == null)
            return;
        BitmapDrawable bitmapDrawable = (BitmapDrawable)
                ContextCompat.getDrawable(getActivity(), R.drawable.icon_target);
        PictureMarkerSymbol markerSymbol = new PictureMarkerSymbol(bitmapDrawable);
        markerSymbol.setHeight(32);
        markerSymbol.setWidth(32);
        markerSymbol.loadAsync();
        markerSymbol.addDoneLoadingListener(new Runnable() {
            @Override
            public void run() {
                if(App2.monitor.getMission().getTargets() == null)
                    return;
                for (Target target: App2.monitor.getMission().getTargets()) {
                    if(target.getLocation() != null){
                        Point point = new Point(target.getLocation().getLon(), target.getLocation().getLat(),spatialReferenceLL);
                        Point proPoint = (Point) GeometryEngine.project(point, spatialReferenceLL);
                        Graphic graphic = new Graphic(proPoint, markerSymbol);
                        locationOverlay.getGraphics().add(graphic);
                    }
                }
            }
        });
    }

    private void displayUAVLocation(){
        if(App2.monitor == null)
            return;
        BitmapDrawable bitmapDrawable = (BitmapDrawable)
                ContextCompat.getDrawable(getActivity(), R.drawable.icon_uav);
        PictureMarkerSymbol markerSymbol = new PictureMarkerSymbol(bitmapDrawable);
        markerSymbol.setHeight(48);
        markerSymbol.setWidth(48);
        markerSymbol.loadAsync();
        markerSymbol.addDoneLoadingListener(new Runnable() {
            @Override
            public void run() {
                if(App2.monitor.getMission().getUavs() == null)
                    return;
                for(UAV uav: App2.monitor.getMission().getUavs()){
                    if(uav.getLocation()!=null){
                        Point point = new Point(uav.getLocation().getLon(), uav.getLocation().getLat(), spatialReferenceLL);
                        Point proPoint = (Point) GeometryEngine.project(point, spatialReferencePro);
                        Graphic graphic = new Graphic(proPoint, markerSymbol);
                        locationOverlay.getGraphics().add(graphic);
                    }
                }
            }
        });
    }

    private void displayPaths(){
        if(App2.monitor == null)
            return;
        if(App2.monitor.getMission() == null)
            return;


        SimpleLineSymbol simpleLineSymbol = new SimpleLineSymbol(
                SimpleLineSymbol.Style.SOLID, Color.GREEN, 4);
        pathOverlay.setRenderer(new SimpleRenderer(simpleLineSymbol));
        //pathOverlay.getGraphics().add(
        //        new Graphic(GeometryTool.transformPath(App2.monitor.getPath())));
        for (Monitor monitor : App2.monitor.getMission().getMonitors()){
            Graphic graphic = new Graphic(GeometryTool.transformPath(monitor.getPath()));
            pathOverlay.getGraphics().add(graphic);
        }
    }
}
