/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.gsgp.population.fitness;

import edu.gsgp.experiment.data.Instance;
import edu.gsgp.utils.Utils;
import edu.gsgp.utils.Utils.DatasetType;
import edu.gsgp.experiment.data.ExperimentalData;

/**
 * @author Luiz Otavio Vilas Boas Oliveira
 * http://homepages.dcc.ufmg.br/~luizvbo/ 
 * luiz.vbo@gmail.com
 * Copyright (C) 20014, Federal University of Minas Gerais, Belo Horizonte, Brazil
 */
public class FitnessRMSE extends Fitness {
    private double[] rmseTr;
    private double[] rmseTs;
    private double[] rmseVt;
    private double[] rmseTrSumSquareErrors;
    private double[] rmseTsSumSquareErrors;
    private double[] rmseVtSumSquareErrors;
    private double[] rmseTrSumSquareErrorsSizes;
    private double[] rmseTsSumSquareErrorsSizes;
    private double[] rmseVtSumSquareErrorsSizes;
    private int numberOfObjectives = 0;
    private boolean skipGroupChecking = false;

    public FitnessRMSE(int numNodes) {
        super(numNodes);
    }

    public FitnessRMSE() {
        this(0);

    }

    public FitnessRMSE(boolean skipGroupChecking) {
        this(0);
        this.skipGroupChecking = skipGroupChecking;
    }

    public FitnessRMSE(double[] semanticsTr, double[] semanticsTs, double[] rmseTr, double[] rmseTs, double[] rmseVt) {
        super(0, semanticsTr, semanticsTs);
        this.rmseTr = rmseTr;
        this.rmseTs = rmseTs;
        this.rmseVt = rmseVt;
    }
    
    public void setRMSE(double[] rmse, DatasetType dataType) {
        if(dataType == DatasetType.TRAINING)
            rmseTr = rmse;
        else if (dataType == DatasetType.TEST)
            rmseTs = rmse;
        else
            rmseVt = rmse;
    }

    public double[] getRMSE(DatasetType dataType){
        if (dataType == DatasetType.TRAINING)
            return rmseTr;
        if (dataType == DatasetType.TEST)
            return rmseTs;

        return rmseVt;
    }
    
    /** Control variables used during fitness calculation. **/
    // Variable to store the sum of squared errors (to compute the RMSE).
    private double ctrSumSquarError;
    // Variable to indicate What fitness we are computing.
//    private DataType ctrFitnessType;
    
    @Override
    public void resetFitness(DatasetType dataType, ExperimentalData datasets, int numberOfObjectives){
        rmseTrSumSquareErrors = new double[numberOfObjectives];
        rmseTsSumSquareErrors = new double[numberOfObjectives];
        rmseVtSumSquareErrors = new double[numberOfObjectives];
        rmseTrSumSquareErrorsSizes = new double[numberOfObjectives];
        rmseTsSumSquareErrorsSizes = new double[numberOfObjectives];
        rmseVtSumSquareErrorsSizes = new double[numberOfObjectives];

        this.numberOfObjectives = numberOfObjectives;

        for (int i = 0; i < numberOfObjectives; i++) {
            rmseTrSumSquareErrors[i] = 0;
            rmseTsSumSquareErrors[i] = 0;
            rmseVtSumSquareErrors[i] = 0;
            rmseTrSumSquareErrorsSizes[i] = 0;
            rmseTsSumSquareErrorsSizes[i] = 0;
            rmseVtSumSquareErrorsSizes[i] = 0;
        }

        setSemantics(datasets.getDataset(dataType).size(), dataType);
    }

    @Override
    public void setSemanticsAtIndex(Instance instance, double estimated, double desired, int index, DatasetType dataType){
        getSemantics(dataType)[index] = estimated;

        double[] sumSquareErrors = getRmseSumSquareErrors(dataType);
        double[] sumSquareErrorsSize = getRmseTrSumSquareErrorsSizes(dataType);

        for (int i = 0; i < numberOfObjectives; i++) {
            if (skipGroupChecking || instance.belongsToGroup(i)) {
                sumSquareErrors[i] += Math.pow(estimated - desired, 2);
                sumSquareErrorsSize[i]++;
            }
        }
    }

    protected double[] getRmseSumSquareErrors(DatasetType dataType) {
        if (dataType == DatasetType.TRAINING)
            return rmseTrSumSquareErrors;
        if (dataType == DatasetType.TEST)
            return rmseTsSumSquareErrors;
        else
            return rmseVtSumSquareErrors;
    }

    protected double[] getRmseTrSumSquareErrorsSizes(DatasetType dataType) {
        if (dataType == DatasetType.TRAINING)
            return rmseTrSumSquareErrorsSizes;
        if (dataType == DatasetType.TEST)
            return rmseTsSumSquareErrorsSizes;
        else
            return rmseVtSumSquareErrorsSizes;
    }

    @Override
    public void computeFitness(DatasetType dataType){
        double[] rmse = new double[numberOfObjectives];
        double[] sumSquareErrors = getRmseSumSquareErrors(dataType);
        double[] sumSquareErrorsSize = getRmseTrSumSquareErrorsSizes(dataType);

        for (int i = 0; i < rmse.length; i++) {
            rmse[i] = sumSquareErrorsSize[i] == 0
                    ? Double.MAX_VALUE
                    : sumSquareErrors[i] / sumSquareErrorsSize[i];
        }

        setRMSE(rmse, dataType);
    }

    @Override
    public Fitness softClone() {
        return new FitnessRMSE();
    }

    @Override
    public double[] getTrainingFitness() {
        return rmseTr;
    }

    @Override
    public double[] getTestFitness(){
        return rmseTs;
    }

    @Override
    public double[] getValidationFitness() { return rmseVt; }

    @Override
    public double[] getFitness(DatasetType datasetType) {
        if (datasetType == DatasetType.TRAINING)
            return this.getTrainingFitness();
        if (datasetType == DatasetType.TEST)
            return this.getTestFitness();
        else
            return this.getValidationFitness();
    }
    
    @Override
    public double[] getComparableValue() {
        return getRMSE(DatasetType.TRAINING);
    }
}