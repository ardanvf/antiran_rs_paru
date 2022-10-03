package com.mazenrashed.example

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mazenrashed.example.Rest_Api.CreatePostResponse
import com.mazenrashed.example.Rest_Api.RetrofitClient
import com.mazenrashed.example.Rest_Api.noAntrianResponse
import com.mazenrashed.printooth.Printooth
import com.mazenrashed.printooth.data.printable.ImagePrintable
import com.mazenrashed.printooth.data.printable.Printable
import com.mazenrashed.printooth.data.printable.RawPrintable
import com.mazenrashed.printooth.data.printable.TextPrintable
import com.mazenrashed.printooth.data.printer.DefaultPrinter
import com.mazenrashed.printooth.ui.ScanningActivity
import com.mazenrashed.printooth.utilities.Printing
import com.mazenrashed.printooth.utilities.PrintingCallback
import kotlinx.android.synthetic.main.activity_main.*
import org.w3c.dom.Text
import retrofit2.Call
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import javax.security.auth.callback.Callback
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {

    private var printing : Printing? = null

    lateinit var txtBpjs: TextView
    lateinit var txtUmum: TextView
    var antrianBpjs = "A 1"
    var antrianUmum = "B 1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        txtBpjs = findViewById(R.id.textBpjs)
        txtUmum = findViewById(R.id.textUmum)


        if (Printooth.hasPairedPrinter())
            printing = Printooth.printer()
        initViews()
        initListeners()
        getNoAntrian()
    }


    private fun postNoAntrian(){
        RetrofitClient.instance.createPost().enqueue(object : retrofit2.Callback<ArrayList<>>)

    }

    private fun getNoAntrian(){
        RetrofitClient.instance.getAntrian().enqueue(object: retrofit2.Callback<ArrayList<noAntrianResponse>>{
            override fun onResponse(
                call: Call<ArrayList<noAntrianResponse>>,
                response: Response<ArrayList<noAntrianResponse>>
            ) {
                if (response.body()?.isEmpty()?:false){
                    txtBpjs.text = "A 1"
                    txtUmum.text = "B 2"
                } else{
                    for (antrian in response.body()!!){
                        if (antrian.kode_antrian.equals("A")){
                            txtBpjs.text = "A ${(antrian.no_antrian).toInt()+1}"
                        } else if (antrian.kode_antrian.equals("B")){
                            txtUmum.text = "B ${(antrian.no_antrian).toInt()+1}"
                        }
                    }
                }
            }

            override fun onFailure(call: Call<ArrayList<noAntrianResponse>>, t: Throwable) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun createPost(kode: String) {
        RetrofitClient.instance.createPost(
            kode
        ).enqueue(object: retrofit2.Callback<CreatePostResponse>{
            override fun onResponse(
                call: Call<CreatePostResponse>,
                response: Response<CreatePostResponse>
            ) {
                if(kode.equals("A")){
                    antrianBpjs = "A ${(response.body()?.no_antrian?:0)+1}"
//                    txtBpjs.text = antrianBpjs
                } else {
                    antrianUmum = "A ${(response.body()?.no_antrian?:0)+1}"
//                    txtUmum.text = antrianUmum
                }
            }
            override fun onFailure(call: Call<CreatePostResponse>, t: Throwable) {
            }
        } )
    }

    private fun initViews() {
        if(Printooth.hasPairedPrinter()){
            btnPiarUnpair.visibility=View.GONE
        } else {
            btnPiarUnpair.text = "Sambungkan Bluetooth"
        }

        //btnPiarUnpair.text = if (Printooth.hasPairedPrinter()) "Un-pair ${Printooth.getPairedPrinter()?.name}" else "Pair with printer"
    }

    private fun initListeners() {
        btnBpjs.setOnClickListener {

            if (!Printooth.hasPairedPrinter()) startActivityForResult(Intent(this,
                    ScanningActivity::class.java),
                    ScanningActivity.SCANNING_FOR_PRINTER)
            else {
                createPost("A")
//                printSomePrintable()
            }
        }

        btnUmum.setOnClickListener {

            if (!Printooth.hasPairedPrinter()) startActivityForResult(Intent(this,
                ScanningActivity::class.java),
                ScanningActivity.SCANNING_FOR_PRINTER)
            else {
                createPost("B")
//                printSomePrintableBPJS()
            }
        }

        btnPrintImages.setOnClickListener {
            if (!Printooth.hasPairedPrinter()) startActivityForResult(Intent(this,
                    ScanningActivity::class.java),
                    ScanningActivity.SCANNING_FOR_PRINTER)
            else printSomeImages()
        }

        btnPiarUnpair.setOnClickListener {
            if (Printooth.hasPairedPrinter()) Printooth.removeCurrentPrinter()
            else startActivityForResult(Intent(this, ScanningActivity::class.java),
                    ScanningActivity.SCANNING_FOR_PRINTER)
            initViews()
        }

        btnCustomPrinter.setOnClickListener {
            startActivity(Intent(this, WoosimActivity::class.java))
        }

        printing?.printingCallback = object : PrintingCallback {
            override fun connectingWithPrinter() {
                Toast.makeText(this@MainActivity, "Tunggu Sebentar..", Toast.LENGTH_SHORT).show()
            }
            // Berhasil mengirim data ke thermal printer
            override fun printingOrderSentSuccessfully() {
                Toast.makeText(this@MainActivity, "Silahkan Kertas Antrian Anda", Toast.LENGTH_SHORT).show()
            }
            // Gagal menyambungkan ke bluetooth
            override fun connectionFailed(error: String) {
                Toast.makeText(this@MainActivity, "Gagal Mencetak Kertas Antrian", Toast.LENGTH_SHORT).show()
            }

            override fun onError(error: String) {
                Toast.makeText(this@MainActivity, error, Toast.LENGTH_SHORT).show()
            }

            override fun onMessage(message: String) {
                Toast.makeText(this@MainActivity, "Pesan Error: $message", Toast.LENGTH_SHORT).show()
            }
            //Memutuskan Koneksi Bluetooth
            override fun disconnected() {
                Toast.makeText(this@MainActivity, "Memutuskan Bluetooth", Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun printSomePrintable() {
        val printables = getSomePrintables()
        printing?.print(printables)
    }

//    private fun printSomePrintableBPJS() {
//        val printables = getSomePrintablesUmum()
//        printing?.print(printables)
//    }

    private fun printSomeImages() {
        val printables = arrayListOf<Printable>(
            ImagePrintable.Builder(R.drawable.logo, resources)
                .setAlignment(DefaultPrinter.ALIGNMENT_CENTER)
                .build()
        )
        printing?.print(printables)
    }

    var cal = Calendar.getInstance()
    var month_date = SimpleDateFormat("EEEE, dd MMMM yyyy, HH:mm")
    var waktu = month_date.format(cal.time)

    private fun getSomePrintables() = ArrayList<Printable>().apply {

        add(RawPrintable.Builder(byteArrayOf(27, 100, 4)).build()) // feed lines example in raw mode
        add(ImagePrintable.Builder(R.drawable.logo, resources)
            .setAlignment(DefaultPrinter.ALIGNMENT_CENTER)
            .build()
        )
        add(TextPrintable.Builder()
            .setText("RS Paru Jember\n")
            .setAlignment(DefaultPrinter.ALIGNMENT_CENTER)
            .setEmphasizedMode(DefaultPrinter.EMPHASIZED_MODE_NORMAL)
            .setNewLinesAfter(1)
            .build())

        add(TextPrintable.Builder()
            .setText("UMUM \n")
            .setAlignment(DefaultPrinter.ALIGNMENT_CENTER)
            .setEmphasizedMode(DefaultPrinter.EMPHASIZED_MODE_BOLD)
            .setCharacterCode(DefaultPrinter.LINE_SPACING_60)
            .setNewLinesAfter(1)
            .build())

        add(TextPrintable.Builder()
            .setText("Antrian Nomer :")
            .setAlignment(DefaultPrinter.ALIGNMENT_CENTER)
            .setNewLinesAfter(1)
            .build())

        add(TextPrintable.Builder()
            .setText("\n")
            .setAlignment(DefaultPrinter.ALIGNMENT_CENTER)
            .setFontSize(DefaultPrinter.FONT_SIZE_LARGE)
            .setEmphasizedMode(DefaultPrinter.EMPHASIZED_MODE_BOLD)
            .setUnderlined(DefaultPrinter.UNDERLINED_MODE_ON)
            .setNewLinesAfter(1)
            .build())

        add(TextPrintable.Builder()
            .setText(waktu + "\n\n\n\n")
            .setAlignment(DefaultPrinter.ALIGNMENT_CENTER)
            .setNewLinesAfter(1)
            .build())
    }

//    private fun getSomePrintablesUmum() = ArrayList<Printable>().apply {
//
//        add(RawPrintable.Builder(byteArrayOf(27, 100, 4)).build()) // feed lines example in raw mode
//        add(ImagePrintable.Builder(R.drawable.logo, resources)
//            .setAlignment(DefaultPrinter.ALIGNMENT_CENTER)
//            .build()
//        )
//        add(TextPrintable.Builder()
//            .setText("RS Paru Jember\n")
//            .setAlignment(DefaultPrinter.ALIGNMENT_CENTER)
//            .setEmphasizedMode(DefaultPrinter.EMPHASIZED_MODE_NORMAL)
//            .setNewLinesAfter(1)
//            .build())
//
//        add(TextPrintable.Builder()
//            .setText("\n")
//            .setAlignment(DefaultPrinter.ALIGNMENT_CENTER)
//            .setEmphasizedMode(DefaultPrinter.EMPHASIZED_MODE_BOLD)
//            .setCharacterCode(DefaultPrinter.LINE_SPACING_60)
//            .setNewLinesAfter(1)
//            .build())
//
//        add(TextPrintable.Builder()
//            .setText("Antrian Nomer :")
//            .setAlignment(DefaultPrinter.ALIGNMENT_CENTER)
//            .setNewLinesAfter(1)
//            .build())
//
//        add(TextPrintable.Builder()
//            .setText("B 1\n")
//            .setAlignment(DefaultPrinter.ALIGNMENT_CENTER)
//            .setFontSize(DefaultPrinter.FONT_SIZE_LARGE)
//            .setEmphasizedMode(DefaultPrinter.EMPHASIZED_MODE_BOLD)
//            .setUnderlined(DefaultPrinter.UNDERLINED_MODE_ON)
//            .setNewLinesAfter(1)
//            .build())
//
//        add(TextPrintable.Builder()
//            .setText(waktu + "\n\n\n\n")
//            .setAlignment(DefaultPrinter.ALIGNMENT_CENTER)
//            .setNewLinesAfter(1)
//            .build())
//    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ScanningActivity.SCANNING_FOR_PRINTER && resultCode == Activity.RESULT_OK)
            printSomePrintable()
        initViews()
    }
}
