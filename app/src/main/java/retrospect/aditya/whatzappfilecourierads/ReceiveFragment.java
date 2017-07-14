package retrospect.aditya.whatzappfilecourierads;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
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

import com.beaglebuddy.mp3.MP3;
import com.getbase.floatingactionbutton.FloatingActionButton;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Aditya on 06-02-2015.
 */
public class ReceiveFragment extends Fragment {

    private ArrayList<FileDetails> item;
    private List<String> path = null;

    private ListView listView;
    private ProgressDialog pd;

    String tempFileDir = Environment.getExternalStorageDirectory() + "/WhatsApp/WhatsAppCourier/Temp/";
    String extractionPath = Environment.getExternalStorageDirectory() + "/WhatsApp/WhatsAppCourier/Extracted/";
    String receivedAudioPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/WhatsApp/Media/WhatsApp Audio/";

    boolean buttonClicked = false;
    TextView exTractionPathTV;
    CustomAdapter adapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_receive, container, false);

        boolean check1 = new File(tempFileDir).exists();
        if (!check1) {
            try {
                new File(tempFileDir).mkdirs();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        FloatingActionButton refreshButton = (FloatingActionButton) v.findViewById(R.id.fab_refresh_receive);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonClicked = true;
                if (item.isEmpty()) {
                    Toast.makeText(getActivity(), "No new received file(s)!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Refreshing!", Toast.LENGTH_SHORT).show();
                }
                new PopulateListviewAsync().execute();
            }
        });

        exTractionPathTV = (TextView) v.findViewById(R.id.textViewExtractionPath);
        exTractionPathTV.setText("Extraction Path: " + extractionPath);


        listView = (ListView) v.findViewById(R.id.listView2);
        //String sdRootDir = Environment.getExternalStorageDirectory().toString();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                new AlertDialog.Builder(getActivity()).setTitle("Extract is file?").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new ExtractAsyncTask().execute(path.get(position));
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getActivity(), "Canceled", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                }).show();
            }
        });


        new PopulateListviewAsync().execute();

        return v;
    }

    private void getDir(String dirPath) {

        item = new ArrayList<>();
        path = new ArrayList<>();
        File f = new File(dirPath);

        File[] files = f.listFiles();

        for (File file : files) {
            if (file.isFile()) {
                if (FilenameUtils.getExtension(file.toString()).equals("mp3")) {
                    try {
                        MP3 mp3 = new MP3(file);
                        int partNumb = mp3.getRating(); // Gets each files' part number.
                        if (partNumb == 101) {
                            String fileName = mp3.getBand();
                            int fileSize = Integer.parseInt(mp3.getMusicBy());
                            String fileExtension = mp3.getLyricsBy();
                            int totalParts = Integer.parseInt(mp3.getPublisher());
                            String splitFileSize = mp3.getComments();

                            item.add(new FileDetails(fileName, splitFileSize, String.valueOf(fileSize), fileExtension, totalParts, partNumb));
                            path.add(file.getPath());
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    class CustomAdapter extends ArrayAdapter<FileDetails> {

        public CustomAdapter(Context context, List<FileDetails> objects) {
            super(context, 0, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.receive_row_item, parent, false);
            }

            FileDetails fd = getItem(position);

            TextView name = (TextView) convertView.findViewById(R.id.row_send_fileNameTV);
            TextView size = (TextView) convertView.findViewById(R.id.row_send_fileSizeTV);
            TextView numParts = (TextView) convertView.findViewById(R.id.row_send_numPartsTV);
            ImageView icon = (ImageView) convertView.findViewById(R.id.row_send_imageView);

            name.setText(fd.getFileName());
            size.setText("Size: " + (int) Float.parseFloat(fd.getTotalFileSize()) / 1024 + " KB");
            numParts.setText("Total Parts: " + fd.getTotalParts());
            String extension = fd.getExtension();

            if (extension.equalsIgnoreCase("mp3") || extension.equalsIgnoreCase("aac") || extension.equalsIgnoreCase("wav")) {
                icon.setImageDrawable(getResources().getDrawable(R.drawable.audio));
            } else if (extension.equalsIgnoreCase("avi") || extension.equalsIgnoreCase("mp4") || extension.equalsIgnoreCase("flv") || extension.equalsIgnoreCase("mpg")) {
                icon.setImageDrawable(getResources().getDrawable(R.drawable.video));
            } else if (extension.equalsIgnoreCase("jpg") || extension.equalsIgnoreCase("png") || extension.equalsIgnoreCase("gif")) {
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


    class PopulateListviewAsync extends AsyncTask<Void, Void, Void> {

        public PopulateListviewAsync() {
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
            getDir(receivedAudioPath);
            return null;
        }

        @Override
        protected void onPostExecute(Void resultPath) {
            super.onPostExecute(resultPath);
            if (item != null) {
                if (path != null) {
                    adapter = new CustomAdapter(getActivity(), item);
                    listView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    listView.invalidateViews();
                    listView.refreshDrawableState();
                }

            }
            if (buttonClicked) {
                if (pd != null) {
                    pd.dismiss();
                    buttonClicked = false;
                }
            }
        }
    }

    class ExtractAsyncTask extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(getActivity());
            pd.setMessage("Extracting file!");
            pd.setTitle("Please Wait...");
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected Void doInBackground(String... params) {

            FileOutputStream fos = null;
            RandomAccessFile raf = null;

            try {
                TreeMap<Integer, String> treeMap = new TreeMap<>();
                ArrayList<String> filePathsWillBeDeleted = new ArrayList<>();

                File clickedFile = new File(params[0]);
                File audioPathFile = new File(receivedAudioPath);
                File[] audioFolder = audioPathFile.listFiles();

                MP3 mp3 = new MP3(clickedFile);
                String clickedFilename = mp3.getBand();

                for (File currentFile : audioFolder) {
                    if (currentFile.isFile()) {
                        if (FilenameUtils.getExtension(currentFile.toString()).equals("mp3")) {
                            MP3 mp31 = new MP3(currentFile);
                            String loopFilename = mp31.getBand();
                            if (loopFilename.equals(clickedFilename)) {
                                MP3 mp3Loop = new MP3(currentFile);
                                treeMap.put(mp3Loop.getRating(), currentFile.getAbsolutePath());
                                filePathsWillBeDeleted.add(currentFile.getAbsolutePath());
                            }
                        }
                    }
                }

                if (treeMap.size() == Integer.parseInt(mp3.getPublisher())) {

                    boolean firstIteration = true;

                    for (Map.Entry<Integer, String> entry : treeMap.entrySet()) {
                        String accessMode = "rw";
                        if (firstIteration) {
                            accessMode = "r";
                        }
                        String loopFilePath = entry.getValue();
                        MP3 loopMp3 = new MP3(loopFilePath);
                        File loopFile = new File(loopFilePath);
                        int splitFileSize = Integer.parseInt(loopMp3.getComments()); // Each split's file size
                        int totalMP3PlusDataFileSize = (int) loopFile.length(); // Full file size
                        int startingByte = totalMP3PlusDataFileSize - splitFileSize; // Bytes to seek
                        File file1 = new File(extractionPath + loopMp3.getBand());
                        raf = new RandomAccessFile(loopFilePath, accessMode);
                        raf.seek(startingByte);
                        byte[] buffer = new byte[8192];
                        while ((raf.read(buffer, 0, buffer.length)) != -1) {
                            FileUtils.writeByteArrayToFile(file1, buffer, true);
                        }
                        firstIteration = false;
                    }

                    for (String s : filePathsWillBeDeleted) {
                        new File(s).delete();
                    }
                    path.clear();

                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getActivity(), "File extracted! Swipe right to \"Extracted\" tab and press \"Refresh\" button to see the file!", Toast.LENGTH_LONG).show();
                        }
                    });

                } else {
                    final int partsTotal = Integer.parseInt(mp3.getPublisher());
                    final int partNotAvail = partsTotal - treeMap.size();
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getActivity(), partNotAvail + " out of " + partsTotal + " parts not available. Please download them and try extracting again.", Toast.LENGTH_LONG).show();
                        }
                    });
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (raf != null) {
                    try {
                        raf.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (pd != null) {
                pd.dismiss();
            }
            new PopulateListviewAsync().execute();
        }
    }


}
