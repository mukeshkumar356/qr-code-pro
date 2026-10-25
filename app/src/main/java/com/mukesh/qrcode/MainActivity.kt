package com.mukesh.qrcode

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.integration.android.IntentIntegrator
import com.mukesh.qrcode.databinding.ActivityMainBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding
    private var qrBitmap: Bitmap? = null
    private var qrFgColor = Color.BLACK
    private var qrBgColor = Color.WHITE
    private var qrSize = 512

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        setupTabs()

        // Generate tab
        b.btnGenerate.setOnClickListener {
            val text = b.etQrInput.text.toString().trim()
            if (text.isEmpty()) {
                Toast.makeText(this, "Enter text or URL", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            generateQR(text)
        }

        b.btnSaveQr.setOnClickListener { saveQrToFile() }
        b.btnShareQr.setOnClickListener { shareQr() }

        // Color options
        val fgColors = listOf(b.btnFg1, b.btnFg2, b.btnFg3, b.btnFg4)
        val fgColorValues = listOf(Color.BLACK, Color.parseColor("#1B5E20"), Color.parseColor("#0D47A1"), Color.parseColor("#880E4F"))
        fgColors.forEachIndexed { i, btn ->
            btn.setBackgroundColor(fgColorValues[i])
            btn.setOnClickListener { qrFgColor = fgColorValues[i]; regenerate() }
        }

        val bgColors = listOf(b.btnBg1, b.btnBg2, b.btnBg3, b.btnBg4)
        val bgColorValues = listOf(Color.WHITE, Color.parseColor("#F1F8E9"), Color.parseColor("#E3F2FD"), Color.parseColor("#FFF9C4"))
        bgColors.forEachIndexed { i, btn ->
            btn.setBackgroundColor(bgColorValues[i])
            btn.setOnClickListener { qrBgColor = bgColorValues[i]; regenerate() }
        }

        // Scan tab
        b.btnScan.setOnClickListener {
            val integrator = IntentIntegrator(this)
            integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
            integrator.setPrompt("Scan QR / Barcode")
            integrator.setBeepEnabled(true)
            integrator.initiateScan()
        }

        b.btnCopy.setOnClickListener {
            val text = b.tvScanResult.text.toString()
            if (text.isNotEmpty() && text != "Scan a QR code to see results") {
                val cm = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                cm.setPrimaryClip(ClipData.newPlainText("QR Result", text))
                Toast.makeText(this, "Copied!", Toast.LENGTH_SHORT).show()
            }
        }

        b.btnOpenUrl.setOnClickListener {
            val text = b.tvScanResult.text.toString()
            if (text.startsWith("http://") || text.startsWith("https://")) {
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(text)))
                } catch (e: ActivityNotFoundException) {
                    // no browser installed/enabled on the device; don't crash
                    Toast.makeText(this, "No app found to open this link", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Not a URL", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupTabs() {
        b.btnTabGenerate.setOnClickListener {
            b.layoutGenerate.visibility = View.VISIBLE
            b.layoutScan.visibility = View.GONE
            b.btnTabGenerate.alpha = 1f
            b.btnTabScan.alpha = 0.5f
        }
        b.btnTabScan.setOnClickListener {
            b.layoutGenerate.visibility = View.GONE
            b.layoutScan.visibility = View.VISIBLE
            b.btnTabGenerate.alpha = 0.5f
            b.btnTabScan.alpha = 1f
        }
        b.btnTabGenerate.alpha = 1f
        b.btnTabScan.alpha = 0.5f
    }

    private fun generateQR(text: String) {
        try {
            val hints = mapOf(EncodeHintType.MARGIN to 1)
            val matrix = MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, qrSize, qrSize, hints)
            // RGB_565 quantizes colors (the chosen foreground/background shades
            // get altered in the saved/shared PNG), so use ARGB_8888 and write
            // all pixels in one call instead of ~262k setPixel() calls.
            val pixels = IntArray(qrSize * qrSize)
            for (y in 0 until qrSize) {
                for (x in 0 until qrSize) {
                    pixels[y * qrSize + x] = if (matrix[x, y]) qrFgColor else qrBgColor
                }
            }
            val bmp = Bitmap.createBitmap(pixels, qrSize, qrSize, Bitmap.Config.ARGB_8888)
            qrBitmap = bmp
            b.ivQr.setImageBitmap(bmp)
            b.layoutQrResult.visibility = View.VISIBLE
        } catch (e: WriterException) {
            Toast.makeText(this, "Failed to generate QR", Toast.LENGTH_SHORT).show()
        }
    }

    private fun regenerate() {
        val text = b.etQrInput.text.toString().trim()
        if (text.isNotEmpty() && qrBitmap != null) generateQR(text)
    }

    private fun saveQrToFile() {
        val bmp = qrBitmap ?: return
        val dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: return
        val file = File(dir, "QR_${System.currentTimeMillis()}.png")
        try {
            FileOutputStream(file).use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
        } catch (e: IOException) {
            // e.g. storage full or unavailable; don't crash the app
            Toast.makeText(this, "Failed to save QR", Toast.LENGTH_SHORT).show()
            return
        }
        Toast.makeText(this, "Saved to ${file.absolutePath}", Toast.LENGTH_LONG).show()
    }

    private fun shareQr() {
        val bmp = qrBitmap ?: return
        val dir = cacheDir
        val file = File(dir, "qr_share.png")
        try {
            FileOutputStream(file).use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
        } catch (e: IOException) {
            Toast.makeText(this, "Failed to share QR", Toast.LENGTH_SHORT).show()
            return
        }
        val uri = FileProvider.getUriForFile(this, "${packageName}.provider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, "Share QR Code"))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        // parseActivityResult returns a non-null IntentResult even when the
        // user cancels the scan (contents is just null in that case), so
        // checking "result != null" alone wrongly overwrote the scan box
        // with "No result" on every cancel instead of leaving it untouched.
        if (result != null) {
            val text = result.contents
            if (text != null) {
                b.tvScanResult.text = text
                b.tvScanFormat.text = result.formatName ?: ""
                val isUrl = text.startsWith("http://") || text.startsWith("https://")
                b.btnOpenUrl.visibility = if (isUrl) View.VISIBLE else View.GONE
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}
