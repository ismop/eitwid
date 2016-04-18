package pl.ismop.web.client.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Singleton;

@Singleton
public class GradientsUtil {
	private Map<String, MinMaxValues> values;
	
	private Map<Double, Color> gradient;
	
	private class MinMaxValues {
		double min, max;
		
		MinMaxValues() {
			min = Double.MAX_VALUE;
			max = Double.MIN_VALUE;
		}
	}
	
	public static class Color {
		private int r, g, b;
		
		public Color(int r, int g, int b) {
			this.r = r;
			this.g = g;
			this.b = b;
		}
		
		public int getR() {
			return r;
		}
		
		public int getG() {
			return g;
		}
		
		public int getB() {
			return b;
		}

		@Override
		public String toString() {
			return "Color [r=" + r + ", g=" + g + ", b=" + b + "]";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + b;
			result = prime * result + g;
			result = prime * result + r;
			
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			
			if (obj == null)
				return false;
			
			if (getClass() != obj.getClass())
				return false;
			
			Color other = (Color) obj;
			
			if (b != other.b)
				return false;
			
			if (g != other.g)
				return false;
			
			if (r != other.r)
				return false;
			
			return true;
		}
	}
	
	public GradientsUtil() {
		values = new HashMap<>();
		gradient = new LinkedHashMap<>();
		gradient.put(0.0, new Color(0, 0, 255));
		gradient.put(0.25, new Color(0, 255, 255));
		gradient.put(0.5, new Color(0, 255, 0));
		gradient.put(0.75, new Color(255, 255, 0));
		gradient.put(1.0, new Color(255, 0, 0));
	}
	
	public void updateValues(String gradientId, double value) {
		if (!values.containsKey(gradientId)) {
			values.put(gradientId, new MinMaxValues());
		}
		
		MinMaxValues minMaxValues = values.get(gradientId);
		
		if (value < minMaxValues.min) {
			minMaxValues.min = value;
		}
		
		if (value > minMaxValues.max) {
			minMaxValues.max = value;
		}
	}
	
	public Color getColor(String gradientId, double value) {
		updateValues(gradientId, value);
		
		MinMaxValues minMaxValues = values.get(gradientId);
		double normalizedColor = (value - minMaxValues.min) / (minMaxValues.max - minMaxValues.min);
		
		if (Double.isNaN(normalizedColor)) {
			normalizedColor = 0.0;
		}
		
		Double lowerBoundary = null, upperBoundary = null, previousBoundary = null;
		
		for (Iterator<Double> i = gradient.keySet().iterator(); i.hasNext();) {
			Double colorBoundary = i.next();
			
			if (colorBoundary.doubleValue() == normalizedColor) {
				return gradient.get(colorBoundary);
			}
			
			if (normalizedColor < colorBoundary) {
				lowerBoundary = previousBoundary;
				upperBoundary = colorBoundary;
				break;
			}
			
			previousBoundary = colorBoundary;
		}
		
		if (lowerBoundary != null && upperBoundary != null) {
			Color lowerColor = gradient.get(lowerBoundary);
			Color upperColor = gradient.get(upperBoundary);
			
			return new Color(
						new Double(((normalizedColor - lowerBoundary)
								/ (upperBoundary - lowerBoundary))
								* (upperColor.getR() - lowerColor.getR()) + lowerColor.getR())
								.intValue(),
						new Double(((normalizedColor - lowerBoundary)
								/ (upperBoundary - lowerBoundary))
								* (upperColor.getG() - lowerColor.getG()) + lowerColor.getG())
								.intValue(),
						new Double(((normalizedColor - lowerBoundary)
								/ (upperBoundary - lowerBoundary))
								* (upperColor.getB() - lowerColor.getB()) + lowerColor.getB())
								.intValue()
					);
		} else {
			throw new IllegalArgumentException("Could not find gradient range for value " + value
					+ " and gradient id " + gradientId);
		}
	}
	
	public Map<Double, Color> getGradient() {
		return new LinkedHashMap<>(gradient);
	}

	public double getValue(String gradientId, double gradientPosition) {
		if (values.containsKey(gradientId)) {
			return gradientPosition * (values.get(gradientId).max - values.get(gradientId).min)
					+ values.get(gradientId).min;
		}
		
		throw new IllegalArgumentException("gradient with id " + gradientId + " does not exist");
	}

	public boolean contains(String gradientId) {
		return values.containsKey(gradientId);
	}

	public double getMinValue(String gradientId) {
		return values.get(gradientId) == null ? 0.0 : values.get(gradientId).min;
	}
	
	public double getMaxValue(String gradientId) {
		return values.get(gradientId) == null ? 0.0 : values.get(gradientId).max;
	}

	public boolean isExtended(String gradientId, double min, double max) {
		return values.get(gradientId) == null ? false :
			(values.get(gradientId).min < min || values.get(gradientId).max > max);
	}

	public void reset() {
		values.clear();
	}
}