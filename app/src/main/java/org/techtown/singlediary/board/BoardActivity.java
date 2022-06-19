package org.techtown.singlediary.board;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import org.techtown.singlediary.R;

import java.util.ArrayList;
import java.util.List;

public class BoardActivity extends AppCompatActivity implements View.OnClickListener{

    private RecyclerView recyclerView;
    private Button button;

    private MainAdapter mainAdapter;
    private List<Board> boardList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);

        recyclerView = findViewById(R.id.main_recycler_view);
        button = findViewById(R.id.main_write_button);

        boardList = new ArrayList<>();
        boardList.add(new Board(null,"반갑습니다",null, "android"));
        boardList.add(new Board(null,"Hello",null, "server"));
        boardList.add(new Board(null,"OK",null, "java"));
        boardList.add(new Board(null,"안녕하세요",null, "php"));
        boardList.add(new Board(null,"ㅋㅋㅋㅋㅋㅋ",null, "python"));

        mainAdapter = new MainAdapter(boardList);
        recyclerView.setAdapter(mainAdapter);


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    @Override
    public void onClick(View view) {

    }

    private class MainAdapter extends RecyclerView.Adapter<MainAdapter.MainViewHolder>{

        private List<Board> BoardList;

        public MainAdapter(List<Board> boardList) {
            this.BoardList = boardList;
        }

        @NonNull
        @Override
        public MainViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MainViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_main,parent,false));
        }

        @Override
        public void onBindViewHolder(@NonNull MainViewHolder holder, int position) {
            Board data = BoardList.get(position);
            holder.TitleTextView.setText(data.getTitle());
            holder.NameTextView.setText(data.getName());
        }

        @Override
        public int getItemCount() {
            return BoardList.size();
        }

        class MainViewHolder extends RecyclerView.ViewHolder{

            private TextView TitleTextView;
            private TextView NameTextView;

            public MainViewHolder(View itemView) {
                super(itemView);

                TitleTextView = itemView.findViewById(R.id.item_title_text);
                NameTextView = itemView.findViewById(R.id.item_name_text);
            }
        }
    }

}
