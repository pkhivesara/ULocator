package com.pratik.ulocator;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseBooleanArray;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

import java.util.ArrayList;
import java.util.List;

public class contacts extends Activity implements OnItemClickListener {

    // variables definition
    static final String TAG = "contacts";
    private MyAdapter myAdapter;
    private Button doneButton;
    private String phoneNumber;
    private ListView listView;
    private Context context;
    private EditText editText;
    private SharedPreferences prefs;
    private ArrayList<String> searchResults;
    private List<String> contactName = new ArrayList<String>();
    public List<String> contactNameFiltered = new ArrayList<String>();
    private List<String> contactNumber = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // setting the content,initializing the variables,widgets

        super.onCreate(savedInstanceState);
        setContentView(R.layout.listview);
        searchResults = new ArrayList<String>(contactName);
        doneButton = (Button) findViewById(R.id.AddNumbers);
        listView = (ListView) findViewById(R.id.lists);
        editText = (EditText) findViewById(R.id.editText);
        listView.setFastScrollEnabled(true);
        myAdapter = new MyAdapter();
        listView.setAdapter(myAdapter);
        listView.setOnItemClickListener(this);
        listView.setItemsCanFocus(false);
        listView.setTextFilterEnabled(true);
        listView.setFastScrollEnabled(true);
        getAllContacts(this.getContentResolver());


        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                myAdapter.getFilter().filter(charSequence);

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        // button onClickListener implementation
        doneButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                // New StringBuilder object(name), appending checked
                // numbers/names in the listView to the object
                StringBuilder checkedcontacts = new StringBuilder();

                for (int i = 0; i < contactName.size(); i++)

                {
                    if (myAdapter.mCheckStates.get(i) == true) {
                        checkedcontacts.append(contactName.get(i).toString());

                        checkedcontacts.append("\n");

                    } else {

                    }

                }

                // New StringBuilder object(number), appending checked
                // numbers/names in the listView to the object
                StringBuilder checkedcontactsno = new StringBuilder();

                for (int i = 0; i < contactNumber.size(); i++)

                {
                    if (myAdapter.mCheckStates.get(i) == true) {
                        checkedcontactsno.append(contactNumber.get(i)
                                .toString());

                        checkedcontactsno.append(",");

                    } else {

                    }

                }

                // Intent firing
                Intent in = new Intent(contacts.this, TrackLogic.class);

                startActivity(in);

                String contact_name = checkedcontacts.toString();

                phoneNumber = checkedcontactsno.toString();

                // Using SharedPrefs to store user selected contacts for further
                // use
                prefs = getSharedPreferences("Prefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("contact_name", contact_name);

                editor.putString("phoneNumber", phoneNumber);

                editor.commit(); // Saving the Prefs
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menuhelp, menu);

        return true;
    }

    @Override
    // creating options menu
    public boolean onOptionsItemSelected(MenuItem item) {

        Intent intentHelp = new Intent(contacts.this, About.class);
        switch (item.getItemId()) {
            case R.id.item_help:
                startActivity(intentHelp);

                return true;
            default:
                return false;

        }
    }

    public int getSectionForPosition(int arg0) {
        return 0;
    }

    public Object[] getSections() {
        return null;
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        myAdapter.toggle(arg2);
    }

    // Method to pull out all contacts from user phone
    public void getAllContacts(ContentResolver contentResolver) {
        Cursor cursor = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null,
                null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                        + " COLLATE LOCALIZED ASC"
        );

        // Looping through the contacts and adding them to arrayList
        while (cursor.moveToNext()) {
            String name = cursor
                    .getString(cursor
                            .getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = cursor
                    .getString(cursor
                            .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            contactName.add(name);
            contactNameFiltered.add(name);
            contactNumber.add(phoneNumber);
        }

        cursor.close();
    }

    class MyAdapter extends BaseAdapter implements
            CompoundButton.OnCheckedChangeListener, Filterable {

        // variables definition

        private SparseBooleanArray mCheckStates;
        LayoutInflater mInflater;
        TextView phoneView, contactView;
        CheckBox contactboxView;
        private ItemFilter filter = new ItemFilter();

        MyAdapter() {
            mCheckStates = new SparseBooleanArray(contactName.size());

            // calling layout inflater service
            mInflater = (LayoutInflater) contacts.this
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return contactName.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {

            return 0;
        }

        @Override
        public View getView(final int position, View convertView,
                            ViewGroup parent) {

            // create a view placeholder called convertView
            View view = convertView;
            if (convertView == null)
                view = mInflater.inflate(R.layout.row, null);
            TextView contactView = (TextView) view
                    .findViewById(R.id.contact_name);
            phoneView = (TextView) view.findViewById(R.id.phone_number);
            contactboxView = (CheckBox) view.findViewById(R.id.checkBox_id);
            contactView.setText(contactName.get(position));
            //phoneView.setText(contactNumber.get(position));
            contactboxView.setTag(position);
            contactboxView.setChecked(mCheckStates.get(position, false));
            contactboxView.setOnCheckedChangeListener(this);

            return view;
        }

        public boolean isChecked(int position) {
            return mCheckStates.get(position, false);
        }

        public void setChecked(int position, boolean isChecked) {
            mCheckStates.put(position, isChecked);

            notifyDataSetChanged();
        }

        public void toggle(int position) {
            setChecked(position, !isChecked(position));
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            // TODO Auto-generated method stub

            mCheckStates.put((Integer) buttonView.getTag(), isChecked);
        }

        @Override
        public Filter getFilter() {
            // TODO Auto-generated method stub
            return filter;
        }


        private class ItemFilter extends Filter {

            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String filterString = charSequence.toString().toLowerCase();

                FilterResults results = new FilterResults();



                final List<String> list = contactNameFiltered;

                int count = list.size();
                final ArrayList<String> nlist = new ArrayList<String>(count);

                String filterableString;

                for (int i = 0; i < count; i++) {
                    filterableString = list.get(i);
                    if (filterableString.toLowerCase().contains(filterString)) {
                        nlist.add(filterableString);
                    }
                }

                results.values = nlist;
                results.count = nlist.size();

                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {

                contactName = (ArrayList<String>) filterResults.values;

                notifyDataSetChanged();
            }
        }
    }
}