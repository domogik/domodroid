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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import android.content.Context;

import activities.Gradients_Manager;
import activities.Graphics_Manager;
import org.domogik.domodroid13.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import rinor.Rest_com;

import database.JSONParser;
import database.WidgetUpdate;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import misc.tracerengine;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class Graphical_List extends FrameLayout implements OnLongClickListener,OnClickListener {


	private FrameLayout imgPan;
	private LinearLayout background;
	private LinearLayout featurePan;
	private LinearLayout featurePan2;
	//private View		  featurePan2_buttons;
	private LinearLayout infoPan;
	private LinearLayout topPan;
	private ImageView img;
	private TextView nameDevices;
	private TextView value;
	private int dev_id;
	private int id;
	private Handler handler;
	private String state_key;
	private TextView state_key_view;
	//private Graphical_Info_View canvas;
	private int update;
	private Animation animation;
	private final Context context;
	private Message msg;
	private String wname;
	private String mytag="Graphical_List";
	private String url = null;
	public FrameLayout container = null;
	public static FrameLayout myself = null;
	public Boolean with_list = true;
	private tracerengine Tracer = null;
	private String parameters;
	private Entity_client session = null; 
	private Boolean realtime = false;
	private int session_type;
	private String[] known_values;
	private ArrayList<HashMap<String,String>> listItem;
	private Vector<String> list_usable_choices;
	private ListView listeChoices;
	private TextView cmd_to_send = null;
	private String cmd_requested = null;
	private String address;
	private String type;
	private String packageName ;
	private String place_type;
	private int place_id;
	private String login;
	private String password;
	private SharedPreferences params;
	
		
	@SuppressLint("HandlerLeak")
	public Graphical_List(tracerengine Trac,Activity context, int id,int dev_id, String name, 
			String type, String address,
			final String state_key, String url,String usage, int period, int update, 
			int widgetSize, int session_type, final String parameters,String model_id,int place_id,String place_type, SharedPreferences params) {
		super(context);
		this.Tracer = Trac;
		this.context = context;
		this.dev_id = dev_id;
		this.id = id;
		this.address = address;
		//this.type = type;
		this.state_key = state_key;
		this.update=update;
		this.wname = name;
		this.url = url;
		String[] model = model_id.split("\\.");
		this.type = model[0];
		this.place_id= place_id;
		this.place_type= place_type;
		this.params=params;
		packageName = context.getPackageName();
		Graphical_List.myself = this;
		this.session_type = session_type;
		this.parameters = parameters;
		setOnLongClickListener(this);
		setOnClickListener(this);
		
		mytag="Graphical_List ("+dev_id+")";
		this.setPadding(5, 5, 5, 5);
		login = params.getString("http_auth_username",null);
    	password = params.getString("http_auth_password",null);
    	
		//panel with border
		background = new LinearLayout(context);
		background.setOrientation(LinearLayout.VERTICAL);
		if(widgetSize==0)
			background.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
		else 
			background.setLayoutParams(new LayoutParams(widgetSize,LayoutParams.WRAP_CONTENT));
		
		background.setBackgroundDrawable(Gradients_Manager.LoadDrawable("white",background.getHeight()));

		//panel with border
		topPan = new LinearLayout(context);
		topPan.setOrientation(LinearLayout.HORIZONTAL);
		topPan.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));

		//panel to set img with padding left
		imgPan = new FrameLayout(context);
		imgPan.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.FILL_PARENT));
		imgPan.setPadding(5, 10, 5, 10);
		
		//img
		img = new ImageView(context);
		img.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT,Gravity.CENTER));
		Tracer.e(mytag, "Get icone for usage : "+usage);
		img.setBackgroundResource(Graphics_Manager.Icones_Agent(usage, 2));
		
		// info panel
		infoPan = new LinearLayout(context);
		infoPan.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT,1));
		infoPan.setOrientation(LinearLayout.VERTICAL);
		infoPan.setGravity(Gravity.CENTER_VERTICAL);
		
		//name of devices
		nameDevices=new TextView(context);
		nameDevices.setText(name);
		nameDevices.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
		nameDevices.setTextColor(Color.BLACK);
		nameDevices.setTextSize(14);
		
		//state key
		state_key_view = new TextView(context);
		state_key_view.setText(state_key);
		state_key_view.setTextColor(Color.parseColor("#333333"));

		//feature panel
		featurePan=new LinearLayout(context);
		featurePan.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT,1));
		featurePan.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
		featurePan.setPadding(10, 0, 10, 0);
		
		//value
		value = new TextView(context);
		value.setTextSize(28);
		value.setTextColor(Color.BLACK);
		animation = new AlphaAnimation(0.0f, 1.0f);
		animation.setDuration(1000);
		
		if(with_list) {
			//Exploit parameters
			JSONObject jparam = null;
			String command;
			JSONArray commandValues = null;
			try {
				jparam = new JSONObject(parameters.replaceAll("&quot;", "\""));
				command = jparam.getString("command");
				commandValues = jparam.getJSONArray("commandValues");
			} catch (Exception e) {
				command = "";
				commandValues = null;
			}
			int nbitems = 0;
			String list = "";
			if(commandValues != null) {
				nbitems = commandValues.length();
				if(nbitems > 0) {
					if(known_values != null)
						known_values = null;
					
					known_values = new String[nbitems];
					for(int i=0; i < nbitems; i++) {
						try {
							known_values[i] = commandValues.getString(i);
						} catch (Exception e) {
							known_values[i] = "???";
						}
						list+=known_values[i];
						list+=", ";
						
					}
				}
					
			}
			//list of choices
			listeChoices = new ListView(context);
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
			
			listeChoices.setLayoutParams(lp);
			
			listItem=new ArrayList<HashMap<String,String>>();
			list_usable_choices = new Vector<String>();
			for (int i=0;i<known_values.length;i++) {
				list_usable_choices.add(getStringResourceByName(known_values[i]));
				HashMap<String,String> map=new HashMap<String,String>();
					map.put("choice",getStringResourceByName( known_values[i]));
					map.put("cmd_to_send",known_values[i]);
					listItem.add(map);
				
			}
			

			SimpleAdapter adapter_map=new SimpleAdapter(getContext(),listItem,
					R.layout.item_choice,new String[] {"choice", "cmd_to_send"},new int[] {R.id.choice, R.id.cmd_to_send});
			listeChoices.setAdapter(adapter_map);
			listeChoices.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					if((position < listItem.size()) && (position > -1) ) {
						//process selected command
						HashMap<String,String> map=new HashMap<String,String>();
						map = listItem.get(position);
						cmd_requested = map.get("cmd_to_send");
						Tracer.d(mytag,"command selected at Position = "+position+"  Commande = "+cmd_requested);
						new CommandeThread().execute();
					}
				}
			});

			
			//feature panel 2 which will contain list of selectable choices
			featurePan2=new LinearLayout(context);
			featurePan2.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
			featurePan2.setGravity(Gravity.CENTER_VERTICAL);
			featurePan2.setPadding(5, 10, 5, 10);
			featurePan2.addView(listeChoices);
			
		}
		featurePan.addView(value);
		infoPan.addView(nameDevices);
		//infoPan.addView(state_key_view);
		imgPan.addView(img);

		topPan.addView(imgPan);
		topPan.addView(infoPan);
		topPan.addView(featurePan);
		background.addView(topPan);
		this.addView(background);
		
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				
				if(msg.what == 2) {
					Toast.makeText(getContext(), "Command Failed", Toast.LENGTH_SHORT).show();
					
				} else if(msg.what == 9999) {
				
					//Message from cache engine
					//state_engine send us a signal to notify value changed
					if(session == null)
						return;
					
					String loc_Value = session.getValue();
					Tracer.d(mytag,"Handler receives a new value <"+loc_Value+">" );
					value.setText(getStringResourceByName(loc_Value));
					
				} else if(msg.what == 9998) {
					// state_engine send us a signal to notify it'll die !
					Tracer.d(mytag,"cache engine disappeared ===> Harakiri !" );
					session = null;
					realtime = false;
					removeView(background);
					myself.setVisibility(GONE);
					if(container != null) {
						container.removeView(myself);
						container.recomputeViewAttributes(myself);
					}
					try { 
						finalize(); 
					} catch (Throwable t) {}	//kill the handler thread itself
				}
			}	
			
		};	//End of handler
		
		//================================================================================
		/*
		 * New mechanism to be notified by widgetupdate engine when our value is changed
		 * 
		 */
		WidgetUpdate cache_engine = WidgetUpdate.getInstance();
		if(cache_engine != null) {
			session = new Entity_client(dev_id, state_key, mytag, handler, session_type);
			if(tracerengine.get_engine().subscribe(session)) {
				realtime = true;		//we're connected to engine
										//each time our value change, the engine will call handler
				handler.sendEmptyMessage(9999);	//Force to consider current value in session
			}
			
		}
		//================================================================================
		//updateTimer();	//Don't use anymore cyclic refresh....	

	}
	public class CommandeThread extends AsyncTask<Void, Integer, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			if(cmd_requested != null) {
				String Url2send = url+"command/"+type+"/"+address+"/"+cmd_requested;
				Tracer.i(mytag,"Sending to Rinor : <"+Url2send+">");
				JSONObject json_Ack = null;
				try {
					json_Ack = Rest_com.connect(Url2send,login,password);
				} catch (Exception e) {
					Tracer.e(mytag, "Rinor exception sending command <"+e.getMessage()+">");
				}
				try {
					Boolean ack = JSONParser.Ack(json_Ack);
					if(ack==false){
						Tracer.i(mytag,"Received error from Rinor : <"+json_Ack.toString()+">");
						handler.sendEmptyMessage(2);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return null;
		}
	}

	
	private String getStringResourceByName(String stringName) {
	      String packageName = context.getPackageName();
	      String search =  stringName.toLowerCase();
	      int resId = 0;
	   
	      resId = getResources().getIdentifier(search, "string", packageName);
	      String result = "";
	      try {
	    	  result = context.getString(resId);
	      } catch (Exception e) {
	    	  result = stringName;
	      }
	      return result;
	    }
	
	
	public void onClick(View arg0, MotionEvent arg1) {
		if(with_list) {
			if(background.getHeight() != 350){
				try {
					background.removeView(featurePan2);
					
				} catch (Exception e) {}
				
				background.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,350));
				background.addView(featurePan2);
				
			}
			else{
				background.removeView(featurePan2);
				background.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
			}
		}
		return;
	}
	
	@Override
	protected void onWindowVisibilityChanged(int visibility) {
		if(visibility==0){
			
		}
	}
	
	public static float Round(float Rval, int Rpl) {
		float p = (float)Math.pow(10,Rpl);
		Rval = Rval * p;
		float tmp = Math.round(Rval);
		return (float)tmp/p;
	}


	public void onClick(View v) {
	}
	
	public boolean onLongClick(View v) {
		//TODO open a menu to ask what to do.
		//list type area,room, widget
		final AlertDialog.Builder list_type_choice = new AlertDialog.Builder(getContext());
		List<String> list_choice = new ArrayList<String>();
			list_choice.add("Rename");
			list_choice.add("Change_icon");
			list_choice.add("Delete");
		final CharSequence[] char_list =list_choice.toArray(new String[list_choice.size()]);
		//list_type_choice.setTitle(R.string.What_to_do_message);
		list_type_choice.setSingleChoiceItems(char_list, -1,
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					ListView lw = ((AlertDialog)dialog).getListView();
					Object checkedItem = lw.getAdapter().getItem(lw.getCheckedItemPosition());
					do_action(checkedItem.toString());
					dialog.cancel();
				}
			}
		);
	
		list_type_choice.show();
		return false;
	}

	private void do_action(String action) {
		if(action.equals("Rename")) {
			AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
			alert.setTitle(R.string.Rename_title);
			alert.setMessage(R.string.Rename_message);
			// Set an EditText view to get user input 
			final EditText input = new EditText(getContext());
				alert.setView(input);
				alert.setPositiveButton(R.string.reloadOK, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog_customname, int whichButton) {
						String result= input.getText().toString(); 
						Tracer.get_engine().descUpdate(id,result,"feature");
					}
				});
				alert.setNegativeButton(R.string.reloadNO, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog_customname, int whichButton) {
						Tracer.e(mytag, "Customname Canceled.");
					}
				});
				alert.show();
		}else if (action.equals("Delete")){
			AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
			alert.setTitle(R.string.Delete_feature_title);
			alert.setMessage(R.string.Delete_feature_message);
			alert.setPositiveButton(R.string.reloadOK, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog_customname, int whichButton) {
					Tracer.get_engine().remove_one_feature_association(id,place_id,place_type);
					if(container != null) {
						container.removeView(myself);
						container.recomputeViewAttributes(myself);
					}
				}
			});
			alert.setNegativeButton(R.string.reloadNO, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog_customname, int whichButton) {
					Tracer.e(mytag, "delete Canceled.");
				}
			});
			alert.show();
		}
	}

}



