package ds.chat;

import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Chat rooms created are displayed using a ListFragment
 */
public class FragmentChatRoomsList extends ListFragment {

    ChatRoomsListAdapter listAdapter = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.chatroom_list, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listAdapter = new ChatRoomsListAdapter(this.getActivity(),
                android.R.layout.simple_list_item_2, android.R.id.text1,
                new ArrayList<ChatRoom>());
        setListAdapter(listAdapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Toast.makeText(getActivity(), "Chat Room Opened", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        intent.putExtra("chatRoomName", ((ChatRoom) l.getItemAtPosition(position)).chatRoomName);
        startActivity(intent);
    }

    public class ChatRoomsListAdapter extends ArrayAdapter<ChatRoom> {

        private List<ChatRoom> chatRooms;
        public ChatRoomsListAdapter(Context context, int resource,
                                    int textViewResourceId, List<ChatRoom> chatRooms) {
            super(context, resource, textViewResourceId, chatRooms);
            this.chatRooms = chatRooms;
        }

        public List<ChatRoom> getRooms() {
            return chatRooms;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getActivity()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(android.R.layout.simple_list_item_2, null);
            }
            ChatRoom room = chatRooms.get(position);
            if (room != null) {
                TextView nameText = (TextView) v
                        .findViewById(android.R.id.text1);

                if (room.chatRoomName != null) {
                    nameText.setText(room.chatRoomName);
                }
            }
            return v;
        }

    }

}
