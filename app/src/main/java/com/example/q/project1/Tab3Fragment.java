package com.example.q.project1;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import java.util.ArrayList;
import java.util.zip.Inflater;

// Instances of this class are fragments representing a single
// object in our collection.

public class Tab3Fragment extends Fragment implements View.OnClickListener {
    static final int NEW_DRAWING_REQUEST = 1;
    private ArrayList<Tab3Item> images;
    private View rootView;
    private GridView grid_view;
    private GridAdapter grid_adapter;
    private ListView list_view;
    private ListAdapter list_adapter;

    private ViewFlipper flipper;

    private static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 2;
    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 3;
    static final int IMAGE_PREVIEW = 100;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.tab3, container, false);

        flipper = (ViewFlipper) rootView.findViewById(R.id.tab3_flipper);

        Button flip_btn = (Button) rootView.findViewById(R.id.tab3_flip_btn);
        flip_btn.setOnClickListener(this);

        FloatingActionButton addButton =
                (FloatingActionButton) rootView.findViewById(R.id.add_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), DrawActivity.class);
                getActivity().startActivityForResult(i, NEW_DRAWING_REQUEST);
            }
        });

        safeFetch();

        return rootView;
    }

    @Override
    public void onClick(View v) {
        flipper.showNext();
    }

    private ArrayList<Tab3Item> fetchAllImages() {
        String[] projection = {
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media._ID
        };
        Cursor cursor_image = getContext().getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null, null, null
        );

        ArrayList<Tab3Item> result = new ArrayList<>();

        if (cursor_image != null) {
            if (cursor_image.moveToFirst()) {
                do {
                    try {
                        String file_path = cursor_image.getString(cursor_image.getColumnIndex(projection[0]));
                        long image_id = cursor_image.getLong(cursor_image.getColumnIndex(projection[1]));
                        Uri image_uri = Uri.parse("file://" + file_path);
                        Bitmap image_bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), image_uri);
                        Bitmap image_thumbnail = MediaStore.Images.Thumbnails.getThumbnail(getContext().getContentResolver(), image_id, MediaStore.Images.Thumbnails.MINI_KIND, null);
                        result.add(new Tab3Item(file_path, image_uri, image_bitmap, image_thumbnail));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } while (cursor_image.moveToNext());
            }
        }
        cursor_image.close();


        return result;
    }

    public void addItem(String file_path) {
        try {
            System.out.println(file_path);
            Uri image_uri_content = Uri.parse(file_path);
            Cursor cursor = getActivity().getContentResolver().query(image_uri_content, new String[] { android.provider.MediaStore.Images.ImageColumns.DATA }, null, null, null);
            cursor.moveToFirst();
            file_path = cursor.getString(0);
            cursor = getActivity().getContentResolver().query(image_uri_content, new String[] {MediaStore.Images.Media._ID}, null, null, null);
            cursor.moveToFirst();
            long image_id = cursor.getLong(0);
            Uri image_uri_file = Uri.parse("file://" + file_path);
            Bitmap image_bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), image_uri_file);
            Bitmap image_thumbnail = MediaStore.Images.Thumbnails.getThumbnail(getContext().getContentResolver(), image_id, MediaStore.Images.Thumbnails.MINI_KIND, null);
            images.add(new Tab3Item(file_path, image_uri_file, image_bitmap, image_thumbnail));
            grid_adapter.notifyDataSetChanged();
            list_adapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteItem(int position) {
        System.out.println(images.get(position).getFilePath());
        images.remove(position);
        grid_adapter.notifyDataSetChanged();
        list_adapter.notifyDataSetChanged();
        Toast.makeText(getActivity(), "Successfully deleted!", Toast.LENGTH_LONG).show();
    }

    private void safeFetch() {
        // Check the SDK version and whether the permission is already granted or not.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && getActivity().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            System.out.println("1");
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            System.out.println("2");
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        } else {
            // Android version is lesser than 6.0 or the permission is already granted.
            System.out.println("3");
            images = fetchAllImages();

            grid_view = (GridView) rootView.findViewById(R.id.tab3_grid_view);
            grid_adapter = new GridAdapter(getActivity());
            grid_view.setAdapter(grid_adapter);
            grid_view.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    Tab3OnLongClickFragment dialog_fragment = new Tab3OnLongClickFragment();
                    Bundle args = new Bundle();
                    args.putString("file_path", images.get(position).getFilePath());
                    args.putSerializable("images", images);
                    args.putInt("position", position);

                    String[] tokens = images.get(position).getFilePath().split("/");
                    String str = tokens[tokens.length - 1];
                    args.putString("name", str);

                    dialog_fragment.setArguments(args);
                    dialog_fragment.show(getActivity().getSupportFragmentManager(), "Deletion Alert");

                    return true;
                }
            });
            grid_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent i = new Intent(getActivity(), Tab3ImagePreviewActivity.class);
                    System.out.println(images.get(position).getFilePath());
                    i.putExtra("file_path", images.get(position).getFilePath());
                    getActivity().startActivityForResult(i, IMAGE_PREVIEW);
                }
            });

            list_view = (ListView) rootView.findViewById(R.id.tab3_list_view);
            list_adapter = new ListAdapter(getActivity());
            list_view.setAdapter(list_adapter);
            list_view.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    Tab3OnLongClickFragment dialog_fragment = new Tab3OnLongClickFragment();
                    Bundle args = new Bundle();
                    args.putString("file_path", images.get(position).getFilePath());
                    args.putSerializable("images", images);
                    args.putInt("position", position);

                    String[] tokens = images.get(position).getFilePath().split("/");
                    String str = tokens[tokens.length - 1];
                    args.putString("name", str);

                    dialog_fragment.setArguments(args);
                    dialog_fragment.show(getActivity().getSupportFragmentManager(), "Deletion Alert");

                    return true;
                }
            });
            list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent i = new Intent(getActivity(), Tab3ImagePreviewActivity.class);
                    System.out.println(images.get(position).getFilePath());
                    i.putExtra("file_path", images.get(position).getFilePath());
                    getActivity().startActivity(i);
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE || requestCode == PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                System.out.println("4");
                safeFetch();
            } else {
                System.out.println("5");
                Toast.makeText(getActivity(), "Until you grant the permission, we cannot display the images", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public class GridAdapter extends BaseAdapter {
        private Context m_context;
        private LayoutInflater inflater;

        public GridAdapter(Context context) {
            m_context = context;
            inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return images.size();
        }

        @Override
        public Object getItem(int position) {
            return images.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.tab3_grid_item, parent, false);
            }

            ImageView image_view = (ImageView) convertView.findViewById(R.id.tab3_grid_image);
            image_view.setLayoutParams(new LinearLayout.LayoutParams(350, 350));
            image_view.setScaleType(ImageView.ScaleType.CENTER_CROP);
            image_view.setImageBitmap(images.get(position).getImageThumbnail());

            TextView text_view = (TextView) convertView.findViewById(R.id.tab3_grid_text);
            String[] tokens = images.get(position).getFilePath().split("/");
            String str = tokens[tokens.length - 1];
            if (str.length() > 12)
                text_view.setText(str.substring(0, 6) + "... " + str.substring(str.length() - 4));
            else
                text_view.setText(str);

            return convertView;
        }
    }

    public class ListAdapter extends BaseAdapter {
        private Context m_context;
        private LayoutInflater inflater;

        public ListAdapter(Context context) {
            m_context = context;
            inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return images.size();
        }

        @Override
        public Object getItem(int position) {
            return images.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.tab3_list_item, parent, false);
            }

            ImageView image_view = (ImageView) convertView.findViewById(R.id.tab3_list_image);
            image_view.setLayoutParams(new LinearLayout.LayoutParams(300, 300));
            image_view.setImageBitmap(images.get(position).getImageThumbnail());

            TextView text_view = (TextView) convertView.findViewById(R.id.tab3_list_text);
            String[] tokens = images.get(position).getFilePath().split("/");
            String str = tokens[tokens.length - 1];
            if (str.length() > 25)
                text_view.setText(str.substring(0, 20) + "... " + str.substring(str.length() - 4));
            else
                text_view.setText(str);

            return convertView;
        }
    }
}