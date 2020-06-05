package com.example.orientationadmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class QuestionsActivity extends AppCompatActivity {
    private Button add,excel;
    private RecyclerView recyclerView;
    private Toolbar toolbar;
    private QuestionsAdapter adapter;
    public static List<QuestionModel> list;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questions);


        toolbar = findViewById(R.id.toolbar_questions);
        setSupportActionBar(toolbar);
        final String CategoryName = getIntent().getStringExtra("category");
        getSupportActionBar().setTitle(CategoryName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loadingBar = new ProgressDialog(this);

        add = findViewById(R.id.add_btn);
        excel = findViewById(R.id.excel_btn);
        recyclerView = findViewById(R.id.question_recyclerView);



        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        list = new ArrayList<>();
        adapter = new QuestionsAdapter(list);
        recyclerView.setAdapter(adapter);
        getData(CategoryName);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addQuestion = new Intent(QuestionsActivity.this,AddQuestionActivity.class);
                addQuestion.putExtra("categoryName",CategoryName);
                startActivity(addQuestion);
            }
        });




    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);

    }

    private void getData(String CategoryName){
        loadingBar.setMessage("Please wait...");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();
        FirebaseDatabase.getInstance().getReference()
                .child("SETS").child(CategoryName)
                .child("questions").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Fixing the order by Later
                for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren()){
                    String id = dataSnapshot1.getKey();
                    String question = dataSnapshot1.child("question").getValue().toString();
                    String A = dataSnapshot1.child("optionA").getValue().toString();
                    String B = dataSnapshot1.child("optionB").getValue().toString();
                    String C = dataSnapshot1.child("optionC").getValue().toString();
                    String D = dataSnapshot1.child("optionD").getValue().toString();
                    String correctAns = dataSnapshot1.child("correctAns").getValue().toString();

                    list.add(new QuestionModel(id,question,A,B,C,D,correctAns));
                }
                loadingBar.dismiss();
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(QuestionsActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.notifyDataSetChanged();
    }
}
