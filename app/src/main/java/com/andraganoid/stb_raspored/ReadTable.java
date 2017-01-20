package com.andraganoid.stb_raspored;


import android.content.Intent;
import android.os.Bundle;
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public class ReadTable extends AppCompatActivity {
    private ArrayList<File> fList = new ArrayList<>();
    ArrayList<File> excels = new ArrayList<>();
    File f;
String []root={"/storage","/storage/sdcard","/sdcard","/storage/sdcard0","/storage/sdcard1","/storage/emulated/0","/mnt/sdcard"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_table);


        excels.clear();

        for (String r:root) {
            f = new File(r);
            Log.i("proba files", String.valueOf(f)+(f.exists()));
            if (f.exists()){ getFile(f);
            }
        }

     /*  f = new File("/storage");
        Log.i("proba files", String.valueOf(f)+(f.exists()));
        if (f.exists()){ fList.clear();
            excels.addAll(getFile(f));}

        f = new File("/storage/sdcard");
        Log.i("proba files", String.valueOf(f)+(f.exists()));
        if (f.exists()){ fList.clear();
            excels.addAll(getFile(f));}

        f = new File("/storage/sdcard0");
        Log.i("proba files", String.valueOf(f)+(f.exists()));
        if (f.exists()){ fList.clear();
            excels.addAll(getFile(f));}

        f = new File("/storage/sdcard1");
        Log.i("proba files", String.valueOf(f)+(f.exists()));
        if (f.exists()){ fList.clear();
            excels.addAll(getFile(f));}

        f = new File("/storage/emulated/0");
        Log.i("proba files", String.valueOf(f)+(f.exists()));
        if (f.exists()){ fList.clear();
            excels.addAll(getFile(f));}

        f = new File("/mnt/sdcard");                            //4.1
        Log.i("proba files", String.valueOf(f)+(f.exists()));
        if (f.exists()){ fList.clear();
            excels.addAll(getFile(f));}*/


   /*     f = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
        Log.i("proba files", String.valueOf(f)+(f.exists()));
        if (f.exists()){ fList.clear();
            excels.addAll(getFile(f));}*/

        //    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {f = new File("/storage/emulated/0"); }







        switch (excels.size()) {
            case 0:
                backToMain("Nema odgovarajuće tabele");
                break;
            case 1:
                readFileXLS(excels.get(0));
                break;
            default:
                 Set<String> set = new HashSet<>();
                for (File fl : excels
                        ) {
                    set.add(fl.getName());
                }
//                final ArrayList<String> estr= new ArrayList<>();
//                for (String s:set) {
//                    estr.add(s);
//                }
                List<String> estr =new ArrayList<> (set);



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


    private void getFile(File f) {
        Log.i("proba f", String.valueOf(f));

//        if (String.valueOf(f).equals("/storage/emulated")) {
//            f = (new File("/storage/emulated/0"));
   //     }
        File[] files = f.listFiles();

//        Log.i("proba f", String.valueOf(files.length));

try {
    for (File file : files) {
        if (file.isFile() && file.getName().contains("raspored")) {
            excels.add(file);

        } else if (file.isDirectory()) {
            if (!file.isHidden()) {
                getFile(file.getAbsoluteFile());
            }
        }
    }
}catch (Exception e){}
       // return fList;
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
