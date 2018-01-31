package com.example.cheshta.chatapp.Activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.cheshta.chatapp.Model.Message;
import com.example.cheshta.chatapp.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private EditText etMessage;
    DatabaseReference mDatabase;
    RecyclerView rvMessage;

    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListener;
    FirebaseUser mCurrentUser;
    DatabaseReference mDatabaseUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etMessage = findViewById(R.id.etMessage);
        rvMessage = findViewById(R.id.rvMessage);
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Messages");

        rvMessage.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        rvMessage.setLayoutManager(linearLayoutManager);

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() == null){
                    startActivity(new Intent(MainActivity.this, RegisterActivity.class));
                }
            }
        };
    }

    public void sendButtonClicked(View view){
        mCurrentUser = mAuth.getCurrentUser();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());
        final String messageValue = etMessage.getText().toString();
        if (!messageValue.isEmpty()){
            final DatabaseReference newPost = mDatabase.push();
            mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    newPost.child("content").setValue(messageValue);
                    etMessage.setText("");
                    newPost.child("username").setValue(dataSnapshot.child("Username").getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                        }
                    });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            rvMessage.scrollToPosition(rvMessage.getAdapter().getItemCount());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        FirebaseRecyclerAdapter<Message, MessageViewHolder> FBRA = new FirebaseRecyclerAdapter<Message, MessageViewHolder>(
                Message.class,
                R.layout.single_message_layout,
                MessageViewHolder.class,
                mDatabase
        ) {
            @Override
            protected void populateViewHolder(MessageViewHolder viewHolder, Message model, int position) {
                viewHolder.setContent(model.getContent());
                viewHolder.setUsername(model.getUsername());
            }
        };
        rvMessage.setAdapter(FBRA);
    }

    public static class MessageViewHolder  extends RecyclerView.ViewHolder{

        View mView;

        public MessageViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setContent(String content){
            TextView tvMessage = mView.findViewById(R.id.tvMessage);
            tvMessage.setText(content);
        }

        public void setUsername(String username){
            TextView tvUsername = mView.findViewById(R.id.tvUsername);
            tvUsername.setText(username);
        }
    }
}
