package tomer.spivak.androidstudio2dgame.intermediate;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import tomer.spivak.androidstudio2dgame.R;

public class AboutFragment extends Fragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_about, container, false);
        Button btnContactUs = view.findViewById(R.id.btnContactUs);
        btnContactUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("message/rfc822"); // Ensures only email apps show
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"spivak.toti@gmail.com"});
                intent.putExtra(Intent.EXTRA_SUBJECT, "Contact Us");
                startActivity(Intent.createChooser(intent, "Choose an Email client:"));
            }
        });
        return view;
    }
}