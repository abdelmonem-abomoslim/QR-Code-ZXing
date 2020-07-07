package com.abdelmonem.qrcodezxing;

import androidx.appcompat.app.AppCompatActivity;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.abdelmonem.qrcodezxing.Model.QRURLModel;
import com.abdelmonem.qrcodezxing.Model.QRVCardModel;
import com.google.zxing.Result;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

public class MainActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler{

    private ZXingScannerView scannerView;
    private TextView txtResult;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Initialization
        scannerView = (ZXingScannerView) findViewById(R.id.zxscan);
        txtResult = (TextView) findViewById(R.id.txt_result);

        //Result permission
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        scannerView.setResultHandler(MainActivity.this);
                        scannerView.startCamera();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(MainActivity.this, "You Must Accept this permission", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                })
                .check();
    }

    @Override
    protected void onDestroy() {
        scannerView.stopCamera();
        super.onDestroy();
    }


    @Override
    public void handleResult(Result rawResult) {
        processRawResult(rawResult.getText());
        scannerView.startCamera();
    }

    private void processRawResult(String text) {
        if(text.startsWith("BEGIN:")){
            String[] tokens = text.split("\n");
            QRVCardModel qrvCardModel = new QRVCardModel();
            for (int i=0; i<tokens.length; i++){
                if(tokens[i].startsWith("BEGIN:")){
                    qrvCardModel.setType(tokens[i].substring("BEGIN:".length()));
                }else if(tokens[i].startsWith("N:")){
                    qrvCardModel.setName(tokens[i].substring("N:".length()));
                }else if(tokens[i].startsWith("ORG:")){
                    qrvCardModel.setOrg(tokens[i].substring("ORG:".length()));
                }else if(tokens[i].startsWith("TEL:")){
                    qrvCardModel.setTel(tokens[i].substring("TEL:".length()));
                }else if(tokens[i].startsWith("URL:")){
                    qrvCardModel.setUrl(tokens[i].substring("URL:".length()));
                }else if(tokens[i].startsWith("EMAIL:")){
                    qrvCardModel.setEmail(tokens[i].substring("EMAIL:".length()));
                }else if(tokens[i].startsWith("ADR:")){
                    qrvCardModel.setAddress(tokens[i].substring("ADR:".length()));
                }else if(tokens[i].startsWith("NOTE:")){
                    qrvCardModel.setNote(tokens[i].substring("NOTE:".length()));
                }else if(tokens[i].startsWith("SUMAMRY:")){
                    qrvCardModel.setSummary(tokens[i].substring("SUMMARY:".length()));
                }else if(tokens[i].startsWith("DTSTART:")){
                    qrvCardModel.setDtstart(tokens[i].substring("DTSTART:".length()));
                }else if(tokens[i].startsWith("DTEND:")){
                    qrvCardModel.setDtend(tokens[i].substring("DTEND:".length()));
                }

            }
            txtResult.setText(qrvCardModel.getType());
        }else if(text.startsWith("http://") ||
            text.startsWith("https://") ||
            text.startsWith("www.")){

            QRURLModel qrurlModel = new QRURLModel(text);
            txtResult.setText(qrurlModel.getUrl());
            try {
                Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(qrurlModel.getUrl()));
                startActivity(myIntent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, "No application can handle this request."
                        + " Please install a webbrowser",  Toast.LENGTH_LONG).show();
                e.printStackTrace();
                processRawResult(qrurlModel.getUrl());
            }
        }else{
            txtResult.setText(text);
        }

        scannerView.startCamera();
    }
}
