package NutriPlan.model.Dao;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.Table;

@Entity
@Table(name = "foodInfo")
public class FoodInfo {
    @jakarta.persistence.Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String foodName;
    private int servingSize;
    private double kcal;
    private double protein;
    private double fat;
    private double carbohydrate;
    public FoodInfo() {
    }

    public FoodInfo(String foodName, double kcal, double carbohydrate, double protein, double fat, int servingSize) {
        this.foodName = foodName;
        this.kcal = kcal;
        this.carbohydrate = carbohydrate;
        this.protein = protein;
        this.fat = fat;
        this.servingSize = servingSize;
    }
    public Long getFoodId() {
        return id;
    }

    public void setFoodId(Long foodId) {
    }
    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public int getServingSize() {
        return servingSize;
    }

    public void setServingSize(int servingSize) {
        this.servingSize = servingSize;
    }

    public double getKcal() {
        return kcal;
    }

    public void setKcal(double kcal) {
        this.kcal = kcal;
    }

    public double getProtein() {
        return protein;
    }

    public void setProtein(double protein) {
        this.protein = protein;
    }

    public double getFat() {
        return fat;
    }

    public void setFat(double fat) {
        this.fat = fat;
    }

    public double getCarbohydrate() {
        return carbohydrate;
    }

    public void setCarbohydrate(double carbohydrate) {
        this.carbohydrate = carbohydrate;
    }



}
