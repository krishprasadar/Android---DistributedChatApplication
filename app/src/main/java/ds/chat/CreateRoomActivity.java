package ds.chat;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;


public class CreateRoomActivity extends Activity  {

    private FragmentChatRoomsList chatRoomsListFragment;
    TextView chatRoomName;
    ChatService chatService;
    DeviceInfo device;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        Button createChatRoomBtn, list_room_Btn;
        chatRoomsListFragment = new FragmentChatRoomsList();
        createChatRoomBtn = (Button)findViewById(R.id.create_chat_room_btn);
        chatRoomName = (TextView) findViewById(R.id.chat_room_name_editText);
        list_room_Btn = (Button) findViewById(R.id.list_rooms);
        device = new DeviceInfo(this);

        /*Chat Service is bound when the chat rooms screen is opened*/
        Intent intent= new Intent(this, ChatService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);


        View.OnClickListener buttonListener1 = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(CreateRoomActivity.this, "list rooms button clicked", Toast.LENGTH_SHORT).show();

                if(device.isWiFiEnabled()) {
                    device.setDeviceInfo();
                    if (getFragmentManager().findFragmentByTag("chatRooms") == null) {
                        getFragmentManager().beginTransaction()
                                .replace(R.id.chat_root_container, chatRoomsListFragment, "chatRooms").commit();
                        getFragmentManager().executePendingTransactions();
                    }
                    if (chatService != null) {
                        List<ChatRoom> chatRooms = chatService.chatRooms;

                        FragmentChatRoomsList fragment = (FragmentChatRoomsList) getFragmentManager()
                                .findFragmentByTag("chatRooms");
                        FragmentChatRoomsList.ChatRoomsListAdapter adapter = ((FragmentChatRoomsList.ChatRoomsListAdapter) fragment
                                .getListAdapter());
                        List<ChatRoom> existingRooms = adapter.getRooms();
                        for (ChatRoom cr : chatRooms)
                            if (!existingRooms.contains(cr))
                                adapter.add(cr);
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        };

        list_room_Btn.setOnClickListener(buttonListener1);

        View.OnClickListener buttonListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(chatService.createChatRoom(chatRoomName.getText().toString()))
                    Toast.makeText(CreateRoomActivity.this, "Chat room created", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(CreateRoomActivity.this, "Chat already exists", Toast.LENGTH_SHORT).show();
            }
        };
        createChatRoomBtn.setOnClickListener(buttonListener);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat_room, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className,
                                       IBinder binder) {
            ChatService.chatBinder cBinder = (ChatService.chatBinder) binder;
            chatService = cBinder.getService();
        }

        public void onServiceDisconnected(ComponentName className) {
            chatService = null;
        }
    };



}
