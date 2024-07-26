package fastcampus.part2.chattingapp.chatdetail

import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import fastcampus.part2.chattingapp.databinding.ItemChatBinding
import fastcampus.part2.chattingapp.userlist.UserItem

class ChatAdapter: ListAdapter<ChatItem, ChatAdapter.ViewHolder>(differ) {

    var otherUserItem: UserItem? = null

    inner class ViewHolder(private val binding: ItemChatBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(item: ChatItem){
            if(item.userId == otherUserItem?.userId){
                // 상대방이 채팅을 보냈을 때
                binding.tvUsername.isVisible = true
                binding.tvUsername.text = otherUserItem?.username
                binding.tvMessage.text = item.message
                binding.tvMessage.gravity = Gravity.START
            }
            else{
                //내가 채팅을 보냈을 때
                binding.tvUsername.isVisible = false
                binding.tvMessage.text = item.message
                binding.tvMessage.gravity = Gravity.END
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemChatBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }


    companion object{
        val differ = object: DiffUtil.ItemCallback<ChatItem>(){
            override fun areItemsTheSame(oldItem: ChatItem, newItem: ChatItem): Boolean {
                return oldItem.chatId == newItem.chatId
            }

            override fun areContentsTheSame(oldItem: ChatItem, newItem: ChatItem): Boolean {
                return oldItem == newItem
            }

        }
    }
}