package retrospect.aditya.whatzappfilecourierads;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Aditya on 06-02-2015.
 */
public class ExtractedFragment extends Fragment {

    private ListView extractedListView;
    private String extractedPath = Environment.getExternalStorageDirectory() + "/WhatsApp/WhatsAppCourier/Extracted/";
    private ArrayList<File> listFile;
    private ProgressDialog pd;
    FloatingActionButton btnFabRefresh;
    boolean buttonClicked = false;
    ExtractedArrayAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_extracted, container, false);

        extractedListView = (ListView) v.findViewById(R.id.listViewExtracted);

        btnFabRefresh = (FloatingActionButton) v.findViewById(R.id.fab_refresh_extracted);
        btnFabRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Refreshing!", Toast.LENGTH_SHORT).show();
                buttonClicked = true;
                new MyExtractedAsyncTask().execute();
            }
        });

        new MyExtractedAsyncTask().execute();

        extractedListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                new AlertDialog.Builder(getActivity()).setTitle("Open this file?").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            OpenFileByIntent.openFile(getActivity(), new File(listFile.get(position).getAbsolutePath()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getActivity(), "Open Canceled", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                }).show();
            }
        });

        extractedListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                new AlertDialog.Builder(getActivity()).setTitle("Delete this file?").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        File file = new File(String.valueOf(listFile.get(position)));
                        file.delete();
                        new MyExtractedAsyncTask().execute();
                        Toast.makeText(getActivity(), "File Deleted!", Toast.LENGTH_SHORT).show();
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getActivity(), "Delete Canceled", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                }).show();

                return true;
            }
        });


        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        new MyExtractedAsyncTask().execute();
    }

    class ExtractedArrayAdapter extends ArrayAdapter<File> {

        public ExtractedArrayAdapter(Context context, List<File> objects) {
            super(context, 0, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.extracted_row_item, parent, false);
            }

            File file = getItem(position);

            TextView name = (TextView) convertView.findViewById(R.id.textView_Row_Extracted_FileName);
            TextView size = (TextView) convertView.findViewById(R.id.textView_Row_Extracted_fileSizeTV);
            TextView modified = (TextView) convertView.findViewById(R.id.textView_Row_Extracted_LastModified);
            ImageView icon = (ImageView) convertView.findViewById(R.id.imageView_Row_Extracted_Icon);

            name.setText(file.getName());
            size.setText("Size: " + (int) (file.length()) / 1024 + " KB");

            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");
            modified.setText("Last Modified: " + sdf.format(file.lastModified()));

            String extension = FilenameUtils.getExtension(file.toString());

            if (extension.equalsIgnoreCase("mp3") || extension.equalsIgnoreCase("aac") || extension.equalsIgnoreCase("wav")) {
                icon.setImageDrawable(getResources().getDrawable(R.drawable.audio));
            } else if (extension.equalsIgnoreCase("avi") || extension.equalsIgnoreCase("mp4") || extension.equalsIgnoreCase("flv") || extension.equalsIgnoreCase("mpg")) {
                icon.setImageDrawable(getResources().getDrawable(R.drawable.video));
            } else if (extension.equalsIgnoreCase("jpg") || extension.equalsIgnoreCase("png") || extension.equalsIgnoreCase("gif") || extension.equalsIgnoreCase("bmp")) {
                icon.setImageDrawable(getResources().getDrawable(R.drawable.photo));
            } else if (extension.equalsIgnoreCase("doc") || extension.equalsIgnoreCase("ppt") || extension.equalsIgnoreCase("docx") || extension.equalsIgnoreCase("txt")) {
                icon.setImageDrawable(getResources().getDrawable(R.drawable.doc));
            } else if (extension.equalsIgnoreCase("pdf")) {
                icon.setImageDrawable(getResources().getDrawable(R.drawable.pdf));
            } else if (extension.equalsIgnoreCase("rar")) {
                icon.setImageDrawable(getResources().getDrawable(R.drawable.rar));
            } else if (extension.equalsIgnoreCase("zip")) {
                icon.setImageDrawable(getResources().getDrawable(R.drawable.zip));
            } else if (extension.equalsIgnoreCase("apk")) {
                icon.setImageDrawable(getResources().getDrawable(R.drawable.android));
            } else {
                icon.setImageDrawable(getResources().getDrawable(R.drawable.file));
            }

            return convertView;
        }

    }


    class MyExtractedAsyncTask extends AsyncTask<Void, Void, Void> {

        public MyExtractedAsyncTask() {
            super();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (buttonClicked) {
                pd = new ProgressDialog(getActivity());
                pd.setMessage("Loading list of files!");
                pd.setTitle("Please Wait...");
                pd.setCancelable(false);
                pd.show();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            File file = new File(extractedPath);
            File[] files = file.listFiles();

            listFile = new ArrayList<>();
            Collections.addAll(listFile, files);

            return null;
        }

        @Override
        protected void onPostExecute(Void resultPath) {
            super.onPostExecute(resultPath);
            if (!listFile.isEmpty()) {

            } else {
                if (buttonClicked) {
                    Toast.makeText(getActivity(), "No extracted file(s)!", Toast.LENGTH_SHORT).show();
                }
            }
            adapter = new ExtractedArrayAdapter(getActivity(), listFile);
            extractedListView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            extractedListView.invalidateViews();
            extractedListView.refreshDrawableState();
            if (buttonClicked) {
                if (pd != null) {
                    pd.dismiss();
                    buttonClicked = false;
                }
            }
        }
    }


}
