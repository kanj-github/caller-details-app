package com.kanj.apps.callercontact;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

/**
 * Created by kanj on 11/5/16.
 */
public class SettingsListAdapter extends ArrayAdapter<String> {
    public int mask;
    private String[] names;
    private Context mContext;

    public SettingsListAdapter(Context context, String[] names, int mask) {
        super(context, R.layout.item_settings, names);
        this.mask = mask;
        this.mContext = context;
        this.names = names;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SettingsViewHolder viewHolder = null;
        View row = convertView;
        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
            row = inflater.inflate(R.layout.item_settings, null);
            viewHolder = new SettingsViewHolder(
                    (TextView) row.findViewById(R.id.tv),
                    (Switch) row.findViewById(R.id.sw),
                    position,
                    this
            );
            row.setTag(viewHolder);
        }

        if (viewHolder == null) {
            viewHolder = (SettingsViewHolder) row.getTag();
        }
        viewHolder.tv.setText(names[position]);
        if (position > 6) {
            viewHolder.tv.setPaddingRelative(40, 0, 0, 0);
        } else {
            viewHolder.tv.setPaddingRelative(25, 0, 0, 0);
        }
        viewHolder.pos = position;
        viewHolder.aSwitch.setOnCheckedChangeListener(null);
        int i = 1;
        i<<=(position + 1);
        if ((mask & i) == 0x0) {
            viewHolder.aSwitch.setChecked(false);
        } else {
            viewHolder.aSwitch.setChecked(true);
        }
        viewHolder.aSwitch.setOnCheckedChangeListener(viewHolder);

        return row;
    }

    class SettingsViewHolder implements CompoundButton.OnCheckedChangeListener{
        public TextView tv;
        public Switch aSwitch;
        public int pos;
        private SettingsListAdapter adapter;

        public SettingsViewHolder(TextView tv, Switch aSwitch, int pos, SettingsListAdapter adapter) {
            this.tv = tv;
            this.aSwitch = aSwitch;
            this.pos = pos;
            this.adapter = adapter;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            int mask = 1;
            mask<<=pos + 1;
            if (isChecked) {
                adapter.mask |= mask;
            } else {
                adapter.mask &= (~mask);
            }
        }
    }
}
