package com.example.rigot.socialapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class ViewPhotos extends AppCompatActivity implements View.OnClickListener{

    private int index = 0;
    int maxIndex;
    Album album;
    TextView tvAlbumName;
    ImageView ivPhoto, prevImgButton, nextImgButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_photos);

        album = (Album) getIntent().getSerializableExtra("album");
        tvAlbumName = (TextView)findViewById(R.id.textViewPicsAlbumName);
        ivPhoto = (ImageView)findViewById(R.id.imageViewPhotos);
        prevImgButton = (ImageView)findViewById(R.id.imageViewPrevPhoto);
        nextImgButton = (ImageView)findViewById(R.id.imageViewNextPhoto);
        Picasso.with(this).load(album.getImages().get(index)).into(ivPhoto);

        //set click listeners
        nextImgButton.setOnClickListener(this);
        prevImgButton.setOnClickListener(this);

        //set album name
        tvAlbumName.setText(album.getTitle());
        //set the max index
        maxIndex = album.getImages().size() - 1;

    }

    @Override
    public void onClick(View view) {
        int i = view.getId();

        if(i == R.id.imageViewNextPhoto){
            if(index == maxIndex){
                index = 0;
            } else {
                index++;
            }
            Picasso.with(ViewPhotos.this).load(album.getImages().get(index)).into(ivPhoto);
        }else if(i == R.id.imageViewPrevPhoto){
            if(index == 0){
                index = maxIndex;
            } else {
                index--;
            }
            Picasso.with(ViewPhotos.this).load(album.getImages().get(index)).into(ivPhoto);
        }
    }
}
