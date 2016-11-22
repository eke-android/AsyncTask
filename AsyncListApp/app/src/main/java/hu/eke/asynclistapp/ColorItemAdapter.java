package hu.eke.asynclistapp;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by szugyi on 15/11/15.
 */
public class ColorItemAdapter extends ArrayAdapter<ColorItem> {
    private static final String TAG = "AsyncListApp_List";
    private static final int LAYOUT = android.R.layout.simple_list_item_2;

    static class ViewHolder {
        public TextView color;
        public TextView value;
    }

    public ColorItemAdapter(Context context, List<ColorItem> items) {
        super(context, LAYOUT, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if (rowView == null) {
            rowView = View.inflate(getContext(), LAYOUT, null);

            ViewHolder viewHolder = new ViewHolder();
            viewHolder.color = (TextView) rowView.findViewById(android.R.id.text1);
            viewHolder.value = (TextView) rowView.findViewById(android.R.id.text2);
            rowView.setTag(viewHolder);
            Log.v(TAG, "List item created");
        }

        ViewHolder holder = (ViewHolder) rowView.getTag();
        ColorItem item = getItem(position);

        Log.v(TAG, String.format("Color: %s -> %s", holder.color.getText(), item.getColor()));
        Log.v(TAG, String.format("Value: %s -> %s", holder.value.getText(), item.getValue()));
        holder.color.setText(item.getColor());
        holder.color.setTextColor(Color.parseColor(item.getColor()));
        holder.value.setText(item.getValue());

        Log.v(TAG, "List item returned");
        return rowView;
    }
}
