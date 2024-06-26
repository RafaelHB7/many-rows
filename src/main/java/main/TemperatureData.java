package main;

public class TemperatureData {
    public float min;
    public float max;
    public double sum=0;
    public int count=0;

    public TemperatureData(float temperature) {
        this.min = temperature;
        this.max = temperature;
        this.sum += temperature;
        this.count++;
    }

    public void addTemperature(float temperature) {
        this.min = Math.min(temperature, this.min);
        this.max = Math.max(temperature, this.max);
        this.sum += temperature;
        this.count++;
    }

    public float getAverage() {
        return (float) (sum/count);
    }
}
