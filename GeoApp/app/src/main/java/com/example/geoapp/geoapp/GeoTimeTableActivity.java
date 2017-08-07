package com.example.geoapp.geoapp;

import android.icu.text.AlphabeticIndex;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class GeoTimeTableActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geo_time_table);
    }

    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

        private List<AlphabeticIndex.Record> records;

        public RecyclerViewAdapter(List<AlphabeticIndex.Record> records) {
            this.records = records;
        }

        /**
         * Создание новых View и ViewHolder элемента списка, которые впоследствии могут переиспользоваться.
         */
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recyclerview_item, viewGroup, false);
            return new ViewHolder(v);
        }

        /**
         * Заполнение виджетов View данными из элемента списка с номером i
         */
        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int i) {
            AlphabeticIndex.Record record = records.get(i);
            //viewHolder.name.setText(record.getName());
        }

        @Override
        public int getItemCount() {
            return records.size();
        }

        /**
         * Реализация класса ViewHolder, хранящего ссылки на виджеты.
         */
        class ViewHolder extends RecyclerView.ViewHolder {
            private TextView name;
            private ImageView icon;

            public ViewHolder(View itemView) {
                super(itemView);
                //name = (TextView) itemView.findViewById(R.id.recyclerViewItemName);
                //icon = (ImageView) itemView.findViewById(R.id.recyclerViewItemIcon);
            }
        }
    }
}
