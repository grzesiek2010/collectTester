/*
 * Copyright 2017 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.collectTester.adapters;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.odk.collectTester.utilities.ListElement;
import org.odk.collectTester.R;

import java.util.List;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(ListElement item);
    }

    private final OnItemClickListener listener;

    private final List<ListElement> listElements;

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView subtext;

        ViewHolder(View v) {
            super(v);
            title = v.findViewById(R.id.title);
            subtext = v.findViewById(R.id.subtext);
        }

        void bind(final ListElement item, final OnItemClickListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    listener.onItemClick(item);
                }
            });
        }
    }

    public ListAdapter(List<ListElement> listElements, OnItemClickListener listener) {
        this.listElements = listElements;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(listElements.get(position), listener);
        holder.title.setText(listElements.get(position).getTitle());
        holder.subtext.setText(listElements.get(position).getSubtext());
    }

    @Override
    public int getItemCount() {
        return listElements.size();
    }
}
