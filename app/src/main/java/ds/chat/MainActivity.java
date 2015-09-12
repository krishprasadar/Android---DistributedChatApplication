package ds.chat;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Initial Screen where the user enters the profile information
 * and creates a user profile in the application
 */
public class MainActivity extends Activity {

    ChatService chatService;
    Button createUserProfile;
    TextView userNameText, userEmailText, userGenderText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createUserProfile = (Button) findViewById(R.id.create_profile_btn);
        userNameText = (TextView) findViewById(R.id.user_name_editText);
        userEmailText = (TextView) findViewById(R.id.user_email_editText);
        userGenderText = (TextView) findViewById(R.id.user_gender_editText);
        DeviceInfo device = new DeviceInfo(this);

        /*To be done on click on search for peers button*/
        if (device.isWiFiEnabled()) {
            device.setDeviceInfo();
        }

        View.OnClickListener buttonListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userNameText.getText().toString().isEmpty() ||
                        userEmailText.getText().toString().isEmpty()
                        || userGenderText.getText().toString().isEmpty()) {
                    Toast.makeText(MainActivity.this, "Complete profile information", Toast.LENGTH_SHORT).show();
                } else {

                    Toast.makeText(MainActivity.this, "User Profile Created", Toast.LENGTH_SHORT).show();
                    UserProfile.userName = userNameText.getText().toString();
                    UserProfile.userGender = userEmailText.getText().toString();
                    Intent intent = new Intent(MainActivity.this, CreateRoomActivity.class);
                    startActivity(intent);
                }
            }
        };

        createUserProfile.setOnClickListener(buttonListener);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, ChatService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
