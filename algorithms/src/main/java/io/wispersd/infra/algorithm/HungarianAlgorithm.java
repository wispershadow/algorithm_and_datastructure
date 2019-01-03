package io.wispersd.infra.algorithm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class HungarianAlgorithm {
	private static final Logger logger = LoggerFactory.getLogger(HungarianAlgorithm.class);
	
	
	
	static class ElementIndex {
		private int rowIndex;
		private int colIndex;
		private Number value;
		
		public String toString() {
			return "Row index=" + rowIndex + ", col index=" + colIndex 
					+ ", value=" + value;
		}
	}
	
	static class CountZeroEntry {
		private final int index;
		private final BitSet countZeroPositions;
	
		CountZeroEntry(int size, int index) {
			this.countZeroPositions = new BitSet(size);
			this.index = index;
		}
		
		void setZeroPos(int pos) {
			this.countZeroPositions.set(pos);
		}
		
	}
	
	static class MaxCountZeroComparator implements Comparator<CountZeroEntry> {

		@Override
		public int compare(CountZeroEntry entry1, CountZeroEntry entry2) {
			return entry2.countZeroPositions.cardinality() 
					- entry1.countZeroPositions.cardinality();
		}
		
	}
	
	static class CountZeroMatrix {
		private List<CountZeroEntry> countZeroEntriesForRow;
		private List<CountZeroEntry> countZeroEntriesForCol;
		
		CountZeroMatrix(int size) {
			countZeroEntriesForRow = new ArrayList<CountZeroEntry>(size);
			for (int i=0; i<size; i++) {
				countZeroEntriesForRow.add(new CountZeroEntry(size, i));
			}
			
			countZeroEntriesForCol = new ArrayList<CountZeroEntry>(size);
			for (int i=0; i<size; i++) {
				countZeroEntriesForCol.add(new CountZeroEntry(size, i));
			}
		}
		
		void setZeroPosition(int rowIndex, int colIndex) {
			countZeroEntriesForRow.get(rowIndex).setZeroPos(colIndex);
			countZeroEntriesForCol.get(colIndex).setZeroPos(rowIndex);
		}
		
		
		
	}
	
	static class CountZeroComparator {
		
	}
	
	
	
	public static void validateInput(Number[][] costMatrix) {
		Objects.requireNonNull(costMatrix);
		if (costMatrix.length == 0) {
			throw new RuntimeException("Cost matrix size must be greater than 0");
		}
		int rows = costMatrix.length;
		for (Number[] column: costMatrix) {
			if (column.length != rows) {
				throw new RuntimeException("Matrix size error, columns=" + column.length
						+ ",rows=" + rows);
			}
		}
		for (int i=0; i<costMatrix.length; i++) {
			for (int j=0; j<costMatrix[i].length; j++) {
				if (costMatrix[i][j] == null) {
					throw new RuntimeException("Matrix value missing, row=" + i + " column=" + j);
				}
				if (costMatrix[i][j].doubleValue() < 0) {
					throw new RuntimeException("Matrix contains negative value, row=" 
							+ i + " column=" + j + " value=" + costMatrix[i][j].doubleValue());
				}
			}
		}
	}
	
	
	public static void findMinCostAssignment(Number[][] costMatrix) {
		CountZeroMatrix countZeroMatrix = new CountZeroMatrix(costMatrix.length);
		for (int i = 0; i < costMatrix.length; i++) {
			findAndSubtractRowMinimum(costMatrix, i, countZeroMatrix);
		}
		for (int i = 0; i < costMatrix.length; i++) {
			findAndSubtractColMinimum(costMatrix, i, countZeroMatrix);
		}
	}
	
	
	/**
	 * find and subtract min value from row, also count zeros for the row
	 * @param costMatrix cost matrix
	 * @param rowIndex  row index
	 * @param countZeroMatrix countZeroMatrix
	 * @return the min value with row and column position
	 */
	private static ElementIndex findAndSubtractRowMinimum(Number[][] costMatrix, 
			int rowIndex, 
			CountZeroMatrix countZeroMatrix) {
		ElementIndex result = new ElementIndex();
		result.rowIndex = rowIndex;
		Number[] currentRow = costMatrix[rowIndex];
		Number minValue = null;
		int minColIndex = 0;
		int colIndex = 0;
		for (Number value: currentRow) {
			if (minValue == null || value.doubleValue() - minValue.doubleValue() < 0) {
				minValue = value;
				minColIndex = colIndex;
			}
			colIndex ++;
		}
		result.colIndex = minColIndex;
		result.value = minValue;
		for (int i = 0; i < currentRow.length; i++) {
			currentRow[i] = subtract(currentRow[i], minValue);
			if (currentRow[i].doubleValue() == 0) {
				countZeroMatrix.setZeroPosition(rowIndex, i);
			}
		}
		logger.debug("Getting row minum: {}", result);
		return result;
	}
	
	/**
	 * find and subtract min value from column, also count zeros for the column
	 * @param costMatrix cost matrix
	 * @param colIndex  column index
	 * @param countZeroMatrix countZeroMatrix
	 * @return the min value with row and column position
	 */
	private static ElementIndex findAndSubtractColMinimum(Number[][] costMatrix, 
			int colIndex,
			CountZeroMatrix countZeroMatrix) {
		ElementIndex result = new ElementIndex();
		result.colIndex = colIndex;
		Number minValue = null;
		Number value = null;
		Number[] curRow = null;
		int minRowIndex = 0;
		for (int i = 0; i < costMatrix.length; i++) {
			curRow = costMatrix[i];
			value = curRow[colIndex].doubleValue();
			if (minValue == null || value.doubleValue() - minValue.doubleValue() < 0) {
				minValue = value;
				minRowIndex = i;
			}
		}
		result.rowIndex = minRowIndex;
		result.value = minValue;
		for (int i = 0; i < costMatrix.length; i++) {
			curRow = costMatrix[i];
			curRow[colIndex] = subtract(curRow[colIndex], minValue);
			if (curRow[colIndex].doubleValue() == 0) {
				countZeroMatrix.setZeroPosition(i, colIndex);
			}
		}
		logger.debug("Getting col minum: {}", result);
		return result;
	}
	
	private static Number subtract(Number n1, Number n2) {
		if (n1 instanceof BigDecimal && n2 instanceof BigDecimal) {
			return ((BigDecimal)n1).subtract((BigDecimal)n2);
		}
		else {
			return n1.doubleValue() - n2.doubleValue();
		}
		
	}
}
