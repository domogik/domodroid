package activities;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.domogik.domodroid13.R;

import java.util.ArrayList;
import java.util.HashMap;

import Entity.Entity_Area;
import Entity.Entity_Feature;
import Entity.Entity_Room;
import database.DomodroidDB;
import database.WidgetUpdate;
import misc.tracerengine;
import widgets.Basic_Graphical_widget;
import widgets.Com_Stats;
import widgets.Graphical_Area;
import widgets.Graphical_Binary;
import widgets.Graphical_Binary_New;
import widgets.Graphical_Boolean;
import widgets.Graphical_Cam;
import widgets.Graphical_Color;
import widgets.Graphical_History;
import widgets.Graphical_Info;
import widgets.Graphical_Info_commands;
import widgets.Graphical_Info_with_achartengine;
import widgets.Graphical_List;
import widgets.Graphical_Openstreetmap;
import widgets.Graphical_Range;
import widgets.Graphical_Room;
import widgets.Graphical_Trigger;

class Widgets_Manager {

    private int widgetSize;
    private boolean columns = false;
    private final Handler widgetHandler;
    public WidgetUpdate widgetupdate = null;
    private tracerengine Tracer = null;
    private final String mytag = this.getClass().getName();

    public Widgets_Manager(tracerengine Trac, Handler handler) {
        super();
        this.widgetHandler = handler;
        this.Tracer = Trac;

    }

    public LinearLayout loadActivWidgets(Activity activity, int id,
                                         String zone, LinearLayout ll, SharedPreferences params, int session_type) {

        DomodroidDB domodb = new DomodroidDB(Tracer, activity, params);
        domodb.owner = "Widgets_Manager.loadActivWidgets";
        Entity_Feature[] listFeature = domodb.requestFeatures(id, zone);

        LinearLayout.LayoutParams layout_param = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1.0f);
        LinearLayout mainPan = new LinearLayout(activity);
        mainPan.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout leftPan = new LinearLayout(activity);
        leftPan.setOrientation(LinearLayout.VERTICAL);
        leftPan.setLayoutParams(layout_param);
        LinearLayout rightPan = new LinearLayout(activity);
        rightPan.setOrientation(LinearLayout.VERTICAL);
        rightPan.setLayoutParams(layout_param);
        FrameLayout tmpPan = new FrameLayout(activity);

        int counter = 0;

        //check option and adapt columns in function
        columns = false;
        colonnes(activity, ll, mainPan, leftPan, rightPan, params);

        if (id == -1) {
            //We've to display statistics widget
            Tracer.i(mytag, "Call to process statistics widget");
            Com_Stats statistics = new Com_Stats(Tracer, activity, counter);
            statistics.container = tmpPan;
            tmpPan.addView(statistics);
            ll.addView(tmpPan);
            return ll;
        }
        //int size = listFeature.length;
        //Entity_Feature feature;

