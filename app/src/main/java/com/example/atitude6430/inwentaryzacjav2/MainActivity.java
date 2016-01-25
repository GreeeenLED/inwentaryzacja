package com.example.atitude6430.inwentaryzacjav2;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.support.annotation.RequiresPermission;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Size;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.StreamCorruptedException;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IllegalFormatCodePointException;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

public class MainActivity extends AppCompatActivity implements Admin_login.CommunicationWithFragment,WarningDialog.NoticeDialogsListener {

    //DATAS FOR TESTING
    String[] description={"data1","woda","tabsy niebieskie","husteczki","data5"};
    String[] code={"1234","5900531001519","5909991102326","5901478033000","1289"};
    InvObjects[] objects= new InvObjects[5];
    boolean workState;//zeminna do sprawdzania czy juz uruchamiano app true-na początku ustwaiane, false- po wybraniu nowej lub wznowienia
    ///////////////
    EditText editQuantity;
    TextView licenceKey;
    EditText codeEntered;

    TextView description2;


    List<InvObjects> invElements = new ArrayList<InvObjects>();
    public void LoadData(){
        //wczytywanie danych z karty SD lub z pliku wewnętrzengo
        for (int i=0;i<5;i++){
            invElements.add(new InvObjects(description[i],code[i],0));
            //objects[i] = new InvObjects(description[i],code[i],666);
        }
        for (int i=0;i<5;i++){
            Log.d(">>>>", "des: " + invElements.get(i).getDescription() + " code: " + invElements.get(i).getCode());
        }
    }
    RelativeLayout main_layout;
    public void MainLayoutStatus(boolean state){
        //method to disable or enable main layout elements, used when login fragemnt shows
        for(int i=0;i<main_layout.getChildCount();i++){
            View child = main_layout.getChildAt(i);
            child.setEnabled(state);
        }
    }
    RelativeLayout fragmentLayout;
    public void LoginFragment(boolean state){
        //method starting login fragment
        MainLayoutStatus(false);
        //zobaczyć co z tego jest potrzebne!!!!
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Admin_login adminLoginFragment = new Admin_login();
        fragmentTransaction.add(R.id.loginContainer, adminLoginFragment);
        if (state){
            fragmentTransaction.commit();
            fragmentLayout.setVisibility(View.VISIBLE);
        }
        if (state==false){
            fragmentLayout.setVisibility(View.INVISIBLE);
            MainLayoutStatus(true);
        }
    }

