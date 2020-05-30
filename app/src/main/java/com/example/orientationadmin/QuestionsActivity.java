package com.example.orientationadmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class QuestionsActivity extends AppCompatActivity {
    private Button add,excel;
    private RecyclerView recyclerView;
    private Toolbar toolbar;
    private QuestionsAdapter adapter;
    private List<QuestionModel> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questions);


        toolbar = findViewById(R.id.toolbar_questions);
        setSupportActionBar(toolbar);
        String CategoryName = getIntent().getStringExtra("category");
        getSupportActionBar().setTitle(CategoryName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        add = findViewById(R.id.add_btn);
        excel = findViewById(R.id.excel_btn);
        recyclerView = findViewById(R.id.question_recyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        list = new ArrayList<>();
        list.add(new QuestionModel("fdsdsjs","Question?","A","B","C","D","A"));
        list.add(new QuestionModel("fdsdsjs","Question?","A","B","C","D","A"));
        list.add(new QuestionModel("fdsdsjs","Question?","A","B","C","D","A"));
        list.add(new QuestionModel("fdsdsjs","Question?","A","B","C","D","A"));
        list.add(new QuestionModel("fdsdsjs","Question?","A","B","C","D","A"));
        list.add(new QuestionModel("fdsdsjs","Question?","A","B","C","D","A"));
        list.add(new QuestionModel("fdsdsjs","Question?","A","B","C","D","A"));
        list.add(new QuestionModel("fdsdsjs","Question?","A","B","C","D","A"));
        adapter = new QuestionsAdapter(list);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);

    }
}
