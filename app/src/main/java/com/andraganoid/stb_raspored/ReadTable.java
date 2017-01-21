package com.andraganoid.stb_raspored;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class ReadTable extends AppCompatActivity {
    private ArrayList<File> fList = new ArrayList<>();
    ArrayList<File> excels = new ArrayList<>();
    File f;
    String[] root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_table);
        root = new String[]{getApplicationContext().getFilesDir().getAbsolutePath(), Environment.getExternalStorageDirectory().getAbsolutePath(), "/storage", "/storage/sdcard", "/storage/sdcard0", "/storage/sdcard1", "/storage/emulated/0", "/mnt", "/mnt/sdcard"};
        excels.clear();
        new fileFinder().execute();
    }

    void showfiles(ArrayList f) {
        fList.addAll(f);
        switch (fList.size()) {
            case 0:
                backToMain("Nema odgovarajuće tabele");
                break;
            case 1:
                readFileXLS(fList.get(0));
                break;
            default:
                Set<String> set = new HashSet<>();
                for (File fl : fList
                        ) {
                    set.add(fl.getName());
                }
                final ArrayList<String> estr = new ArrayList<>();

                for (String s : set) {
                    for (int i = 0; i < fList.size(); i++) {
                        if (s.equals(fList.get(i).getName())) {
                            estr.add(fList.get(i).getName());
                            excels.add(fList.get(i));
                            break;
                        }
                    }
                }
                ListView lv = (ListView) findViewById(R.id.excels);
                ArrayAdapter<String> aa = new ArrayAdapter<>(this, android.R.layout.simple_list_item_activated_1, estr);
                lv.setAdapter(aa);
                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        readFileXLS(excels.get(position));
                    }
                });
        }
    }

    private void readFileXLS(File file) {
        ArrayList<String> sList = new ArrayList<>();
        HashMap<String, String> peopleMap = new HashMap<>();
        try {
            FileInputStream myInput = new FileInputStream(file);
            POIFSFileSystem myFileSystem = new POIFSFileSystem(myInput);
            HSSFWorkbook myWorkBook = new HSSFWorkbook(myFileSystem);
            HSSFSheet sSheet = myWorkBook.getSheetAt(0);

            Iterator<Row> sRowIter = sSheet.rowIterator();
            while (sRowIter.hasNext()) {
                HSSFRow sRow = (HSSFRow) sRowIter.next();
                Iterator<Cell> sCellIter = sRow.cellIterator();

                while (sCellIter.hasNext()) {
                    HSSFCell sCell = (HSSFCell) sCellIter.next();
                    sList.add(sCell.toString());
                }
            }

            HSSFSheet pSheet = myWorkBook.getSheetAt(1);
            Iterator<Row> pRowIter = pSheet.rowIterator();
            while (pRowIter.hasNext()) {
                HSSFRow pRow = (HSSFRow) pRowIter.next();
                Iterator<Cell> pCellIter = pRow.cellIterator();

                while (pCellIter.hasNext()) {
                    HSSFCell p1Cell = (HSSFCell) pCellIter.next();
                    HSSFCell p2Cell = (HSSFCell) pCellIter.next();
                    peopleMap.put((p1Cell.toString()).trim(), (p2Cell.toString()).replaceAll("[^0-9]", ""));
                }
            }

            Intent i = new Intent(this, Send.class);
            i.putExtra("schedule", sList);
            i.putExtra("people", peopleMap);
            startActivity(i);
            finish();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void backToMain(String s) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
        startActivity(new Intent(this, MainActivity.class));
    }


    private class fileFinder extends AsyncTask<String, Integer, ArrayList> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ((ProgressBar) findViewById(R.id.readBar)).setVisibility(View.VISIBLE);
        }

        @Override
        protected ArrayList doInBackground(String... params) {
            fList.clear();
            for (String r : root) {
                f = new File(r);
                if (f.exists()) {
                    getFile(f);
                }
            }
            return fList;
        }

        private void getFile(File f) {
            File[] files = f.listFiles();
            try {
                for (File file : files) {
                    if (file.isFile() && file.getName().contains("stb_raspored")) {
                        fList.add(file);

                    } else if (file.isDirectory()) {
                        if (!file.isHidden()) {
                            getFile(file.getAbsoluteFile());
                        }
                    }
                }
            } catch (Exception e) {
            }
        }

        @Override
        protected void onPostExecute(ArrayList a) {
            super.onPostExecute(a);
            ((ProgressBar) findViewById(R.id.readBar)).setVisibility(View.GONE);
            showfiles(a);
        }
    }
}



