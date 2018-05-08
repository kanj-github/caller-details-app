package com.kanj.apps.callercontact

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import android.widget.Switch
import android.widget.TextView

class SettingsListAdapter(val mContext: Context, val names: Array<String>, var mask: Int)
    : ArrayAdapter<String>(mContext, R.layout.item_settings, names) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var viewHolder: SettingsViewHolder
        val row: View = if (convertView != null) {
            viewHolder = convertView.getTag() as SettingsViewHolder
            convertView
        } else {
            val inflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val inflatedRow = inflater.inflate(R.layout.item_settings, null)
            viewHolder = SettingsViewHolder(
                    inflatedRow.findViewById(R.id.tv),
                    inflatedRow.findViewById(R.id.sw),
                    position,
                    this
            )
            inflatedRow.setTag(viewHolder)
            inflatedRow
        }

        viewHolder.tv.setText(names[position])
        viewHolder.tv.setPaddingRelative(if (position > 6) 40 else 25, 0, 0, 0)
        viewHolder.pos = position
        viewHolder.aSwitch.setOnCheckedChangeListener(null)
        val i = 1.shl(position + 1)
        viewHolder.aSwitch.isChecked = mask and i == 0
        viewHolder.aSwitch.setOnCheckedChangeListener(viewHolder)
        return row
    }

    inner class SettingsViewHolder(val tv: TextView, val aSwitch: Switch, var pos: Int, val adapter: SettingsListAdapter)
        : CompoundButton.OnCheckedChangeListener {
        override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
            val mask = 1.shl(pos + 1)
            adapter.mask = if (isChecked) {
                adapter.mask or mask
            } else {
                adapter.mask and mask.inv()
            }
        }
    }
}