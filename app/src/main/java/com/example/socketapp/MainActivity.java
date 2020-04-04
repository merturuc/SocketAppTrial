package com.example.socketapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DownloadManager;
import android.os.Bundle;
import android.os.Message;
import android.util.JsonReader;
import android.view.View;
import android.view.ViewGroup;
import android.view.textclassifier.TextLinks;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class MainActivity extends AppCompatActivity {
 private WebSocket webSocket;
 private MessageAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView messageList = findViewById(R.id.messageList);
        final EditText messageBox = findViewById(R.id.messageBox);
        TextView send = findViewById(R.id.send);

        instantiateWebSocket();
         adapter = new MessageAdapter();
        messageList.setAdapter(adapter);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = messageBox.getText().toString();
                if(!message.isEmpty()){
                    webSocket.send(message);
                    messageBox.setText("");
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("message",message);
                        jsonObject.put("byServer",false);

                        adapter.addItem(jsonObject);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void instantiateWebSocket() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("ws://192.168.1.104:80").build();
        SocketListener socketListener = new SocketListener(this);
         webSocket = client.newWebSocket(request, socketListener );
    }
    public class SocketListener extends WebSocketListener{
        public MainActivity activity;
        public SocketListener(MainActivity activity){
            this.activity = activity;
        }
        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            super.onOpen(webSocket, response);

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity,"Connection Established!",Toast.LENGTH_LONG).show();
                }
            });
        }

        @Override
        public void onMessage(WebSocket webSocket, final String text) {
           // super.onMessage(webSocket, text);
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("message",text);
                        jsonObject.put("byServer",true);

                        adapter.addItem(jsonObject);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }



        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            super.onClosing(webSocket, code, reason);
        }

        @Override
        public void onClosed(WebSocket webSocket, int code, String reason) {
            super.onClosed(webSocket, code, reason);
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, @Nullable Response response) {
            super.onFailure(webSocket, t, response);
        }
    }
    public class MessageAdapter extends BaseAdapter {

        List<JSONObject> messageList = new ArrayList<>();
        @Override
        public int getCount() {
            return messageList.size();
        }

        @Override
        public Object getItem(int i) {
            return messageList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if(view == null){
                view = getLayoutInflater().inflate(R.layout.message_list_item , viewGroup , false);

                TextView sentMessage = view.findViewById(R.id.sentMessage);
                TextView receivedMessage = view.findViewById(R.id.receivedMessage);

                JSONObject item = messageList.get(i);

                try {
                    if(item.getBoolean("byServer")){
                        receivedMessage.setVisibility(View.VISIBLE);
                       receivedMessage.setText(item.getString("message"));
                       sentMessage.setVisibility(View.INVISIBLE);
                    }else{
                        sentMessage.setVisibility(View.VISIBLE);
                        sentMessage.setText(item.getString("message"));
                        receivedMessage.setVisibility(View.INVISIBLE);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            return view;
        }
        void addItem(JSONObject item){
            messageList.add(item);
            notifyDataSetChanged();

        }
    }
}
