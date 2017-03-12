/*
 * This file is part of Domodroid.
 * 
 * Domodroid is Copyright (C) 2011 Pierre LAINE, Maxime CHOFARDET
 * 
 * Domodroid is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * Domodroid is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Domodroid. If not, see <http://www.gnu.org/licenses/>.
 */
package widgets;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.domogik.domodroid13.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import Abstract.common_method;
import Abstract.pref_utils;
import Entity.Entity_client;
import activities.Gradients_Manager;
import activities.Graphics_Manager;
import adapter.ArrayAdapterWithIcon;
import database.Cache_management;
import database.DmdContentProvider;
import database.DomodroidDB;
import misc.List_Icon_Adapter;
import misc.tracerengine;

public class Basic_Graphical_widget extends FrameLayout implements OnLongClickListener {

    final LinearLayout LL_background;
    final LinearLayout LL_infoPan;
    final LinearLayout LL_featurePan;
    final LinearLayout LL_topPan;
    private final ImageView IV_img;
    private final TextView TV_name;
    private final int id;
    final float api_version;
    public final pref_utils prefUtils;
    tracerengine Tracer = null;
    final Activity activity;
    private String icon;
    private final String place_type;
    private final int place_id;
    private final String mytag;
    final String name;
    private final String state_key;
    private int icon_status;
    private final Handler widgetHandler;
    private DomodroidDB domodb = null;
    public Entity_client session = null;
    public Typeface typefaceweather;
    public Typeface typefaceawesome;

