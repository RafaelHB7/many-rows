package data;

import java.util.List;

public class CityTemperatures extends City {
    private List<Float> temperatures;

    public CityTemperatures(String city, List<Float> temperatures) {
        this.setCity(city);
        this.temperatures = temperatures;
    }

    public List<Float> getTemperatures() {
        return temperatures;
    }

    public void setTemperatures(List<Float> temperatures) {
        this.temperatures = temperatures;
    }
}
