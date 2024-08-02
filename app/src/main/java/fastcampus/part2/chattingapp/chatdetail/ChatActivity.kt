package fastcampus.part2.chattingapp.chatdetail

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import fastcampus.part2.chattingapp.Key
import fastcampus.part2.chattingapp.R
import fastcampus.part2.chattingapp.databinding.ActivityChatdetailBinding
import fastcampus.part2.chattingapp.userlist.UserItem
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import com.google.auth.oauth2.GoogleCredentials
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatdetailBinding
    private lateinit var chatAdapter: ChatAdapter

    private var chatRoomId: String = ""
    private var otherUserId: String = ""
    private var otherUserFcmToken: String = ""
    private var myUserId: String = ""
    private var myUserName: String = ""
    private var isInit = false

    private val chatItemList = mutableListOf<ChatItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatdetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        chatRoomId = intent.getStringExtra(EXTRA_CHAT_ROOM_ID) ?: return
        otherUserId = intent.getStringExtra(EXTRA_OTHER_USER_ID) ?: return
        myUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        chatAdapter = ChatAdapter()

        FirebaseDatabase.getInstance().reference.child(Key.DB_USERS).child(myUserId).get()
            .addOnSuccessListener {
                val myUserItem = it.getValue(UserItem::class.java)
                myUserName = myUserItem?.username ?: ""

                getOtherUserData()
            }

        binding.chatRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = chatAdapter
        }

        binding.btnSend.setOnClickListener {
            val message = binding.etMessage.text.toString()

            if (!isInit) {
                return@setOnClickListener
            }

            //빈 메시지 전송 예외처리
            if (message.isEmpty()) {
                Toast.makeText(applicationContext, "빈 메시지를 전송할 수는 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newChatItem = ChatItem(
                message = message,
                userId = myUserId,
            )
            //채팅 전송
            FirebaseDatabase.getInstance().reference.child(Key.DB_CHATS).child(chatRoomId).push()
                .apply {
                    newChatItem.chatId = key
                    setValue(newChatItem)
                }

            val updates: MutableMap<String, Any> = hashMapOf(
                "${Key.DB_CHAT_ROOMS}/$myUserId/$otherUserId/lastMessage" to message,
                "${Key.DB_CHAT_ROOMS}/$otherUserId/$myUserId/lastMessage" to message,
                "${Key.DB_CHAT_ROOMS}/$otherUserId/$myUserId/chatRoomId" to chatRoomId,
                "${Key.DB_CHAT_ROOMS}/$otherUserId/$myUserId/otherUserId" to myUserId,
                "${Key.DB_CHAT_ROOMS}/$otherUserId/$myUserId/otherUserName" to myUserName,
            )
            FirebaseDatabase.getInstance().reference.updateChildren(updates)

            // FCM 메시지 보내기
            sendFcmMessage(otherUserFcmToken, message)

            binding.etMessage.text.clear()
        }
    }

    private fun sendFcmMessage(fcmToken: String, message: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val client = OkHttpClient()

            val root = JSONObject()
            val notification = JSONObject()
            notification.put("title", getString(R.string.app_name))
            notification.put("body", message)
            root.put("to", fcmToken)
            root.put("priority", "high")
            root.put("notification", notification)

            val requestBody =
                root.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val accessToken = getAccessToken() // OAuth2 토큰 가져오기

            if (accessToken == null) {
                Log.e("ChatActivity", "FCM 메시지 전송 실패")
                return@launch
            }

            val request = Request.Builder()
                .post(requestBody)
                .url("https://fcm.googleapis.com/v1/projects/chattingapp-e1548/messages:send")
                .header("Authorization", "Bearer $accessToken")
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    Log.d("ChatActivity", "FCM 서버 응답: ${response.code}, ${response.message}")
                    if (response.isSuccessful) {
                        Log.d("ChatActivity", "FCM 메시지 전송 성공")
                    } else {
                        Log.e("ChatActivity", "FCM 메시지 전송 실패: ${response.body?.string()}")
                    }
                }
            })
        }
    }

    private suspend fun getAccessToken(): String? = withContext(Dispatchers.IO) {
        try {
            val inputStream: InputStream = assets.open("firebase_service_key.json")
            val googleCredentials = GoogleCredentials.fromStream(inputStream)
                .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))
            googleCredentials.refreshIfExpired() // 만료된 액세스 토큰 자동 갱신
            googleCredentials.accessToken.tokenValue // 유효한 액세스 토큰 가져옴
        } catch (e: IOException) {
            Log.e("ChatActivity", "Failed to get access token", e)
            null
        }
    }

    private fun getOtherUserData() {
        FirebaseDatabase.getInstance().reference.child(Key.DB_USERS).child(otherUserId).get()
            .addOnSuccessListener {
                val otherUserItem = it.getValue(UserItem::class.java)
                otherUserFcmToken = otherUserItem?.fcmToken.orEmpty()
                chatAdapter.otherUserItem = otherUserItem

                isInit = true
                getChatData()
            }
    }

    private fun getChatData() {
        //채팅 가져오기
        FirebaseDatabase.getInstance().reference.child(Key.DB_CHATS).child(chatRoomId)
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val chatItem = snapshot.getValue(ChatItem::class.java)
                    chatItem ?: return

                    chatItemList.add(chatItem)
                    chatAdapter.submitList(chatItemList.toMutableList())
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
    }

    companion object {
        const val EXTRA_CHAT_ROOM_ID = "CHAT_ROOM_ID"
        const val EXTRA_OTHER_USER_ID = "OTHER_USER_ID"
    }
}