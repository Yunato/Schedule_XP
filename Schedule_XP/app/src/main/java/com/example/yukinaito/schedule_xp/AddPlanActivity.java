package com.example.yukinaito.schedule_xp;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.text.DecimalFormat;
import java.text.Format;
import java.util.ArrayList;

public class AddPlanActivity extends AppCompatActivity {
    //データ
    private int date = 0;
    private int startTime = 0;
    private int overTime = 0;

    private boolean editFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_plan);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //region 前画面に戻るボタンの生成
        final Drawable upArrow = ResourcesCompat.getDrawable(getResources(), R.drawable.abc_ic_ab_back_material, null);
        upArrow.setColorFilter(ContextCompat.getColor(this, R.color.input), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        //endregion

        //region リスナーの登録
        findViewById(R.id.input_date).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //region 予定の日時を選択 DatePickerの呼び出し
                DatePickerDialogFragment datePickerDialog = new DatePickerDialogFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("Activity", 1);
                datePickerDialog.setArguments(bundle);
                datePickerDialog.show(getSupportFragmentManager(), "datePicker");
                //endregion
            }
        });
        findViewById(R.id.input_startTime).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //region 予定の開始時刻を選択 TimePickerの呼び出し
                TimePickerDialogFragment timePickerDialog = new TimePickerDialogFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("Activity", 1);
                timePickerDialog.setArguments(bundle);
                timePickerDialog.show(getSupportFragmentManager(), "timePicker");
                //endregion
            }
        });
        findViewById(R.id.input_overTime).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //region 予定の制限(終了)時刻を選択 TimePickerの呼び出し
                TimePickerDialogFragment timePickerDialog = new TimePickerDialogFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("Activity", 2);
                timePickerDialog.setArguments(bundle);
                timePickerDialog.show(getSupportFragmentManager(), "timePicker");
                //endregion
            }
        });
        //endregion

        //region 編集かどうか
        Card editCard;
        if((editCard = (Card) getIntent().getSerializableExtra("EditCard")) != null){
            editFlag = true;
            Format format = new DecimalFormat("00");
            date = editCard.getDate();
            startTime = editCard.getStartTime();
            overTime = editCard.getOverTime();
            ((Button)findViewById(R.id.input_date)).setText(Integer.toString(date).substring(0, 4) + "年"
                                                          + Integer.toString(date).substring(4, 6) + "月"
                                                          + Integer.toString(date).substring(6, 8) + "日");
            ((Button)findViewById(R.id.input_startTime)).setText(format.format(startTime / 100) + "時"
                                                               + format.format(startTime % 100) + "分");
            ((Button)findViewById(R.id.input_overTime)).setText(format.format(overTime / 100) + "時"
                                                              + format.format(overTime % 100) + "分");
            ((EditText)findViewById(R.id.input_content)).setText(editCard.getContent());
            ((EditText)findViewById(R.id.input_place)).setText(editCard.getPlace());
            if(editCard.getMemo() != null){
                ((EditText)findViewById(R.id.input_memo)).setText(editCard.getMemo());
            }
        }
        //endregion
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuInflater inflater = getMenuInflater();
        if(editFlag)
            inflater.inflate(R.menu.edit_menu, menu);
        else
            inflater.inflate(R.menu.add_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //Pickerによるデータの反映処理
    public void onReturnValue(int data, String text, int picker) {
        if(picker == 1) {
            date = data;
            Button button = (Button) findViewById(R.id.input_date);
            button.setText(text);
        }else if(picker == 2){
            startTime = data;
            Button button = (Button) findViewById(R.id.input_startTime);
            button.setText(text);
        }else if(picker == 3){
            overTime = data;
            Button button = (Button) findViewById(R.id.input_overTime);
            button.setText(text);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //戻るキーを押されたときの処理
            if(editFlag){
                Intent intent = new Intent();
                intent.putExtra("AddEditCard", getIntent().getSerializableExtra("EditCard"));
                intent.putExtra("Index", getIntent().getIntExtra("Index", -1));
                setResult(RESULT_OK, intent);
            }
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    //タイトルバーのオブジェクトが選択されたとき
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            //region 前画面に戻るボタンタップ時
            Card editCard;
            if((editCard = (Card) getIntent().getSerializableExtra("EditCard")) != null){
                Intent intent = new Intent();
                intent.putExtra("AddEditCard", editCard);
                intent.putExtra("Index", getIntent().getIntExtra("Index", -1));
                setResult(RESULT_OK, intent);
            }
            finish();
            //endregion
        }
        if(id == R.id.add_action || id == R.id.update_action){
            //region 追加|更新ボタンタップ時
            //入力チェック
            boolean check = inputCheck();
            int index= addCheck();
            if(!check || index < 0){
                //ダイアログの生成
                String message = "追加できませんでした。以下の項目を確認してください。\n";
                if(!check){
                    message += "\n・表示している予定の入力欄の日付、開始時刻、終了時刻、内容、場所が入力されているか" +
                            "\n・最下位の予定に制限(終了)時刻が入力されているか";
                }
                if(index < 0){
                    index = (index + 1) * -1;
                    ArrayList<Card> cards = ((ScheduleApplication) getApplication()).getPlanCards();
                    Card overlapCard = cards.get(index);
                    Format format = new DecimalFormat("00");
                    String date = Integer.toString(overlapCard.getDate());
                    int start = overlapCard.getStartTime();
                    int finish;
                    if(overlapCard.getConnect()){
                        if(cards.size() == (index + 1)){
                            finish = 2400;
                        }else{
                            finish = cards.get(index + 1).getStartTime();
                        }
                    }
                    else{
                        finish = overlapCard.getOverTime();
                    }
                    start = (start / 100) * 60 + (start % 100);
                    finish = (finish / 100) * 60 + (finish % 100);

                    message += "\n・次の予定と日時が重なっています。\n　　"
                            + "日付 : " + date.substring(0, 4) + "/" + date.substring(4, 6) + "/" + date.substring(6, 8) + "\n　　"
                            + "時刻 : " + format.format(start / 60) + ":" + format.format(start % 60) +
                            " - " + format.format(finish / 60) + ":" + format.format(finish % 60) + "\n　　"
                            + "内容 : " + overlapCard.getContent() + "\n　　"
                            + "場所 : " + overlapCard.getPlace();
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(message);
                builder.setPositiveButton("OK", null);
                builder.show();
                return super.onOptionsItemSelected(item);
            }

            //追加するオブジェクトを作成
            Card addCard = new Card(date, startTime, overTime, false,
                    ((EditText)findViewById(R.id.input_content)).getText().toString(),
                    ((EditText)findViewById(R.id.input_place)).getText().toString());
            if(!((EditText)findViewById(R.id.input_memo)).getText().toString().equals("")){
                addCard.setMemo(((EditText)findViewById(R.id.input_memo)).getText().toString());
            }
            if(editFlag){
                addCard.setId(Long.parseLong(((Card) getIntent().getSerializableExtra("EditCard")).getId()));
            }

            //intent作成
            Intent intent = new Intent();
            intent.putExtra("AddEditCard", addCard);
            intent.putExtra("Index", index);
            setResult(RESULT_OK, intent);
            finish();
            //endregion
        }

        return super.onOptionsItemSelected(item);
    }

    //入力チェック 未選択|空欄でないかどうか
    public boolean inputCheck(){
        if(!((Button)findViewById(R.id.input_date)).getText().toString().equals("日付の指定[タップ]") &&
                !((Button)findViewById(R.id.input_startTime)).getText().toString().equals("時刻の指定[タップ]") &&
                !((Button)findViewById(R.id.input_overTime)).getText().toString().equals("時刻の指定[タップ]") &&
                ((EditText)findViewById(R.id.input_content)).getText().toString().trim().length() != 0 &&
                ((EditText)findViewById(R.id.input_place)).getText().toString().trim().length() != 0)
            return true;
        return false;
    }

    //追加する予定が他の予定と重なっていないかチェック&追加位置を返す
    public int addCheck(){
        //後で変更 アプリケーションから取得
        ArrayList<Card> planCards = ((ScheduleApplication)getApplication()).getPlanCards();

        //予定が空のとき
        if(planCards.size() == 0)
            return 0;

        //確認処理
        for(int index = 0; index < planCards.size(); index++) {
            int originalDate = planCards.get(index).getDate();
            if (originalDate > date) {
                return index;
            } else if (originalDate == date) {
                //同日付の予定の重なり|前後を調べる
                int originalStart = planCards.get(index).getStartTime();
                int originalOver = planCards.get(index).getOverTime();
                if(originalStart >= startTime){
                    if(originalStart < overTime) {
                        if(originalStart == startTime){
                            continue;
                        }else {
                            return -1 * index - 1;
                        }
                    }else{
                        if(planCards.size() > index + 1 && originalStart == startTime && originalStart == overTime){
                            return indexDownCheck(planCards, index + 1);
                        }
                        return index;
                    }
                }else if(startTime < originalOver){
                    return -1 * index - 1;
                }
            }
        }
        return planCards.size();
    }

    public int indexDownCheck(ArrayList<Card> planCards, int index){
        int originalStart = planCards.get(index).getStartTime();
        int originalOver = planCards.get(index).getOverTime();
        if(originalStart < overTime && startTime < originalOver){
            return -1 * (index - 1) - 1;
        }else{
            if(planCards.size() > index + 1 && originalStart == startTime && originalStart == overTime){
                return indexDownCheck(planCards, index + 1);
            }else{
                return index;
            }
        }
    }
}
