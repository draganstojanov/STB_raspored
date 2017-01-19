package com.andraganoid.stb_raspored;


import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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
import java.util.Iterator;


public class ReadTable extends AppCompatActivity {
    private ArrayList<File> fList = new ArrayList<>();
    ArrayList<File> excels = new ArrayList<>();
    File f;


    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_table);


        excels.clear();

        // verzija >16 emulated



//  f = new File("/storage/emulated/0");

   f = new File("/storage/sdcard0");
        File fc = f;
        fList.clear();
        excels.addAll(getFile(f));
        Log.i("proba 1", String.valueOf(f));


        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            f = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
            if (!(f.equals(fc))) {
                fList.clear();


                excels.addAll(getFile(f));
                Log.i("proba 2", String.valueOf(f));
            }
        }


        switch (excels.size()) {
            case 0:
                backToMain("Nema odgovarajuće tabele");
                break;
            case 1:
                readFileXLS(excels.get(0));
                break;
            default:
                final ArrayList<String> estr = new ArrayList<>();
                for (File fl : excels
                        ) {
                    estr.add(fl.getName());
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
        //  } else {
        //       backToMain("Nema SD kartice u uređaju!");
        //    }

    }


    private ArrayList<File> getFile(File f) {
       File[] files = f.listFiles();
        Log.i("proba f", String.valueOf(files.length));
        for (File file : files) {
            if (file.isFile() && file.getName().contains("raspored")) {
                fList.add(file);

            } else if (file.isDirectory()) {
                if (!file.isHidden()) {
                    getFile(file.getAbsoluteFile());
                }
            }
        }

        return fList;
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

}
