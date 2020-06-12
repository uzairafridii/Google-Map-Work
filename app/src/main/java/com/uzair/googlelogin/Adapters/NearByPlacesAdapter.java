package com.uzair.googlelogin.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.uzair.googlelogin.R;
import com.uzair.googlelogin.Utils.NearPlaces;

import java.util.List;

public class NearByPlacesAdapter extends RecyclerView.Adapter<NearByPlacesAdapter.MyNearPlacesViewHolder>
{

    private List<NearPlaces> nearPlacesList;
    private Context context;

    public NearByPlacesAdapter(List<NearPlaces> nearPlacesList, Context context) {
        this.nearPlacesList = nearPlacesList;
        this.context = context;
    }

    @NonNull
    @Override
    public MyNearPlacesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View myView = LayoutInflater.from(context).inflate(R.layout.design_for_recycler_view, null);
        return new MyNearPlacesViewHolder(myView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyNearPlacesViewHolder holder, int position) {

        final NearPlaces  nearPlaces = nearPlacesList.get(position);

        holder.setName(nearPlaces.getName());
        holder.setTotalRatings(nearPlaces.getUserRatingsTotal());
        holder.setRatingBar((float) nearPlaces.getRating());
        holder.setVicinity(nearPlaces.getVicinity());
        holder.setImageView(nearPlaces.getIcon());

    }

    @Override
    public int getItemCount() {
        return nearPlacesList.size();
    }

    public class MyNearPlacesViewHolder extends RecyclerView.ViewHolder
    {
        private TextView name , totalRatings , vicinity;
        private RatingBar ratingBar;
        private ImageView imageView;
        private View myView;

        public MyNearPlacesViewHolder(@NonNull View itemView) {
            super(itemView);

            myView = itemView;
        }

        private void setName(String locationName)
        {
            name = myView.findViewById(R.id.placeNameInDesign);

            if(!locationName.isEmpty())
            {
                name.setText(locationName);
            }
            else
            {
                name.setVisibility(View.GONE);
            }

        }


        private void setTotalRatings(int ratings)
        {
            totalRatings = myView.findViewById(R.id.totalRating);

            if(ratings > 0)
            {
                totalRatings.setText(""+ratings);
            }
            else
            {
                totalRatings.setVisibility(View.GONE);
            }

        }


        private void setVicinity(String vicinityOfLocation)
        {
            vicinity = myView.findViewById(R.id.vicinity);

            if(!vicinityOfLocation.isEmpty())
            {
                vicinity.setText(vicinityOfLocation);
            }
            else
            {
                vicinity.setVisibility(View.GONE);
            }

        }


        private void setImageView(String image)
        {
            imageView = myView.findViewById(R.id.placeImage);

            if(!image.isEmpty())
            {
                Glide.with(context)
                        .load(image)
                        .placeholder(R.drawable.ic_launcher_background)
                        .into(imageView);
            }
            else
            {
                imageView.setVisibility(View.GONE);
            }
        }


        private void setRatingBar(float ratingBarValue)
        {
            ratingBar = myView.findViewById(R.id.ratingBar);
            if(ratingBarValue != 0)
            {
                ratingBar.setRating(ratingBarValue);
            }
            else
            {
                ratingBar.setVisibility(View.GONE);
            }
        }


    }
}
