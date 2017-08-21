package yxdroid.qrbar.demo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import yxdroid.fastqr.encode.ErrorCorrection;
import yxdroid.fastqr.encode.QRCode;
import yxdroid.fastqr.encode.FastQRCodeGenerator;

public class MainActivity extends AppCompatActivity {

    ImageView ivQr;

    View rootView;

    MyAdapter myAdapter;

    CheckBox ckCorner;
    CheckBox ckLogo;
    CheckBox ckBorder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        rootView = getLayoutInflater().inflate(R.layout.activity_main, null);

        setContentView(rootView);

        ivQr = (ImageView) findViewById(R.id.iv_qr);

        RecyclerView lvColor = (RecyclerView) findViewById(R.id.lv_color);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        lvColor.setLayoutManager(mLayoutManager);
        lvColor.setHasFixedSize(true);
        myAdapter = new MyAdapter(initData());
        lvColor.setAdapter(myAdapter);

        ckCorner = (CheckBox) findViewById(R.id.ck_corner);
        ckCorner.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    rootView.setBackgroundColor(Color.TRANSPARENT);
                } else {
                    rootView.setBackgroundColor(Color.WHITE);
                }
                genCode();
            }
        });

        ckLogo = (CheckBox) findViewById(R.id.ck_logo);
        ckLogo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                genCode();
            }
        });

        ckBorder = (CheckBox) findViewById(R.id.ck_border);
        ckBorder.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                genCode();
            }
        });


        genCode();
    }

    private int[] initData() {
        int firstColor = R.mipmap.color1;
        int[] colorImages = new int[30];
        for (int i = 0; i < colorImages.length; i++) {
            colorImages[i] = firstColor + i;
        }
        return colorImages;
    }

    private void genCode() {
        QRCode.Builder builder = new QRCode.Builder(MainActivity.this)
                // 二维码内容
                .content("hello world")
                // 二维码前景色
                .oneColor(Color.BLACK)
                // 二维码背景色
                .zeroColor(Color.WHITE)
                // 二维码边距
                .margin(1)
                // 通过bitmap 来设置二维码前景色
                .colorBitmap(myAdapter.getCurrentColor())
                // 圆角
                .corner(14)
                // 中间logo
                .logo(R.mipmap.logo)
                // 边框
                .border(R.mipmap.border2)
                // 二维码容错级别
                .errorCorrection(ErrorCorrection.H);

        if (ckCorner.isChecked()) {
            builder.corner(14);
        }

        if (ckLogo.isChecked()) {
            builder.logo(R.mipmap.logo);
        }

        if (ckBorder.isChecked()) {
            builder.border(R.mipmap.border2);
        }

        Bitmap bitmap = FastQRCodeGenerator.genQrCodeBitmap(builder.build());
        ivQr.setImageBitmap(bitmap);
    }

    public void onScan(View view) {
        startActivity(new Intent(this, QrScanActivity.class));
    }

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        public int[] colorImages = null;

        private int currentPosition = -1;

        public int getCurrentColor() {
            if (currentPosition == -1) {
                return 0;
            }
            return colorImages[currentPosition];
        }

        public MyAdapter(int[] colorImages) {
            this.colorImages = colorImages;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_lv_color,
                    viewGroup, false);
            ViewHolder vh = new ViewHolder(view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = (int) v.getTag();
                    currentPosition = position;
                    genCode();
                }
            });
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int position) {
            viewHolder.ivColor.setBackgroundResource(colorImages[position]);
            viewHolder.itemView.setTag(position);
        }

        @Override
        public int getItemCount() {
            return colorImages.length;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            public ImageView ivColor;

            public ViewHolder(View view) {
                super(view);
                ivColor = (ImageView) view.findViewById(R.id.iv_color);
            }
        }
    }
}
