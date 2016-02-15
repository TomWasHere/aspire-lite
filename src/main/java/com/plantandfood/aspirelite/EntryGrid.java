/* Grid containing the entry widgets and a "+" button */

package com.plantandfood.aspirelite;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

class EntryGrid extends ElementGrid {

    /* The local EditTexts... */
    ArrayList<EditText> values;
    /* The local text changed listener */
    TextChangedListener textChangedListener;
    /* The scroll to listener */
    ScrollToListener scrollToListener;
    /* The number of boxes to have, at a minimum */
    int min_count = getResources().getInteger(R.integer.MIN_BRIX_READINGS);

    /* Initialisers */
    public EntryGrid(Context context) {
        super(context);
        onCreate();
    }
    public EntryGrid(Context context, AttributeSet attrs) {
        super(context, attrs);
        onCreate();
    }
    public EntryGrid(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        onCreate();
    }

    /* Bulk "on init" initialiser */
    private void onCreate() {
        /* Init the default variables, and create an "add" button */
        values = new ArrayList<>();

        /* Add the button */
        /* Inflate the button */
        LayoutInflater inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ImageButton plus = (ImageButton) inflater.inflate(R.layout.plus_button, this, false);
        /* Set some attributes */
        plus.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                /* Handle the click event; add another entry and request focus
                   for it.
                 */
                EditText entry = add("");
                entry.requestFocus();
                /* Call the text changed listener, since we do not currently handle
                   persisting.
                 */
                if (textChangedListener != null) {
                    textChangedListener.textChangedCallback();
                }
            }
        });
        plus.setContentDescription(context.getResources().getString(R.string.Plus));
        /* Add it to this */
        this.addView(plus);

        /* Load the old values */
        load();
    }

    /* Persist/resume code */
    private void persist() {
        /* Attempt to persist the entries */
        try {
            FileOutputStream file = context.openFileOutput(
                    getResources().getString(R.string.persist_brix_readings),
                    Context.MODE_PRIVATE);
            EntryGrid grid = (EntryGrid) findViewById(R.id.BrixEntryLayout);
            for (int i = 0; i < grid.size(); i++) {
                /* Persist this item */
                String value = grid.get(i).toString();
                for (int c = 0; c < value.length(); c++) {
                    int character = value.charAt(c);
                    if (character != '\0') {
                        /* We use \0 (a null byte) as a delimiter */
                        file.write(character);
                    }
                }
                file.write('\0');
            }
            file.close();
            Log.d("Aspire Lite", "Saved Brix readings");
        } catch (Exception e) {
            Log.e("Aspire Lite", "Error saving values; got " + e.toString());
        }
    }
    private void load() {
        /* Load the saved values */
        try {
            FileInputStream file = context.openFileInput(
                    getResources().getString(R.string.persist_brix_readings));
            int character;
            StringBuilder current = new StringBuilder();
            while ((character = file.read()) != -1) {
                if (character == '\0') {
                    /* Dump the current string */
                    add(current.toString());
                    current = new StringBuilder();
                } else {
                    current.append((char) character);
                }
            }
            file.close();
            Log.d("Aspire Lite", "Loaded Brix readings");
        } catch (Exception e) {
            Log.e("Aspire Lite", "Error loading brix readings; got " + e.toString());
        }
        /* Add any more missing boxes */
        for (int i = values.size(); i < min_count; i++) {
            add("");
        }
    }

    /* Internal 'add' function */
    private EditText add(String string) {
        /* Add another item */

        /* Inflate the XML for the entry widget */
        LayoutInflater inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        EditText entry = (EditText) inflater.inflate(R.layout.brix_entry,
                this, false);

        /* Set the string */
        if (string != null) {
            entry.setText(string);
        }

        /* Add the watcher for this item */
        entry.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (textChangedListener != null) {
                    textChangedListener.textChangedCallback();
                }
                persist();
            }
        });

        /* Add a handler so that the screens scrolls to this element once focused */
        entry.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(final View view, boolean hasFocus) {
                if (hasFocus) {
                    if (scrollToListener != null) {
                        scrollToListener.scrollTo(view);
                    }
                    /* Clear the hint */
                    ((EditText) view).setHint("");
                } else {
                    /* Set the hint */
                    ((EditText) view).setHint(R.string.BrixHint);
                }
            }
        });

        /* Add the value to the list */
        values.add(entry);
        this.addView(entry, this.getChildCount() - 1);

        /* Return the result */
        return entry;
    }

    /* Listeners */
    public void setTextChangedListener(TextChangedListener listener) {
        /* Set the local listener for an entry changing */
        this.textChangedListener = listener;
    }
    public void setScrollToListener(ScrollToListener listener) {
        /* Set the local listener for an entry changing */
        this.scrollToListener = listener;
    }

    public void reset() {
        /* Reset the fields */

        /* Remove the excess fields */
        while (values.size() > min_count) {
            EditText edit = values.remove(values.size() - 1);
            this.removeView(edit);
        }

        /* Clear the remaining */
        for (int i = 0; i < values.size(); i ++) {
            values.get(i).setText("");
        }

        /* Persist the current state */
        persist();

        /* Call the text changed callback */
        if (textChangedListener != null) {
            textChangedListener.textChangedCallback();
        }
    }

    public int size() {
        /* Return the size - the number of entries */
        return values.size();
    }

    public EntryItem get(int index) {
        /* Return an item representing the item at the given index */
        return new EntryItem(values.get(index));
    }
}