    public void Clear(View view){
        cleraFields();
    }
    public void ToastMessage(String msg){
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
    }
    public void Delete(View view){
        Integer toDelete = FindElement();
        if (editQuantity.getText().toString().equals("")) {
            ToastMessage("Niew wprowadzono ilości");

        }else {
            int wynik = invElements.get(toDelete).getQuantity() - Integer.parseInt(editQuantity.getText().toString());
            Log.d("WYNIK ", "odejmowania " + wynik);
            if (wynik<=0){
                ToastMessage("Nie można usnąć elementu");
            }else{
                invElements.get(toDelete).DecreaseQuantity(Integer.parseInt(editQuantity.getText().toString()));
                ToastMessage("usunięto, pozostało: " +invElements.get(toDelete).getQuantity()+" elementów");
                cleraFields();
            }
        }
    }
    public void cleraFields(){
        editQuantity.getText().clear();
        codeEntered.getText().clear();
    }
    public Integer FindElement(){
        Log.d("size of", "list elements: " + invElements.size());
        for (int i=0;i<invElements.size();i++){
            if (codeEntered.getText().toString().equals(invElements.get(i).getCode())){
                Log.d("size of", "founded!!!: " + invElements.get(i).getDescription()+" : "+invElements.get(i).getQuantity());
                //Toast.makeText(this,">"+invElements.get(i).getDescription()+" : "+invElements.get(i).getQuantity(),Toast.LENGTH_LONG).show();
                return i;
            }
        }
        Toast.makeText(this, "Brak kodu w bazie", Toast.LENGTH_SHORT).show();
        return null;
    }
    public void Zatwierdz(View view){
        Integer element=FindElement();//finding element based on scanned code
            if (element==null){
                Log.d("nie", "ma takiego obiektu");
                //dodanie obiektu gdy nie bedzie w bazie111
            }else {
                Log.d("znaleziono", "obiekt: " + element); //przez to wywoluje sie find record
                if (editQuantity.getText().toString().equals(""))
                    ToastMessage("NIE WPROWADZONO ILOŚCI");
                else {
                    invElements.get(element).IncreaseQuantity(Integer.parseInt(editQuantity.getText().toString()));//(Integer.parseInt(editQuantity.getText().toString()));
                    Log.d("nowa", "wartosc code: " + invElements.get(element).getCode() + " quantity: " + invElements.get(element).getQuantity());
                    cleraFields();
                }
            }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //inicializing components
        main_layout = (RelativeLayout) findViewById(R.id.main_layout);
        fragmentLayout = (RelativeLayout)findViewById(R.id.loginContainer);
        editQuantity = (EditText) findViewById(R.id.editTextQuantity);

        workState=true;
        licenceKey = (TextView) findViewById(R.id.textViewKey);
        codeEntered = (EditText) findViewById(R.id.editTextCode);

        description2 = (TextView) findViewById(R.id.textViewOpis);

        //LoadData();// testowe obiekt
        //ReadSD();
        GenreteKey();
        GenerateLicence();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.meniu_layout, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void ShowWarning(){
        //worning shown when sombede would like to start new job durring present job
        DialogFragment newFragment = new WarningDialog ();
        newFragment.show(getFragmentManager(), "Warrning");
    }
    public void ClearList(){
        invElements.clear();
    }
    public void SaveCopy(){
        SharedPreferences sharedCopy = getApplication().getSharedPreferences(getString(R.string.WorkCopy), Context.MODE_PRIVATE);
        SharedPreferences.Editor editorCopy = sharedCopy.edit();
        editorCopy.clear();
        for (int i=0;i<invElements.size();i++){
            editorCopy.putInt(invElements.get(i).code,invElements.get(i).getQuantity());
        }
        editorCopy.commit();
    }
    public void LoadCopy(){
        SharedPreferences sharedCopy = getApplication().getSharedPreferences(getString(R.string.WorkCopy), Context.MODE_PRIVATE);
        StringBuilder stringBuilder = new StringBuilder();
        Map<String,Integer> copies = (Map<String, Integer>) sharedCopy.getAll();
        for (Map.Entry<String,?> entry : copies.entrySet()){
            invElements.add(new InvObjects("default",entry.getKey(), (Integer) entry.getValue()));
            stringBuilder.append("code: "+entry.getKey()+" quantity: "+entry.getValue()+"\n");
        }
        description2.setText(stringBuilder);
    }
    //
    boolean selectedOption;//od tego zależy która metoda zostanie wybrana w worningach!!!
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.exit:
                SaveCopy();
                WriteSD();
                ClearList();
                finish();
                break;
            case R.id.newWork://nowa praca
                if (!workState){
                    selectedOption = false;
                    ShowWarning();
                }else {
                    ClearList();
                    ReadSD();
                    workState = false;
                }
                break;
            case R.id.reloadLast:
                if(!workState){
                    ShowWarning();
                    selectedOption = true;
                }else
                {
                    workState=false;
                    ClearList();
                    LoadCopy();
                }
                break;
            case R.id.saveResults:
                WriteSD();
                ClearList();
                break;
            case R.id.login:
                LoginFragment(true);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
//methods for licening==============================================================================
    double szyfr;
    public void GenerateLicence(){
        //method used to generate licence for device
        szyfr = Double.parseDouble(GenreteKey());
        Log.d("Klucz licencyjny", " wersja double to: " + String.format("%d",(long)szyfr));
        szyfr +=2500-25+34+966665;
        Log.d("Klucz zaszyfrowany", " wersja double to: " + String.format("%d", (long) szyfr));
        SharedPreferences sharedPreferences = this.getSharedPreferences(getString(R.string.LicencePref), Context.MODE_PRIVATE);
        if (String.format("%d", (long) szyfr).equals(sharedPreferences.getString(getString(R.string.Licence), ""))){
            LicenceFragment(false);
        }else{
            LicenceFragment(true);
            MainLayoutStatus(false);
            Toast.makeText(this,"Wprowadź klucz licencyjny",Toast.LENGTH_LONG).show();
        }
    }
    public String GenreteKey(){
        //method which generates key for user to get licence key
        String[] macParts = new String[6];
        WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);

        if(wifiManager.isWifiEnabled()) {
            // WIFI ALREADY ENABLED. GRAB THE MAC ADDRESS HERE
            WifiInfo info = wifiManager.getConnectionInfo();
            macParts = info.getMacAddress().split(":");
        } else {
            // ENABLE THE WIFI FIRST
            wifiManager.setWifiEnabled(true);
            // WIFI IS NOW ENABLED. GRAB THE MAC ADDRESS HERE
            WifiInfo info = wifiManager.getConnectionInfo();
            macParts = info.getMacAddress().split(":");
        }
        StringBuilder klucz = new StringBuilder();
        StringBuilder clientKey = new StringBuilder();
        int buffer=0;
        for (int j=0;j<6;j++){
            buffer = Integer.parseInt(macParts[j],16);
            clientKey.append(buffer);
        }

        Log.d("appended", "Key parts " + clientKey.toString());
        return clientKey.toString();
    }

