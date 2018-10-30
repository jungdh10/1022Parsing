package com.example.a503_14.a1022parsing;

import android.content.Intent;
import android.net.Uri;
import android.os.Message;
import android.provider.DocumentsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import android.os.*;

public class MainActivity extends AppCompatActivity {
    //ListView 출력을 위한 변수
    ArrayList<String> list;
    ArrayList<String> linkList;

    ArrayAdapter<String> adapter;
    ListView listView;

    //데이터를 다운로드 받는 스레드
    class ThreadEx extends Thread{
        @Override
        public void run(){
            try{
                //다운로드 받을 주소 생성(사이트가 EUC-KR이기 때문에 아래 인코딩설정)
                URL url = new URL("https://movie.naver.com/movie/sdb/rank/rmovie.nhn");
                //URL에 연결
                HttpURLConnection con = (HttpURLConnection)url.openConnection();
                //옵션 설정
                con.setUseCaches(false);
                con.setConnectTimeout(20000);

                //다운로드 받은 문자열을 저장하기 위한 인스턴스 생성
                StringBuilder sb = new StringBuilder();
                //문자열을 다운로드 받기 위한 스트림을 생성
                BufferedReader br = null;
                //헤더 가져오기
                //String headerType = con.getContentType();
                //있으면 EUC-KR, 없으면 else
                //if(headerType.toUpperCase().indexOf("UTF-8") >= 0){
                    br = new BufferedReader(new InputStreamReader(con.getInputStream(), "EUC-KR"));
                //}else{
                //    br = new BufferedReader(new InputStreamReader(con.getInputStream(), "EUC-KR"));
                //}

                //문자열을 읽어서 저장
                while(true){
                    String line = br.readLine();
                    if(line == null) break;
                    sb.append(line + "\n");
                }
                //읽은 데이터 확인
                //Log.e("html", sb.toString());
                //사용한 스트림과 연결 해제
                br.close();
                con.disconnect();
                //파싱하는 메소드 호출
                parsing(sb.toString());

            }catch(Exception e){
                Log.e("다운로드 실패", e.getMessage());
            }
        }
    }

    public void parsing(String html){
        //html 을 Memory에 DOM으로 펼치기
        Document dom = Jsoup.parse(html);
        //원하는 항목을 추출
        Elements elements = dom.select("a");
        //iterator를 이용해서 순회
        for(Element element : elements){
            //태그 안의 내용을 리스트에 추가
            list.add(element.text().trim());
            //태그 내의 href 속성의 값을 리스트에 추가
            linkList.add(element.attr("href"));
        }
        //리스트 뷰를 다시 출력 - 핸들러를 호출해서 수행
        handler.sendEmptyMessage(0);
    }

    //리스트 뷰의 내용을 다시 출력할 핸들러
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            adapter.notifyDataSetChanged();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        linkList=new ArrayList<>();
        list = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);
        listView = (ListView)findViewById(R.id.listView);
        listView.setAdapter(adapter);

        Button btn = (Button)findViewById(R.id.html);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ThreadEx th = new ThreadEx();
                th.start();
            }
        });

        //listView를 클릭했을 때(이벤트가 밸생한 대상이 첫번째 매개변수 parent<-listView, possition은 몇번째인지)
        listView.setOnItemClickListener(new ListView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int possition, long id){
                Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(linkList.get(possition)));
                startActivity(intent);
            }
        });

    }
}