    public Basic_Graphical_widget(Activity activity, tracerengine Trac, int id, String name, String state_key, String icon, int widgetSize, int place_id, String place_type, String mytag, FrameLayout container, Handler handler) {
        super(activity);
        this.Tracer = Trac;
        this.activity = activity;
        this.icon = icon;
        this.id = id;
        this.setPadding(5, 5, 5, 5);
        this.place_id = place_id;
        this.place_type = place_type;
        this.mytag = mytag;
        FrameLayout container1 = container;
        FrameLayout myself = this;
        this.name = name;
        this.state_key = state_key;

        prefUtils = new pref_utils();

        //global variable
        api_version = prefUtils.GetDomogikApiVersion();

        this.widgetHandler = handler;
        domodb = DomodroidDB.getInstance(this.Tracer, this.activity);

        setOnLongClickListener(this);

        //Fonts
        typefaceweather = Typeface.createFromAsset(activity.getAssets(), "fonts/weathericons-regular-webfont.ttf");
        typefaceawesome = Typeface.createFromAsset(activity.getAssets(), "fonts/fontawesome-webfont.ttf");

        //panel with border
        LL_background = new LinearLayout(activity);
        LL_background.setOrientation(LinearLayout.VERTICAL);
        if (widgetSize == 0)
            LL_background.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        else
            LL_background.setLayoutParams(new LayoutParams(widgetSize, LayoutParams.WRAP_CONTENT));
        LL_background.setBackgroundDrawable(Gradients_Manager.LoadDrawable("white", LL_background.getHeight()));

        //panel with border
        LL_topPan = new LinearLayout(activity);
        LL_topPan.setOrientation(LinearLayout.HORIZONTAL);
        LL_topPan.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        //panel to set icon with padding left
        FrameLayout FL_imgPan = new FrameLayout(activity);
        FL_imgPan.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
        FL_imgPan.setPadding(5, 8, 10, 10);

        //icon
        IV_img = new ImageView(activity);
        IV_img.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        IV_img.setBackgroundResource(Graphics_Manager.Icones_Agent(icon, 0));

        //info panel
        LL_infoPan = new LinearLayout(activity);
        LL_infoPan.setOrientation(LinearLayout.VERTICAL);
        LL_infoPan.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1));
        LL_infoPan.setGravity(Gravity.CENTER_VERTICAL);
        LL_infoPan.setPadding(0, 0, 10, 0);

        //feature panel
        LL_featurePan = new LinearLayout(activity);
        LL_featurePan.setOrientation(LinearLayout.VERTICAL);
        LL_featurePan.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1));
        LL_featurePan.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
        LL_featurePan.setPadding(0, 0, 20, 0);

        //name of widgets
        TV_name = new TextView(activity);
        TV_name.setText(name);
        TV_name.setTextSize(14);
        TV_name.setTextColor(Color.BLACK);
        TV_name.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

        LL_infoPan.addView(TV_name);
        FL_imgPan.addView(IV_img);

        LL_topPan.addView(FL_imgPan);
        LL_topPan.addView(LL_infoPan);
        LL_topPan.addView(LL_featurePan);

        LL_background.addView(LL_topPan);
        this.addView(LL_background);
    }

    public boolean onLongClick(View v) {
        final AlertDialog.Builder list_type_choice = new AlertDialog.Builder(getContext());
        final String[] String_list_action = new String[]{activity.getString(R.string.change_icon), activity.getString(R.string.rename),
                activity.getString(R.string.delete), activity.getString(R.string.move_up), activity.getString(R.string.move_down)};
        final Integer[] Integer_list_action_icon = new Integer[]{R.drawable.ic_rounded_corner_black, R.drawable.ic_description_black,
                R.drawable.ic_delete_black, R.drawable.ic_arrow_upward_black, R.drawable.ic_arrow_downward_black};
        ListAdapter adapter = new ArrayAdapterWithIcon(activity, String_list_action, Integer_list_action_icon);
        list_type_choice.setTitle(R.string.Widget_longclic_menu_title);
        list_type_choice.setAdapter(adapter, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        do_action(String_list_action[item]);
                        dialog.cancel();
                    }
                }
        );

        list_type_choice.show();
        return false;
    }

    @SuppressWarnings("Convert2Diamond")
    private void do_action(String action) {
        if (action.equals(activity.getString(R.string.rename))) {
            AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
            alert.setTitle(activity.getString(R.string.Rename_title) + " " + name + "-" + state_key);
            alert.setMessage(R.string.Rename_message);
            // Set an EditText view to get user input
            final EditText input = new EditText(getContext());
            alert.setView(input);
            alert.setPositiveButton(R.string.reloadOK, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog_customname, int whichButton) {
                    String result = input.getText().toString();
                    Tracer.get_engine().descUpdate(id, result, "feature");
                    // need to save table_feature to json but this method do not exists
                    //prefUtils.SetFeatureList( domodb.request_json_FeatureList().toString());
                    //prefUtils.save_params_to_file(Tracer, mytag, getContext());
                    TV_name.setText(result);
                    Snackbar.make(getRootView(), R.string.widget_renamed, Snackbar.LENGTH_LONG).show();
                }
            });
            alert.setNegativeButton(R.string.reloadNO, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog_customname, int whichButton) {
                    Tracer.v(mytag, "Customname Canceled.");
                }
            });
            alert.show();
        } else if (action.equals(activity.getString(R.string.delete))) {
            AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
            alert.setTitle(activity.getString(R.string.Delete_feature_title) + " " + name + "-" + state_key);
            alert.setMessage(R.string.Delete_feature_message);
            alert.setPositiveButton(R.string.reloadOK, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog_customname, int whichButton) {
                    Tracer.d(mytag, "deleting widget id= " + id + " place_id= " + place_id + " placetype= " + place_type);
                    Tracer.get_engine().remove_one_feature_association(id, place_id, place_type);
                    // #76
                    prefUtils.SetFeatureListAssociation(domodb.request_json_Features_association().toString());
                    prefUtils.save_params_to_file(Tracer, mytag, getContext());
                    //recheck cache element to remove those no more need.
                    Cache_management.checkcache(Tracer, activity);
                    common_method.refresh_the_views(widgetHandler);
                    Snackbar.make(getRootView(), R.string.widget_deleted, Snackbar.LENGTH_LONG).show();
                }
            });
            alert.setNegativeButton(R.string.reloadNO, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog_customname, int whichButton) {
                    Tracer.v(mytag, "delete Canceled.");
                }
            });
            alert.show();
        } else if (action.equals(activity.getString(R.string.change_icon))) {
            final AlertDialog.Builder list_icon_choice = new AlertDialog.Builder(getContext());
            List<String> list_icon = new ArrayList<>();
            String[] fiilliste;
            fiilliste = activity.getResources().getStringArray(R.array.icon_area_array);
            Collections.addAll(list_icon, fiilliste);
            final CharSequence[] char_list_icon = list_icon.toArray(new String[list_icon.size()]);
            list_icon_choice.setTitle(activity.getString(R.string.Wich_ICON_message) + " " + name + "-" + state_key);
            List_Icon_Adapter adapter = new List_Icon_Adapter(Tracer, getContext(), fiilliste, fiilliste);
            list_icon_choice.setAdapter(adapter, null);
            list_icon_choice.setSingleChoiceItems(char_list_icon, -1,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int item) {
                            ListView lw = ((AlertDialog) dialog).getListView();
                            Object checkedItem = lw.getAdapter().getItem(lw.getCheckedItemPosition());
                            icon = checkedItem.toString();
                            ContentValues values = new ContentValues();
                            //type = area, room, feature
                            values.put("name", "feature");
                            //icon is the name of the icon wich will be select
                            values.put("value", icon);
                            //reference is the id of the area, room, or feature
                            int reference = id;
                            values.put("reference", reference);
                            activity.getContentResolver().insert(DmdContentProvider.CONTENT_URI_UPDATE_ICON_NAME, values);
                            // #76
                            prefUtils.SetIconList(domodb.request_json_Icon().toString());
                            prefUtils.save_params_to_file(Tracer, mytag, getContext());
                            change_this_icon(icon_status);
                            dialog.cancel();
                            Snackbar.make(getRootView(), R.string.widget_icon_changed, Snackbar.LENGTH_LONG).show();
                        }
                    }
            );
            AlertDialog alert_list_icon = list_icon_choice.create();
            alert_list_icon.show();
        } else if (action.equals(activity.getString(R.string.move_down))) {
            Tracer.d(mytag, "moving down");
            Tracer.get_engine().move_one_feature_association(id, place_id, place_type, "down");
            prefUtils.SetFeatureListAssociation(domodb.request_json_Features_association().toString());
            // #76
            prefUtils.save_params_to_file(Tracer, mytag, getContext());
            common_method.refresh_the_views(widgetHandler);
            Snackbar.make(getRootView(), R.string.widget_moved_down, Snackbar.LENGTH_LONG).show();

        } else if (action.equals(activity.getString(R.string.move_up))) {
            Tracer.d(mytag, "moving up");
            Tracer.get_engine().move_one_feature_association(id, place_id, place_type, "up");
            // #76
            prefUtils.SetFeatureListAssociation(domodb.request_json_Features_association().toString());
            prefUtils.save_params_to_file(Tracer, mytag, getContext());
            common_method.refresh_the_views(widgetHandler);
            Snackbar.make(getRootView(), R.string.widget_moved_up, Snackbar.LENGTH_LONG).show();

        }
    }

    void change_this_icon(int icon_status) {
        set_this_icon_status(icon_status);
        IV_img.setBackgroundResource(Graphics_Manager.Icones_Agent(icon, icon_status));
    }

    private void set_this_icon_status(int icon_status) {
        this.icon_status = icon_status;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // View is now detached, and about to be destroyed
        try {
            Tracer.get_engine().unsubscribe(session);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

