package com.example.gossips;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class messageActivity extends AppCompatActivity {

    Toolbar toolbar;
    CircleImageView msg_image;
    TextView msg_name;
    EditText msg;
    ImageView send_btn;
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    String fid,uid,name,image;
    RecyclerView recyclerView;
    FirestoreRecyclerAdapter adapter;
    ImageView floatingActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        //initialization
        floatingActionButton=findViewById(R.id.floatingActionButton);
        recyclerView=findViewById(R.id.message_recyclerview);
        db=FirebaseFirestore.getInstance();
        mAuth=FirebaseAuth.getInstance();
        send_btn=findViewById(R.id.send_btn);
        msg=findViewById(R.id.msg);
        msg_name=findViewById(R.id.msg_name);
        msg_image=findViewById(R.id.msg_image);
        toolbar=findViewById(R.id.msg_toolbar);
        uid= Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        fid=getIntent().getStringExtra("fid");
        name=getIntent().getStringExtra("name");
        msg_name.setText(name);

        //set up msg environment
        db.collection("users").document(fid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    image=documentSnapshot.getString("image");
                    Picasso.get().load(image).into(msg_image);
                }else{
                    startActivity(new Intent(messageActivity.this,ChatActivity.class));
                    finish();
                }
            }
        });

        //send msg
        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String m=msg.getText().toString();
                if(m.isEmpty()){
                    Toast.makeText(messageActivity.this,"no message!", Toast.LENGTH_SHORT).show();
                }else{
                    sendMsg(m);
                    msg.setText("");
                }
            }
        });

        //displaying messages
        //query
        Query query=db.collection("users").document(uid).collection("chats").document(fid).collection("chat")
                .orderBy("time", Query.Direction.ASCENDING);

        //options
        FirestoreRecyclerOptions<msg_data_model>options=new FirestoreRecyclerOptions.Builder<msg_data_model>()
                .setQuery(query,msg_data_model.class).build();

        //adapter
        adapter= new FirestoreRecyclerAdapter<msg_data_model, msg_data_holder>(options) {
            @NonNull
            @Override
            public msg_data_holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.single_msg_layout,parent,false);
                return new msg_data_holder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull msg_data_holder holder, int position, @NonNull msg_data_model model) {
                if(uid.equals(model.getSender())){
                    holder.rec_msg.setText("");
                    holder.send_msg.setText(model.getMsg());
                }else{
                    holder.send_msg.setText("");
                    holder.rec_msg.setText(model.getMsg());
                }
            }
        };

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        ((LinearLayoutManager) Objects.requireNonNull(recyclerView.getLayoutManager())).setStackFromEnd(true);

        //goto down
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerView.scrollToPosition(adapter.getItemCount()-1);
                floatingActionButton.setVisibility(View.INVISIBLE);
            }
        });
        //scroll detection
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if(dy>0){
                    floatingActionButton.setVisibility(View.INVISIBLE);
                }else{
                    floatingActionButton.setVisibility(View.VISIBLE);
                }
            }
        });

    }


    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    //sending message
    public  void sendMsg(String s){
        HashMap<String,String>mp=new HashMap<>();
        mp.put("sender",uid);
        mp.put("receiver",fid);
        mp.put("msg",s);
        mp.put("time",String.valueOf(System.currentTimeMillis()));
        db.collection("users").document(uid).collection("chats").document(fid).collection("chat")
                .document().set(mp);
        db.collection("users").document(fid).collection("chats").document(uid).collection("chat")
                .document().set(mp).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                recyclerView.scrollToPosition(adapter.getItemCount()-1);
            }
        });
        db.collection("users").document(uid).collection("chats").document(fid).set(mp);
        db.collection("users").document(uid).collection("chats").document(uid).set(mp);
    }

    private class msg_data_holder extends RecyclerView.ViewHolder {
        ConstraintLayout send_msg_layout,rec_msg_layout;
        TextView send_msg,rec_msg;
        public msg_data_holder(@NonNull View itemView) {
            super(itemView);
            send_msg_layout=itemView.findViewById(R.id.send_msg_layout);
            rec_msg_layout=itemView.findViewById(R.id.rec_msg_layout);
            send_msg=itemView.findViewById(R.id.send_msg_text);
            rec_msg=itemView.findViewById(R.id.rec_msg_text);
        }
    }
}