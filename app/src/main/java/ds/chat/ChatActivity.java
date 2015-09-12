package ds.chat;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

/**
 * Chat rooms list that displayed all the chat rooms that have been created in the network.
 * Uses a chat room list fragment to display the list of chat rooms.
 */
public class ChatActivity extends Activity implements ChatService.OnServiceListener{

    ChatService chatService;
    ChatRoomFragment cmFragment;
    String chatRoomName;

    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className,
                                       IBinder binder) {
            chatService = ((ChatService.chatBinder) binder).getService();
            chatService.setOnServiceListener(ChatActivity.this);
        }

        public void onServiceDisconnected(ComponentName className) {
            chatService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Intent intent= new Intent(this, ChatService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        cmFragment = new ChatRoomFragment();
        chatRoomName = getIntent().getStringExtra("chatRoomName");

        if(getFragmentManager().findFragmentByTag("chatRooms") == null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.chat_root_container, cmFragment).commit();
            getFragmentManager().executePendingTransactions();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat, menu);
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

    /**
     * As and when the chat message is received in the chat service, the data is updated
     * in the chat room message list.
     * @param data
     */
    @Override
    public void onDataReceived(final String data) {
        Handler refresh = new Handler(Looper.getMainLooper());
        refresh.post(new Runnable() {
            public void run() {
                (cmFragment).pushMessage(data);
            }
        });

    }
}