        for (Entity_Feature aListFeature : listFeature) {
            //feature = aListFeature;
            //-----add component-------
            //tmpPan = null;
            tmpPan = new FrameLayout(activity);
            String label = aListFeature.getDescription();
            String Value_type = aListFeature.getValue_type();
            String Address = aListFeature.getAddress();
            String parameters = aListFeature.getParameters();
            String device_type_id = aListFeature.getDevice_type_id();
            String State_key = aListFeature.getState_key();
            String iconName = aListFeature.getIcon_name();
            int DevId = aListFeature.getDevId();
            //int Id = feature.getId();
            //int Graph = params.getInt("GRAPH", 3);
            int update_timer = params.getInt("UPDATE_TIMER", 300);

            Tracer.i(mytag, "Call to process device : " + DevId + " Address : " + Address + " Value_type : " + Value_type + " Label : " + label + " Key : " + State_key);
            try {
                if (Value_type.equals("binary")) {
                    if (aListFeature.getDevice_type().equals("rgb_leds") && (State_key.equals("command"))) {
                        //ignore it : it'll have another device for Color, displaying the switch !)
                    } else {
                        if (!params.getBoolean("WIDGET_CHOICE", false)) {
                            Graphical_Binary onoff = new Graphical_Binary(Tracer, activity,
                                    widgetSize, session_type, id, zone, params, aListFeature, widgetHandler);
                            Graphical_Binary.container = tmpPan;
                            tmpPan.addView(onoff);
                            Tracer.i(mytag, "   ==> Graphical_Binary");
                        } else {
                            Graphical_Binary_New onoff_New = new Graphical_Binary_New(Tracer, activity,
                                    widgetSize, session_type, id, zone, params, aListFeature, widgetHandler);
                            Graphical_Binary_New.container = tmpPan;
                            tmpPan.addView(onoff_New);
                            Tracer.i(mytag, "   ==> Graphical_Binary");
                        }
                    }
                } else if (Value_type.equals("boolean") || Value_type.equals("bool")) {
                    if (parameters.contains("command")) {
                        if (!params.getBoolean("WIDGET_CHOICE", false)) {
                            Graphical_Binary onoff = new Graphical_Binary(Tracer, activity,
                                    widgetSize, session_type, id, zone, params, aListFeature, widgetHandler);
                            Graphical_Binary.container = tmpPan;
                            tmpPan.addView(onoff);
                            Tracer.i(mytag, "   ==> Graphical_Binary");
                        } else {
                            Graphical_Binary_New onoff_New = new Graphical_Binary_New(Tracer, activity,
                                    widgetSize, session_type, id, zone, params, aListFeature, widgetHandler);
                            Graphical_Binary_New.container = tmpPan;
                            tmpPan.addView(onoff_New);
                            Tracer.i(mytag, "   ==> Graphical_Binary");
                        }
                    } else {
                        Graphical_Boolean bool = new Graphical_Boolean(Tracer, activity,
                                widgetSize, session_type, id, zone, params, aListFeature, widgetHandler);
                        Graphical_Boolean.container = tmpPan;
                        tmpPan.addView(bool);
                        Tracer.i(mytag, "   ==> Graphical_Boolean");
                    }
                } else if (Value_type.equals("range") || ((parameters.contains("command")) && (aListFeature.getDevice_feature_model_id().startsWith("DT_Scaling")))) {
                    Graphical_Range variator = new Graphical_Range(Tracer, activity,
                            widgetSize, session_type, id, zone, params, aListFeature, widgetHandler);
                    Graphical_Range.container = tmpPan;
                    tmpPan.addView(variator);
                    Tracer.i(mytag, "   ==> Graphical_Range");
                } else if (Value_type.equals("trigger")) {
                    //#51 change widget for 0.4 if it's not a command
                    if (parameters.contains("command")) {
                        Graphical_Trigger trigger = new Graphical_Trigger(Tracer, activity,
                                widgetSize, session_type, id, zone, params, aListFeature, widgetHandler);
                        Graphical_Trigger.container = tmpPan;
                        tmpPan.addView(trigger);
                        Tracer.i(mytag, "   ==> Graphical_Trigger");
                    } else {
                        if (params.getBoolean("Graph_CHOICE", false)) {
                            Tracer.d(mytag, "add Graphical_Info_with_achartengine for " + label + " (" + DevId + ") key=" + State_key);
                            Graphical_Info_with_achartengine info_with_achartengine = new Graphical_Info_with_achartengine(Tracer, activity,
                                    widgetSize, session_type, id, zone, params, aListFeature, widgetHandler);
                            info_with_achartengine.setLayoutParams(layout_param);
                            Graphical_Info_with_achartengine.container = tmpPan;
                            tmpPan.addView(info_with_achartengine);
                    /* Todo when #89
                    Graphical_Info_with_mpandroidchart info_with_mpandroidchart = new Graphical_Info_with_mpandroidchart(Tracer, activity, URL,
                    widgetSize, session_type, id, zone, params, feature, widgetHandler);
                    info_with_mpandroidchart.setLayoutParams(layout_param);
                    Graphical_Info_with_mpandroidchart.container = tmpPan;
                    tmpPan.addView(info_with_mpandroidchart);
                    Tracer.i(mytag, "   ==> Graphical_Info_with_achartengine + Graphic");
                    */
                        } else {
                            Graphical_Info info = new Graphical_Info(Tracer, activity,
                                    widgetSize, session_type, id, zone, params, update_timer, aListFeature, widgetHandler);
                            info.setLayoutParams(layout_param);
                            info.with_graph = false;
                            Graphical_Info.container = tmpPan;
                            tmpPan.addView(info);
                            Tracer.i(mytag, "   ==> Graphical_Info + No graphic !!!");
                        }
                    }
                } else if (State_key.equals("color")) {
                    Tracer.d(mytag, "add Graphical_Color for " + label + " (" + DevId + ") key=" + State_key);
                    Graphical_Color color = new Graphical_Color(Tracer, activity,
                            widgetSize, session_type, id, zone, params, aListFeature, widgetHandler);
                    Graphical_Color.container = tmpPan;
                    tmpPan.addView(color);
                    Tracer.i(mytag, "   ==> Graphical_Color");
                } else if (Value_type.equals("number")) {
                    if (aListFeature.getParameters().contains("command_type")) {
                        Graphical_Info_commands info_commands = new Graphical_Info_commands(Tracer, activity,
                                widgetSize, session_type, id, zone, params, aListFeature, widgetHandler);
                        info_commands.setLayoutParams(layout_param);
                        Graphical_Info_commands.container = tmpPan;
                        tmpPan.addView(info_commands);
                        Tracer.i(mytag, "   ==> Graphical_Info_commands !!!");
                    } else if (params.getBoolean("Graph_CHOICE", false)) {
                        Tracer.d(mytag, "add Graphical_Info_with_achartengine for " + label + " (" + DevId + ") key=" + State_key);
                        Graphical_Info_with_achartengine info_with_achartengine = new Graphical_Info_with_achartengine(Tracer, activity,
                                widgetSize, session_type, id, zone, params, aListFeature, widgetHandler);
                        info_with_achartengine.setLayoutParams(layout_param);
                        Graphical_Info_with_achartengine.container = tmpPan;
                        tmpPan.addView(info_with_achartengine);
                    /* Todo when #89
                    Graphical_Info_with_mpandroidchart info_with_mpandroidchart = new Graphical_Info_with_mpandroidchart(Tracer, activity, URL,
                    widgetSize, session_type, id, zone, params, feature, widgetHandler);
                    info_with_mpandroidchart.setLayoutParams(layout_param);
                    Graphical_Info_with_mpandroidchart.container = tmpPan;
                    tmpPan.addView(info_with_mpandroidchart);
                    Tracer.i(mytag, "   ==> Graphical_Info_with_achartengine + Graphic");
                    */
                    } else {
                        Tracer.d(mytag, "add Graphical_Info for " + label + " (" + DevId + ") key=" + State_key);
                        Graphical_Info info = new Graphical_Info(Tracer, activity,
                                widgetSize, session_type, id, zone, params, update_timer, aListFeature, widgetHandler);
                        info.setLayoutParams(layout_param);
                        info.with_graph = true;
                        Graphical_Info.container = tmpPan;
                        tmpPan.addView(info);
                        Tracer.i(mytag, "   ==> Graphical_Info + Graphic");
                    }
                } else if (Value_type.equals("list")) {
                    if (!aListFeature.getDevice_feature_model_id().startsWith("DT_ColorRGB") && !aListFeature.getDevice_feature_model_id().startsWith("DT_ColorCMYK")) {
                        Tracer.d(mytag, "add Graphical_List for " + label + " (" + DevId + ") key=" + State_key);
                        Graphical_List list = new Graphical_List(Tracer, activity,
                                widgetSize, session_type, id, zone, params, aListFeature, widgetHandler);
                        list.setLayoutParams(layout_param);
                        Graphical_List.container = tmpPan;
                        tmpPan.addView(list);
                        Tracer.i(mytag, "   ==> Graphical_List");
                    } else {
                        if (!aListFeature.getParameters().contains("command_type")) {
                            Tracer.d(mytag, "add Graphical_Info for " + label + " (" + DevId + ") key=" + State_key);
                            Graphical_History history = new Graphical_History(Tracer, activity,
                                    widgetSize, session_type, id, zone, params, aListFeature, widgetHandler);
                            history.setLayoutParams(layout_param);
                            Graphical_Info.container = tmpPan;
                            tmpPan.addView(history);
                            Tracer.i(mytag, "   ==> Graphical_Info + Graphic");
                        } else {
                            Tracer.d(mytag, "add Graphical_Color for " + label + " (" + DevId + ") key=" + State_key);
                            Graphical_Color color = new Graphical_Color(Tracer, activity,
                                    widgetSize, session_type, id, zone, params, aListFeature, widgetHandler);
                            Graphical_Color.container = tmpPan;
                            tmpPan.addView(color);
                            Tracer.i(mytag, "   ==> Graphical_Color");
                        }

                    }
                } else if (Value_type.equals("string") || Value_type.equals("datetime")) {
                    //TODO for #117 handle parent dt_datetime in a proper way
                    if (aListFeature.getDevice_feature_model_id().contains("camera")) {
                        Graphical_Cam cam = new Graphical_Cam(Tracer, activity,
                                widgetSize, session_type, id, zone, params, aListFeature, widgetHandler);
                        Graphical_Cam.container = tmpPan;
                        tmpPan.addView(cam);
                        Tracer.i(mytag, "   ==> Graphical_Cam");
                    } else if (aListFeature.getParameters().contains("command_type")) {
                        if (aListFeature.getDevice_feature_model_id().startsWith("DT_ColorRGBHexa.")) {
                            Tracer.d(mytag, "add Graphical_Color for " + label + " (" + DevId + ") key=" + State_key);
                            Graphical_Color color = new Graphical_Color(Tracer, activity,
                                    widgetSize, session_type, id, zone, params, aListFeature, widgetHandler);
                            Graphical_Color.container = tmpPan;
                            tmpPan.addView(color);
                            Tracer.i(mytag, "   ==> Graphical_Color");
                        } else {
                            Graphical_Info_commands info_commands = new Graphical_Info_commands(Tracer, activity,
                                    widgetSize, session_type, id, zone, params, aListFeature, widgetHandler);
                            info_commands.setLayoutParams(layout_param);
                            Graphical_Info_commands.container = tmpPan;
                            tmpPan.addView(info_commands);
                            Tracer.i(mytag, "   ==> Graphical_Info_commands !!!");
                        }
                        //New widget for callerID apply to all other string sensor
                    } else if (aListFeature.getDevice_feature_model_id().startsWith("DT_CoordD")) {
                        Graphical_Openstreetmap Openstreetmap = new Graphical_Openstreetmap(Tracer, activity,
                                widgetSize, session_type, id, zone, params, aListFeature, widgetHandler);
                        Openstreetmap.setLayoutParams(layout_param);
                        Graphical_History.container = tmpPan;
                        tmpPan.addView(Openstreetmap);
                        Tracer.i(mytag, "   ==> Openstreetmap");
                    } else if (aListFeature.getDevice_feature_model_id().startsWith("DT_ColorRGBHexa")) {
                        if (!aListFeature.getParameters().contains("command_type")) {
                            Tracer.d(mytag, "add Graphical_Info for " + label + " (" + DevId + ") key=" + State_key);
                            Graphical_History history = new Graphical_History(Tracer, activity,
                                    widgetSize, session_type, id, zone, params, aListFeature, widgetHandler);
                            history.setLayoutParams(layout_param);
                            Graphical_Info.container = tmpPan;
                            tmpPan.addView(history);
                            Tracer.i(mytag, "   ==> Graphical_Info + Graphic");
                        } else {
                            Tracer.d(mytag, "add Graphical_Color for " + label + " (" + DevId + ") key=" + State_key);
                            Graphical_Color color = new Graphical_Color(Tracer, activity,
                                    widgetSize, session_type, id, zone, params, aListFeature, widgetHandler);
                            Graphical_Color.container = tmpPan;
                            tmpPan.addView(color);
                            Tracer.i(mytag, "   ==> Graphical_Color");
                        }
                    } else {
                        Tracer.d(mytag, "feature model id:" + aListFeature.getDevice_feature_model_id().toString());
                        Graphical_History info_with_history = new Graphical_History(Tracer, activity,
                                widgetSize, session_type, id, zone, params, aListFeature, widgetHandler);
                        info_with_history.setLayoutParams(layout_param);
                        Graphical_History.container = tmpPan;
                        tmpPan.addView(info_with_history);
                        Tracer.i(mytag, "   ==> Graphical_history");
                    }
                    //used by knx.HVACMode 	HVACMode 	actuator 	knx.HVACMode
                } else if (Value_type.equals("video")) {
                    if (!parameters.contains("command")) {
                        Graphical_Cam cam = new Graphical_Cam(Tracer, activity,
                                widgetSize, session_type, id, zone, params, aListFeature, widgetHandler);
                        Graphical_Cam.container = tmpPan;
                        tmpPan.addView(cam);
                        Tracer.i(mytag, "   ==> Graphical_Cam");
                    } else {
                        Graphical_Info_commands info_commands = new Graphical_Info_commands(Tracer, activity,
                                widgetSize, session_type, id, zone, params, aListFeature, widgetHandler);
                        info_commands.setLayoutParams(layout_param);
                        Graphical_Info_commands.container = tmpPan;
                        tmpPan.addView(info_commands);
                        Tracer.i(mytag, "   ==> Graphical_Info_commands !!!");
                    }
                } else if (aListFeature.getDevice_feature_model_id().startsWith("DT_HVACVent") || aListFeature.getDevice_feature_model_id().startsWith("DT_HVACFan")
                        || aListFeature.getDevice_feature_model_id().startsWith("DT_HVACMode") || aListFeature.getDevice_feature_model_id().startsWith("DT_HVACHeat")
                        || aListFeature.getDevice_feature_model_id().startsWith("DT_HeatingPilotWire") || aListFeature.getDevice_feature_model_id().startsWith("DT_DayOfWeek")
                        || aListFeature.getDevice_feature_model_id().startsWith("DT_UPSState") || aListFeature.getDevice_feature_model_id().startsWith("DT_UPSEvent")
                        || aListFeature.getDevice_feature_model_id().startsWith("DT_ColorCII")) {
                    Tracer.d(mytag, "add Graphical_List for " + label + " (" + DevId + ") key=" + State_key);
                    Graphical_List list = new Graphical_List(Tracer, activity,
                            widgetSize, session_type, id, zone, params, aListFeature, widgetHandler);
                    list.with_list = parameters.contains("command");
                    list.setLayoutParams(layout_param);
                    Graphical_List.container = tmpPan;
                    tmpPan.addView(list);
                    Tracer.i(mytag, "   ==> Graphical_List");
                } else {
                    Basic_Graphical_widget basic_widget = new Basic_Graphical_widget(params, activity, Tracer, id,
                            activity.getString(R.string.contact_devs), "", "",
                            widgetSize, 0, zone, mytag, null, widgetHandler);
                    basic_widget.setLayoutParams(layout_param);
                    tmpPan.addView(basic_widget);
                    Tracer.i(mytag, "   ==> Basic widget not handle by dev");

                }
                // todo add missing datatype from 0.4 see all datatype that have no parent but values.
                //String but carreful
                //datetime done
                //ColorCII
                //Char
                //DayOfWeek
                //HVACVent
                //HVACFan
                //HVACMode
                //HVACHeat
                //UPSEvent
                //UPSState
                //DT_Char

                if (columns) {
                    if (counter == 0) {
                        leftPan.addView(tmpPan);
                    } else if (counter == 1) {
                        rightPan.addView(tmpPan);
                    }
                    counter++;
                    if (counter == 2) counter = 0;
                } else ll.addView(tmpPan);
            } catch (Exception e) {
                Tracer.e(mytag, "Can not draw widget:" + e.toString());
                e.printStackTrace();
                Toast.makeText(activity, activity.getString(R.string.widget_error) + " : " + label, Toast.LENGTH_SHORT).show();
            }
        }
        return ll;
    }

    public LinearLayout loadAreaWidgets(Activity activity, LinearLayout ll, SharedPreferences params) {

        DomodroidDB domodb = new DomodroidDB(Tracer, activity, params);
        domodb.owner = "Widgets_Manager.loadAreaWidgets";
        Entity_Area[] listArea = domodb.requestArea();
        Tracer.d(mytag + " loadAreaWidgets", "Areas list size : " + listArea.length);
        //New list item that will contains list of area
        Activity_Main.listItem = new ArrayList<>();
        HashMap<String, String> map = new HashMap<>();

        LinearLayout.LayoutParams layout_param = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1.0f);
        LinearLayout mainPan = new LinearLayout(activity);
        mainPan.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout leftPan = new LinearLayout(activity);
        leftPan.setOrientation(LinearLayout.VERTICAL);
        leftPan.setLayoutParams(layout_param);
        LinearLayout rightPan = new LinearLayout(activity);
        rightPan.setOrientation(LinearLayout.VERTICAL);
        rightPan.setLayoutParams(layout_param);
        FrameLayout tmpPan = new FrameLayout(activity);

        int counter = 0;

        //check option and adapt columns in function
        columns = false;
        colonnes(activity, ll, mainPan, leftPan, rightPan, params);

        int size = listArea.length;
        Entity_Area area;

        try {
            for (Entity_Area aListArea : listArea) {
                area = aListArea;
                int Id = area.getId();
                String iconId = "unknown";
                try {
                    iconId = domodb.requestIcons(Id, "area").getValue();
                } catch (Exception e) {
                    Tracer.i(mytag, "No specific icon for this area");
                }
                tmpPan = null;
                tmpPan = new FrameLayout(activity);
                Tracer.d(mytag + " loadRoomWidgets", "Adding area : " + area.getName());
                String name = area.getName();
                name = Graphics_Manager.Names_Agent(activity, name);

                Graphical_Area graph_area = new Graphical_Area(params, Tracer, activity, Id, name, area.getDescription(), iconId, widgetSize, widgetHandler);

                //Fill List from value
                map = new HashMap<>();
                map.put("type", "area");
                map.put("name", name);
                map.put("id", String.valueOf(Id));
                map.put("icon", Integer.toString(Graphics_Manager.Icones_Agent(area.getIcon_name(), 0)));
                Activity_Main.listItem.add(map);

                tmpPan.addView(graph_area);
                if (columns) {
                    if (counter == 0) {
                        leftPan.addView(tmpPan);
                    } else if (counter == 1) {
                        rightPan.addView(tmpPan);
                    }
                    counter++;
                    if (counter == 2) counter = 0;
                } else ll.addView(tmpPan);
            }
        } catch (Exception e) {
            Tracer.e(mytag, "Can't load area: " + e.toString());

        }

        return ll;
    }

    public LinearLayout loadRoomWidgets(Activity activity, int id, LinearLayout ll, SharedPreferences params) {

        DomodroidDB domodb = new DomodroidDB(Tracer, activity, params);
        domodb.owner = "Widgets_Manager.loadRoomWidgets";
        Entity_Room[] listRoom = domodb.requestRoom(id);
        Tracer.d(mytag + " loadRoomWidgets", "Rooms list size : " + listRoom.length);

        //New list item that will contains list of area
        Activity_Main.listItem = new ArrayList<>();
        HashMap<String, String> map = new HashMap<>();
        map.put("type", "action");
        map.put("name", activity.getApplicationContext().getResources().getString(R.string.action_back));
        map.put("icon", Integer.toString(R.drawable.ic_action_undo));
        Activity_Main.listItem.add(map);

        LinearLayout.LayoutParams layout_param = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1.0f);
        LinearLayout mainPan = new LinearLayout(activity);
        mainPan.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout leftPan = new LinearLayout(activity);
        leftPan.setOrientation(LinearLayout.VERTICAL);
        leftPan.setLayoutParams(layout_param);
        LinearLayout rightPan = new LinearLayout(activity);
        rightPan.setOrientation(LinearLayout.VERTICAL);
        rightPan.setLayoutParams(layout_param);
        FrameLayout tmpPan = new FrameLayout(activity);
        int counter = 0;

        //check option and adapt columns in function
        columns = false;
        colonnes(activity, ll, mainPan, leftPan, rightPan, params);
        int size = listRoom.length;
        Entity_Room room;

        for (Entity_Room aListRoom : listRoom) {
            room = aListRoom;
            int room_id = room.getId();
            int area_id = room.getArea_id();
            String iconId = "unknown";
            try {
                iconId = domodb.requestIcons(room_id, "room").getValue();
            } catch (Exception e) {
                Tracer.i(mytag, "No specific icon for this room");
                //e.printStackTrace();
            }
            if (iconId.equals("unknown")) {
                //iconId="usage";
                iconId = room.getName();
            }
            tmpPan = null;
            tmpPan = new FrameLayout(activity);
            String ref = room.getDescription();
            if (ref.length() == 0)
                ref = room.getName();
            Tracer.d(mytag + " loadRoomWidgets", "Adding room : " + ref);
            String name = room.getName();
            name = Graphics_Manager.Names_Agent(activity, name);

            Graphical_Room graph_room = new Graphical_Room(params, Tracer, activity, area_id, room_id, name, room.getDescription(), iconId, widgetSize, widgetHandler);
            tmpPan.addView(graph_room);

            //Fill List from value
            map = new HashMap<>();
            map.put("type", "room");
            map.put("name", name);
            map.put("id", String.valueOf(room_id));
            map.put("icon", Integer.toString(Graphics_Manager.Icones_Agent(room.getIcon_name(), 0)));
            Activity_Main.listItem.add(map);

            if (columns) {
                if (counter == 0) {
                    leftPan.addView(tmpPan);
                } else if (counter == 1) {
                    rightPan.addView(tmpPan);
                }
                counter++;
                if (counter == 2) counter = 0;
            } else ll.addView(tmpPan);
        }

        return ll;
    }

    private void colonnes(Activity activity, LinearLayout ll, LinearLayout mainPan, LinearLayout leftPan, LinearLayout rightPan, SharedPreferences params) {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        boolean landscape;
        landscape = width > height;

        int maxSize = 700;
        if (width > maxSize) {
            if (landscape && !params.getBoolean("twocol_lanscape", false)) {
                Tracer.v(mytag, "params.getBoolean twocol_lanscape " + params.getBoolean("twocol_lanscape", false));

                columns = true;
                mainPan.addView(leftPan);
                mainPan.addView(rightPan);
                ll.addView(mainPan);
            } else if (!landscape && !params.getBoolean("twocol_portrait", false)) {
                Tracer.v(mytag, "params.getBoolean twocol_portrait " + params.getBoolean("twocol_portrait", false));

                columns = true;
                mainPan.addView(leftPan);
                mainPan.addView(rightPan);
                ll.addView(mainPan);
            }
        }

    }
}


