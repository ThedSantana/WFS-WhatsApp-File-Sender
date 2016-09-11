package retrospect.aditya.whatzappfilecourier;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
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
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Aditya on 06-02-2015.
 */
public class SendFragment extends Fragment {

    ProgressDialog pd;

    private List<FileProperties> item = null;
    private List<String> path = null;
    private String rootPath = "/";
    private TextView myPath, cardButton, cardButtonLolli;
    private ListView listView;

    FloatingActionButton fabVault, fabHelp, fabShare, btnOpenExplorer, fabSettings;
    FloatingActionsMenu fam;

    View v;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_send, container, false);

        boolean check = new File(Environment.getExternalStorageDirectory() + "/WhatsApp/WhatsAppCourier/Stub/asset.mp3").exists();
        if (!check) {
            try {
                copyFileFromAssets(getActivity(), "asset.mp3", Environment.getExternalStorageDirectory() + "/WhatsApp/WhatsAppCourier/Stub/asset.mp3");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


////////////////////////////============= Button Click Handles ================////////////////////////////

        fam = (FloatingActionsMenu) v.findViewById(R.id.fam);

        CardView cv = (CardView) v.findViewById(R.id.card_view);
        CardView cvLolli = (CardView) v.findViewById(R.id.card_view_Lolli);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cv.setVisibility(View.GONE);
            cardButtonLolli = (TextView) v.findViewById(R.id.select_file_cardbuttonLolli);
            cardButtonLolli.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("file*//**//*");
                    startActivityForResult(intent, 1);
                }
            });

        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            cvLolli.setVisibility(View.GONE);
            cardButton = (TextView) v.findViewById(R.id.select_file_cardbutton);
            cardButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("file*//**//*");
                    startActivityForResult(intent, 1);
                }
            });
        }


        /*btnOpenExplorer = (FloatingActionButton) v.findViewById(R.id.fab_openExplorer);
        btnOpenExplorer.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("file*//**//*");
                startActivityForResult(intent, 1);
            }
        });*/


        fabSettings = (FloatingActionButton) v.findViewById(R.id.fab_settings);
        fabSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Coming soon!", Toast.LENGTH_SHORT).show();
                fam.collapse();
            }
        });

        fabVault = (FloatingActionButton) v.findViewById(R.id.fab_vault);
        fabVault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Coming Soon!", Toast.LENGTH_SHORT).show();
                fam.collapse();
                /*Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://details?id=com.gameloft.android.ANMP.GloftBPHM.ML"));
                startActivity(intent);*/
            }
        });

        fabHelp = (FloatingActionButton) v.findViewById(R.id.fab_button_help);
        fabHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fam.collapse();
                startActivity(new Intent(getActivity(), HelpActivity.class));
            }
        });

        fabShare = (FloatingActionButton) v.findViewById(R.id.fab_button_share);
        fabShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fam.collapse();
                Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
                whatsappIntent.setType("text/plain");
                whatsappIntent.putExtra(Intent.EXTRA_TEXT, "WhatzApp File Courier: https://play.google.com/store/apps/details?id=retrospect.aditya.whatzappfilecourierads");
                try {
                    getActivity().startActivity(Intent.createChooser(whatsappIntent, "Share 'WhatzApp File Courier' using..."));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(getActivity(), "No messaging apps available.", Toast.LENGTH_SHORT).show();
                }
            }
        });


