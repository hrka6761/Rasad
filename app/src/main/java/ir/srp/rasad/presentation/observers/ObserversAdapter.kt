package ir.srp.rasad.presentation.observers

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ir.srp.rasad.R
import ir.srp.rasad.core.utils.Dialog
import ir.srp.rasad.databinding.ObserverRowBinding
import ir.srp.rasad.domain.models.PermittedObserversModel

class ObserversAdapter(
    private val context: Context,
    private val listener: ObserverClickListener,
) : RecyclerView.Adapter<ObserversAdapter.ObserverViewHolder>() {

    private lateinit var binding: ObserverRowBinding
    private lateinit var _observers: MutableList<PermittedObserversModel>


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ObserverViewHolder {
        binding = ObserverRowBinding.inflate(LayoutInflater.from(context), parent, false)

        return ObserverViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ObserverViewHolder, position: Int) =
        holder.bindRow(position)

    override fun getItemCount() = _observers.size


    fun setList(observers: MutableList<PermittedObserversModel>) {
        _observers = observers
    }

    @SuppressLint("NotifyDataSetChanged")
    fun deleteRow(observer: PermittedObserversModel) {
        _observers.remove(observer)
        notifyDataSetChanged()
        if (_observers.isEmpty())
            listener.onEmptyList()
    }


    inner class ObserverViewHolder(private val binding: ObserverRowBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindRow(position: Int) {
            val observer = _observers[position]
            setRowId(position + 1)
            setUsername(observer.username)
            binding.imgDelete.setOnClickListener { onClickDelete(observer) }
        }

        private fun onClickDelete(observer: PermittedObserversModel) {
            Dialog.showSimpleDialog(
                context = context,
                msg = context.getString(R.string.dialog_delete_observer_permission_msg),
                negativeAction = {},
                positiveAction = { listener.onClickDelete(observer) }
            )
        }

        private fun setRowId(position: Int) {
            binding.txtId.text = position.toString()
        }

        private fun setUsername(username: String) {
            binding.txtUsername.text = username
        }
    }

    interface ObserverClickListener {
        fun onClickDelete(observer: PermittedObserversModel)
        fun onEmptyList()
    }
}