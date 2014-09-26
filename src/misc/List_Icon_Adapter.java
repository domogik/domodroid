package misc;

import org.domogik.domodroid13.R;

import activities.Graphics_Manager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class List_Icon_Adapter extends ArrayAdapter<String> {
  private final Context context;
  private final String[] values;

  public List_Icon_Adapter(Context context, String[] values) {
    super(context, R.layout.row_layout_list_icon, values);
    this.context = context;
    this.values = values;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    LayoutInflater inflater = (LayoutInflater) context
        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View rowView = inflater.inflate(R.layout.row_layout_list_icon, parent, false);
    TextView textView = (TextView) rowView.findViewById(R.id.label);
    ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
    textView.setText(values[position]);
    // change the icon for Windows and iPhone
    String s = values[position];
    imageView.setImageResource(Graphics_Manager.Icones_Agent(values[position], 0));
    return rowView;
  }
} 