/////////////////////////////// ======== Button Click Handles End ==========/////////////////////////////


        myPath = (TextView) v.findViewById(R.id.editTextCurrentPath);
        listView = (ListView) v.findViewById(R.id.listView);
        String sdRootDir = Environment.getExternalStorageDirectory().toString();
        getDir(sdRootDir);

        return v;
    }


    ////////////////////////////// ======== FileExplorer Open Here ========== ////////////////////////////////
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == -1 && data != null) {
            String path = null;
            switch (requestCode) {
                case 1:
                    if (resultCode == -1) {
                        Uri uri = data.getData();
                        try {
                            path = getPath(getActivity(), uri);
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }

            File file = new File(path);
            if (path != null) {
                long lengthBytes = file.length();
                long lengthMB = file.length() / 1024 / 1024;
                if (file.length() > 500 * 1024 * 1024) {
                    Toast.makeText(getActivity(), "    Selected file size: " + lengthMB + "MB\n\n" + "File sending limit: 500 MB!", Toast.LENGTH_LONG).show();
                } else {
                    new MyAsyncTask().execute(file.getAbsolutePath());
                }
            }
        }
    }

    public static String getPath(Context context, Uri uri)
            throws URISyntaxException {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {"_data"};
            Cursor cursor = null;

            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
                cursor.close();
            } catch (Exception e) {
                // Eat it
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    ///////////////////////////////////----------- Get Directory---------- /////////////////////////////////
    private void getDir(String dirPath) {

        myPath.setText("Current Path: " + dirPath);
        item = new ArrayList<>();
        path = new ArrayList<>();
        File f = new File(dirPath);
        File[] files = f.listFiles();

        if (!dirPath.equals(rootPath)) {
            //item.add(new FileProperties("./", "", "", false));
            //path.add(rootPath);
            item.add(new FileProperties("../", "", "", false, 0, "01-01-2000"));
            path.add(f.getParent());
        }

        for (File file : files) {
            path.add(file.getPath());
            file.lastModified();
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");
            String lasModified = sdf.format(file.lastModified());

            if (file.isDirectory()) {
                //item.add(file.getName() + "/");
                File[] filesList = file.listFiles();

                if (filesList != null && filesList.length > 0) {
                    item.add(new FileProperties(file.getName() + "/", "", String.valueOf(file.length()), false, filesList.length, lasModified));
                } else {
                    item.add(new FileProperties(file.getName() + "/", "", String.valueOf(file.length()), false, 0, lasModified));
                }
            } else
                //item.add(file.getName());
                item.add(new FileProperties(file.getName(), FilenameUtils.getExtension(file.toString()), String.valueOf(file.length()), true, 0, lasModified));
        }


        Collections.sort(path, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                return lhs.compareToIgnoreCase(rhs);
            }
        });

        CustomAdapter adapter = new CustomAdapter(getActivity(), item);
        adapter.sort(new Comparator<FileProperties>() {
            @Override
            public int compare(FileProperties lhs, FileProperties rhs) {
                return lhs.getName().compareToIgnoreCase(rhs.getName());
            }
        });
        listView.setAdapter(adapter);

        //listView.setAdapter(new ArrayAdapter<>(this, R.layout.row, item));
        final File[] file = new File[1];
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                file[0] = new File(path.get(position));
                if (file[0].isDirectory()) {
                    if (file[0].canRead())
                        getDir(path.get(position));
                    else {
                        new AlertDialog.Builder(getActivity())
                                .setTitle("[" + file[0].getName() + "] folder can't be read!")
                                .setPositiveButton("OK",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        }).show();
                    }

                } else

                {
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Send this file?")
                            .setPositiveButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            long lengthBytes = file[0].length();
                                            long lengthMB = file[0].length() / 1024 / 1024;
                                            if (file[0].length() > 500 * 1024 * 1024) {
                                                Toast.makeText(getActivity(), "    Selected file size: " + lengthMB + "MB\n\n" + "File sending limit: 500 MB!", Toast.LENGTH_LONG).show();
                                            } else {
                                                new MyAsyncTask().execute(file[0].getAbsolutePath());
                                            }
                                        }
                                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(getActivity(), "Canceled", Toast.LENGTH_SHORT).show();
                        }
                    }).show();
                }
            }
        });

    }

    class CustomAdapter extends ArrayAdapter<FileProperties> {

        public CustomAdapter(Context context, List<FileProperties> objects) {
            super(context, 0, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.send_row_item, parent, false);
            }

            FileProperties fd = getItem(position);

            TextView name = (TextView) convertView.findViewById(R.id.row_fileNameTV);
            TextView lastMod = (TextView) convertView.findViewById(R.id.row_LastModTV);
            TextView numFiles = (TextView) convertView.findViewById(R.id.row_numFilesSizeFileTV);
            ImageView icon = (ImageView) convertView.findViewById(R.id.imageView);

            name.setText(fd.getName());
            lastMod.setText("Last Modified: " + fd.getLastModified());
            String extension = fd.getExtension();

            if (fd.getIsFile()) {
                numFiles.setText("Size: " + String.valueOf(Long.parseLong(fd.getSize()) / 1024) + " KB");
                if (extension.equalsIgnoreCase("mp3") || extension.equalsIgnoreCase("aac") || extension.equalsIgnoreCase("wav")) {
                    icon.setImageDrawable(getResources().getDrawable(R.drawable.audio));
                } else if (extension.equalsIgnoreCase("avi") || extension.equalsIgnoreCase("mp4") || extension.equalsIgnoreCase("flv") || extension.equalsIgnoreCase("mpg")) {
                    icon.setImageDrawable(getResources().getDrawable(R.drawable.video));
                } else if (extension.equalsIgnoreCase("jpg") || extension.equalsIgnoreCase("png") || extension.equalsIgnoreCase("gif")) {
                    icon.setImageDrawable(getResources().getDrawable(R.drawable.photo));
                } else if (extension.equalsIgnoreCase("doc") || extension.equalsIgnoreCase("ppt") || extension.equalsIgnoreCase("txt") || extension.equalsIgnoreCase("docx")) {
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

            } else {
                numFiles.setText("Contains: " + fd.getnumFilesInFolder() + " files");
                icon.setImageDrawable(getResources().getDrawable(R.drawable.folder));
            }

            if (fd.getLastModified().equals("01-01-2000")) {
                icon.setImageDrawable(getResources().getDrawable(R.drawable.back));
            }

            return convertView;
        }
    }

    class MyAsyncTask extends AsyncTask<String, String, ArrayList<String>> {

        public MyAsyncTask() {
            super();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(getActivity());
            pd.setMessage("Preparing your file!");
            pd.setTitle("Please Wait...");
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected ArrayList<String> doInBackground(String... fileToSendPath) {
            File file = new File(fileToSendPath[0]);
            if (file.length() > 1024 * 1024 * 15) {
                return multiPrepareForCourier(fileToSendPath[0]);
            } else {
                return prepareForCourier(fileToSendPath[0]);
            }
        }


        @Override
        protected void onPostExecute(ArrayList<String> resultPath) {
            super.onPostExecute(resultPath);
            pd.dismiss();
            if (resultPath.size() > 1) {
                final Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND_MULTIPLE);
                shareIntent.setType("audio/mp3");
                ArrayList<Uri> files = new ArrayList<>();
                for (String path : resultPath) {
                    File file = new File(path);
                    Uri uri = Uri.fromFile(file);
                    files.add(uri);
                }
                shareIntent.putExtra(android.content.Intent.EXTRA_STREAM, files);
                shareIntent.setPackage("com.whatsapp");
                startActivity(Intent.createChooser(shareIntent, "Aditya"));
            } else {
                final Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
                shareIntent.setType("audio/mp3");
                shareIntent.putExtra(android.content.Intent.EXTRA_STREAM, Uri.parse(resultPath.get(0)));
                shareIntent.setPackage("com.whatsapp");
                startActivity(Intent.createChooser(shareIntent, "Aditya"));
            }

        }
    }


    //////////////// Returns: String... Mp3 file embedded with data to be sent /////////////////
    public ArrayList<String> prepareForCourier(String fileToSendPath) {

        final String assetMp3 = Environment.getExternalStorageDirectory() + "/WhatsApp/WhatsAppCourier/Stub/asset.mp3";
        final String stubPath = Environment.getExternalStorageDirectory() + "/WhatsApp/WhatsAppCourier/Stub/";

        boolean check = new File(assetMp3).exists();
        if (!check) {
            try {
                new File(stubPath).mkdirs();
                copyFileFromAssets(getActivity(), "asset.mp3", assetMp3);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return MultiSplitUtils.makeSingleDataPlusMP3(fileToSendPath);

    }


    public ArrayList<String> multiPrepareForCourier(String fileToSendPath) {

        final String stubPath = Environment.getExternalStorageDirectory() + "/WhatsApp/WhatsAppCourier/Stub/";
        final String fileReadyToCourier = Environment.getExternalStorageDirectory() + "/WhatsApp/WhatsAppCourier/Temp/Splits/";
        final String assetMp3 = Environment.getExternalStorageDirectory() + "/WhatsApp/WhatsAppCourier/Stub/asset.mp3";

        ArrayList<String> listReadyFiles = new ArrayList<>();

        boolean check = new File(assetMp3).exists();
        if (!check) {
            try {
                new File(stubPath).mkdirs();
                copyFileFromAssets(getActivity(), "asset.mp3", assetMp3);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            listReadyFiles = MultiSplitUtils.split(fileToSendPath, fileReadyToCourier);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return listReadyFiles;

    }


////////////// Conversion to OBjectOutputStream Starts, from 'to be sent' MP3 file ////////////////


    static public void copyFileFromAssets(Context context, String file, String dest) throws Exception {
        InputStream in = null;
        OutputStream fout = null;
        int count;

        try {
            in = context.getAssets().open(file);
            fout = new FileOutputStream(new File(dest));

            byte data[] = new byte[1024];
            while ((count = in.read(data, 0, 1024)) != -1) {
                fout.write(data, 0, count);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    //
                }
            }
            if (fout != null) {
                try {
                    fout.close();
                } catch (IOException e) {
                    //
                }
            }
        }
    }


}
