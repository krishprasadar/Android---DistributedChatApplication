package ds.chat;

import java.util.ArrayList;
import java.util.List;

/**
 * Chat room information that is maintained by each user
 */
public class ChatRoom {
    public String chatRoomName;
    public Member GroupOwner;
    public List<Member> members = new ArrayList<>();
    public List<String> messages = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChatRoom chatRoom = (ChatRoom) o;

        return !(chatRoomName != null ? !chatRoomName.equals(chatRoom.chatRoomName) : chatRoom.chatRoomName != null);

    }

    @Override
    public int hashCode() {
        return chatRoomName != null ? chatRoomName.hashCode() : 0;
    }
}
