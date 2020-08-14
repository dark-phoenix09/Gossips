package com.example.gossips;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChatActivity extends AppCompatActivity {

    Toolbar toolbar;
    FirebaseAuth mAuth=FirebaseAuth.getInstance();
    FirebaseUser mCurrrentuser=mAuth.getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        toolbar=findViewById(R.id.chat_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Gossips");
        getSupportActionBar().setSubtitle("Say Hello!");

        if(mCurrrentuser.isEmailVerified()==false){
            Toast.makeText(this, "Please, verify your Email-ID.", Toast.LENGTH_SHORT).show();
            Intent intent=new Intent(ChatActivity.this,Login.class);
            startActivity(intent);
            finish();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_toolbar_item,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        switch(item.getItemId()){
            case R.id.chat_logout_btn:
                mAuth.signOut();
                startActivity(new Intent(ChatActivity.this, Login.class));
                finish();
                break;
            case R.id.accnt_btn:
                startActivity(new Intent(ChatActivity.this,Accnt_setup.class));
                break;
            case R.id.find_friend_btn:
                startActivity(new Intent(ChatActivity.this,findFriendActivity.class));
                break;
            case R.id.my_request_btn:
                startActivity(new Intent(ChatActivity.this,myRequests.class));
                break;
            case R.id.my_friend_btn:
                startActivity(new Intent(ChatActivity.this,my_friends.class));
                break;
        }
        return  true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mCurrrentuser.isEmailVerified()==false){
            Toast.makeText(this, "Please, verify your Email-ID.", Toast.LENGTH_SHORT).show();
            Intent intent=new Intent(ChatActivity.this,Login.class);
            startActivity(intent);
            finish();
        }
    }
}