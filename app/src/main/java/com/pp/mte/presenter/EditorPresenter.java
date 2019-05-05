package com.pp.mte.presenter;

import android.content.Context;

import com.pp.mte.EditorI;
import com.pp.mte.EditorI.*;
import com.pp.mte.models.GestureModel;

import nn.DRNetwork;
import utils.Utils;


/**
 * Created by kevin on 22/02/19.
 */

public class EditorPresenter implements EditorI.Presenter{

    private View editorView;
    private Model gestureModel;
    private DRNetwork brain;

    public EditorPresenter(Context context, View view) {
        editorView = view;
        gestureModel = new GestureModel(context);

        //setup NeuralNetwork
        brain = new DRNetwork(Utils.ActivationFunction.NONE, 128*128, 9);

        //loads the weights of the neural network to recognise images
        try {
            brain.loadWeights(context.getAssets().open("weights.nn"));
        } catch (Exception e) {}
    }

    @Override
    public void handleGesture(double[] pixels) {
        brain.feedForward(pixels);

        double result = 0;
        int neuronIndex = 0;
        for (int i = 0; i < brain.getOutputs().length; i++) {
            if ((brain.getOutputs()[i] > result && i != 0) || (i==0)) {
                result = brain.getOutputs()[i];
                neuronIndex = i;
            }
        }

        String code = gestureModel.getCode(neuronIndex);
        editorView.setCode(code);
    }
}
