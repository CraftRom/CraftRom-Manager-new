package com.craftrom.manager.core.utils.interfaces

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckedTextView
import androidx.core.content.ContextCompat

class CustomDialogAdapter(
    context: Context,
    private val resource: Int,
    private val textViewResourceId: Int,
    objects: Array<CharSequence>,
    private val textColor: Int,
    private val selectedIndex: Int // Передаємо індекс обраного елемента
) : ArrayAdapter<CharSequence>(context, resource, textViewResourceId, objects) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(resource, parent, false)
        val checkedTextView = view.findViewById<CheckedTextView>(textViewResourceId)
        checkedTextView.text = getItem(position)
        checkedTextView.setTextColor(textColor)
        checkedTextView.isChecked = position == selectedIndex // Встановлюємо відмітку для обраного елемента
        return view
    }
}
