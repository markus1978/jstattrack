package de.hub.jstattrack.data;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

public class BatchedDataSeries {

	private final int maxSize;
	private final List<Double> values = new ArrayList<Double>();
	private final List<Double> batchedValues = new ArrayList<Double>();
	
	private int currentBatchSize = 1;
	private int currentBatchIndex = 0;
	private double currentBatchSum = 0;

	public BatchedDataSeries(int maxSize) {
		super();
		this.maxSize = maxSize;
	}

	public void add(double value) {
		currentBatchSum += value;
		if (++currentBatchIndex == currentBatchSize) {
			values.add(currentBatchSum/currentBatchSize);
			if (values.size() > maxSize*2) {
				batch();
			}
			currentBatchIndex = 0;
			currentBatchSum = 0;
		}
	}

	private void batch() {
		int batchSize = values.size() / maxSize;
		if (batchSize >= 1) {
			batchedValues.clear();
			List<List<Double>> partition = Lists.partition(values, batchSize);
			for (List<Double> batch: partition) {
				double sum = 0;
				for (Double value: batch) {
					sum += value;
				}
				batchedValues.add(sum/batch.size());
			}
			
			values.clear();
			values.addAll(batchedValues);
		}
		currentBatchSize *= 2;
	}
	
	public List<Double> asList() {
		return values;
	}
}
