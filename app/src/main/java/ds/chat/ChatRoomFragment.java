package ds.chat;


import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Chat room fragment used to post and receive messages in the chat room
 */
public class ChatRoomFragment extends Fragment {

    private View view;
    private TextView messageBox;
    private ListView listView;
    ChatMessageAdapter adapter = null;
    private List<String> messages = new ArrayList<String>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.chat_room_layout, container, false);
        messageBox = (TextView) view.findViewById(R.id.txtChatLine);
        listView = (ListView) view.findViewById(android.R.id.list);
        adapter = new ChatMessageAdapter(getActivity(), android.R.id.text1,
                messages);
        listView.setAdapter(adapter);

        view.findViewById(R.id.button1).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        String message = messageBox.getText().toString();
                        pushMessage("Me: " + message);
                        messageBox.setText("");
                        messageBox.clearFocus();
                        ((ChatActivity) getActivity()).chatService.sendMessage(message, ((ChatActivity) getActivity()).chatRoomName);
                    }
                });
        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void pushMessage(String readMessage) {
        adapter.add(readMessage);
        adapter.notifyDataSetChanged();
    }

    public class ChatMessageAdapter extends ArrayAdapter<String> {

        public ChatMessageAdapter(Context context, int textViewResourceId,
                                  List<String> items) {
            super(context, textViewResourceId, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getActivity()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(android.R.layout.simple_list_item_1, null);
            }
            String message = messages.get(position);
            if (message != null && !message.isEmpty()) {
                TextView nameText = (TextView) v
                        .findViewById(android.R.id.text1);

                if (nameText != null) {
                    nameText.setText(message);
                    if (message.startsWith("Me: ")) {
                        nameText.setTextAppearance(getActivity(),
                                R.style.normalText);
                    } else {
                        nameText.setTextAppearance(getActivity(),
                                R.style.boldText);
                    }
                }
            }
            return v;
        }
    }
}
