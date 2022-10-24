package com.mazenrashed.example

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri.decode
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.URLUtil.decode
import android.widget.TextView
import android.widget.Toast
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.mazenrashed.example.Model.TicketRequest
import com.mazenrashed.example.Model.TicketResponse
import com.mazenrashed.example.Model.UserRequest
import com.mazenrashed.example.Model.UserResponse
import com.mazenrashed.example.Rest_Api.Api
import com.mazenrashed.example.Rest_Api.Retro
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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Byte.decode
import java.net.URLDecoder.decode
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {

    private var printing : Printing? = null

    private lateinit var txtBpjs: TextView
    private lateinit var txtUmum: TextView
    private lateinit var imgView: ImageView
    private lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        txtBpjs = findViewById(R.id.textBpjs)
        txtUmum = findViewById(R.id.textUmum)
        imgView = findViewById(R.id.cardView)
        textView = findViewById(R.id.textView2)

        if (Printooth.hasPairedPrinter())
            printing = Printooth.printer()
        initViews()
        initListeners()
        postNoAntrian("A")
        postNoAntrian("B")
        refresh()
    }

    private fun refresh(){
        refreshLayout.setOnRefreshListener {
            refreshLayout.isRefreshing = false
            postNoAntrian("A")
            postNoAntrian("B")
        }
    }

    fun soundEffect(){
        val play = MediaPlayer.create(this, R.raw.sound)
        play.start()
    }

    val BASE_URL = "https://internationalchest.com/antrian_rsparu/api/"

    private fun postNoAntrian(jenis: String){
        val getQueue = UserRequest()
        getQueue.kode = jenis
        val retro = Retro().getRetroClientInstance(BASE_URL).create(Api::class.java)
        retro.getNumberQueue(getQueue).enqueue(object : Callback<UserResponse>{
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>){
                Log.e("Berhasil", response.code().toString())
                if (jenis.equals("A")) {
                    val cetakTiket = "A" + response.body()?.antrian?.toInt()
                    txtBpjs.text = cetakTiket
                } else if (jenis.equals("B")) {
                    val cetakTiket = "B" + response.body()?.antrian?.toInt()
                    txtUmum.text = cetakTiket
                }
            }
            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
            }
        })
    }

    interface SomeCallBack{
        fun onSucces()

        fun onFailure(error: String)
    }

    private fun postTicketQueue(jenis: String): String{
        Toast.makeText(this,"Tunggu Sebentar..", Toast.LENGTH_SHORT).show()
        val ticketQueue = TicketRequest()
        ticketQueue.kode = jenis
        var nyetak = ""
        val retro = Retro().getRetroClientInstance(BASE_URL).create(Api::class.java)
        retro.getTicketQueue(ticketQueue).enqueue(object : Callback<TicketResponse> {
            override fun onResponse(
                call: Call<TicketResponse>,
                response: Response<TicketResponse>
            ){
                if (jenis.equals("A")) {
                    var cetakTiket = response.body()?.no_antrian
                    nyetak = "A$cetakTiket"
                    val printUnit =  getSomePrintables("BPJS", nyetak)
                    printing?.print(printUnit)
                } else {
                    var cetakTiket = response.body()?.no_antrian
                    nyetak = "B$cetakTiket"
                    val printUnit =  getSomePrintables("UMUM", nyetak)
                    printing?.print(printUnit)
                }
            }
            override fun onFailure(call: Call<TicketResponse>, t: Throwable) {
                t.stackTrace
            }
        })
        return nyetak
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
//                printSomePrintable()
                soundEffect()
                postTicketQueue("A")

                postNoAntrian("A")
                postNoAntrian("B")
            }
        }

        btnUmum.setOnClickListener {

            if (!Printooth.hasPairedPrinter()) startActivityForResult(Intent(this,
                ScanningActivity::class.java),
                ScanningActivity.SCANNING_FOR_PRINTER)
            else {
                //printSomePrintableUmum()
                soundEffect()
                postTicketQueue("B")

                postNoAntrian("A")
                postNoAntrian("B")
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
        Toast.makeText(this,"Tunggu Sebentar..", Toast.LENGTH_SHORT).show()
        val printables = getSomePrintables("BPJS", postTicketQueue("A"))
        printing?.print(printables)
    }

    private fun printSomePrintableUmum() {
        Toast.makeText(this,"Tunggu Sebentar..", Toast.LENGTH_SHORT).show()
        val printables = getSomePrintables("UMUM", postTicketQueue("B"))
        printing?.print(printables)
    }

    private fun printSomeImages() {
        val printables = arrayListOf<Printable>(
            ImagePrintable.Builder(R.drawable.logo, resources)
                .setAlignment(DefaultPrinter.ALIGNMENT_CENTER)
                .build()
        )
        printing?.print(printables)
    }

    var cal = Calendar.getInstance()
    var month_date = SimpleDateFormat("EEEE,dd MMMM yyyy,HH:mm")
    var waktu = month_date.format(cal.time)

    private fun getSomePrintables(jenis: String, nomer: String) = ArrayList<Printable>().apply {
        add(RawPrintable.Builder(byteArrayOf(27, 100, 4)).build()) // feed lines example in raw mode
        add(ImagePrintable.Builder(R.drawable.logo_gede, resources)
            .setAlignment(DefaultPrinter.ALIGNMENT_CENTER)
            .build()
        )
        add(TextPrintable.Builder()
            .setText("\nRS Paru Jember\n")
            .setAlignment(DefaultPrinter.ALIGNMENT_CENTER)
            .setEmphasizedMode(DefaultPrinter.EMPHASIZED_MODE_NORMAL)
            .setNewLinesAfter(1)
            .build())

        add(TextPrintable.Builder()
            .setText("Jenis Antrian:")
            .setAlignment(DefaultPrinter.ALIGNMENT_CENTER)
            .setNewLinesAfter(1)
            .build())

        add(TextPrintable.Builder()
            .setText("${jenis}\n")
            .setFontSize(DefaultPrinter.FONT_SIZE_LARGE)
            .setAlignment(DefaultPrinter.ALIGNMENT_CENTER)
            .setEmphasizedMode(DefaultPrinter.EMPHASIZED_MODE_BOLD)
//            .setCharacterCode(DefaultPrinter.LINE_SPACING_60)
            .setNewLinesAfter(1)
            .build())

        add(TextPrintable.Builder()
            .setText("Antrian Nomer:")
            .setAlignment(DefaultPrinter.ALIGNMENT_CENTER)
            .setNewLinesAfter(1)
            .build())

        add(TextPrintable.Builder()
            .setText("$nomer\n")
            .setAlignment(DefaultPrinter.ALIGNMENT_CENTER)
            .setFontSize(DefaultPrinter.FONT_SIZE_LARGE)
            .setEmphasizedMode(DefaultPrinter.EMPHASIZED_MODE_BOLD)
            .setNewLinesAfter(1)
            .build())

        add(TextPrintable.Builder()
            .setText(waktu + "\n\n\n")
            .setAlignment(DefaultPrinter.ALIGNMENT_CENTER)
            .setNewLinesAfter(1)
            .build())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ScanningActivity.SCANNING_FOR_PRINTER && resultCode == Activity.RESULT_OK)
            printSomePrintable()
        initViews()
    }
}
