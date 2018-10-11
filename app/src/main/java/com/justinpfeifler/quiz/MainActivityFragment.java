package com.justinpfeifler.quiz;

import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private static final String TAG = "Flag Fragment";

    private static final int FLAGS_IN_QUIZ = 10;

    private List<String> nFileNameList;
    private List<String> nCountriesList;
    private Set<String> nRegionSet;
    private String nCorrectAnswer;
    private int nTotalGuesses;
    private int nCorrectAnswers;
    private int nGuessRow;
    private SecureRandom nRandom;
    private Handler nHandler;
    private Animation nShakeAnimation;

    private LinearLayout nQuizLinearLayout;
    private TextView nQuestionNumberTextView;
    private ImageView nFlagImageView;
    private LinearLayout[] nGuessLayouts;
    private TextView nAnswerTextView;

    public MainActivityFragment() {
    }

    // Save preferences even after the app is closed
    public void updateGuessRow(SharedPreferences preferences) {
        String choice = preferences.getString(MainActivity.NUMBER_OF_BUTTONS, "2");

        this.nGuessRow = Integer.parseInt(choice) / 2;

        // hide buttons
        for (LinearLayout layout : this.nGuessLayouts) {
            layout.setVisibility(View.GONE);
        }

        // iterate from 0 to < guess count and set buttons visible
        for (int row = 0; row < this.nGuessRow; row++) {
            this.nGuessLayouts[row].setVisibility(View.VISIBLE);
        }
    }

    public void updateRegions(SharedPreferences preferences) {
        this.nRegionSet = preferences.getStringSet(MainActivity.REGIONS, null);
    }

    public void resetQuiz() {
        AssetManager assets = this.getActivity().getAssets();
        this.nFileNameList.clear();

        try {
            // loop through the regions
            for (String region : this.nRegionSet) {
                // get flags for region
                String[] paths = assets.list(region);

                // remove the file extension
                for (String path : paths) {
                    this.nFileNameList.add(path.replace(".png", ""));
                }
            }
        } catch (IOException iOEx) {
            Log.e(TAG, "Error loading image files", iOEx);
        }

        this.nCorrectAnswers = 0;
        this.nTotalGuesses = 0;
        this.nCountriesList.clear();

        int flagCounter = 1;
        int numberOfFlags = this.nFileNameList.size();

        while (flagCounter <= FLAGS_IN_QUIZ) {
            int index = this.nRandom.nextInt(numberOfFlags);

            String fileName = this.nFileNameList.get(index);

            if (!this.nCountriesList.contains(fileName)) {
                this.nCountriesList.add(fileName);
                flagCounter++;
            }
        }

        this.loadNextFlag();
    }

    public void loadNextFlag() {
        String nextImage = this.nCountriesList.remove(0);
        this.nCorrectAnswer = nextImage;
        this.nAnswerTextView.setText(" ");

        this.nQuestionNumberTextView.setText(this.getString(R.string.question, (this.nCorrectAnswers + 1), FLAGS_IN_QUIZ));
        String region = nextImage.substring(0, nextImage.indexOf('-'));
        AssetManager assets = this.getActivity().getAssets();

        try (InputStream stream = assets.open(region + "/" + nextImage + ".png")) {
            Drawable flag = Drawable.createFromStream(stream, nextImage);
            this.nFlagImageView.setImageDrawable(flag);
        } catch (IOException iOEx) {
            Log.e(TAG, "Error loading flag", iOEx);
        }

        int correct = this.nFileNameList.indexOf(this.nCorrectAnswer);
        this.nFileNameList.add(this.nFileNameList.remove(correct));
        for (int row = 0; row < this.nGuessRow; row++) {
            for (int column = 0; column < this.nGuessLayouts[row].getChildCount(); column++) {
                Button guessButton = (Button) this.nGuessLayouts[row].getChildAt(column);
                guessButton.setEnabled(true);

                String fileName = this.nFileNameList.get((row * 2) + column);
                guessButton.setText(this.getCountryName(fileName));
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        this.nFileNameList = new ArrayList<>();
        this.nCountriesList = new ArrayList<>();
        this.nRandom = new SecureRandom();
        this.nHandler = new Handler();

        this.nShakeAnimation = AnimationUtils.loadAnimation(
                this.getActivity(),
                R.anim.incorrect_shake);
        this.nShakeAnimation.setRepeatCount(3);

        // Setup views for the fragment
        this.nQuizLinearLayout = view.findViewById(R.id.quiz_linear_layout);
        this.nQuestionNumberTextView = view.findViewById(R.id.question_number_text_view);
        this.nFlagImageView = view.findViewById(R.id.flag_image_view);
        this.nGuessLayouts = new LinearLayout[4];
        this.nGuessLayouts[1] = view.findViewById(R.id.row_one);
        this.nGuessLayouts[2] = view.findViewById(R.id.row_two);
        this.nGuessLayouts[3] = view.findViewById(R.id.row_three);
        this.nGuessLayouts[4] = view.findViewById(R.id.row_four);
        this.nAnswerTextView = view.findViewById(R.id.answer_text_view);

        this.nQuestionNumberTextView.setText(
                this.getString(R.string.question, 1, FLAGS_IN_QUIZ)
        );

        return view;
    }
}
