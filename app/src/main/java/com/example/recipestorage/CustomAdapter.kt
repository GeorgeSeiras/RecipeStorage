//import android.content.Context
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.BaseAdapter
//import android.widget.Button
//import android.widget.EditText
//import android.widget.TextView
//import androidx.recyclerview.widget.RecyclerView.ViewHolder
//import com.example.recipestorage.R
//
//public class CustomAdapter(
//    private val context: Context,
//    private val items: ArrayList<String>
//    ):BaseAdapter(){
//    val inflater = LayoutInflater.from(context)
//
//    override fun getCount(): Int {
//        return items.size
//    }
//
//    override fun getItem(pos: Int): Any {
//        return items[pos]
//    }
//
//    override fun getItemId(id: Int): Long {
//        return id.toLong()
//    }
//
//    override fun getView(pos: Int, contentView: View?, parent: ViewGroup?): View {
//        val rowView = inflater.inflate(R.layout.,null,true)
//    }
//
//
//}