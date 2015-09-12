package ds.chat;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Chat Service that takes of broadcast and chat message propagation to and from chat members
 */
public class ChatService extends Service {
    final public static int BROADCAST_PORT = 7777;
    public static final int SERVER_PORT = 4545;
    public static InetAddress BROADCAST_IP;
    List<Member> members;
    List<ChatRoom> chatRooms;
    MulticastSocket bSocket;
    private final IBinder cBinder = new chatBinder();
    private OnServiceListener mOnServiceListener = null;

    public ChatService()
    {
        members = new ArrayList<>();
        chatRooms = new ArrayList<>();

        try {
            bSocket = new MulticastSocket(BROADCAST_PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        BroadcastTransmitter transmitter = new BroadcastTransmitter();
        Thread t1 = new Thread(transmitter);
        t1.start();

        BroadcastReceiver receiver = null;
        try {
            BROADCAST_IP = InetAddress.getByName("224.0.0.1");
            receiver = new BroadcastReceiver();

        } catch (IOException e) {
            e.printStackTrace();
        }
        Thread t2 = new Thread(receiver);
        t2.start();

        ChatServer server = new ChatServer();
        Thread t3 = new Thread(server);
        t3.start();

    }

    /*Service listeners to update the chat messages in the user interface when it is received*/
    public interface OnServiceListener{
        void onDataReceived(String data);
    }

    public void setOnServiceListener(OnServiceListener serviceListener){
        mOnServiceListener = serviceListener;
    }

    public class chatBinder extends Binder {
        ChatService getService() {
            return ChatService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return cBinder;
    }

    public boolean createChatRoom(String chatRoomName){
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.chatRoomName = chatRoomName;

        Member mem = new Member();
        try {
            mem.inetAddress = InetAddress.getByName(DeviceInfo.IpAddress);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        chatRoom.GroupOwner = mem;

        if(chatRooms.contains(chatRoom)) {
            Log.i("Chat Service", "Chat Room already exists!!");
            return false;
        }
        else
        {
            chatRooms.add(chatRoom);

            return true;
        }
    }

    public class BroadcastTransmitter implements Runnable
    {

        @Override
        public void run() {

                try {
                    while(true) {
                        BROADCAST_IP = InetAddress.getByName("224.0.0.1");

                        String message = null;
                        String roomNames = "";

                        for (ChatRoom room : ChatService.this.chatRooms) {
                            roomNames += room.chatRoomName + "#";
                        }

                        String deviceDetails = DeviceInfo.IpAddress;
                        message = deviceDetails + "#" + roomNames;
                        byte[] buffer = message.toString().getBytes();
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, BROADCAST_IP, BROADCAST_PORT);
                        bSocket.send(packet);
                        Thread.sleep(2000);

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

        }
    }

    public class BroadcastReceiver implements Runnable
    {
        public MulticastSocket bSocket;
        public BroadcastReceiver() throws IOException {
            bSocket = new MulticastSocket(BROADCAST_PORT);
            bSocket.joinGroup(BROADCAST_IP);
        }


        @Override
        public void run() {
            try {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                while(true)
                {
                    //Toast.makeText(BroadcastService.this, "Received message!", Toast.LENGTH_SHORT).show();
                    if(!(DeviceInfo.IpAddress == null && DeviceInfo.MacAddress == null))
                    {
                        bSocket.receive(packet);
                        String IpAddress = packet.getAddress().getHostAddress();
                        if(!DeviceInfo.IpAddress.equals(IpAddress)) {
                            buffer = packet.getData();
                            String s = new String(buffer);
                            String[] message = s.split("#");
                            List<ChatRoom> chatRooms = new ArrayList<>();
                            //Toast.makeText(BroadcastService.this, message[0], Toast.LENGTH_SHORT).show();

                            for (int i = 1; i < message.length - 1; i++) {
                                ChatRoom room = new ChatRoom();
                                room.chatRoomName = message[i];
                                //
                                // Toast.makeText(BroadcastService.this, room.chatRoomName, Toast.LENGTH_SHORT).show();
                                String IP = message[0];
                                Member m = new Member();
                                m.inetAddress = InetAddress.getByName(IP);

                                room.GroupOwner = m;
                                chatRooms.add(room);
                            }
                            ChatService.this.addChatRooms(chatRooms);
                        }
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void addChatRooms(List<ChatRoom> rooms) {
        for(ChatRoom room : rooms) {
            if(!this.chatRooms.contains(room))
                this.chatRooms.add(room);
        }
    }


    public class ChatServer implements Runnable {

        public ServerSocket tSocket;
        ChatServer()
        {
            try {
                tSocket = new ServerSocket(SERVER_PORT);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void run()
        {
            while(true)
            {
                try
                {
                    Socket server = tSocket.accept();
                    InputStream iStream = server.getInputStream();
                    BufferedReader in = new BufferedReader(new InputStreamReader(iStream));

                    String message = null;
                    String[] msg = null;
                    while((message = in.readLine()) != null)
                    {

                        msg = message.split("#");
                        System.out.println ("Server: " + message);
                        updateMessage(msg);

                    }


                    server.close();

                }catch(SocketTimeoutException s)
                {
                    System.out.println("Socket timed out!");
                    break;
                }catch(IOException e)
                {
                    e.printStackTrace();
                    break;
                }
            }
        }
    }

    public class ChatClient implements Runnable {

        public Socket socket;
        private InetAddress mAddress;
        private String message;

        ChatClient(String message, InetAddress mAddress)
        {
            this.mAddress = mAddress;
            this.message = message;

        }

        @Override
        public void run()
        {
            try {
                socket = new Socket();
                socket.bind(null);
                socket.connect(new InetSocketAddress(mAddress.getHostAddress(),
                        SERVER_PORT), 3000);
                OutputStream oStream = socket.getOutputStream();
                oStream.write(message.getBytes());
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    socket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }

        }
    }

    private void updateMessage(String[] msg) {

        if(msg.length >= 3) {
            for (ChatRoom room : chatRooms) {
                if (room.chatRoomName.equals(msg[1])) {
                    List<Member> members = room.members;
                    String userName = msg[2].split(":")[0];
                    addMember(msg[0],userName, room);
                    room.messages.add(msg[2]);
                    if(mOnServiceListener != null) {
                        mOnServiceListener.onDataReceived(msg[2]);
                    }
                    /*Forward the message to members if the message is not from the group owner*/
                    if(!room.GroupOwner.inetAddress.getHostAddress().equals(msg[0])) {
                        sendMessageToPeers(msg, members);
                    }
                }
            }
        }
    }

    public void sendMessageToPeers(String[] msg, List<Member> members)
    {
        List<Member> otherMembers = new ArrayList<>();
        try {
            InetAddress address = InetAddress.getByName(msg[0]);
            for(Member mem : members)
            {
                if(!mem.inetAddress.equals(address))
                {
                    otherMembers.add(mem);
                }
            }
            if(otherMembers.size() > 0)
            {
                msg[0] = DeviceInfo.IpAddress; //Broadcast from current IP Address
                for(Member mem : otherMembers) {
                    String message = formMessage(msg);
                    ChatClient r = new ChatClient(message, mem.inetAddress);
                    Thread t = new Thread(r);
                    t.start();
                }
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    private void addMember(String address, String userName, ChatRoom room)
    {
        try {

            InetAddress mAddress = InetAddress.getByName(address);
            boolean isMemberPresent = false;

            for(Member member : room.members) {

                if(member.inetAddress != null && member.inetAddress.equals(mAddress))
                {
                    isMemberPresent = true;
                }
            }

            if(!isMemberPresent) {
                Member member = new Member();
                member.name = userName;
                member.inetAddress = mAddress;
                room.members.add(member);
            }

        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

    }

    public void sendMessage(String message, String chatRoomName)
    {
        ChatRoom room = findChatRoomInfo(chatRoomName);
        message = DeviceInfo.IpAddress + "#" + chatRoomName + "#" + UserProfile.userName + ": " + message;
        if(DeviceInfo.IpAddress.equals(room.GroupOwner.inetAddress.getHostAddress())) {
            multicastMessage(message, room.members);
        }
        else
        {
            ChatClient r = new ChatClient(message, room.GroupOwner.inetAddress);
            Thread t = new Thread(r);
            t.start();
        }
    }

    public void multicastMessage(String message, List<Member> members)
    {
        if(members.size() > 0)
        {
            for(Member member : members) {
                ChatClient r = new ChatClient(message, member.inetAddress);
                Thread t = new Thread(r);
                t.start();
            }
        }
    }

    private String formMessage(String[] msg) {
        StringBuilder builder = new StringBuilder();
        for(String s : msg) {
            builder.append(s + "#");
        }
        return builder.toString();
    }

    private ChatRoom findChatRoomInfo(String chatRoomName)
    {
        for(ChatRoom room: chatRooms)
        {
            if(room.chatRoomName.equals(chatRoomName))
            {
                return room;
            }
        }
        return null;
    }


}
