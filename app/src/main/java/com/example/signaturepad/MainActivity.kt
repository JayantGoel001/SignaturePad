package com.example.signaturepad

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.github.gcacace.signaturepad.views.SignaturePad
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStreamWriter

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        verifyStoragePermissions()

        save.isEnabled=false
        clear.isEnabled=false
        signature_pad.setOnSignedListener(object :SignaturePad.OnSignedListener {
            override fun onStartSigning() {
                Toast.makeText(this@MainActivity,"Started Signing",Toast.LENGTH_SHORT).show()
            }
            override fun onClear() {
                save.isEnabled=false
                clear.isEnabled=false
            }
            override fun onSigned() {
                save.isEnabled=true
                clear.isEnabled=true
            }
        })
        clear.setOnClickListener {
            signature_pad.clear()
        }
        save.setOnClickListener {
            val signatureBitmap=signature_pad.signatureBitmap
            if(addJPGSignatureToGallery(signatureBitmap))
            {
                Toast.makeText(this,"Signature Saved into Gallery",Toast.LENGTH_SHORT).show()
            }
            else
            {
                Toast.makeText(this,"Unable to store the Signature_1",Toast.LENGTH_SHORT).show()
            }

            if(addSVGSignatureToGallery(signature_pad.signatureSvg))
            {
                Toast.makeText(this,"SVG signature saved into Gallery",Toast.LENGTH_SHORT).show()
            }
            else
            {
                Toast.makeText(this,"Unable To Store The Signature_2",Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun addSVGSignatureToGallery(signatureSvg: String): Boolean {
        var result=false
        try {
            val svgfile=File(getAlbumStorageDir("SignaturePad"), String.format("Signature_%d.svg",System.currentTimeMillis()))
            val stream=FileOutputStream(svgfile)
            val writer=OutputStreamWriter(stream)
            writer.write(signatureSvg)
            writer.close()
            stream.flush()
            stream.close()
            scanMediaFile(svgfile)
            result=true
        }catch (e:IOException)
        {
            e.printStackTrace()
            Log.i("KOTLIN",e.message!!)
        }
        return result
    }
    private fun addJPGSignatureToGallery(signatureBitmap: Bitmap): Boolean {
        var result=false
        try
        {
            val photo= File(getAlbumStorageDir("SignaturePad"), String.format("Signature_%d.jpg",System.currentTimeMillis()))
            saveBitmapToJPG(signatureBitmap,photo)
            scanMediaFile(photo)
            result=true
        }catch (e:IOException)
        {
            e.printStackTrace()
        }
        return result
    }
    private fun scanMediaFile(photo: File) {
        val intent= Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val contentURI= Uri.fromFile(photo)
        intent.data=contentURI
        this.sendBroadcast(intent)
    }
    private fun saveBitmapToJPG(signatureBitmap: Bitmap, photo: File) {
        val newBitmap=Bitmap.createBitmap(signatureBitmap.width,signatureBitmap.height,Bitmap.Config.ARGB_8888)
        val canvas= Canvas(newBitmap)
        canvas.drawColor(Color.WHITE)
        canvas.drawBitmap(signatureBitmap,0f,0f,null)
        val stream=FileOutputStream(photo)
        newBitmap.compress(Bitmap.CompressFormat.JPEG,80,stream)
    }
    private fun getAlbumStorageDir(s: String): File {
        val file=File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),s)
        if(!file.mkdirs())
        {
            Toast.makeText(this,"Directory not created",Toast.LENGTH_SHORT).show()
        }
        return file
    }
    fun verifyStoragePermissions()
    {
        if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),1234)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(requestCode==1234)
        {
            if(grantResults.isEmpty() || grantResults[0]!=PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this,"Cannot Write image to external directory",Toast.LENGTH_SHORT).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}
