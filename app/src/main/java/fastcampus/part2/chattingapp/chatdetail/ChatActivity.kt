package fastcampus.part2.chattingapp.chatdetail

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.database
import fastcampus.part2.chattingapp.Key
import fastcampus.part2.chattingapp.databinding.ActivityChatdetailBinding
import fastcampus.part2.chattingapp.userlist.UserItem

class ChatActivity: AppCompatActivity() {

    private lateinit var binding:ActivityChatdetailBinding

    private var chatRoomId: String = ""
    private var otherUserId: String = ""
    private var myUserId: String = ""

    private val chatItemList = mutableListOf<ChatItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatdetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        chatRoomId = intent.getStringExtra(EXTRA_CHAT_ROOM_ID)?: return
        otherUserId = intent.getStringExtra(EXTRA_OTHER_USER_ID)?:return
        myUserId = Firebase.auth.currentUser?.uid?:""
        val chatAdapter = ChatAdapter()

        Firebase.database.reference.child(Key.DB_USERS).child(myUserId).get()
            .addOnSuccessListener {
                val myUserItem = it.getValue(UserItem::class.java)
                val myUserName = myUserItem?.username
            }
        Firebase.database.reference.child(Key.DB_USERS).child(otherUserId).get()
            .addOnSuccessListener {
                val otherUserItem = it.getValue(UserItem::class.java)

                chatAdapter.otherUserItem = otherUserItem
            }

        //채팅 가져오기
        Firebase.database.reference.child(Key.DB_CHATS).child(chatRoomId).addChildEventListener(object : ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatItem = snapshot.getValue(ChatItem::class.java)
                chatItem ?: return

                chatItemList.add(chatItem)
                chatAdapter.submitList(chatItemList)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                // TODO: 수정 구현
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                // TODO: 삭제 구현
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                // TODO: 이동 구현
            }

            override fun onCancelled(error: DatabaseError) {
                // TODO: 에러 예외처리 구현
            }

        })


        binding.chatRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = chatAdapter
        }

    }
    companion object{
        const val EXTRA_CHAT_ROOM_ID = "CHAT_ROOM_ID"
        const val EXTRA_OTHER_USER_ID = "OTHER_USER_ID"
    }
}