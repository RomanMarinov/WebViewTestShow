package com.dev_marinov.webviewtest;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.HashMap;

public class FragmentFormReg extends Fragment {

    View frag;

    EditText edt_login_post, edt_fio;
    TextView tv_next, tv_blink;
    Button bt_reg;
    SharedPreferences sharedPreferences;

    HashMap<Integer, cl_fio_login> hashMap = new HashMap<>();
    String fio, login;
    Animation anim;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.e("frag_reg","-ОТКРЫЛСЯ ФРАГМЕНТ-");

        frag = inflater.inflate(R.layout.fragment_form_reg, container, false);

        edt_login_post = frag.findViewById(R.id.edt_login_post);
        edt_fio = frag.findViewById(R.id.edt_fio);
        tv_next = frag.findViewById(R.id.tv_next);
        tv_blink = frag.findViewById(R.id.tv_blink);
        bt_reg = frag.findViewById(R.id.bt_reg);

        ConstraintLayout constraintLayout = frag.findViewById(R.id.cl_main);
        AnimationDrawable animationDrawable = (AnimationDrawable) constraintLayout.getBackground();
        animationDrawable.setEnterFadeDuration(1500);
        animationDrawable.setExitFadeDuration(3000);
        animationDrawable.start();

            bt_reg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    login = edt_login_post.getText().toString();
                    fio = edt_fio.getText().toString();

                    if (login.equals("") | fio.equals(""))
                    {
                        tv_blink.setText("fill in the lines");
                        tv_blink.setVisibility(View.VISIBLE);
                        anim = AnimationUtils.loadAnimation(getContext(), R.anim.blink);
                        tv_blink.startAnimation(anim);
                    }
                    else
                    {
                        // выгрузка данных из sharedpref
                        String json1 = loadSettingString("json","");

                        Gson gjson2 = new Gson();
                        int flag  =0;

                        int i=0;
                        Log.e("test","json1="+json1);
                        try {
                            JSONObject jsonObject = new JSONObject(json1);

                            for (i = 0; i < jsonObject.length(); i++) {
                                String fio_file =jsonObject.getJSONObject(""+i).getString("fio");
                                String login_file =jsonObject.getJSONObject(""+i).getString("login");
                                Log.e("fio",""+fio);
                                hashMap.put(i, new cl_fio_login(fio_file, login_file));

                                if ((fio.equals(fio_file)) | (login.equals(login_file))) // если такие данные уже есть
                                {
                                    flag = 1;
                                    break;

                                }
                                else // иначе таких введенных данных нет и переход в fragenter
                                {

                                    edt_login_post.setText("");
                                    edt_fio.setText("");

                                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                                    FragmentEnter fragmentEnter = new FragmentEnter();
                                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                    fragmentTransaction.replace(R.id.ll_frag_enter, fragmentEnter);
                                    fragmentTransaction.addToBackStack(null);
                                    fragmentTransaction.commit();
                                    break; // чтобы при нажатии кнопки назад из след фрагмента
                                    // выполнился один раз, иначе кнопку назад надо будет нажать столько раз
                                    // сколько в массиве записей
                                }

                                //hashMap.put(i,new cl_fio_login(fio,login));


                            }

                        } catch (Exception e) {
                            Log.e("frag_reg","-try_catch-" + e);
                        }

                        if (flag == 1)
                        {
// написать ошибку
                            tv_blink.setText("this login already exists");
                            tv_blink.setVisibility(View.VISIBLE);
                            anim = AnimationUtils.loadAnimation(getContext(), R.anim.blink);
                            tv_blink.startAnimation(anim);
                        }
                        else
                        {
                            hashMap.put(hashMap.size(), new cl_fio_login(fio, login));
                            Gson gjson = new Gson();
                            String json = gjson.toJson(hashMap);
                            saveSettingString("json", json);
                        }
                    }

                   // HashMap<Integer, cl_fio_login> hashMap2 =     gjson2.fromJson(json1, HashMap.class); // HashMap.class какой класс
                   // Log.e("test","hashMap2="+hashMap2.size());
                  //  Log.e("test","hashMap2="+hashMap2.get(0));
                //Log.e("test","index0="+hashMap2.get(0).fio+ " "+hashMap2.get(0).login);
                }
            });

        tv_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("frag_reg","-tv_next click-");
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentEnter fragmentEnter = new FragmentEnter();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.ll_frag_enter, fragmentEnter);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });

        return frag;
    }

    // считывает файл
    public String loadSettingString(String key, String default_value) {
        sharedPreferences = getActivity().getSharedPreferences("myPref_reg_form", MODE_PRIVATE);
        return sharedPreferences.getString(key, default_value);
    }

    // сохраняет в файл
    public void saveSettingString(String key, String value) {
        sharedPreferences = getActivity().getSharedPreferences("myPref_reg_form", MODE_PRIVATE);
        SharedPreferences.Editor ed = sharedPreferences.edit(); // edit() - редактирование файлов
        ed.putString(key, value); // добавляем ключ и его значение
        if (ed.commit()) // сохранить файл
        {
            //успешно записано данные в файл
        }
        else
        {
            //ошибка при записи
            Toast.makeText(getActivity(), "Write error", Toast.LENGTH_SHORT).show();
        }
    }

}


