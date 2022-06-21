package com.example.tvvideolooper


import android.app.Activity
import android.app.Dialog
import android.graphics.Typeface
import android.os.Environment
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class FileSelector(private val context: Activity, private val extensions: Array<String>) {
    private val itemsData = ArrayList<SelectedFile>()

    interface OnSelectListener {
        fun onSelect(path: String?)
    }

    fun selectFile(listener: OnSelectListener) {
        val sdCard = Environment.getExternalStorageDirectory().absolutePath
        listOfFile(File(sdCard))
        dialogFileList(listener)
    }

    private fun listOfFile(dir: File) {
        val list = dir.listFiles()
        for (file in list) {
            if (file.isDirectory) {
                if (!File(file, ".nomedia").exists() && !file.name.startsWith(".")) {
                    Log.w("LOG", "IS DIR $file")
                    listOfFile(file)
                }
            } else {
                val path = file.absolutePath
                for (ext in extensions) {
                    if (path.endsWith(ext)) {
                        val selectedFile = SelectedFile()
                        selectedFile.path = path
                        val split = path.split("/").toTypedArray()
                        selectedFile.name = split[split.size - 1]
                        itemsData.add(selectedFile)
                        Log.i("LOG", "ADD " + selectedFile.path + " " + selectedFile.name)
                    }
                }
            }
        }
        Log.d("LOG", itemsData.size.toString() + " DONE")
    }

    private fun dialogFileList(listener: OnSelectListener) {
        val lytMain = LinearLayout(context)
        lytMain.layoutParams =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        lytMain.orientation = LinearLayout.VERTICAL
        val p = convertToPixels(12)
        lytMain.setPadding(p, p, p, p)
        lytMain.gravity = Gravity.CENTER
        val textView = TextView(context)
        textView.layoutParams =
            LinearLayout.LayoutParams(screenWidth(), ViewGroup.LayoutParams.WRAP_CONTENT)
        textView.gravity = Gravity.CENTER
        textView.text = "~JDM File Selecor~"
        val recyclerView = RecyclerView(
            context
        )
        lytMain.addView(textView)
        lytMain.addView(recyclerView)
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        // dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(lytMain)
        dialog.setCancelable(true)
        dialog.show()
        val adapter = AdapterFile(dialog, listener, itemsData)
        recyclerView.adapter = adapter
        val LayoutManager = LinearLayoutManager(
            context
        )
        LayoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = LayoutManager
    }

    private fun convertToPixels(dp: Int): Int {
        val scale = context.resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }

    private fun screenWidth(): Int {
        return context.resources.displayMetrics.widthPixels
    }

    private inner class SelectedFile {
        var path = ""
        var name = ""
    }

    private inner class AdapterFile(
        private val dialog: Dialog,
        private val listener: OnSelectListener,
        private val itemsData: ArrayList<SelectedFile>
    ) :
        RecyclerView.Adapter<AdapterFile.ViewHolder?>() {
        // Create new views (invoked by the layout manager)
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): ViewHolder {
            val linearLayout = LinearLayout(context)
            linearLayout.orientation = LinearLayout.VERTICAL
            val txtName = TextView(context)
            val txtPath = TextView(context)
            txtPath.setTypeface(txtPath.typeface, Typeface.ITALIC)
            txtPath.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
            linearLayout.addView(txtName)
            linearLayout.addView(txtPath)
            return ViewHolder(linearLayout)
        }

        // inner class to hold a reference to each item of RecyclerView
        inner class ViewHolder(itemLayoutView: View) : RecyclerView.ViewHolder(itemLayoutView) {
            var linearLayout: LinearLayout
            var txtName: TextView
            var txtPath: TextView

            init {
                linearLayout = itemLayoutView as LinearLayout
                txtName = linearLayout.getChildAt(0) as TextView
                txtPath = linearLayout.getChildAt(1) as TextView
            }
        }

        override fun getItemCount(): Int {
            return itemsData.size
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
            val selectedFile = itemsData[position]
            viewHolder.txtName.text = selectedFile.name
            viewHolder.txtPath.text = selectedFile.path
            viewHolder.linearLayout.setOnClickListener {
                dialog.dismiss()
                listener.onSelect(selectedFile.path)
            }
        }
    }

    companion object {
        const val MP4 = ".mp4"
        const val MP3 = ".mp3"
        const val JPG = ".jpg"
        const val JPEG = ".jpeg"
        const val PNG = ".png"
        const val DOC = ".doc"
        const val DOCX = ".docx"
        const val XLS = ".xls"
        const val XLSX = ".xlsx"
        const val PDF = ".pdf"
    }
}