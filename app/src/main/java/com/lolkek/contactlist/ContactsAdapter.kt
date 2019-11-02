package com.lolkek.contactlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lolkek.R
import kotlinx.android.synthetic.main.list_item.view.*

class ContactsAdapter : RecyclerView.Adapter<ContactsAdapter.ContactsViewHolder>() {
    private var items: List<ContactModel> = emptyList()

    fun bindItem(items: List<ContactModel>) {
        this.items = items
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ContactsViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.list_item,
                parent,
                false
            )
        )

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ContactsViewHolder, position: Int) {
        holder.bindItem(items[position])
    }

    class ContactsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItem(contactModel: ContactModel) {
            itemView.textViewContactName.text = contactModel.fullName
            itemView.textViewPhoneNumbers.visibility =
                if (contactModel.phoneNumbers.isEmpty()) View.GONE else View.VISIBLE
            itemView.textViewPhoneNumbers.text = composePhoneNumbersText(contactModel.phoneNumbers)
        }

        private fun composePhoneNumbersText(phoneNumbers: Set<String>): String =
            phoneNumbers.joinToString(separator = "\n")
    }
}