    @Override
    public void LicenceFragment(boolean state) {
        if (state){
            //show licence fragment all block everything else
            Bundle bundle = new Bundle();
            bundle.putString("licence",String.format("%d", (long) szyfr));
            bundle.putString("key",GenreteKey());
            LicenceFragment licenceFragment = new LicenceFragment();
            licenceFragment.setArguments(bundle);

            Log.d("Licence","object Created");
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.loginContainer, licenceFragment);
            fragmentTransaction.commit();

            fragmentLayout.setVisibility(View.VISIBLE);
            Log.d("Licence", "fragment started");
        }
        if (state==false){
            Toast.makeText(this,"Dobra Licencja",Toast.LENGTH_LONG).show();
            MainLayoutStatus(true);
            fragmentLayout.setVisibility(View.INVISIBLE);
        }
    }
    File root1 = Environment.getExternalStorageDirectory();
    public void WriteSD() {
        List <String[]> NoweData = new ArrayList<String[]>();//do tego są dopisywane obiekty wszystkie
        for (int i=0;i<invElements.size();i++){
            NoweData.add(new String[]{invElements.get(i).getDescription(),invElements.get(i).getCode(),String.valueOf(invElements.get(i).getQuantity())});
        }
        File write = new File(root1,"wynik.csv");
        CSVWriter writer = null;
        try {
            writer = new CSVWriter(new OutputStreamWriter(new FileOutputStream(write)),';');
            writer.writeAll(NoweData);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        NoweData.clear();
    }
    public void ReadSD(){
        ClearList();//czyszczenie listy obiektow
        File read = new File(root1, "dane.csv");
        CSVReader reader = null;
        List<String[]>DodadneData = new ArrayList<String[]>();//tu są dopisywane tylko nowe obiekty
        String line[]={};
        try {
            reader = new CSVReader(new InputStreamReader(new FileInputStream(read)), ';');
            int i = 0;
            while (true) {
                line = reader.readNext();
                if (line != null) {
                    DodadneData.add(line);
                    invElements.add(new InvObjects(DodadneData.get(i)[0],DodadneData.get(i)[1],0));
                    i++;
                } else
                    break;
            }
        } catch (FileNotFoundException e) {
            ToastMessage("nie znaleziono pliku");
            e.printStackTrace();
        } catch (IOException e) {
            ToastMessage("błąd podczas wgrywania");
            e.printStackTrace();
        }
        ToastMessage("Wgrano listę elementów");
    }

    @Override
    public void onDialogOK(DialogFragment dialog) {
        ToastMessage("OK");
        if (selectedOption){
            ClearList();
            LoadCopy();
            ToastMessage("wgrano kopię");
        }else {
            ClearList();
            ReadSD();
        }
    }

    @Override
    public void onDialogCancel(DialogFragment dialog) {
        ToastMessage("Cancel");
    }
}