package com.example.themo.musicmarvelous.ui.main;

import com.example.themo.musicmarvelous.data.model.Track;

import java.util.List;

public interface TrackListener {
    void onPlayTrack(int position, List<Track> tracks);

    void onAddToNowPlaying(Track track);

    void onAddToFavorite(Track track);

    void downloadTrack(Track track);

}
