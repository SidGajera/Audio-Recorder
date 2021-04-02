package com.example.audiorecorder;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.audiorecorder.R;

import java.io.File;

public class AudioListAdapter extends RecyclerView.Adapter<AudioListAdapter.AudioViewHolder> {

    File[] allFiles;
    TimeAgo timeAgo;
    Context context;

    onItemListClick onItemListClick;

    public AudioListAdapter(File[] allFiles, onItemListClick onItemListClick, Context context) {
        this.allFiles = allFiles;
        this.onItemListClick = onItemListClick;
        this.context = context;
    }

    @NonNull
    @Override
    public AudioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_list_item, parent, false);
        timeAgo = new TimeAgo();
        return new AudioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AudioViewHolder holder, int position) {
        holder.list_title.setText(allFiles[position].getName());
        holder.list_date.setText(timeAgo.getTimeAgo(allFiles[position].lastModified()));
    }

    @Override
    public int getItemCount() {
        return allFiles.length;
    }

    public class AudioViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView list_image;
        TextView list_title, list_date;

        String authorities = "com.example.audiorecorder.fileprovider";

        public AudioViewHolder(@NonNull final View itemView) {
            super(itemView);

            list_image = itemView.findViewById(R.id.list_image_view);
            list_title = itemView.findViewById(R.id.list_title);
            list_date = itemView.findViewById(R.id.list_date);

            itemView.setOnClickListener(this);

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    final AlertDialog.Builder dialog = new AlertDialog.Builder(itemView.getContext());
                    dialog.setTitle("Choose Option");
                    dialog.setCancelable(false);

                    final String[] options = {"Share", "Rename", "Delete", "Cancle"};

                    dialog.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if ("Share".equals(options[which])) {

                                Uri path = FileProvider.getUriForFile(itemView.getContext(), authorities, allFiles[getAdapterPosition()]);

                                Intent shareIntent = new Intent();
                                shareIntent.setAction(Intent.ACTION_SEND);
                                
                                shareIntent.putExtra(Intent.EXTRA_STREAM, path);
                                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                shareIntent.setType("audio/*");
                                itemView.getContext().startActivity(Intent.createChooser(shareIntent, "share recording file"));
                            } else if ("Rename".equals(options[which])) {
                                renameAlertDialog();
                            } else if ("Delete".equals(options[which])) {
                                deleteAlertDialog();
                            } else if ("Cancle".equals(options[which])) {                                dialog.dismiss();
                            }
                        }

                        private void renameAlertDialog() {
                            LayoutInflater layoutInflater = LayoutInflater.from(itemView.getContext());
                            final View promptView = layoutInflater.inflate(R.layout.rename_alert_dialog, null);

                            final android.app.AlertDialog alertDialogBuilder = new android.app.AlertDialog.Builder(itemView.getContext()).create();

                            alertDialogBuilder.setView(promptView);

                            final EditText userInput = promptView.findViewById(R.id.rename_text);
                            //load current saved file name to the AlertDialog EditText
                            userInput.setText(allFiles[getAdapterPosition()].getName());

                            Button renameAlertBtnPositive = promptView.findViewById(R.id.renameAlertBtnPositive);
                            Button renameAlertBtnNegative = promptView.findViewById(R.id.renameAlertBtnNegative);

                            //doesn't cancel if tap alert dialog excluded area
                            alertDialogBuilder.setCancelable(false);

                            //--- OK button OnClick ---
                            renameAlertBtnPositive.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    Editable renamedText = userInput.getText();
                                    list_title.setText(renamedText);

                                    //previous file name
                                    File filepath = new File("/storage/emulated/0/Android/data/com.example.audiorecorder/files/" + allFiles[getAdapterPosition()].getName());

                                    //new renamed file
                                    File renamedPath = new File("/storage/emulated/0/Android/data/com.example.audiorecorder/files/" + renamedText + ".mp3");

                                    filepath.renameTo(renamedPath);

                                    //updating current audio file
                                    allFiles[getAdapterPosition()] = renamedPath;

                                    //Log.d("AudioTAG", "Successful Rename: " + renamedPath.getPath());
                                    Toast.makeText(itemView.getContext(), "File renamed", Toast.LENGTH_SHORT).show();

                                    alertDialogBuilder.dismiss(); //dismiss after button is clicled

                                }
                            });
                            //--- End OK button OnClick ---

                            //--- CANCEl button OnClick ---
                            renameAlertBtnNegative.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    alertDialogBuilder.cancel();
                                }
                            });
                            //---End CANCEl button OnClick ---

                            alertDialogBuilder.show();
                        }

                        private void deleteAlertDialog() {

                            final LayoutInflater layoutInflater = LayoutInflater.from(itemView.getContext());

                            final View promptView = layoutInflater.inflate(R.layout.simple_dialog, null);

                            final android.app.AlertDialog alertDialogBuilder = new android.app.AlertDialog.Builder(itemView.getContext()).create();

                            // set common_alert_dialog.xml to alertdialog builder
                            alertDialogBuilder.setView(promptView);

                            Button alertBtnPositive = promptView.findViewById(R.id.commonAlertPosBtn);
                            Button alertBtnNegative = promptView.findViewById(R.id.commonAlertNegBtn);

                            //doesn't cancel if tap alert dialog excluded area
                            alertDialogBuilder.setCancelable(false);

                            alertBtnPositive.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    //deleting the specified position audio file
                                    allFiles[getAdapterPosition()].delete();

                                    //--- disabling visibility of the item after deleted ---
                                    list_image.setVisibility(View.GONE);
                                    list_title.setVisibility(View.GONE);
                                    list_date.setVisibility(View.GONE);

                                    //--- End disabling visibility of the item after deleted ---

                                    Toast.makeText(v.getContext(), "File deleted", Toast.LENGTH_LONG).show();

                                    notifyItemRemoved(getAdapterPosition());

                                    alertDialogBuilder.dismiss();

                                }
                            });

                            alertBtnNegative.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    alertDialogBuilder.cancel();
                                }
                            });

                            alertDialogBuilder.setTitle("Are you sure want to delete ?");
                            alertDialogBuilder.show();
                        }
                    });
                    dialog.show();

                    return false;
                }
            });
        }

        @Override
        public void onClick(View v) {
            onItemListClick.onClickListener(allFiles[getAdapterPosition()], getAdapterPosition());
        }
    }

    public interface onItemListClick {
        void onClickListener(File file, int position);
    }
}