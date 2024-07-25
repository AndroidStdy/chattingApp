package fastcampus.part2.chattingapp

class Key {
    companion object{
        const val DB_URL = "https://chattingapp-e1548-default-rtdb.firebaseio.com/" //다른지역으로 프로젝트 생성했을 시, 사용됨
        const val DB_USERS = "Users"
        const val DB_CHAT_ROOMS = "ChatRooms"
        const val DB_CHATS = "Chats"
    }
}