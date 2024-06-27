package com.craftrom.manager.core.utils.interfaces

import android.app.AlertDialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.preference.ListPreference
import com.craftrom.manager.R
import com.craftrom.manager.core.utils.theme.ThemePreferences
import com.craftrom.manager.core.utils.theme.ThemeType
import com.craftrom.manager.core.utils.theme.applyTheme

class CustomThemeListPreference : ListPreference {
    private val themePreferences: ThemePreferences by lazy {
        ThemePreferences(context)
    }

    private var dialogBackgroundDrawable: Drawable? = null
    private var dialogTitle: CharSequence? = null
    private var dialogCancelButton: CharSequence? = null
    private var dialogTitleColor: Int = 0 // Title text color
    private var dialogTextColor: Int = 0 // List item text color

    constructor(context: Context?) : super(context!!)
    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs) {
        setupAttributes(attrs)
    }

    private fun setupAttributes(attrs: AttributeSet?) {
        attrs?.let {
            val a = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.CustomThemeListPreference,
                0, 0
            )
            try {
                dialogBackgroundDrawable = a.getDrawable(R.styleable.CustomThemeListPreference_dialogBackground)
                dialogTitle = a.getString(R.styleable.CustomThemeListPreference_dialogTitle)
                dialogCancelButton = a.getString(R.styleable.CustomThemeListPreference_dialogCancelButton)
                dialogTitleColor = a.getColor(
                    R.styleable.CustomThemeListPreference_dialogTitleColor,
                    ContextCompat.getColor(context, android.R.color.black)
                )
                dialogTextColor = a.getColor(
                    R.styleable.CustomThemeListPreference_dialogTextColor,
                    ContextCompat.getColor(context, android.R.color.black)
                )
            } finally {
                a.recycle()
            }
        }
    }

    override fun onClick() {
        val currentSelectedValue = value
        val entries = entries
        val entryValues = entryValues

        val selectedIndex = findIndexOfValue(currentSelectedValue)
        var selectedThemeType = ThemeType.DEFAULT_MODE

        val adapter = object : ArrayAdapter<CharSequence>(
            context,
            android.R.layout.select_dialog_singlechoice,
            entries
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val textView = view.findViewById<TextView>(android.R.id.text1)

                // Add padding to the text view
                val horizontalPadding = context.resources.getDimensionPixelSize(R.dimen.dialog_item_padding_horizontal)
                val verticalPadding = context.resources.getDimensionPixelSize(R.dimen.dialog_item_padding_vertical)
                textView.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f) // Set text size in SP
                textView.setTextColor(dialogTextColor)
                return view
            }
        }


        val builder = AlertDialog.Builder(context)
            .setSingleChoiceItems(adapter, selectedIndex, null)
            .setNegativeButton(dialogCancelButton ?: context.getString(android.R.string.cancel)) { dialog, _ -> dialog.dismiss() }

// Create custom title view with specified text color and size
        val customTitleView = TextView(context).apply {
            text = dialogTitle ?: title
            setTextColor(dialogTitleColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f) // Set text size in SP
            val horizontalPadding = resources.getDimensionPixelSize(R.dimen.dialog_title_padding_horizontal)
            val verticalPadding = resources.getDimensionPixelSize(R.dimen.dialog_title_padding_vertical)
            setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)
        }

// Set custom title view in the dialog builder
        builder.setCustomTitle(customTitleView)



        val dialog = builder.create()

        // Customize dialog background if set
        dialog.window?.setBackgroundDrawable(dialogBackgroundDrawable ?: ColorDrawable(ContextCompat.getColor(context, android.R.color.white)))

        // Customize text color of dialog items
        dialog.listView?.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val selectedValue = entryValues[position].toString()
            if (callChangeListener(selectedValue)) {
                value = selectedValue
                selectedThemeType = when (selectedValue) {
                    "1" -> ThemeType.LIGHT_MODE
                    "2" -> ThemeType.DARK_MODE
                    else -> ThemeType.DEFAULT_MODE
                }
                dialog.dismiss()
            }
        }

        dialog.show()

        // Apply theme immediately on selection
        if (selectedThemeType != ThemeType.DEFAULT_MODE) {
            themePreferences.setSelectedTheme(selectedThemeType)
            applyTheme(selectedThemeType)
        }
    }
}